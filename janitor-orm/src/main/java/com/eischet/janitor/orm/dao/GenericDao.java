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
import com.eischet.janitor.api.errors.glue.JanitorGlueException;
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
import com.eischet.janitor.orm.JanitorOrm;
import com.eischet.janitor.orm.FilterExpression;
import com.eischet.janitor.orm.entity.OrmEntity;
import com.eischet.janitor.orm.meta.EntityIndex;
import com.eischet.janitor.orm.sql.ColumnTypeHint;
import com.eischet.janitor.orm.sql.StatementCreator;
import com.eischet.janitor.toolbox.json.api.JsonException;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.eischet.janitor.api.util.ObjectUtilities.simpleClassNameOf;

public abstract class GenericDao<T extends OrmEntity> extends JanitorComposed<GenericDao<?>> implements Dao<T>, JCallable {

    // TOOD: cache the database version after first retrieving it

    public static final DispatchTable<GenericDao<?>> DISPATCH = new DispatchTable<>();
    private static final Predicate<String> INVALID_FIELD = Pattern.compile("[a-zA-Z0-9_]+").asMatchPredicate().negate();

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

    }

    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected final @NotNull String tableName;
    protected final @NotNull String idColumn;
    protected final @NotNull
    @Unmodifiable List<String> columns;
    protected final String keyColumn;
    protected final DispatchTable<T> entityDispatch;
    protected final Supplier<T> newValue;
    protected final Map<String, String> columnForField = new HashMap<>();
    protected final Map<String, String> fieldForColumn = new HashMap<>();
    protected final EntityIndex entityIndex;
    protected final String className;
    protected final @NotNull Class<T> entityClass;
    protected boolean verbose = false;
    protected @Nullable DaoLogging logging;

    public GenericDao(
            final @NotNull DispatchTable<? extends GenericDao<T>> childDispatch,
            final @NotNull EntityIndex entityIndex,
            final @NotNull Class<T> entityClass,
            final @NotNull DispatchTable<T> entityDispatch,
            final @NotNull Supplier<T> newValue) {
        super(Dispatcher.inherit(DISPATCH, childDispatch));
        this.entityIndex = entityIndex;
        this.entityClass = entityClass;
        this.entityDispatch = entityDispatch;
        this.newValue = newValue;
        this.className = Objects.requireNonNull(entityDispatch.getMetaData(Janitor.MetaData.CLASS), "missing required CLASS");
        this.tableName = Objects.requireNonNull(entityDispatch.getMetaData(JanitorOrm.MetaData.TABLE_NAME), "missing required TABLE_NAME");
        this.idColumn = Objects.requireNonNull(entityDispatch.getMetaData(JanitorOrm.MetaData.ID_FIELD), "missing required ID_FIELD");
        this.keyColumn = entityDispatch.getMetaData(JanitorOrm.MetaData.KEY_FIELD); // made optional because it's not in every table (upstream)

        if (log.isDebugEnabled()) {
            log.debug("initializing dao for entity {} in table {}", className, tableName);
        }
        final List<String> databaseBackedFields = new ArrayList<>();
        final List<String> allFields = entityDispatch.streamAttributeNames().toList();
        for (final String field : allFields) {
            @Nullable final String columnName = entityDispatch.getMetaData(field, JanitorOrm.MetaData.COLUMN_NAME);
            if (columnName != null && !columnName.isBlank()) {
                columnForField.put(field, columnName);
                fieldForColumn.put(columnName, field);
                databaseBackedFields.add(columnName);
            }
        }
        this.columns = List.copyOf(databaseBackedFields);

        entityIndex.setDao(className, this);
    }

    /**
     * Mainly for scripts, which cannot get this for themselves, we need a method to acquire a data manager when needed.
     * How this happens behind the scenes will be implementation-specific.
     *
     * @return a data manager object
     */
    protected abstract DataManager getDataManager();

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
        final SelectStatement select = SelectStatement.of(creator.createSelectStatement(tableName, columns, keyColumn));
        if (verbose) {
            log.info("{}::findByKey(key='{}'): running {}", className, key, select);
        }
        return conn.queryForObject(select, stmt -> stmt.addString(key), rs -> readAllProperties(conn, rs));
    }

    @Override
    public @Nullable T findById(final @NotNull DatabaseConnection conn, final long id) throws DatabaseError {
        final StatementCreator creator = new StatementCreator(getDataManager().getDialect());
        final SelectStatement select = SelectStatement.of(creator.createSelectStatement(tableName, columns, idColumn));
        if (verbose) {
            log.info("{}::findById(id={}): running {}", className, id, select);
        }
        return conn.queryForObject(select, stmt -> stmt.addLong(id), rs -> readAllProperties(conn, rs));
    }

    @Override
    public @NotNull List<T> findAll(final @NotNull DatabaseConnection conn, final @Nullable Integer limit) throws DatabaseError {
        final StatementCreator creator = new StatementCreator(getDataManager().getDialect());
        final SelectStatement select = SelectStatement.of(creator.createSelectAllStatement(tableName, columns));
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

    private String expressionToSql(final FilterExpression filterExpression, final Consumer<Prepper> prepperConsumer) throws FilterExpression.MalformedExpression {
        @NotNull final DatabaseDialect dialect = getDataManager().getDialect();

        if (filterExpression.getField() != null && INVALID_FIELD.test(filterExpression.getField())) {
            throw new FilterExpression.MalformedExpression("invalid field '" + filterExpression.getField() + "'");
        }
        if (filterExpression.getOperator() != null && INVALID_FIELD.test(filterExpression.getOperator())) {
            throw new FilterExpression.MalformedExpression("invalid operator '" + filterExpression.getField() + "'");
        }
        if (filterExpression.getLogic() != null && INVALID_FIELD.test(filterExpression.getLogic())) {
            throw new FilterExpression.MalformedExpression("invalid logic '" + filterExpression.getField() + "'");
        }
        if (filterExpression.isGroup()) {
            return filterExpression.getFilters().stream()
                    .map((FilterExpression element) -> expressionToSql(element, prepperConsumer))
                    .collect(Collectors.joining(" " + filterExpression.getLogic() + " ", "(", ")"));
        } else if (filterExpression.isExpression()) {
            final String namedField = filterExpression.getField();
            final String column = columnForField.get(namedField);
            if (column == null) {
                throw new FilterExpression.MalformedExpression("missing column for field '" + namedField + "'");
            }
            final @Nullable ColumnTypeHint columnTypeHint = entityDispatch.getMetaData(namedField, JanitorOrm.MetaData.COLUMN_TYPE);
            if (columnTypeHint == null) {
                throw new FilterExpression.MalformedExpression("missing type for column '" + column + "' of field '" + namedField + "'");
            }
            // might be needed, or not:

            final Prepper prepLong = new NamedPrepper((conn, stmt) -> stmt.addLong(filterExpression.getValueLong().longValue()), "long=" + (filterExpression.getValueLong() != null ? filterExpression.getValueLong().longValue() : "?"));
            final Prepper prepDate = new NamedPrepper((conn, stmt) -> stmt.addDate(filterExpression.getValueDate()), "date=" + filterExpression.getValueDate());
            final Prepper prepDateTime = new NamedPrepper((conn, stmt) -> stmt.addTimestamp(filterExpression.getValueDateTime()), "datetime=" + filterExpression.getValueDateTime());
            final Prepper prepDouble = new NamedPrepper((conn, stmt) -> stmt.addDouble(filterExpression.getValueDouble()), "double=" + filterExpression.getValueDouble());
            final Prepper prepBoolean = new NamedPrepper((conn, stmt) -> stmt.addInt(filterExpression.getValueBoolean() ? 1 : 0), "bool=" + filterExpression.getValueBoolean());
            final Prepper prepString = new NamedPrepper((conn, stmt) -> stmt.addString(filterExpression.getValueString()), "string=" + filterExpression.getValueString());
            final Prepper prepStringLike = new NamedPrepper((conn, stmt) -> stmt.addString(filterExpression.getValueString() + "%"), "string=" + filterExpression.getValueString() + "%");
            final Prepper prepLikeString = new NamedPrepper((conn, stmt) -> stmt.addString("%s" + filterExpression.getValueString()), "string=%" + filterExpression.getValueString());
            final Prepper prepLikeStringLike = new NamedPrepper((conn, stmt) -> stmt.addString("%" + filterExpression.getValueString() + "%"), "string=%" + filterExpression.getValueString() + "%");

            Prepper eq = prepString;
            if (filterExpression.isDate()) {
                eq = prepDate;
            } else if (filterExpression.isDateTime()) {
                eq = prepDateTime;
            }  else if (filterExpression.isDouble()) {
                eq = prepDouble;
            } else if (filterExpression.isBoolean()) {
                eq = prepBoolean;
            } else if (filterExpression.isLong()) {
                eq = prepLong;
            } else if (filterExpression.isDate()) {
                eq = prepDate;
            } else if (filterExpression.isDouble()) {
                eq = prepDouble;
            }
            final Prepper simpleEquality = eq;

            return switch (filterExpression.getOperatorEnum()) {
                case EQ -> {
                    prepperConsumer.accept(simpleEquality);
                    yield dialect.quoteColumn(column) + " = ?";
                }
                case NEQ -> {
                    prepperConsumer.accept(simpleEquality);
                    yield dialect.quoteColumn(column) + " != ?";
                }
                case LT -> {
                    prepperConsumer.accept(simpleEquality);
                    yield dialect.quoteColumn(column) + " < ?";
                }
                case LTE -> {
                    prepperConsumer.accept(simpleEquality);
                    yield dialect.quoteColumn(column) + " <= ?";
                }
                case GT -> {
                    prepperConsumer.accept(simpleEquality);
                    yield dialect.quoteColumn(column) + " > ?";
                }
                case GTE -> {
                    prepperConsumer.accept(simpleEquality);
                    yield column + " >= ?";
                }
                case STARTSWITH -> {
                    prepperConsumer.accept(prepStringLike);
                    yield dialect.quoteColumn(column) + " like ?";
                }
                case ENDSWITH -> {
                    prepperConsumer.accept(prepLikeString);
                    yield dialect.quoteColumn(column) + " like ?";
                }
                case CONTAINS -> {
                    prepperConsumer.accept(prepLikeStringLike);
                    yield dialect.quoteColumn(column) + " like ?";
                }
                case DOESNOTCONTAIN -> {
                    prepperConsumer.accept(prepLikeStringLike);
                    yield dialect.quoteColumn(column) + " not like ?";
                }
                case ISNULL -> dialect.quoteColumn(column) + " is null";
                case ISNOTNULL -> dialect.quoteColumn(column) + " is not null";
                case ISEMPTY -> "(" + dialect.quoteColumn(column) + " is null or " + dialect.quoteColumn(column) + " = '')";
                case ISNOTEMPTY -> "(" + dialect.quoteColumn(column) + " is not null and " + dialect.quoteColumn(column) + " != '')";
            };
        } else {
            throw new FilterExpression.MalformedExpression("part is neither group nor expression");
        }
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
        @Language("SQL") final String sql = creator.createSelectAllStatement(tableName, columns) + "\nWHERE\n  " + getDataManager().getDialect().quoteColumn(foreignKeyColumn) + " = ?";
        if (verbose) {
            log.info("findByAssociation, sql: {}, fk = {}", sql, foreignKeyValue);
        }
        return conn.queryForList(new SelectStatement(sql), stmt -> stmt.addLong(foreignKeyValue), rs -> readAllProperties(conn, rs));
    }


    @Override
    public @NotNull List<T> findByFilter(final @NotNull DatabaseConnection conn, final @NotNull FilterExpression filterExpression, final Integer limit) throws DatabaseError {
        final StatementCreator creator = new StatementCreator(getDataManager().getDialect());
        final List<Prepper> preppers = new LinkedList<>();
        @Language("SQL") final String sql = creator.createSelectAllStatement(tableName, columns) + "\nWHERE\n  " + expressionToSql(filterExpression, preppers::add);
        if (verbose) {
            log.info("findByFilter, sql: {}", sql);
            log.info("preppers: {}", preppers);
        }
        @NotNull final DatabaseVersion databaseVersion = DatabaseVersion.getDatabaseVersion(getDataManager());
        if (limit != null && limit > 0 && getDataManager().getDialect().canLimitAndOffset(databaseVersion)) {
            final SelectStatement limited = getDataManager().getDialect().addLimitAndOffset(SelectStatement.of(sql + " order by 2"));
            return conn.queryForList(
                    limited,
                    stmt -> {
                        for (final Prepper prepper : preppers) {
                            prepper.prepare(conn, stmt);
                        }
                        getDataManager().getDialect().addLimitAndOffset(stmt, limit, 0);
                    },
                    rs -> readAllProperties(conn, rs));

        } else {
            return conn.queryForList(
                    new SelectStatement(sql),
                    stmt -> {
                        for (final Prepper prepper : preppers) {
                            prepper.prepare(conn, stmt);
                        }
                    },
                    rs -> readAllProperties(conn, rs));
        }
    }


    protected T readAllProperties(final DatabaseConnection conn, final SimpleResultSet rs) throws DatabaseError {
        final T value = newValue.get();
        int columnIndex = 0;
        for (final String column : columns) {
            ++columnIndex;
            String field = Objects.requireNonNull(fieldForColumn.get(column));
            final @NotNull ColumnTypeHint columnTypeHint = Objects.requireNonNull(entityDispatch.getMetaData(field, JanitorOrm.MetaData.COLUMN_TYPE));
            final @Nullable String lookupType = entityDispatch.getMetaData(field, Janitor.MetaData.REF);
            final @Nullable Boolean hostNullable = entityDispatch.getMetaData(field, Janitor.MetaData.HOST_NULLABLE);
            try {
                final JanitorObject propertyValue = Objects.requireNonNull(entityDispatch.get(field).lookupAttribute(value));
                if (propertyValue instanceof JAssignable assignableProperty) {
                    CommonDao.readProperty(entityIndex, column, conn, assignableProperty, rs, columnTypeHint, lookupType, hostNullable);
                } else {
                    throw new DatabaseError("invalid field '" + field + "' / column '" + column + "' is not assignable");
                }
            } catch (SQLException e) {
                log.warn("SQL exception on class '{}', column #{} = '{}', field '{}', type hint '{}', column order: {}", className, columnIndex, column, field, columnTypeHint, columns, e);
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
        final String sequence = Objects.requireNonNull(entityDispatch.getMetaData(JanitorOrm.MetaData.ID_SEQUENCE));
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
    }

    @Override
    public void delete(@NotNull final DatabaseConnection conn, @NotNull final T record) throws DatabaseError {
        final StatementCreator creator = new StatementCreator(getDataManager().getDialect());
        final UpdateStatement updateStatement = UpdateStatement.of(creator.createDeleteStatement(tableName, idColumn));
        conn.update(updateStatement, ps -> ps.addLong(record.getId()));
    }

    private void writeAllColumns(final DatabaseConnection conn, final T record, final List<String> updatingColumns, final SimplePreparedStatement ps) throws SQLException {
        if (verbose) {
            log.info("writeAllColumns({})", updatingColumns);
        }
        for (final String column : updatingColumns) {
            String field = Objects.requireNonNull(fieldForColumn.get(column));
            try {
                final JanitorObject propertyValue = Objects.requireNonNull(entityDispatch.get(field).lookupAttribute(record), "no value for field '" + field + "' in record " + record + " / column '" + column + "'");
                final @NotNull ColumnTypeHint columnTypeHint = Objects.requireNonNull(entityDispatch.getMetaData(field, JanitorOrm.MetaData.COLUMN_TYPE), "no column type hint for field '" + field + "' in record " + record + " / column '" + column + "'");
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
                final T instance = entityDispatch.getConstructor().call(process, JCallArgs.empty("constructor", process));
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
                final T instance = entityDispatch.getConstructor().call(process, JCallArgs.empty("constructor", process));
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
        if (!(o instanceof final GenericDao<?> that)) return false;
        return Objects.equals(tableName, that.tableName);
    }

    /**
     * Hash code generation.
     * Note that this is based on the table name by default, which should be reasonable for many use cases.
     * @return
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(tableName);
    }
}
