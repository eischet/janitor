// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.dao;


import com.eischet.dbxs.*;
import com.eischet.dbxs.dialects.DatabaseDialect;
import com.eischet.dbxs.exceptions.DatabaseError;
import com.eischet.dbxs.results.SimpleResultSet;
import com.eischet.dbxs.statements.SelectStatement;
import com.eischet.dbxs.statements.UpdateStatement;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.errors.runtime.JanitorError;
import com.eischet.janitor.api.errors.runtime.JanitorNativeException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JAssignable;
import com.eischet.janitor.api.types.JIterable;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JMap;
import com.eischet.janitor.api.types.builtin.JNumber;
import com.eischet.janitor.api.types.builtin.JString;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.api.types.dispatch.Dispatcher;
import com.eischet.janitor.api.types.functions.JCallArgs;
import com.eischet.janitor.api.types.functions.JCallable;
import com.eischet.janitor.logging.JanitorLogger;
import com.eischet.janitor.orm.meta.OrmPropertyHandle;
import com.eischet.janitor.orm.JanitorOrm;
import com.eischet.janitor.orm.cache.EntityCache;
import com.eischet.janitor.orm.cache.SimpleEntityCache;
import com.eischet.janitor.orm.metrics.CacheState;
import com.eischet.janitor.orm.metrics.DaoMetrics;
import com.eischet.janitor.orm.metrics.OrmMetrics;
import com.eischet.janitor.orm.filter.FilterExpression;
import com.eischet.janitor.orm.entity.HasModifiedDate;
import com.eischet.janitor.orm.entity.OrmEntity;
import com.eischet.janitor.orm.entity.Versioned;
import com.eischet.janitor.orm.filter.FilterOperator;
import com.eischet.janitor.orm.filter.MalformedExpression;
import com.eischet.janitor.orm.meta.EntityDispatchTable;
import com.eischet.janitor.orm.sql.ColumnCase;
import com.eischet.janitor.orm.sql.ColumnTypeHint;
import com.eischet.janitor.orm.sql.StatementCreator;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.listeners.ListenerRegistration;
import com.eischet.janitor.toolbox.listeners.ListenerSet;
import com.eischet.janitor.toolbox.listeners.ListenerSetStandard;
import com.eischet.janitor.versioning.Version;
import com.eischet.janitor.versioning.VersionRange;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.eischet.janitor.api.util.ObjectUtilities.simpleClassNameOf;

/**
 * The standard {@link Dao}, which maps an entity type to a database table according to the meta-data in the entity's dispatch table.
 * @param <T> the type of the entities
 * @param <U> the type of the DAO collection that this DAO belongs to
 */
public abstract class GenericDao<T extends OrmEntity, U extends OrmDaoCollection<U>> extends JanitorComposed<GenericDao<?, ?>> implements Dao<T>, JCallable {

    public static final DispatchTable<GenericDao<?, ?>> DISPATCH = new DispatchTable<>();
    private static final Predicate<String> INVALID_FIELD = Pattern.compile("[a-zA-Z0-9_@-]+").asMatchPredicate().negate();

    static {
        DISPATCH.addBooleanProperty("verbose", GenericDao::isVerbose, GenericDao::setVerbose);
        DISPATCH.addStringProperty("tableName", dao -> dao.tableName);
        DISPATCH.addStringProperty("idColumn", dao -> dao.idColumn);
        DISPATCH.addListProperty("columns", dao -> Janitor.list(dao.columns.stream().map(Janitor::string)));
        DISPATCH.addStringProperty("keyColumn", dao -> dao.keyColumn);
        DISPATCH.addStringProperty("className", dao -> dao.className);
        // should columnForField and fieldForColumn be made public as well!?

        DISPATCH.addMethod("insert", (self, process, arguments) -> self.insertForScript(process, arguments.require(1).get(0)));
        DISPATCH.addVoidMethod("update", (self, process, arguments) -> self.updateForScript(process, arguments.require(1).get(0)));
        DISPATCH.addMethod("findAll", GenericDao::scriptFindAll);
        DISPATCH.addMethod("getById", GenericDao::scriptFindById);
        DISPATCH.addMethod("getByKey", GenericDao::scriptFindByKey);
        DISPATCH.addMethod("getAll", GenericDao::scriptFindAll);
        DISPATCH.addMethod("findById", GenericDao::scriptFindById);
        DISPATCH.addMethod("findByKey", GenericDao::scriptFindByKey);
        DISPATCH.addMethod("findByIds", GenericDao::scriptFindByIds);
        DISPATCH.addMethod("queryForEach", GenericDao::scriptQueryForEach);

        DISPATCH.addStringProperty("jsonSchema", self -> Janitor.current().writeJson(self.entityDispatchTable::writeSchemaToJson));
    }

    /** How many entities {@link #scriptQueryForEach} loads in one transaction before it runs the callbacks for them. */
    protected static final int QUERY_FOR_EACH_CHUNK_SIZE = 100;

    /**
     * How many IDs {@link #findByIds} puts into one {@code IN (...)} list. Databases limit this (Oracle: 1000 elements; SQL Server: 2100
     * parameters per statement), so longer lists are split into several queries.
     */
    protected static final int FIND_BY_IDS_CHUNK_SIZE = 500;

    protected final JanitorLogger log = JanitorLogger.getLogger(getClass());
    protected final ListenerSet<EntityChangeListener<T>> entityChangeListeners= new ListenerSetStandard<>();
    protected final @NotNull String tableName;
    protected final @NotNull String idColumn;
    protected final @NotNull
    @Unmodifiable List<String> columns;
    /**
     * Like {@link #columns}, but excluding any column registered via
     * {@link com.eischet.janitor.orm.entity.OrmObject#addLazyTextProperty} (see
     * {@link JanitorOrm.MetaData#LAZY_LOAD}). This is what {@link #findById}/{@link #findByKey}/
     * {@link #findAll}/{@link #findByFilter}/{@link #findByAssociation} actually SELECT; INSERT/UPDATE
     * keep using {@link #columns}, since writing a lazily-loaded field's current value is unaffected by
     * whether it was eagerly fetched.
     */
    protected final @NotNull @Unmodifiable List<String> selectColumns;
    protected final String keyColumn;
    /** True if the entity declares a modification date column, see {@link JanitorOrm.MetaData#MODIFIED_FIELD}. */
    private final boolean hasModifiedColumn;
    protected final EntityDispatchTable<T, U> entityDispatchTable;
    protected final Supplier<T> newValue;
    protected final Map<String, String> columnForField = new HashMap<>();
    protected final Map<String, String> fieldForColumn = new HashMap<>();
    protected final OrmDaoCollection<?> collection;
    protected final String className;
    protected final @NotNull Class<T> entityClass;
    protected boolean verbose = false;
    protected @Nullable DaoLogging logging;
    protected @NotNull EntityCache<T> cache = EntityCache.noop();
    private volatile @Nullable DaoMetrics metrics;

    // The SQL that only depends on the table, the mapped columns and the dialect is built once, in the constructor, so that a DAO that cannot
    // work (e.g. no columns) fails early instead of on first use. Statement objects are immutable and can be shared between threads.
    private final @NotNull SelectStatement selectByIdStatement;
    private final @Nullable SelectStatement selectByKeyStatement;
    /** Select of all selectable columns without a where clause; the base for queries that append their own conditions. */
    private final @NotNull String selectAllSql;
    private final @NotNull String countAllSql;
    /**
     * The id of the row that stands for "no record" in this collection ({@link OrmDaoCollection#getNullReferenceId()}), or null if there is
     * none. Since a reference to it means nothing, the row is no record either: this DAO never reads, updates or deletes it.
     */
    private final @Nullable Long nullRowId;
    /** The condition that leaves out the row {@link #nullRowId}, or null if there is none. */
    private final @Nullable String visibleRowsCondition;
    private final @NotNull UpdateStatement insertStatement;
    private final @NotNull UpdateStatement updateStatement;
    private final @NotNull UpdateStatement deleteStatement;
    /** The version column if the entity is versioned (see {@link JanitorOrm.MetaData#VERSION_FIELD}) and the column is mapped, otherwise null. */
    private final @Nullable String versionColumn;
    /** Like {@link #updateStatement}, but only for the row that still has the expected version; null if there is no version column. */
    private final @Nullable UpdateStatement versionedUpdateStatement;
    /** Like {@link #deleteStatement}, but only for the row that still has the expected version; null if there is no version column. */
    private final @Nullable UpdateStatement versionedDeleteStatement;
    private final @NotNull @Unmodifiable List<String> updateColumns;
    private final Map<String, SelectStatement> lazyColumnSelects = new ConcurrentHashMap<>();
    /** The {@code IN (...)} queries of {@link #findByIds}, by the number of IDs in the list; there are at most {@link #FIND_BY_IDS_CHUNK_SIZE}. */
    private final Map<Integer, SelectStatement> selectByIdsStatements = new ConcurrentHashMap<>();
    private final @NotNull @Unmodifiable Map<String, ColumnMapping<T>> mappingForColumn;
    /** The mappings of {@link #selectColumns}, in that order. */
    private final @NotNull ColumnMapping<T>[] selectMappings;

    public GenericDao(
            final @NotNull DispatchTable<? extends GenericDao<T, U>> childDispatch,
            final @NotNull OrmDaoCollection<?> collection,
            final @NotNull Class<T> entityClass,
            final @NotNull EntityDispatchTable<T, U> entityDispatchTable,
            final @NotNull Supplier<T> newValue) {
        super(Dispatcher.inherit(DISPATCH, childDispatch));
        this.collection = collection;
        this.entityClass = entityClass;
        this.entityDispatchTable = entityDispatchTable;
        this.newValue = newValue;
        this.className = Objects.requireNonNull(entityDispatchTable.getMetaData(Janitor.MetaData.CLASS), "missing required CLASS");
        this.tableName = Objects.requireNonNull(entityDispatchTable.getMetaData(JanitorOrm.MetaData.TABLE_NAME), "missing required TABLE_NAME");
        this.idColumn = Objects.requireNonNull(entityDispatchTable.getMetaData(JanitorOrm.MetaData.ID_FIELD), "missing required ID_FIELD");
        this.keyColumn = entityDispatchTable.getMetaData(JanitorOrm.MetaData.KEY_FIELD); // made optional because it's not in every table (upstream)
        this.hasModifiedColumn = entityDispatchTable.getMetaData(JanitorOrm.MetaData.MODIFIED_FIELD) != null;

        if (log.isDebugEnabled()) {
            log.debug("initializing dao for entity {} in table {}", className, tableName);
        }
        final List<String> databaseBackedFields = new ArrayList<>();
        final List<String> selectableFields = new ArrayList<>();
        final List<String> allFields = entityDispatchTable.streamAttributeNames().toList();

        @Nullable Version schemaVersion = collection.getSchemaVersion();

        for (final String field : allFields) {
            @Nullable final String columnName = entityDispatchTable.getMetaData(field, JanitorOrm.MetaData.COLUMN_NAME);

            // When specified: skip fields that do not match the schema version, to self-adjust to schema changes
            @Nullable final VersionRange versionRange = entityDispatchTable.getMetaData(field, JanitorOrm.MetaData.VERSION_RANGE);
            if (!OrmPropertyHandle.isAvailableIn(versionRange, schemaVersion)) {
                continue;
            }

            if (columnName != null && !columnName.isBlank()) {
                columnForField.put(field, columnName);
                fieldForColumn.put(columnName, field);
                databaseBackedFields.add(columnName);
                if (!Boolean.TRUE.equals(entityDispatchTable.getMetaData(field, JanitorOrm.MetaData.LAZY_LOAD))) {
                    selectableFields.add(columnName);
                }
            }
        }
        this.columns = List.copyOf(databaseBackedFields);
        this.selectColumns = List.copyOf(selectableFields);

        final Map<String, ColumnMapping<T>> mappings = new HashMap<>();
        for (final String column : columns) {
            mappings.put(column, ColumnMapping.of(entityDispatchTable, fieldForColumn.get(column), column, collection.getNullReferenceId()));
        }
        this.mappingForColumn = Map.copyOf(mappings);
        @SuppressWarnings("unchecked") final ColumnMapping<T>[] selected = new ColumnMapping[selectColumns.size()];
        for (int i = 0; i < selected.length; i++) {
            selected[i] = mappings.get(selectColumns.get(i));
        }
        this.selectMappings = selected;

        this.nullRowId = collection.getNullReferenceId();
        this.visibleRowsCondition = nullRowId == null ? null : getDialect().quoteColumn(idColumn) + " <> " + nullRowId;
        final StatementCreator creator = new StatementCreator(getDialect());
        this.selectByIdStatement = SelectStatement.of(creator.createSelectStatement(tableName, selectColumns, idColumn));
        this.selectByKeyStatement = keyColumn == null ? null : SelectStatement.of(creator.createSelectStatement(tableName, selectColumns, keyColumn)
                + (visibleRowsCondition == null ? "" : " and " + visibleRowsCondition));
        this.selectAllSql = creator.createSelectAllStatement(tableName, selectColumns);
        this.countAllSql = creator.createCountStatement(tableName);
        this.updateColumns = columns.stream().filter(column -> !Objects.equals(column, idColumn)).toList();
        this.insertStatement = UpdateStatement.of(creator.createInsertStatement(tableName, columns));
        this.updateStatement = UpdateStatement.of(creator.createUpdateStatement(tableName, updateColumns, idColumn));
        this.deleteStatement = UpdateStatement.of(creator.createDeleteStatement(tableName, idColumn));
        final @Nullable String declaredVersionColumn = entityDispatchTable.getMetaData(JanitorOrm.MetaData.VERSION_FIELD);
        this.versionColumn = declaredVersionColumn != null && mappingForColumn.containsKey(declaredVersionColumn) && !declaredVersionColumn.equals(idColumn) ? declaredVersionColumn : null;
        if (versionColumn == null) {
            this.versionedUpdateStatement = null;
            this.versionedDeleteStatement = null;
        } else {
            final String versionCondition = " and " + getDialect().quoteColumn(versionColumn) + " = ?";
            this.versionedUpdateStatement = UpdateStatement.of(creator.createUpdateStatement(tableName, updateColumns, idColumn) + versionCondition);
            this.versionedDeleteStatement = UpdateStatement.of(creator.createDeleteStatement(tableName, idColumn) + versionCondition);
        }

        collection.registerDao(this);
    }

    /**
     * Appends a where clause to a query: the condition, if any, and the one that leaves out the row that stands for "no record", if there is one.
     */
    private @NotNull String where(final @NotNull String sql, final @Nullable String condition) {
        if (visibleRowsCondition == null) {
            return condition == null ? sql : sql + "\nWHERE\n  " + condition;
        }
        return sql + "\nWHERE\n  " + visibleRowsCondition + (condition == null ? "" : " and (" + condition + ")");
    }

    /**
     * Whether an id is that of the row that stands for "no record" in this collection, see {@link OrmDaoCollection#getNullReferenceId()}.
     * That row is never read, updated or deleted.
     * @param id an id
     * @return true for the id of that row
     */
    protected final boolean isNullRowId(final long id) {
        return nullRowId != null && nullRowId == id;
    }

    private void refuseNullRow(final @NotNull T record, final @NotNull String operation) throws DatabaseError {
        if (isNullRowId(record.getId())) {
            throw new DatabaseError("cannot " + operation + " " + className + " " + record.getId() + ": that row stands for \"no record\"");
        }
    }

    /**
     * Returns how a mapped column is read and written. This is what {@link #readAllProperties} and {@link #writeAllColumns} use, and what
     * subclasses that override them should use, too, instead of looking up the meta-data again for every column of every row.
     * @param column the name of the column
     * @return the mapping, or null if the column is not mapped (also: not in the schema version of the collection)
     */
    protected final @Nullable ColumnMapping<T> getColumnMapping(final @NotNull String column) {
        return mappingForColumn.get(column);
    }

    /**
     * The collection this DAO belongs to; e.g. for a {@link com.eischet.janitor.orm.metrics.DaoMetricsProvider} that labels its metrics by
     * collection.
     */
    public @NotNull OrmDaoCollection<?> getDaoCollection() {
        return collection;
    }

    /**
     * @return the dialect of the database, as defined by the collection that this DAO belongs to
     */
    protected final @NotNull DatabaseDialect getDialect() {
        return collection.getDialect();
    }

    /**
     * Opts this DAO into caching: {@link #findById}/{@link #findByKey} will consult {@code cache} before
     * querying the database, and it's kept in sync automatically via {@link #addChangeListener}, which
     * every insert/update/delete already fires — {@code cache.put(record)} on insert/update (so a write
     * refreshes the cache with the value just written, no extra round trip needed on the next read), and
     * {@code cache.invalidateById(record.getId())} on delete. Call this once, from a subclass's
     * constructor, if that entity type should be cached; by default {@link #cache} is
     * {@link EntityCache#noop()}, so DAOs that never call this behave exactly as before caching existed.
     *
     * @param cache the cache to attach; see {@link SimpleEntityCache} for a ready-to-use, dependency-free
     *              implementation
     */
    protected final void enableCache(final @NotNull EntityCache<T> cache) {
        this.cache = cache;
        cache.setEvictionListener(reason -> metrics().evicted(reason));
        addChangeListener((type, entity) -> {
            switch (type) {
                case INSERT, UPDATE -> cache.put(copyForCache(entity));
                case DELETE -> cache.invalidateById(entity.getId());
            }
        });
    }

    /**
     * Hook for the entities this DAO hands to <b>scripts</b>: scripts must not be able to change a cached entity just by assigning to
     * its fields, without ever saving it. A DAO with a cache therefore overrides this to return a private copy. Applied to everything
     * the script-facing finders return ({@code getById}, {@code getByKey}, {@code getAll}, {@code findAll}, {@code findById},
     * {@code findByKey}) and to what {@code queryForEach} passes to its callback. Default: the entity itself.
     */
    protected @NotNull T forScript(final @NotNull T entity) {
        return entity;
    }

    /**
     * Hook for the entity that goes <b>into the cache</b> after a write. Without it, the cache would share the instance with whoever
     * called {@code insert}/{@code update} (typically a script, which may well keep working on it). A DAO with a cache overrides this
     * to return a private copy. Default: the entity itself.
     */
    protected @NotNull T copyForCache(final @NotNull T entity) {
        return entity;
    }

    /** True if a cache was attached with {@link #enableCache}. */
    protected final boolean isCachingEnabled() {
        return (Object) cache != EntityCache.noop();
    }

    @Override
    public final boolean cachesEntities() {
        return isCachingEnabled();
    }

    /**
     * The state of the attached cache (size, maximum, time to live), for monitoring; null if this DAO has no cache.
     */
    public @Nullable CacheState getCacheState() {
        return isCachingEnabled() ? new CacheState(cache.size(), cache.maxSize(), cache.ttl()) : null;
    }

    /**
     * Where this DAO reports cache hits, misses, load times and evictions; asked for once, see {@link OrmMetrics}.
     */
    private @NotNull DaoMetrics metrics() {
        DaoMetrics current = metrics;
        if (current == null) {
            current = OrmMetrics.forDao(this);
            metrics = current;
        }
        return current;
    }

    private @NotNull JanitorObject scriptResult(final @Nullable T entity) {
        return entity == null ? Janitor.NULL : forScript(entity);
    }

    /**
     * Mainly for scripts, which cannot get this for themselves, we need a method to acquire a data manager when needed.
     * By default, this is the data manager of the collection that this DAO belongs to.
     *
     * @return a data manager object
     */
    protected DataManager getDataManager() {
        return collection.getDataManager();
    }

    /**
     * @return true if the entities of this DAO track their changes; the default is false
     */
    public boolean isChangeTracked() {
        return false;
    }

    @Override
    public JanitorObject call(final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        if (arguments.size() == 0) {
            return newValue.get();
        }
        if (arguments.size() == 1) {
            final JanitorObject arg = arguments.get(0);
            if (arg instanceof JMap map) {
                final T nv = newValue.get();
                map.applyTo(process, nv);
                return nv;
            }
            if (arg instanceof JString str) {
                try {
                    final JMap map = Janitor.map();
                    map.readJson(Janitor.current().getLenientJsonConsumer(str.janitorGetHostValue()));
                    final T nv = newValue.get();
                    map.applyTo(process, nv);
                    return nv;
                } catch (JsonException e) {
                    throw new JanitorNativeException(process, "invalid JSON", e);
                }
            }
        }
        throw new JanitorNativeException(process, "the constructor for new objects takes no parameter, a map to apply, or a string to parse to a map and then apply", null);
    }

    /**
     * @return true if this DAO logs what it does in detail
     */
    public boolean isVerbose() {
        return verbose;
    }

    /**
     * Sets whether this DAO logs what it does in detail.
     * @param verbose true to enable detailed logging
     */
    public void setVerbose(final boolean verbose) {
        this.verbose = verbose;
    }

    @Override
    public @Nullable T findByKey(final @NotNull DatabaseConnection conn, final @Nullable String key) throws DatabaseError {
        final T cached = cache.findByKey(key);
        if (cached != null) {
            metrics().cacheHit();
            return cached;
        }
        if (!isCachingEnabled()) {
            return findByKeyIgnoringCache(conn, key);
        }
        metrics().cacheMiss();
        final long start = System.nanoTime();
        try {
            return findByKeyIgnoringCache(conn, key);
        } finally {
            metrics().loaded(System.nanoTime() - start);
        }
    }

    /**
     * Like {@link #findByKey}, but always queries the database, bypassing any cache attached via
     * {@link #enableCache}. The result is still stored into the cache (if any), same as a normal cache
     * miss would.
     */
    public @Nullable T findByKeyIgnoringCache(final @NotNull DatabaseConnection conn, final @Nullable String key) throws DatabaseError {
        if (keyColumn == null) {
            throw new DatabaseError("no key column defined for table '" + tableName + "'");
        }
        if (key == null || key.isBlank()) {
            if (verbose) {
                log.info("{}::findByKey(null-or-blank={}) -> returning null", className, key);
            }
            return null;
        }
        final SelectStatement select = Objects.requireNonNull(selectByKeyStatement);
        if (verbose) {
            log.info("{}::findByKey(key='{}'): running {}", className, key, select);
        }
        final T loaded = conn.queryForObject(select, stmt -> stmt.addString(key), rs -> readAllProperties(conn, rs));
        cache.put(loaded);
        return loaded;
    }

    @Override
    public @Nullable T findById(final @NotNull DatabaseConnection conn, final long id) throws DatabaseError {
        if (isNullRowId(id)) {
            return null;
        }
        final T cached = cache.findById(id);
        if (cached != null) {
            metrics().cacheHit();
            return cached;
        }
        if (!isCachingEnabled()) {
            return findByIdIgnoringCache(conn, id);
        }
        metrics().cacheMiss();
        final long start = System.nanoTime();
        try {
            return findByIdIgnoringCache(conn, id);
        } finally {
            metrics().loaded(System.nanoTime() - start);
        }
    }

    @Override
    public @NotNull @Unmodifiable List<T> findByIds(final @NotNull DatabaseConnection conn, final @NotNull Collection<Long> ids) throws DatabaseError {
        final Set<Long> distinct = new LinkedHashSet<>();
        for (final Long id : ids) {
            if (id != null && !isNullRowId(id)) {
                distinct.add(id);
            }
        }
        final Map<Long, T> found = new HashMap<>();
        final List<Long> missing = new ArrayList<>();
        for (final Long id : distinct) {
            final T cached = cache.findById(id);
            if (cached != null) {
                metrics().cacheHit();
                found.put(id, cached);
            } else {
                if (isCachingEnabled()) {
                    metrics().cacheMiss();
                }
                missing.add(id);
            }
        }
        for (int from = 0; from < missing.size(); from += FIND_BY_IDS_CHUNK_SIZE) {
            final List<Long> chunk = missing.subList(from, Math.min(from + FIND_BY_IDS_CHUNK_SIZE, missing.size()));
            final SelectStatement select = selectByIdsStatements.computeIfAbsent(chunk.size(), this::createSelectByIdsStatement);
            if (verbose) {
                log.info("{}::findByIds({} ids): running {}", className, chunk.size(), select);
            }
            final long start = System.nanoTime();
            final List<T> loaded = conn.queryForList(select, stmt -> {
                for (final Long id : chunk) {
                    stmt.addLong(id);
                }
            }, rs -> readAllProperties(conn, rs));
            if (isCachingEnabled()) {
                metrics().loaded(System.nanoTime() - start);
            }
            for (final T entity : loaded) {
                found.put(entity.getId(), entity);
                cache.put(entity);
            }
        }
        final List<T> result = new ArrayList<>(found.size());
        for (final Long id : distinct) {
            final T entity = found.get(id);
            if (entity != null) {
                result.add(entity);
            }
        }
        return Collections.unmodifiableList(result);
    }

    private @NotNull SelectStatement createSelectByIdsStatement(final int idCount) {
        return SelectStatement.of(selectAllSql + "\nWHERE\n  " + getDialect().quoteColumn(idColumn) + " in (" + "?, ".repeat(idCount - 1) + "?)");
    }

    /**
     * Like {@link #findById}, but always queries the database, bypassing any cache attached via
     * {@link #enableCache}. The result is still stored into the cache (if any), same as a normal cache
     * miss would.
     */
    public @Nullable T findByIdIgnoringCache(final @NotNull DatabaseConnection conn, final long id) throws DatabaseError {
        if (isNullRowId(id)) {
            return null;
        }
        final SelectStatement select = selectByIdStatement;
        if (verbose) {
            log.info("{}::findById(id={}): running {}", className, id, select);
        }
        final T loaded = conn.queryForObject(select, stmt -> stmt.addLong(id), rs -> readAllProperties(conn, rs));
        cache.put(loaded);
        return loaded;
    }

    /**
     * Fetches a single lazily-loaded CLOB or NCLOB column's current value for one row, running its own,
     * independent query instead of going through {@link #findById}/{@link #readAllProperties}. This is
     * what {@link com.eischet.janitor.orm.entity.LazyLoadedString} calls the first time such a field is
     * actually read (see {@link JanitorOrm.MetaData#LAZY_LOAD}); it isn't meant to be called directly
     * otherwise.
     *
     * @param id     the entity's ID
     * @param column the column to fetch (must be one of {@link #columns}, i.e. actually mapped)
     * @return the column's current value, or {@code null} if the row doesn't exist or the value is null
     */
    public @Nullable String fetchLazyColumn(final long id, final @NotNull String column) throws DatabaseError {
        if (!columns.contains(column)) {
            return null;
        }
        final SelectStatement select = lazyColumnSelects.computeIfAbsent(column, c -> SelectStatement.of(new StatementCreator(getDialect()).createSelectStatement(tableName, List.of(c), idColumn)));
        if (verbose) {
            log.info("{}::fetchLazyColumn(id={}, column='{}'): running {}", className, id, column, select);
        }
        final @Nullable String field = fieldForColumn.get(column);
        final @Nullable ColumnTypeHint columnTypeHint = field == null ? null : entityDispatchTable.getMetaData(field, JanitorOrm.MetaData.COLUMN_TYPE);
        if (columnTypeHint == ColumnTypeHint.NCLOB) {
            return getDataManager().callTransaction(conn -> conn.queryForObject(select, stmt -> stmt.addLong(id), rs -> rs.readNationalClob()));
        }
        return getDataManager().callTransaction(conn -> conn.queryForObject(select, stmt -> stmt.addLong(id), rs -> rs.readClob()));
    }

    @Override
    public @NotNull List<T> findAll(final @NotNull DatabaseConnection conn, final @Nullable Integer limit) throws DatabaseError {
        return findAll(conn, limit, null);
    }

    /**
     * Like {@link #findAll(DatabaseConnection, Integer)}, but skips the first rows, for paging. The offset needs a limit, which is the page size.
     * Without a limit the result is not sorted; with a limit the rows are sorted by the key column, or by the ID if the table has no key,
     * and if there is an offset, by the ID column after the key, so that pages do not overlap. To page through rows sorted otherwise, use
     * {@link #findByFilter} with {@link FilterQuery#orderBy(String)}.
     * <p>
     * On databases that cannot limit rows in SQL, the limit and offset are applied while reading, so rows before the end of the page are
     * still transferred, but not turned into entities.
     * </p>
     *
     * @param conn the database connection
     * @param limit the maximum number of entities, or null for no limit
     * @param offset the number of entities to skip, or null for none; requires a limit if above 0
     * @return the entities
     * @throws DatabaseError on database errors
     */
    public @NotNull List<T> findAll(final @NotNull DatabaseConnection conn, final @Nullable Integer limit, final @Nullable Integer offset) throws DatabaseError {
        final boolean limited = limit != null && limit > 0;
        final int skip = checkOffset(offset, limited);
        final String sql = limited ? where(selectAllSql, null) + " " + defaultOrderBy(offset != null) : where(selectAllSql, null);
        if (verbose) {
            log.info("{}::findAll(): running {}, limit={}, offset={}", className, sql, limit, offset);
        }
        return queryForEntities(conn, sql, limited ? limit : null, skip, ps -> {});
    }

    /** Validates an offset; returns it as a number of rows to skip. */
    private static int checkOffset(final @Nullable Integer offset, final boolean limited) {
        if (offset == null) {
            return 0;
        }
        if (offset < 0) {
            throw new IllegalArgumentException("the offset must not be negative");
        }
        if (offset > 0 && !limited) {
            throw new IllegalArgumentException("an offset needs a limit (the page size)");
        }
        return offset;
    }

    /**
     * The sort order of queries that did not ask for one: by the key column if the table has one, by the ID otherwise. Paged queries
     * sorted by the key get the ID as the last criterion, to make the order unambiguous (a key need not be unique on its own).
     * The position of a column is never used: the second column may be a LOB, which some databases (Oracle) cannot sort by.
     */
    private @NotNull String defaultOrderBy(final boolean paged) {
        final DatabaseDialect dialect = getDialect();
        if (keyColumn == null) {
            return "order by " + dialect.quoteColumn(idColumn);
        }
        return paged
                ? "order by " + dialect.quoteColumn(keyColumn) + ", " + dialect.quoteColumn(idColumn)
                : "order by " + dialect.quoteColumn(keyColumn);
    }

    /**
     * Runs a query for entities, applying a limit and an offset in SQL if the database can do that, and while reading otherwise.
     * @param sql the query, including the sort order, without limit and offset
     * @param limit the maximum number of rows, or null for no limit
     * @param offset the number of rows to skip
     * @param configurator binds the parameters of the query, except for the limit and offset
     */
    private @NotNull List<T> queryForEntities(final DatabaseConnection conn, final String sql,
                                              final @Nullable Integer limit, final int offset, final StatementConfigurator configurator) throws DatabaseError {
        if (limit == null) {
            return conn.queryForList(SelectStatement.of(sql), configurator, rs -> readAllProperties(conn, rs));
        }
        if (collection.canLimitAndOffset()) {
            final SelectStatement limited = getDialect().addLimitAndOffset(SelectStatement.of(sql));
            return conn.queryForList(limited, ps -> {
                configurator.configure(ps);
                getDialect().addLimitAndOffset(ps, limit, offset);
            }, rs -> readAllProperties(conn, rs));
        }
        return readLimitedWhileReading(conn, SelectStatement.of(sql), configurator, limit, offset);
    }

    /** For databases that cannot limit in SQL: stops reading after the page, and skips the rows before it without turning them into entities. */
    private @NotNull List<T> readLimitedWhileReading(final DatabaseConnection conn, final SelectStatement select, final StatementConfigurator configurator,
                                                     final int limit, final int offset) throws DatabaseError {
        final List<T> result = new ArrayList<>(Math.min(limit, 1000));
        final int[] skipped = {0};
        conn.queryForEach(select, configurator, rs -> {
            if (skipped[0] < offset) {
                skipped[0]++;
            } else {
                result.add(readAllProperties(conn, rs));
            }
        }, (long) offset + limit, null);
        return Collections.unmodifiableList(result);
    }

    @Override
    public @NotNull @Unmodifiable List<T> findByQuery(@NotNull final DatabaseConnection conn, @NotNull final String query, @NotNull final StatementConfigurator statementConfigurator) throws DatabaseError {
        final SelectStatement select = SelectStatement.of(query);
        final List<T> found = conn.queryForList(select, statementConfigurator, rs -> readAllProperties(conn, rs));
        return nullRowId == null ? found : found.stream().filter(entity -> !isNullRowId(entity.getId())).toList();

    }

    public static final ExpressionPrepperBuilder PREP_LONG = filterExpression -> new NamedPrepper((conn, stmt) -> stmt.addLong(Objects.requireNonNull(filterExpression.getValueLong()).longValue()), "long=" + (filterExpression.getValueLong() != null ? filterExpression.getValueLong().longValue() : "?"));
    public static final ExpressionPrepperBuilder PREP_DATE = filterExpression -> new NamedPrepper((conn, stmt) -> stmt.addNullableDate(filterExpression.getValueDate()), "date=" + filterExpression.getValueDate());
    public static final ExpressionPrepperBuilder PREP_DATETIME = filterExpression -> new NamedPrepper((conn, stmt) -> stmt.addNullableDateTime(filterExpression.getValueDateTime()), "datetime=" + filterExpression.getValueDateTime());
    public static final ExpressionPrepperBuilder PREP_DOUBLE = filterExpression -> new NamedPrepper((conn, stmt) -> stmt.addNullableDouble(filterExpression.getValueDouble()), "double=" + filterExpression.getValueDouble());
    public static final ExpressionPrepperBuilder PREP_BOOLEAN = filterExpression -> new NamedPrepper((conn, stmt) -> stmt.addInt(Boolean.TRUE == filterExpression.getValueBoolean() ? 1 : 0), "bool=" + filterExpression.getValueBoolean());
    /**
     * Like {@link #PREP_BOOLEAN}, but for columns using {@link ColumnTypeHint#BOOL_CHAR} storage, i.e. a
     * {@code "y"}/{@code "n"} character column instead of an integer one.
     */
    public static final ExpressionPrepperBuilder PREP_BOOL_CHAR = filterExpression -> new NamedPrepper((conn, stmt) -> stmt.addString(Boolean.TRUE == filterExpression.getValueBoolean() ? "y" : "n"), "boolChar=" + filterExpression.getValueBoolean());

    public static final ExpressionPrepperBuilder PREP_STRING = filterExpression -> new NamedPrepper((conn, stmt) -> {
        if (filterExpression.getValueString() != null) {
            stmt.addString(filterExpression.getValueString());
        } else {
            stmt.addNullString();
        }
    }, filterExpression.getValueString() != null ? ("string='" + filterExpression.getValueString() + "'") : ("string=null"));

    public static final ExpressionPrepperBuilder PREP_STARTS_WITH_STRING = filterExpression -> new NamedPrepper((conn, stmt) -> {
        if (filterExpression.getValueString() != null) {
            stmt.addString(conn.getDialect().escapeLikePattern(filterExpression.getValueString()) + "%");
        } else {
            stmt.addNullString();
        }
    }, filterExpression.getValueString() != null ? ("string='" + filterExpression.getValueString() + "%'") : ("string=null"));

    public static final ExpressionPrepperBuilder PREP_ENDS_WITH_STRING = filterExpression -> new NamedPrepper((conn, stmt) -> {
        if (filterExpression.getValueString() != null) {
            stmt.addString("%" + conn.getDialect().escapeLikePattern(filterExpression.getValueString()));
        } else {
            stmt.addNullString();
        }
    }, filterExpression.getValueString() != null ? ("string='%" + filterExpression.getValueString() + "'") : ("string=null"));

    public static final ExpressionPrepperBuilder PREP_CONTAINS_STRING = filterExpression -> new NamedPrepper((conn, stmt) -> {
        if (filterExpression.getValueString() != null) {
            stmt.addString("%" + conn.getDialect().escapeLikePattern(filterExpression.getValueString()) + "%");
        } else {
            stmt.addNullString();
        }
    }, filterExpression.getValueString() != null ? ("string='%" + filterExpression.getValueString() + "%'") : ("string=null"));


    /**
     * Hook for subclasses to handle certain filter expressions with custom SQL, e.g. for virtual fields.
     * @param filterExpression the expression
     * @return the handler, or null to use the default handling
     */
    protected @Nullable ExpressionHandler getCustomExpressionHandler(final FilterExpression filterExpression) {
        return null;
    }

    /**
     * Creates the prepper that binds the value of a filter expression, according to the type of the value.
     * @param filterExpression the expression
     * @return the prepper
     */
    protected @NotNull Prepper getPrepper(final FilterExpression filterExpression) {
        return getPrepper(filterExpression, null);
    }

    /**
     * Like {@link #getPrepper(FilterExpression)}, but takes the target column's type into account where it
     * matters, i.e. to distinguish {@link ColumnTypeHint#BIT} from {@link ColumnTypeHint#BOOL_CHAR} storage
     * for a boolean-valued expression. Pass {@code null} when the column type isn't known (e.g. for a
     * synthetic/non-entity column), which falls back to the same behavior as {@link #getPrepper(FilterExpression)}.
     */
    protected @NotNull Prepper getPrepper(final FilterExpression filterExpression, final @Nullable ColumnTypeHint columnTypeHint) {
        if (filterExpression.isDate()) {
            return PREP_DATE.getPrepper(filterExpression);
        } else if (filterExpression.isDateTime()) {
            return PREP_DATETIME.getPrepper(filterExpression);
        } else if (filterExpression.isDouble()) {
            return PREP_DOUBLE.getPrepper(filterExpression);
        } else if (filterExpression.isBoolean()) {
            return columnTypeHint == ColumnTypeHint.BOOL_CHAR ? PREP_BOOL_CHAR.getPrepper(filterExpression) : PREP_BOOLEAN.getPrepper(filterExpression);
        } else if (filterExpression.isLong()) {
            return PREP_LONG.getPrepper(filterExpression);
        } else {
            return PREP_STRING.getPrepper(filterExpression);
        }
    }

    /**
     * Converts a filter expression to an SQL condition, collecting the preppers that bind its values.
     * @param filterExpression the expression
     * @param prepperConsumer receives the preppers, in the order of the parameters in the SQL
     * @return the SQL condition
     * @throws MalformedExpression if the expression is invalid
     */
    protected String expressionToSql(final FilterExpression filterExpression, final Consumer<Prepper> prepperConsumer) throws MalformedExpression {
        @NotNull final DatabaseDialect dialect = getDialect();
        if (filterExpression.getField() != null && INVALID_FIELD.test(filterExpression.getField())) {
            throw new MalformedExpression("invalid field '" + filterExpression.getField() + "'");
        }
        if (filterExpression.isGroup()) {
            return filterExpression.getFilters().stream()
                    .map((FilterExpression element) -> expressionToSql(element, prepperConsumer))
                    .collect(Collectors.joining(" " + filterExpression.getLogic() + " ", "(", ")"));
        } else if (filterExpression.isExpression()) {
            @Nullable final ExpressionHandler customExpressionHandler = getCustomExpressionHandler(filterExpression);
            if (customExpressionHandler != null) {
                @Nullable final ExpressionPrepperBuilder prepper = customExpressionHandler.buildPrepper();
                if (prepper != null) {
                    prepperConsumer.accept(prepper.getPrepper(filterExpression));
                }
                return customExpressionHandler.getSqlFragment();
            } else {
                final String namedField = filterExpression.getField();
                final String column = columnForField.get(namedField);
                if (column == null) {
                    throw new MalformedExpression("missing column for field '" + namedField + "'");
                }
                final @Nullable ColumnTypeHint columnTypeHint = entityDispatchTable.getMetaData(namedField, JanitorOrm.MetaData.COLUMN_TYPE);
                final @Nullable ColumnCase columnCase = entityDispatchTable.getMetaData(namedField, JanitorOrm.MetaData.COLUMN_CASE);
                if (columnTypeHint == null) {
                    throw new MalformedExpression("missing type for column '" + column + "' of field '" + namedField + "'");
                }
                if (filterExpression.getOperator() == null) {
                    throw new MalformedExpression("missing operator in expression " + filterExpression);
                }
                final String quotedColumn = dialect.quoteColumn(column);
                final @Nullable ColumnMapping<T> mapping = mappingForColumn.get(column);
                final @Nullable Long nullReferenceId = mapping == null ? null : mapping.nullReferenceId();
                if (nullReferenceId != null) {
                    // a reference to nothing can be NULL or the configured id, see JanitorOrm.MetaData.NULL_REFERENCE_ID
                    switch (filterExpression.getOperator()) {
                        case ISNULL -> {
                            return "(" + quotedColumn + " is null or " + quotedColumn + " = " + nullReferenceId + ")";
                        }
                        case ISNOTNULL -> {
                            return "(" + quotedColumn + " is not null and " + quotedColumn + " <> " + nullReferenceId + ")";
                        }
                        default -> {
                        }
                    }
                }
                return applyExpressionToColumn(filterExpression, quotedColumn, columnTypeHint, columnCase, prepperConsumer);
            }
        } else {
            throw new MalformedExpression("part is neither group nor expression");
        }
    }

    /**
     * Converts a filter expression to an SQL condition for one column, collecting the preppers that bind its values.
     * @param filterExpression the expression
     * @param quotedColumn the column, already quoted for the database
     * @param prepperConsumer receives the preppers, in the order of the parameters in the SQL
     * @return the SQL condition
     * @throws MalformedExpression if the expression is invalid
     */
    protected String applyExpressionToColumn(final FilterExpression filterExpression, final String quotedColumn, final Consumer<Prepper> prepperConsumer) throws MalformedExpression {
        return applyExpressionToColumn(filterExpression, quotedColumn, null, prepperConsumer);
    }

    /**
     * Like {@link #applyExpressionToColumn(FilterExpression, String, Consumer)}, but takes the target
     * column's type into account (see {@link #getPrepper(FilterExpression, ColumnTypeHint)}). Pass
     * {@code null} for a synthetic/non-entity column whose type isn't known.
     */
    protected String applyExpressionToColumn(final FilterExpression filterExpression, final String quotedColumn, final @Nullable ColumnTypeHint columnTypeHint, final Consumer<Prepper> prepperConsumer) throws MalformedExpression {
        return applyExpressionToColumn(filterExpression, quotedColumn, columnTypeHint, null, prepperConsumer);
    }

    /**
     * Like {@link #applyExpressionToColumn(FilterExpression, String, ColumnTypeHint, Consumer)}, but also takes the
     * letter case of the column's values into account (see {@link ColumnCase}); pass {@code null} if unknown.
     * <p>
     * Text matching ({@code STARTSWITH}, {@code ENDSWITH}, {@code CONTAINS}, {@code DOESNOTCONTAIN}) ignores case
     * unless {@link FilterExpression#getIgnoreCase()} is explicitly {@code false}. Comparing a text value with
     * {@code EQ}, {@code NEQ}, {@code LT} etc. is exact unless {@code ignoreCase} is explicitly {@code true}.
     * If the column is known to be uniformly upper or lower case, a case-insensitive comparison is done by
     * normalizing the search value here, instead of folding the column in the database.
     * </p>
     */
    protected String applyExpressionToColumn(final FilterExpression original, final String quotedColumn, final @Nullable ColumnTypeHint columnTypeHint, final @Nullable ColumnCase columnCase, final Consumer<Prepper> prepperConsumer) throws MalformedExpression {
        @Nullable final FilterOperator op = original.getOperator();
        final DatabaseDialect dialect = getDialect();
        final boolean textMatch = switch (op) {
            case STARTSWITH, ENDSWITH, CONTAINS, DOESNOTCONTAIN -> true;
            default -> false;
        };
        final boolean ignoreCase;
        if (textMatch) {
            ignoreCase = !Boolean.FALSE.equals(original.getIgnoreCase()); // text searches ignore case unless asked otherwise
        } else {
            ignoreCase = original.isString() && Boolean.TRUE.equals(original.getIgnoreCase()); // comparisons only on request
        }
        // For uniformly cased columns, adjust the value to the column and leave the column itself alone.
        final boolean normalizeValue = ignoreCase && original.isString() && ColumnCase.isUniform(columnCase);
        final boolean foldColumn = ignoreCase && !normalizeValue;
        final FilterExpression filterExpression;
        if (normalizeValue) {
            filterExpression = original.deepCopy();
            filterExpression.setValueString(columnCase.normalize(original.getValueString()));
        } else {
            filterExpression = original;
        }
        final Prepper simpleEquality = getPrepper(filterExpression, columnTypeHint);
        final String left = foldColumn ? dialect.foldCase(quotedColumn) : quotedColumn;
        final String right = foldColumn ? dialect.foldCase("?") : "?";
        return switch (op) {
            case EQ -> {
                prepperConsumer.accept(simpleEquality);
                yield left + " = " + right;
            }
            case NEQ -> {
                prepperConsumer.accept(simpleEquality);
                yield left + " != " + right;
            }
            case LT -> {
                prepperConsumer.accept(simpleEquality);
                yield left + " < " + right;
            }
            case LTE -> {
                prepperConsumer.accept(simpleEquality);
                yield left + " <= " + right;
            }
            case GT -> {
                prepperConsumer.accept(simpleEquality);
                yield left + " > " + right;
            }
            case GTE -> {
                prepperConsumer.accept(simpleEquality);
                yield left + " >= " + right;
            }
            case STARTSWITH -> {
                prepperConsumer.accept(PREP_STARTS_WITH_STRING.getPrepper(filterExpression));
                yield dialect.likeCondition(quotedColumn, false, foldColumn);
            }
            case ENDSWITH -> {
                prepperConsumer.accept(PREP_ENDS_WITH_STRING.getPrepper(filterExpression));
                yield dialect.likeCondition(quotedColumn, false, foldColumn);
            }
            case CONTAINS -> {
                prepperConsumer.accept(PREP_CONTAINS_STRING.getPrepper(filterExpression));
                yield dialect.likeCondition(quotedColumn, false, foldColumn);
            }
            case DOESNOTCONTAIN -> {
                prepperConsumer.accept(PREP_CONTAINS_STRING.getPrepper(filterExpression));
                yield dialect.likeCondition(quotedColumn, true, foldColumn);
            }
            case ISNULL -> quotedColumn + " is null";
            case ISNOTNULL -> quotedColumn + " is not null";
            case ISEMPTY -> dialect.isEmptyCondition(quotedColumn);
            case ISNOTEMPTY -> dialect.isNotEmptyCondition(quotedColumn);
        };

    }

    @Override
    public int countByFilter(@NotNull final DatabaseConnection conn, @Nullable final FilterExpression filterExpression) throws DatabaseError {
                final List<Prepper> preppers = new LinkedList<>();
        @Language("SQL") final String sql = where(countAllSql, filterExpression == null ? null : expressionToSql(filterExpression, preppers::add));
        if (verbose) {
            log.info("countByFilter, sql: {}, preppers: {}", sql, preppers);
        }
        return conn.queryForInt(
                new SelectStatement(sql),
                stmt -> {
                    for (final Prepper prepper : preppers) {
                        prepper.prepare(conn, stmt);
                    }
                });
    }


    @Override
    public @NotNull List<T> findByAssociation(final @NotNull DatabaseConnection conn, final String foreignKeyColumn, final long foreignKeyValue) throws DatabaseError {
        @Language("SQL") final String sql = where(selectAllSql, getDialect().quoteColumn(foreignKeyColumn) + " = ?");
        if (verbose) {
            log.info("findByAssociation, sql: {}, fk = {}", sql, foreignKeyValue);
        }
        return conn.queryForList(new SelectStatement(sql), stmt -> stmt.addLong(foreignKeyValue), rs -> readAllProperties(conn, rs));
    }


    // TODO: what I actually want to have is a class that contains the SQL and all information to fully prepare the stmt.
    //   The whole query side, really. A calles can then look at them and e.g. report on them when needed.
    /** A query for a filter, together with its row limit and offset and the preppers that bind its parameters. */
    public static class LimitedSelectStatement extends SelectStatement {

        protected final @Nullable Integer rowLimit;
        protected final int offset;
        protected final boolean limitedInSql;
        @NotNull @Unmodifiable protected final List<Prepper> preppers;

        /** A query whose SQL limits the rows if there is a row limit. */
        public LimitedSelectStatement(final @NotNull SelectStatement wrapped, final @Nullable Integer rowLimit, final List<Prepper> preppers) {
            this(wrapped.getSql(), rowLimit, 0, rowLimit != null, preppers);
        }

        /** A query whose SQL limits the rows if there is a row limit. */
        public LimitedSelectStatement(@NotNull @Language("SQL") final String sql, final @Nullable Integer rowLimit, final List<Prepper> preppers) {
            this(sql, rowLimit, 0, rowLimit != null, preppers);
        }

        /**
         * @param sql          the query
         * @param rowLimit     the maximum number of rows, or null for no limit
         * @param offset       the number of rows to skip
         * @param limitedInSql true if the SQL contains the placeholders for limit and offset, false if the limit and offset must be applied while reading
         * @param preppers     the preppers that bind the parameters of the query, in order
         */
        public LimitedSelectStatement(@NotNull @Language("SQL") final String sql, final @Nullable Integer rowLimit, final int offset, final boolean limitedInSql, final List<Prepper> preppers) {
            super(sql);
            this.rowLimit = rowLimit;
            this.offset = offset;
            this.limitedInSql = limitedInSql;
            this.preppers = List.copyOf(preppers);
        }

        /**
         * @return the maximum number of rows, or null if the query is not limited
         */
        public @Nullable Integer getRowLimit() {
            return rowLimit;
        }

        /**
         * @return the number of rows to skip
         */
        public int getOffset() {
            return offset;
        }

        /**
         * @return true if the query has a row limit
         */
        public boolean isLimited() {
            return rowLimit != null;
        }

        /**
         * @return true if the limit and offset are part of the SQL, and have to be bound as parameters after the preppers; false if they have to be
         * applied while reading, because the database cannot limit rows in SQL
         */
        public boolean isLimitedInSql() {
            return limitedInSql;
        }

        /**
         * @return the preppers that bind the parameters of the query, in order
         */
        public @NotNull @Unmodifiable List<Prepper> getPreppers() {
            return preppers;
        }
    }

    /**
     * Creates the sort order of a filter query: the raw clause if there is one, or the {@link FilterQuery#orderBy(String) sort keys} mapped to
     * columns, or the default. A paged query (with an offset) gets the ID column as the last criterion, unless it is already sorted by it,
     * so that pages cannot overlap.
     * @throws MalformedExpression if a sort key names an unknown field, or both a raw clause and sort keys are set
     */
    protected @NotNull String orderByFor(final @NotNull FilterQuery filterQuery) {
        final boolean paged = filterQuery.getOffset() != null;
        final @Nullable String raw = filterQuery.getOrderByClause();
        final List<SortKey> keys = filterQuery.getSortKeys();
        if (raw != null) {
            if (!keys.isEmpty()) {
                throw new MalformedExpression("use either an order by clause or sort keys, not both");
            }
            return raw;
        }
        if (keys.isEmpty()) {
            return defaultOrderBy(paged);
        }
        final DatabaseDialect dialect = getDialect();
        final List<String> parts = new ArrayList<>();
        boolean sortsById = false;
        for (final SortKey key : keys) {
            final String column = columnForField.get(key.field());
            if (column == null) {
                throw new MalformedExpression("cannot sort by unknown field '" + key.field() + "'");
            }
            sortsById |= column.equals(idColumn);
            parts.add(dialect.quoteColumn(column) + (key.descending() ? " desc" : " asc"));
        }
        if (paged && !sortsById) {
            parts.add(dialect.quoteColumn(idColumn) + " asc");
        }
        return "order by " + String.join(", ", parts);
    }

    /**
     * Creates the query for finding entities by a filter, including the ordering, the row limit and the offset.
     * @param filterQuery the filter query
     * @return the query
     */
    protected LimitedSelectStatement createFindByFilterQuery(@NotNull final FilterQuery filterQuery) {
        final List<Prepper> preppers = new LinkedList<>();
        final @Nullable Integer limit = filterQuery.getMaxRows();
        final boolean limited = limit != null && limit > 0;
        final int offset = checkOffset(filterQuery.getOffset(), limited);
        final @NotNull String finalOrderBy = orderByFor(filterQuery);
        @Language("SQL") final String sql = where(selectAllSql, expressionToSql(filterQuery.filterExpression, preppers::add));
        if (limited && collection.canLimitAndOffset()) {
            SelectStatement limitedSelect = filterQuery.rewriteQuery(getDialect().addLimitAndOffset(SelectStatement.of(sql + " " + finalOrderBy)));
            return new LimitedSelectStatement(limitedSelect.getSql(), limit, offset, true, preppers);
        } else {
            return new LimitedSelectStatement(filterQuery.rewriteQuery(sql + " " + finalOrderBy), limited ? limit : null, offset, false, preppers);
        }
    }

    @Override
    public @NotNull @Unmodifiable List<T> findByFilter(@NotNull final DatabaseConnection conn, @NotNull final FilterQuery filterQuery) throws DatabaseError {
        final LimitedSelectStatement q = createFindByFilterQuery(filterQuery);
        final StatementConfigurator prepare = stmt -> {
            if (filterQuery.getQueryTimeout() != null) {
                stmt.setQueryTimeout(filterQuery.getQueryTimeout());
            }
            for (final Prepper prepper : q.getPreppers()) {
                prepper.prepare(conn, stmt);
            }
            if (q.isLimitedInSql() && q.getRowLimit() != null) {
                getDialect().addLimitAndOffset(stmt, q.getRowLimit(), q.getOffset());
            }
        };
        if (q.isLimited() && !q.isLimitedInSql()) {
            return readLimitedWhileReading(conn, q, prepare, q.getRowLimit(), q.getOffset());
        }
        return conn.queryForList(q, prepare, rs -> readAllProperties(conn, rs));
    }

    /**
     * Reads all selected columns of the current row into a new entity.
     * @param conn the database connection
     * @param rs the result set, positioned at the row to read
     * @return the new entity
     * @throws DatabaseError if a column cannot be read
     */
    protected T readAllProperties(final DatabaseConnection conn, final SimpleResultSet rs) throws DatabaseError {
        final T value = newValue.get();
        for (int i = 0; i < selectMappings.length; i++) {
            final ColumnMapping<T> mapping = selectMappings[i];
            try {
                final JanitorObject propertyValue = Objects.requireNonNull(mapping.handler().lookupAttribute(value));
                if (propertyValue instanceof JAssignable assignableProperty) {
                    CommonDao.readColumn(collection, conn, mapping, assignableProperty, rs);
                } else {
                    throw new DatabaseError("invalid field '" + mapping.field() + "' / column '" + mapping.column() + "' is not assignable");
                }
            } catch (SQLException e) {
                log.warn("SQL exception on class '{}', column #{} = '{}', field '{}', type hint '{}', column order: {}", className, i + 1, mapping.column(), mapping.field(), mapping.type(), selectColumns, e);
                final String message = String.format("SQL exception on class '%s', column '%s', field '%s', type hint '%s'", className, mapping.column(), mapping.field(), mapping.type());
                throw new DatabaseError(message, e);
            } catch (Exception e) {
                throw new DatabaseError("invalid field '" + mapping.field() + "' caused an exception", e);
            }
        }
        return value;
    }


    @Override
    public void insert(@NotNull DatabaseConnection conn, @NotNull T record) throws DatabaseError {
        final List<String> insertingColumns = columns;
        final String sequence = Objects.requireNonNull(entityDispatchTable.getMetaData(JanitorOrm.MetaData.ID_SEQUENCE));
        final SelectStatement nextIdQuery = Objects.requireNonNull(getDialect().getNextValueQuery(sequence));
        final long generatedId = conn.queryForLong(nextIdQuery);
        record.setId(generatedId);
        if (verbose) {
            log.info("{}::insert() with new id {}, running {} on columns {}", className, generatedId, insertStatement, insertingColumns);
        }
        touchModifiedDate(record);
        record.beforeInsert();
        conn.update(insertStatement, ps -> {
            // pointless / harmful: ps.addLong(generatedId);
            writeAllColumns(conn, record, insertingColumns, ps);
        });
        entityChangeListeners.fire(listener -> listener.onChange(EntityChangeListener.Type.INSERT, record));
    }

    @Override
    public void update(@NotNull DatabaseConnection conn, @NotNull T record) throws DatabaseError {
        refuseNullRow(record, "update");
        final List<String> updatingColumns = updateColumns;
        touchModifiedDate(record);
        record.beforeUpdate();
        if (versionedUpdateStatement != null && record instanceof Versioned versioned) {
            // optimistic locking: write only if the row still has the version that this entity was loaded with, and count up
            final long expected = versioned.getVersion();
            versioned.setVersion(expected + 1);
            final int rows = conn.update(versionedUpdateStatement, ps -> {
                writeAllColumns(conn, record, updatingColumns, ps);
                ps.addLong(record.getId());
                ps.addLong(expected);
            });
            if (rows == 0) {
                versioned.setVersion(expected);
                throw new OptimisticLockError(className, record.getId(), expected);
            }
        } else {
            conn.update(updateStatement, ps -> {
                writeAllColumns(conn, record, updatingColumns, ps);
                ps.addLong(record.getId());
            });
        }
        entityChangeListeners.fire(listener -> listener.onChange(EntityChangeListener.Type.UPDATE, record));
    }

    /**
     * Sets the modification date of the record to now, if the entity declares one (see {@link JanitorOrm.MetaData#MODIFIED_FIELD}) and
     * implements {@link HasModifiedDate}. Runs before the {@code before...} hooks of the entity, so that they can still override the value.
     */
    private void touchModifiedDate(final @NotNull T record) {
        if (hasModifiedColumn && record instanceof HasModifiedDate modifiable) {
            modifiable.setModifiedDate(LocalDateTime.now(collection.getClock()));
        }
    }

    @Override
    public void delete(@NotNull final DatabaseConnection conn, @NotNull final T record) throws DatabaseError {
        refuseNullRow(record, "delete");
        if (versionedDeleteStatement != null && record instanceof Versioned versioned) {
            final long expected = versioned.getVersion();
            final int rows = conn.update(versionedDeleteStatement, ps -> {
                ps.addLong(record.getId());
                ps.addLong(expected);
            });
            if (rows == 0) {
                throw new OptimisticLockError(className, record.getId(), expected);
            }
        } else {
            conn.update(deleteStatement, ps -> ps.addLong(record.getId()));
        }
        entityChangeListeners.fire(listener -> listener.onChange(EntityChangeListener.Type.DELETE, record));
    }

    /**
     * Binds the values of the given columns of an entity to the next parameters of a prepared statement.
     * @param conn the database connection
     * @param record the entity
     * @param updatingColumns the columns to write, in the order of the parameters
     * @param ps the statement to bind the values to
     * @throws SQLException if a value cannot be written
     */
    protected void writeAllColumns(final DatabaseConnection conn, final T record, final List<String> updatingColumns, final SimplePreparedStatement ps) throws SQLException {
        if (verbose) {
            log.info("writeAllColumns({})", updatingColumns);
        }
        for (final String column : updatingColumns) {
            final ColumnMapping<T> mapping = Objects.requireNonNull(mappingForColumn.get(column), "no mapping for column '" + column + "'");
            try {
                final JanitorObject propertyValue = Objects.requireNonNull(mapping.handler().lookupAttribute(record), "no value for field '" + mapping.field() + "' in record " + record + " / column '" + column + "'");
                CommonDao.writeColumn(conn, className, mapping, propertyValue, ps);
            } catch (Exception e) {
                throw new SQLException("error writing column '" + column + "' / field '" + mapping.field() + "' into the database", e);
            }
        }
    }

    /**
     * Throws an exception that tells that a function is not supported by this DAO.
     * @param what a description of the function
     * @throws DatabaseError always
     */
    @SuppressWarnings("unused") // it's used, the IDE just can't see it.
    protected void unsupported(final String what) throws DatabaseError {
        throw new DatabaseError("Unsupported function for " + getClass().getSimpleName() + ": " + what);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    /**
     * Returns all columns that are mapped to properties, quoted for the database.
     * @param qualifyWithTableName true to prefix each column with the table name
     * @return the column names
     */
    protected @NotNull @Unmodifiable List<String> getAllMappedColumns(boolean qualifyWithTableName) {
        final var dialect = getDialect();
        if (qualifyWithTableName) {
            return columns.stream().map(col -> tableName + "." + dialect.quoteColumn(col)).collect(Collectors.toList());
        } else {
            return columns.stream().map(dialect::quoteColumn).toList();
        }
    }

    @Override
    public void setLogging(final DaoLogging logging) {
        this.logging = logging;
    }

    private final java.util.concurrent.atomic.LongAdder lazyLoadHits = new java.util.concurrent.atomic.LongAdder();
    private final java.util.concurrent.atomic.LongAdder lazyLoadDbLoads = new java.util.concurrent.atomic.LongAdder();

    /** Counts a reference that was resolved from a cache; for subclasses with a cache of their own that answer {@link #lazyLoadById} themselves. */
    protected final void countLazyLoadHit() {
        lazyLoadHits.increment();
    }

    /** How many references to this entity were resolved from a cache, without opening a transaction. */
    public long getLazyLoadHits() {
        return lazyLoadHits.sum();
    }

    /** How many references to this entity needed a transaction (and usually a query) to be resolved. */
    public long getLazyLoadDbLoads() {
        return lazyLoadDbLoads.sum();
    }

    @Override
    public @Nullable T lazyLoadById(final long id) {
        // A cache hit must not cost a connection: ask the cache before opening a transaction. A miss is counted by findById.
        final T cached = cache.findById(id);
        if (cached != null) {
            countLazyLoadHit();
            metrics().cacheHit();
            return cached;
        }
        lazyLoadDbLoads.increment();
        try {
            final @Nullable T result = getDataManager().callTransaction(conn -> findById(conn, id));
            if (logging != null) {
                logging.lazyLoadedForeignKey(className, id, result);
            }
            return result;
        } catch (DatabaseError e) {
            throw new JanitorError("failed to load " + className + " by id=" + id, e);
        }
    }

    @Override
    public @Nullable T lazyLoadByKey(final String key) {
        // A cache hit must not cost a connection: ask the cache before opening a transaction. A miss is counted by findByKey.
        final T cached = cache.findByKey(key);
        if (cached != null) {
            countLazyLoadHit();
            metrics().cacheHit();
            return cached;
        }
        lazyLoadDbLoads.increment();
        try {
            final @Nullable T result = getDataManager().callTransaction(conn -> findByKey(conn, key));
            if (logging != null) {
                logging.lazyLoadedForeignKey(className, key, result);
            }
            return result;
        } catch (DatabaseError e) {
            throw new JanitorError("failed to load " + className + " by key='" + key + "'", e);
        }
    }

    @Override
    public @NotNull @Unmodifiable List<T> lazyLoadByAssociation(final String foreignKeyColumn, final OrmEntity parentEntity) {
        try {
            @NotNull final List<T> results = getDataManager().callTransaction(conn -> findByAssociation(conn, foreignKeyColumn, parentEntity.getId()));
            if (logging != null) {
                logging.lazyLoadedAssociation(className, foreignKeyColumn, parentEntity.getId(), results);
            }
            return results;
        } catch (DatabaseError e) {
            throw new JanitorError("lazyLoadByAssociation, failed to load lazy loaded entities referring to " + parentEntity + " via " + foreignKeyColumn, e);
        }
    }

    /**
     * Runs a function in a transaction on behalf of a script, turning database errors into script errors.
     * @param process the running script process
     * @param function the function to run
     * @param <X> the type of the result
     * @return the result of the function
     * @throws JanitorRuntimeException if the function fails
     */
    protected <X> X callScriptTransaction(final JanitorScriptProcess process, final DatabaseFunction<DatabaseConnection, X> function) throws JanitorRuntimeException {
        try {
            return getDataManager().callTransaction(function);
        } catch (DatabaseError e) {
            throw new JanitorNativeException(process, e.getMessage(), e);
        }
    }

    private T insertForScript(final @NotNull JanitorScriptProcess process, final @NotNull JanitorObject janitorObject) throws JanitorRuntimeException {
        for (final JanitorObject object : janitorObject.janitorUnpackAll()) {
            if (object instanceof JMap janitorMap) {
                final T instance = entityDispatchTable.getConstructor().call(process, JCallArgs.empty("constructor", process));
                janitorMap.applyTo(process, instance);
                try {
                    getDataManager().executeTransaction(conn -> insert(conn, instance));
                    return instance;
                } catch (DatabaseError e) {
                    throw new JanitorNativeException(process, e.getMessage(), e);
                }
            }
            if (entityClass.isInstance(object)) {
                final T instance = entityClass.cast(object);
                try {
                    getDataManager().executeTransaction(conn -> insert(conn, instance));
                    return instance;
                } catch (DatabaseError e) {
                    throw new JanitorNativeException(process, e.getMessage(), e);
                }
            }
        }
        throw new JanitorArgumentException(process, "invalid argument " + janitorObject + " [" + simpleClassNameOf(janitorObject) + "], expecting map or " + entityClass.getSimpleName());
    }

    private T updateForScript(final @NotNull JanitorScriptProcess process, final @NotNull JanitorObject janitorObject) throws JanitorRuntimeException {
        for (final JanitorObject object : janitorObject.janitorUnpackAll()) {
            if (object instanceof JMap janitorMap) {
                final T instance = entityDispatchTable.getConstructor().call(process, JCallArgs.empty("constructor", process));
                janitorMap.applyTo(process, instance);
                try {
                    getDataManager().executeTransaction(conn -> update(conn, instance));
                    return instance;
                } catch (DatabaseError e) {
                    throw new JanitorNativeException(process, e.getMessage(), e);
                }
            }
            if (entityClass.isInstance(object)) {
                final T instance = entityClass.cast(object);
                try {
                    getDataManager().executeTransaction(conn -> update(conn, instance));
                    return instance;
                } catch (DatabaseError e) {
                    throw new JanitorNativeException(process, e.getMessage(), e);
                }
            }
        }
        throw new JanitorArgumentException(process, "invalid argument " + janitorObject + " [" + simpleClassNameOf(janitorObject) + "], expecting map or " + entityClass.getSimpleName());
    }

    /**
     * Script method {@code dao.findById(id)}: finds an entity by its ID.
     * @param process the running script process
     * @param arguments the call arguments
     * @return the entity, or null if there is none
     * @throws JanitorRuntimeException if the arguments are invalid or the query fails
     */
    public JanitorObject scriptFindById(final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        try {
            final T single = getDataManager().callTransaction(conn -> {
                try {
                    return findById(conn, arguments.getRequiredLongValue(0));
                } catch (JanitorArgumentException e) {
                    throw new DatabaseError(e.getMessage(), e);
                }
            });
            return scriptResult(single);
        } catch (DatabaseError e) {
            throw new JanitorNativeException(process, e.getMessage(), e);
        }
    }

    /**
     * Script method {@code dao.findByIds(ids)}: finds the entities for a list (or set) of IDs with few queries.
     * IDs without an entity, duplicates and nulls are skipped; the entities come in the order of their IDs.
     * @param process the running script process
     * @param arguments the call arguments
     * @return a list of the entities
     * @throws JanitorRuntimeException if the argument is not a collection of numbers or the query fails
     */
    public JanitorObject scriptFindByIds(final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        final JIterable source = arguments.require(1).getRequired(0, JIterable.class);
        final List<Long> ids = new ArrayList<>();
        for (final Iterator<? extends JanitorObject> it = source.getIterator(); it.hasNext(); ) {
            final JanitorObject element = it.next();
            if (element instanceof JNumber number) {
                ids.add(number.toLong());
            } else if (element != Janitor.NULL) {
                throw new JanitorArgumentException(process, "findByIds: expecting a collection of numbers, but found " + simpleClassNameOf(element));
            }
        }
        return callScriptTransaction(process, conn -> Janitor.list(findByIds(conn, ids).stream().map(this::forScript)));
    }

    /**
     * Script method {@code dao.findByKey(key)}: finds an entity by its key.
     * @param process the running script process
     * @param arguments the call arguments
     * @return the entity, or null if there is none
     * @throws JanitorRuntimeException if the arguments are invalid or the query fails
     */
    public JanitorObject scriptFindByKey(final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        try {
            final T single = getDataManager().callTransaction(conn -> {
                try {
                    return findByKey(conn, arguments.getRequiredStringValue(0));
                } catch (JanitorRuntimeException e) {
                    throw new DatabaseError(e.getMessage(), e);
                }
            });
            return scriptResult(single);
        } catch (DatabaseError e) {
            throw new JanitorNativeException(process, e.getMessage(), e);
        }
    }

    /**
     * Script method {@code dao.findAll()}: finds all entities.
     * @param process the running script process
     * @param arguments the call arguments
     * @return a list of the entities
     * @throws JanitorRuntimeException if the query fails
     */
    public JanitorObject scriptFindAll(final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        return callScriptTransaction(process, conn -> Janitor.list(findAll(conn, null).stream().map(this::forScript)));
    }

    /**
     * Script method {@code dao.queryForEach(sql, callback)}: runs a query that returns IDs, and calls the callback with each entity and its ID.
     * @param process the running script process
     * @param arguments the call arguments
     * @return the number of entities that the callback was called for
     * @throws JanitorRuntimeException if the arguments are invalid or the query fails
     */
    public JanitorObject scriptQueryForEach(final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        try {
            @Language("SQL") final String sql = arguments.require(2).getString(0).janitorGetHostValue();
            final JCallable callback = (JCallable) arguments.get(1);
            final List<Long> identifiers = getDataManager().callTransaction(conn -> conn.queryForList(new SelectStatement(sql), SimpleResultSet::getLong));
            long count = 0;
            // Load the entities chunk-wise: one transaction (one connection checkout and commit) and one query per chunk instead of per entity. The callbacks
            // run outside of these transactions, as they always did, so whatever a script writes from within its callback is committed on its own,
            // and entities that a callback changes are seen as changed by the chunks that are loaded after it.
            for (int from = 0; from < identifiers.size(); from += QUERY_FOR_EACH_CHUNK_SIZE) {
                final List<Long> chunk = identifiers.subList(from, Math.min(from + QUERY_FOR_EACH_CHUNK_SIZE, identifiers.size()));
                final Map<Long, T> loaded = new HashMap<>();
                for (final T entity : getDataManager().callTransaction(conn -> findByIds(conn, chunk))) {
                    loaded.put(entity.getId(), entity);
                }
                for (final Long identifier : chunk) {
                    final T obj = identifier == null ? null : loaded.get(identifier);
                    if (obj != null) {
                        ++count;
                        callback.call(process, new JCallArgs("callback", process, List.of(forScript(obj), Janitor.nullableInteger(identifier))));
                    } else if (identifier == null || !isNullRowId(identifier)) {
                        log.warn("queryForEach: object not found for identifier {}", identifier);
                    }
                }
            }
            return Janitor.integer(count);
        } catch (DatabaseError e) {
            throw new JanitorNativeException(process, e.getMessage(), e);
        }
    }

    /**
     * Equality check.
     * Note that this is based on the table name by default, which should be reasonable for many use cases.
     * @param o   the reference object with which to compare.
     * @return true if the reference object is equal to the argument object or
     */
    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof final GenericDao<?, ?> that)) return false;
        return Objects.equals(tableName, that.tableName);
    }

    /**
     * Hash code generation.
     * Note that this is based on the table name by default, which should be reasonable for most use cases.
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(tableName);
    }

    @Override
    public ListenerRegistration addChangeListener(final EntityChangeListener<T> listener) {
        return entityChangeListeners.add(listener);
    }

    @Override
    public DispatchTable<T> getEntityDispatchTable() {
        return entityDispatchTable;
    }
}
