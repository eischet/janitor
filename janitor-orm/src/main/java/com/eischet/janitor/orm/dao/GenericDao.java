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
import com.eischet.janitor.orm.JanitorOrm;
import com.eischet.janitor.orm.cache.EntityCache;
import com.eischet.janitor.orm.cache.SimpleEntityCache;
import com.eischet.janitor.orm.filter.FilterExpression;
import com.eischet.janitor.orm.entity.OrmEntity;
import com.eischet.janitor.orm.filter.FilterOperator;
import com.eischet.janitor.orm.filter.MalformedExpression;
import com.eischet.janitor.orm.meta.EntityDispatchTable;
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
        // soll ich auch columnForField und fieldForColumn veröffentlichen!?

        DISPATCH.addMethod("insert", (self, process, arguments) -> self.insertForScript(process, arguments.require(1).get(0)));
        DISPATCH.addVoidMethod("update", (self, process, arguments) -> self.updateForScript(process, arguments.require(1).get(0)));
        DISPATCH.addMethod("findAll", (self, process, arguments) -> self.callScriptTransaction(process, conn -> Janitor.list(self.findAll(conn))));


        DISPATCH.addMethod("getById", (self, process, args) -> {
            try {
                final long id = args.getRequiredLongValue(0);
                return Janitor.nullableObject(self.getDataManager().callTransaction(conn -> self.findById(conn, id)));
            } catch (DatabaseError e) {
                throw new JanitorNativeException(process, "error getting entity be id", e);
            }
        });
        DISPATCH.addMethod("getByKey", (self, process, args) -> {
            try {
                final String key = args.getRequiredStringValue(0);
                return Janitor.nullableObject(self.getDataManager().callTransaction(conn -> self.findByKey(conn, key)));
            } catch (DatabaseError e) {
                throw new JanitorNativeException(process, "error getting entity by key", e);
            }
        });
        DISPATCH.addMethod("getAll", (self, process, args) -> {
            try {
                return Janitor.nullableObject(self.getDataManager().callTransaction(conn -> Janitor.list(self.findAll(conn, null))));
            } catch (DatabaseError e) {
                throw new JanitorNativeException(process, "error getting all entities", e);
            }
        });
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
            if (schemaVersion != null && versionRange != null && !versionRange.includes(schemaVersion)) {
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
                case INSERT, UPDATE -> cache.put(entity);
                case DELETE -> cache.invalidateById(entity.getId());
            }
        });
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

    public boolean isVerbose() {
        return verbose;
    }

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
     * Fetches a single lazily-loaded NCLOB column's current value for one row, running its own,
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
        return getDataManager().callTransaction(conn -> conn.queryForObject(select, stmt -> stmt.addLong(id), rs -> rs.readNationalClob()));
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
            stmt.addString(filterExpression.getValueString() + "%");
        } else {
            stmt.addNullString();
        }
    }, filterExpression.getValueString() != null ? ("string='" + filterExpression.getValueString() + "%'") : ("string=null"));

    public static final ExpressionPrepperBuilder PREP_ENDS_WITH_STRING = filterExpression -> new NamedPrepper((conn, stmt) -> {
        if (filterExpression.getValueString() != null) {
            stmt.addString("%s" + filterExpression.getValueString());
        } else {
            stmt.addNullString();
        }
    }, filterExpression.getValueString() != null ? ("string='%" + filterExpression.getValueString() + "'") : ("string=null"));

    public static final ExpressionPrepperBuilder PREP_CONTAINS_STRING = filterExpression -> new NamedPrepper((conn, stmt) -> {
        if (filterExpression.getValueString() != null) {
            stmt.addString("%" + filterExpression.getValueString() + "%");
        } else {
            stmt.addNullString();
        }
    }, filterExpression.getValueString() != null ? ("string='%" + filterExpression.getValueString() + "%'") : ("string=null"));


    protected @Nullable ExpressionHandler getCustomExpressionHandler(final FilterExpression filterExpression) {
        return null;
    }

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
        } else if (filterExpression.isDate()) {
            return PREP_DATE.getPrepper(filterExpression);
        } else if (filterExpression.isDouble()) {
            return PREP_DOUBLE.getPrepper(filterExpression);
        } else {
            return PREP_STRING.getPrepper(filterExpression);
        }
    }

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
                if (columnTypeHint == null) {
                    throw new MalformedExpression("missing type for column '" + column + "' of field '" + namedField + "'");
                }
                if (filterExpression.getOperator() == null) {
                    throw new MalformedExpression("missing operator in expression " + filterExpression);
                }
                final String quotedColumn = dialect.quoteColumn(column);
                return applyExpressionToColumn(filterExpression, quotedColumn, columnTypeHint, prepperConsumer);
            }
        } else {
            throw new MalformedExpression("part is neither group nor expression");
        }
    }

    protected String applyExpressionToColumn(final FilterExpression filterExpression, final String quotedColumn, final Consumer<Prepper> prepperConsumer) throws MalformedExpression {
        return applyExpressionToColumn(filterExpression, quotedColumn, null, prepperConsumer);
    }

    /**
     * Like {@link #applyExpressionToColumn(FilterExpression, String, Consumer)}, but takes the target
     * column's type into account (see {@link #getPrepper(FilterExpression, ColumnTypeHint)}). Pass
     * {@code null} for a synthetic/non-entity column whose type isn't known.
     */
    protected String applyExpressionToColumn(final FilterExpression filterExpression, final String quotedColumn, final @Nullable ColumnTypeHint columnTypeHint, final Consumer<Prepper> prepperConsumer) throws MalformedExpression {
        @Nullable final FilterOperator op = filterExpression.getOperator();
        final Prepper simpleEquality = getPrepper(filterExpression, columnTypeHint);
        return switch (op) {
            case EQ -> {
                prepperConsumer.accept(simpleEquality);
                yield quotedColumn + " = ?";
            }
            case NEQ -> {
                prepperConsumer.accept(simpleEquality);
                yield quotedColumn + " != ?";
            }
            case LT -> {
                prepperConsumer.accept(simpleEquality);
                yield quotedColumn + " < ?";
            }
            case LTE -> {
                prepperConsumer.accept(simpleEquality);
                yield quotedColumn + " <= ?";
            }
            case GT -> {
                prepperConsumer.accept(simpleEquality);
                yield quotedColumn + " > ?";
            }
            case GTE -> {
                prepperConsumer.accept(simpleEquality);
                yield quotedColumn + " >= ?";
            }
            case STARTSWITH -> {
                prepperConsumer.accept(PREP_STARTS_WITH_STRING.getPrepper(filterExpression));
                yield quotedColumn + " like ?";
            }
            case ENDSWITH -> {
                prepperConsumer.accept(PREP_ENDS_WITH_STRING.getPrepper(filterExpression));
                yield quotedColumn + " like ?";
            }
            case CONTAINS -> {
                prepperConsumer.accept(PREP_CONTAINS_STRING.getPrepper(filterExpression));
                yield quotedColumn + " like ?";
            }
            case DOESNOTCONTAIN -> {
                prepperConsumer.accept(PREP_CONTAINS_STRING.getPrepper(filterExpression));
                yield quotedColumn + " not like ?";
            }
            case ISNULL -> quotedColumn + " is null";
            case ISNOTNULL -> quotedColumn + " is not null";
            case ISEMPTY -> "(" + quotedColumn + " is null or " + quotedColumn + " = '')";
            case ISNOTEMPTY -> "(" + quotedColumn + " is not null and " + quotedColumn + " != '')";
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

        public @Nullable Integer getRowLimit() {
            return rowLimit;
        }

        public boolean isLimited() {
            return rowLimit != null;
        }

        public @NotNull @Unmodifiable List<Prepper> getPreppers() {
            return preppers;
        }
    }

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
            // unsinnig / schädlich: ps.addLong(generatedId);
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

    @SuppressWarnings("unused") // it's used, the IDE just can't see it.
    protected void unsupported(final String what) throws DatabaseError {
        throw new DatabaseError("Nicht unterstützte Funktion für " + getClass().getSimpleName() + ": " + what);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

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

    public JanitorObject scriptFindById(final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        try {
            final T single = getDataManager().callTransaction(conn -> {
                try {
                    return findById(conn, arguments.getRequiredLongValue(0));
                } catch (JanitorArgumentException e) {
                    throw new DatabaseError(e.getMessage(), e);
                }
            });
            return Janitor.nullableObject(single);
        } catch (DatabaseError e) {
            throw new JanitorNativeException(process, e.getMessage(), e);
        }
    }

    public JanitorObject scriptFindByKey(final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        try {
            final T single = getDataManager().callTransaction(conn -> {
                try {
                    return findByKey(conn, arguments.getRequiredStringValue(0));
                } catch (JanitorRuntimeException e) {
                    throw new DatabaseError(e.getMessage(), e);
                }
            });
            return Janitor.nullableObject(single);
        } catch (DatabaseError e) {
            throw new JanitorNativeException(process, e.getMessage(), e);
        }
    }

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
                    callback.call(process, new JCallArgs("callback", process, List.of(obj, Janitor.nullableInteger(identifier))));
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
