// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.dao;


import com.eischet.dbxs.*;
import com.eischet.dbxs.dialects.DatabaseDialect;
import com.eischet.dbxs.exceptions.DatabaseError;
import com.eischet.dbxs.metadata.DatabaseVersion;
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
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JMap;
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
import com.eischet.janitor.orm.filter.FilterExpression;
import com.eischet.janitor.orm.entity.OrmEntity;
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
import java.util.*;
import java.util.function.Consumer;
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

    // TOOD: cache the database version after first retrieving it

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
        DISPATCH.addMethod("queryForEach", GenericDao::scriptQueryForEach);

        DISPATCH.addStringProperty("jsonSchema", self -> Janitor.current().writeJson(self.entityDispatchTable::writeSchemaToJson));
    }

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

        collection.registerDao(this);
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
            return cached;
        }
        return findByKeyIgnoringCache(conn, key);
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
        final StatementCreator creator = new StatementCreator(getDataManager().getDialect());
        final SelectStatement select = SelectStatement.of(creator.createSelectStatement(tableName, selectColumns, keyColumn));
        if (verbose) {
            log.info("{}::findByKey(key='{}'): running {}", className, key, select);
        }
        final T loaded = conn.queryForObject(select, stmt -> stmt.addString(key), rs -> readAllProperties(conn, rs));
        cache.put(loaded);
        return loaded;
    }

    @Override
    public @Nullable T findById(final @NotNull DatabaseConnection conn, final long id) throws DatabaseError {
        final T cached = cache.findById(id);
        if (cached != null) {
            return cached;
        }
        return findByIdIgnoringCache(conn, id);
    }

    /**
     * Like {@link #findById}, but always queries the database, bypassing any cache attached via
     * {@link #enableCache}. The result is still stored into the cache (if any), same as a normal cache
     * miss would.
     */
    public @Nullable T findByIdIgnoringCache(final @NotNull DatabaseConnection conn, final long id) throws DatabaseError {
        final StatementCreator creator = new StatementCreator(getDataManager().getDialect());
        final SelectStatement select = SelectStatement.of(creator.createSelectStatement(tableName, selectColumns, idColumn));
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
        final StatementCreator creator = new StatementCreator(getDataManager().getDialect());
        final SelectStatement select = SelectStatement.of(creator.createSelectStatement(tableName, List.of(column), idColumn));
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
        final StatementCreator creator = new StatementCreator(getDataManager().getDialect());
        final SelectStatement select = SelectStatement.of(creator.createSelectAllStatement(tableName, selectColumns));
        if (verbose) {
            log.info("{}::findAll(): running {}, limit={}", className, select, limit);
        }
        @NotNull final DatabaseVersion databaseVersion = DatabaseVersion.getDatabaseVersion(getDataManager());
        if (limit != null && limit > 0 && getDataManager().getDialect().canLimitAndOffset(databaseVersion)) {
            final SelectStatement limited = getDataManager().getDialect().addLimitAndOffset(SelectStatement.of(select.getSql() + " order by 2"));
            return conn.queryForList(
                    limited,
                    ps -> getDataManager().getDialect().addLimitAndOffset(ps, limit, 0),
                    rs -> readAllProperties(conn, rs)
            );
        } else {
            return conn.queryForList(select, rs -> readAllProperties(conn, rs));
        }
    }

    @Override
    public @NotNull @Unmodifiable List<T> findByQuery(@NotNull final DatabaseConnection conn, @NotNull final String query, @NotNull final StatementConfigurator statementConfigurator) throws DatabaseError {
        final SelectStatement select = SelectStatement.of(query);
        return conn.queryForList(select, statementConfigurator, rs -> readAllProperties(conn, rs));

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
        @NotNull final DatabaseDialect dialect = getDataManager().getDialect();
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
        final DatabaseDialect dialect = getDataManager().getDialect();
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
        final StatementCreator creator = new StatementCreator(getDataManager().getDialect());
        final List<Prepper> preppers = new LinkedList<>();
        @Language("SQL") final String sql =
                filterExpression == null ? creator.createCountStatement(tableName) :
                        creator.createCountStatement(tableName) + "\nWHERE\n  " + expressionToSql(filterExpression, preppers::add);
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
        final StatementCreator creator = new StatementCreator(getDataManager().getDialect());
        @Language("SQL") final String sql = creator.createSelectAllStatement(tableName, selectColumns) + "\nWHERE\n  " + getDataManager().getDialect().quoteColumn(foreignKeyColumn) + " = ?";
        if (verbose) {
            log.info("findByAssociation, sql: {}, fk = {}", sql, foreignKeyValue);
        }
        return conn.queryForList(new SelectStatement(sql), stmt -> stmt.addLong(foreignKeyValue), rs -> readAllProperties(conn, rs));
    }


    // TODO: what I actually want to have is a class that contains the SQL and all information to fully prepare the stmt.
    //   The whole query side, really. A calles can then look at them and e.g. report on them when needed.
    /** A query for a filter, together with its row limit and the preppers that bind its parameters. */
    public static class LimitedSelectStatement extends SelectStatement {

        protected final @Nullable Integer rowLimit;
        @NotNull @Unmodifiable protected final List<Prepper> preppers;

        public LimitedSelectStatement(final @NotNull SelectStatement wrapped, final @Nullable Integer rowLimit, final List<Prepper> preppers) {
            super(wrapped.getSql());
            this.rowLimit = rowLimit;
            this.preppers = List.copyOf(preppers);
        }

        public LimitedSelectStatement(@NotNull @Language("SQL") final String sql, final @Nullable Integer rowLimit, final List<Prepper> preppers) {
            super(sql);
            this.rowLimit = rowLimit;
            this.preppers = List.copyOf(preppers);
        }

        /**
         * @return the maximum number of rows, or null if the query is not limited
         */
        public @Nullable Integer getRowLimit() {
            return rowLimit;
        }

        /**
         * @return true if the query has a row limit
         */
        public boolean isLimited() {
            return rowLimit != null;
        }

        /**
         * @return the preppers that bind the parameters of the query, in order
         */
        public @NotNull @Unmodifiable List<Prepper> getPreppers() {
            return preppers;
        }
    }

    /**
     * Creates the query for finding entities by a filter, including the ordering and the row limit.
     * @param filterQuery the filter query
     * @return the query
     */
    protected LimitedSelectStatement createFindByFilterQuery(@NotNull final FilterQuery filterQuery) {
        final List<Prepper> preppers = new LinkedList<>();
        final @Nullable String orderBy = filterQuery.getOrderByClause();
        final @Nullable Integer limit = filterQuery.getMaxRows();
        final @NotNull String finalOrderBy = orderBy == null ? "order by 2" : orderBy;
        final StatementCreator creator = new StatementCreator(getDataManager().getDialect());
        @Language("SQL") final String sql = creator.createSelectAllStatement(tableName, selectColumns) + "\nWHERE\n  " + expressionToSql(filterQuery.filterExpression, preppers::add);
        @NotNull final DatabaseVersion databaseVersion = DatabaseVersion.getDatabaseVersion(getDataManager());
        if (limit != null && limit > 0 && getDataManager().getDialect().canLimitAndOffset(databaseVersion)) {
            SelectStatement limited = filterQuery.rewriteQuery(getDataManager().getDialect().addLimitAndOffset(SelectStatement.of(sql + " " + finalOrderBy)));
            return new LimitedSelectStatement(limited, limit, preppers);
        } else {
            return new LimitedSelectStatement(filterQuery.rewriteQuery(sql + " " + finalOrderBy), null, preppers);
        }
    }

    @Override
    public @NotNull @Unmodifiable List<T> findByFilter(@NotNull final DatabaseConnection conn, @NotNull final FilterQuery filterQuery) throws DatabaseError {
        final LimitedSelectStatement q = createFindByFilterQuery(filterQuery);
        return conn.queryForList(
            q,
            stmt -> {
                if (filterQuery.getQueryTimeout() != null) {
                    stmt.setQueryTimeout(filterQuery.getQueryTimeout());
                }
                for (final Prepper prepper : q.getPreppers()) {
                    prepper.prepare(conn, stmt);
                }
                if (q.getRowLimit() != null) {
                    getDataManager().getDialect().addLimitAndOffset(stmt, q.getRowLimit(), 0);
                }
            },
            rs -> readAllProperties(conn, rs));
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
        int columnIndex = 0;
        for (final String column : selectColumns) {
            ++columnIndex;
            String field = Objects.requireNonNull(fieldForColumn.get(column));
            final @NotNull ColumnTypeHint columnTypeHint = Objects.requireNonNull(entityDispatchTable.getMetaData(field, JanitorOrm.MetaData.COLUMN_TYPE));
            final @Nullable String lookupType = entityDispatchTable.getMetaData(field, Janitor.MetaData.REF);
            final @Nullable Boolean hostNullable = entityDispatchTable.getMetaData(field, Janitor.MetaData.HOST_NULLABLE);
            try {
                final JanitorObject propertyValue = Objects.requireNonNull(entityDispatchTable.get(field).lookupAttribute(value));
                if (propertyValue instanceof JAssignable assignableProperty) {
                    CommonDao.readProperty(collection, column, conn, assignableProperty, rs, columnTypeHint, lookupType, hostNullable);
                } else {
                    throw new DatabaseError("invalid field '" + field + "' / column '" + column + "' is not assignable");
                }
            } catch (SQLException e) {
                log.warn("SQL exception on class '{}', column #{} = '{}', field '{}', type hint '{}', column order: {}", className, columnIndex, column, field, columnTypeHint, selectColumns, e);
                final String message = String.format("SQL exception on class '%s', column '%s', field '%s', type hint '%s'", className, column, field, columnTypeHint);
                throw new DatabaseError(message, e);
            } catch (Exception e) {
                throw new DatabaseError("invalid field '" + field + "' caused an exception", e);
            }
        }
        return value;
    }


    @Override
    public void insert(@NotNull DatabaseConnection conn, @NotNull T record) throws DatabaseError {
        final StatementCreator creator = new StatementCreator(getDataManager().getDialect());
        final List<String> insertingColumns = columns.stream().toList();
        final UpdateStatement insertStatement = UpdateStatement.of(creator.createInsertStatement(tableName, insertingColumns));
        final String sequence = Objects.requireNonNull(entityDispatchTable.getMetaData(JanitorOrm.MetaData.ID_SEQUENCE));
        final SelectStatement nextIdQuery = Objects.requireNonNull(conn.getDialect().getNextValueQuery(sequence));
        final long generatedId = conn.queryForLong(nextIdQuery);
        record.setId(generatedId);
        if (verbose) {
            log.info("{}::insert() with new id {}, running {} on columns {}", className, generatedId, insertStatement, insertingColumns);
        }
        record.beforeInsert();
        conn.update(insertStatement, ps -> {
            // pointless / harmful: ps.addLong(generatedId);
            writeAllColumns(conn, record, insertingColumns, ps);
        });
        entityChangeListeners.fire(listener -> listener.onChange(EntityChangeListener.Type.INSERT, record));
    }

    @Override
    public void update(@NotNull DatabaseConnection conn, @NotNull T record) throws DatabaseError {
        final StatementCreator creator = new StatementCreator(getDataManager().getDialect());
        final List<String> updatingColumns = columns.stream().filter(field -> !Objects.equals(field, idColumn)).toList();
        final UpdateStatement updateStatement = UpdateStatement.of(creator.createUpdateStatement(tableName, updatingColumns, idColumn));
        record.beforeUpdate();
        conn.update(updateStatement, ps -> {
            writeAllColumns(conn, record, updatingColumns, ps);
            ps.addLong(record.getId());
        });
        entityChangeListeners.fire(listener -> listener.onChange(EntityChangeListener.Type.UPDATE, record));
    }

    @Override
    public void delete(@NotNull final DatabaseConnection conn, @NotNull final T record) throws DatabaseError {
        final StatementCreator creator = new StatementCreator(getDataManager().getDialect());
        final UpdateStatement updateStatement = UpdateStatement.of(creator.createDeleteStatement(tableName, idColumn));
        conn.update(updateStatement, ps -> ps.addLong(record.getId()));
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
            String field = Objects.requireNonNull(fieldForColumn.get(column));
            try {
                final JanitorObject propertyValue = Objects.requireNonNull(entityDispatchTable.get(field).lookupAttribute(record), "no value for field '" + field + "' in record " + record + " / column '" + column + "'");
                final @NotNull ColumnTypeHint columnTypeHint = Objects.requireNonNull(entityDispatchTable.getMetaData(field, JanitorOrm.MetaData.COLUMN_TYPE), "no column type hint for field '" + field + "' in record " + record + " / column '" + column + "'");
                CommonDao.writeProperty(conn, className, column, field, propertyValue.janitorUnpack(), ps, columnTypeHint);
            } catch (Exception e) {
                throw new SQLException("error writing column '" + column + "' / field '" + field + "' into the database", e);
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
        final var dialect = getDataManager().getDialect();
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

    @Override
    public @Nullable T lazyLoadById(final long id) {
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
            for (final Long identifier : identifiers) {
                final T obj = identifier == null ? null : getDataManager().callTransaction(conn -> findById(conn, identifier));
                if (obj != null) {
                    ++count;
                    callback.call(process, new JCallArgs("callback", process, List.of(forScript(obj), Janitor.nullableInteger(identifier))));
                } else {
                    log.warn("queryForEach: object not found for identifier {}", identifier);
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
