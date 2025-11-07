package com.eischet.janitor.orm.dao;

import com.eischet.dbxs.*;
import com.eischet.dbxs.exceptions.DatabaseError;
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
import com.eischet.janitor.api.types.builtin.JList;
import com.eischet.janitor.api.types.builtin.JMap;
import com.eischet.janitor.api.types.builtin.JNumber;
import com.eischet.janitor.api.types.builtin.JString;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.api.types.dispatch.Dispatcher;
import com.eischet.janitor.api.types.functions.JCallArgs;
import com.eischet.janitor.api.types.functions.JCallable;
import com.eischet.janitor.orm.JanitorOrm;
import com.eischet.janitor.orm.entity.OrmEntity;
import com.eischet.janitor.orm.entity.OrmJoined;
import com.eischet.janitor.orm.meta.EntityIndex;
import com.eischet.janitor.orm.ref.ForeignKey;
import com.eischet.janitor.orm.sql.ColumnTypeHint;
import com.eischet.janitor.orm.sql.StatementCreator;
import com.eischet.janitor.toolbox.json.api.JsonException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.eischet.janitor.api.util.ObjectUtilities.simpleClassNameOf;

public abstract class JoinDao<T extends OrmJoined> extends JanitorComposed<JoinDao<?>> implements JCallable {

    public static final DispatchTable<JoinDao<?>> DISPATCH = new DispatchTable<>();

    static {
        DISPATCH.addBooleanProperty("verbose", JoinDao::isVerbose, JoinDao::setVerbose);
        DISPATCH.addStringProperty("tableName", dao -> dao.tableName);
        DISPATCH.addListProperty("columns", dao -> Janitor.list(dao.columns.stream().map(Janitor::string)));
        DISPATCH.addStringProperty("className", dao -> dao.className);
        DISPATCH.addMethod("insert", JoinDao::insertForScript);
        DISPATCH.addMethod("update", JoinDao::updateForScript);
        DISPATCH.addMethod("merge", JoinDao::mergeForScript);
        DISPATCH.addVoidMethod("delete", JoinDao::deleteForScript);
    }

    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected final @NotNull String tableName;
    protected final @NotNull
    @Unmodifiable List<String> columns;
    protected final DispatchTable<T> entityDispatch;
    protected final Supplier<T> newValue;
    protected final Map<String, String> columnForField = new HashMap<>();
    protected final Map<String, String> fieldForColumn = new HashMap<>();
    protected final EntityIndex entityIndex;
    protected final String className;
    protected final @NotNull List<String> primaryKeyColumns;
    protected final Class<T> entityClass;
    protected boolean verbose = false;
    protected @Nullable DaoLogging logging;

    /**
     * Mainly for scripts, which cannot get this for themselves, we need a method to acquire a data manager when needed.
     * How this happens behind the scenes will be implementation-specific.
     *
     * @return a data manager object
     */
    protected abstract DataManager getDataManager();

    /**
     * Helper method for automatic implementation of an "add" method on JoinedList objects.
     * @param parentEntity a parent entity
     * @param addedEntity an entity added to a JoinedList property of the parent entity
     * @return an instance of T that is populated with parentEntity and addedEntity in a meaningful way
     * @throws JanitorRuntimeException when the entity types do not match T
     */
    public abstract T createFromEntityPair(final @NotNull JanitorScriptProcess process, final @NotNull ForeignKey<?> parentEntity, final @NotNull ForeignKey<?> addedEntity) throws JanitorRuntimeException;

    public JoinDao(
            final Class<T> entityClass,
            final DispatchTable<? extends JoinDao<T>> childDispatch,
            final EntityIndex entityIndex,
            final DispatchTable<T> entityDispatch,
            final Supplier<T> newValue) {

        super(Dispatcher.inherit(DISPATCH, childDispatch));
        this.entityClass = entityClass;
        this.entityIndex = entityIndex;
        this.entityDispatch = entityDispatch;
        this.newValue = newValue;
        this.className = Objects.requireNonNull(entityDispatch.getMetaData(Janitor.MetaData.CLASS), "missing required CLASS");
        this.tableName = Objects.requireNonNull(entityDispatch.getMetaData(JanitorOrm.MetaData.TABLE_NAME), "missing required TABLE_NAME");
        this.primaryKeyColumns = List.copyOf(Objects.requireNonNull(entityDispatch.getMetaData(JanitorOrm.MetaData.JOIN_TABLE_PK), "missing required JOIN_TABLE_PK columns"));

        if (log.isDebugEnabled()) {
            log.debug("initializing dao for joined {} in table {}", className, tableName);
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
        entityIndex.setJoinDao(entityClass.getSimpleName(), this);

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
            } catch (NullPointerException | JanitorGlueException e) {
                throw new DatabaseError("invalid field '" + field + "' caused an exception", e);
            }
        }
        return value;
    }

    public @NotNull @Unmodifiable List<T> findByQuery(@NotNull final DatabaseConnection conn, @NotNull final String query, @NotNull final StatementConfigurator statementConfigurator) throws DatabaseError {
        final SelectStatement select = SelectStatement.of(query);
        return conn.queryForList(select, statementConfigurator, rs -> readAllProperties(conn, rs));
    }

    protected List<T> findByColumn(final DatabaseConnection conn, final String columnName, final long id) throws DatabaseError {
        final StatementCreator creator = new StatementCreator(getDataManager().getDialect());
        final SelectStatement select = SelectStatement.of(creator.createSelectStatement(tableName, columns, columnName));
        return conn.queryForList(select, stmt -> stmt.addLong(id), rs -> readAllProperties(conn, rs));
    }



    public void insert(@NotNull DatabaseConnection conn, @NotNull T record) throws DatabaseError {
        final StatementCreator creator = new StatementCreator(getDataManager().getDialect());
        final List<String> insertingColumns = columns.stream().toList();
        final UpdateStatement insertStatement = UpdateStatement.of(creator.createInsertStatement(tableName, insertingColumns));
        final int changedRows = conn.update(insertStatement, ps -> writeAllColumns(conn, record, insertingColumns, ps));
        if (verbose) {
            log.info("inserted {} rows", changedRows);
        }
        if (changedRows == 0) {
            throw new DatabaseError("no rows affected by insert");
        }
        if (changedRows > 1) {
            throw new DatabaseError("multiple rows affected by insert");
        }
    }

    public void merge(@NotNull DatabaseConnection conn, @NotNull T record) throws DatabaseError {
        final StatementCreator creator = new StatementCreator(getDataManager().getDialect());
        final List<String> updatingColumns = columns.stream().filter(col -> !primaryKeyColumns.contains(col)).toList();
        if (updatingColumns.isEmpty()) {
            final List<String> countingColumns = columns.stream().toList();
            final SelectStatement countStatement = SelectStatement.of(creator.createCountStatement(tableName, countingColumns));
            final int count = conn.queryForInt(countStatement, ps -> writeAllColumns(conn, record, countingColumns, ps));
            log.info("merge: suche via {} -> {} Zeilen", countStatement.getSql(), count);
            if (count == 0) {
                insert(conn, record);
            }
        } else {
            final UpdateStatement updateStatement = UpdateStatement.of(creator.createUpdateStatement(tableName, updatingColumns, primaryKeyColumns));
            final int changedRows = conn.update(updateStatement, ps -> {
                writeAllColumns(conn, record, updatingColumns, ps);
                writeAllColumns(conn, record, primaryKeyColumns, ps);
            });
            if (verbose) {
                log.info("updated {} rows", changedRows);
            }
            if (changedRows == 0) {
                update(conn, record);
            }
            if (changedRows > 1) {
                throw new DatabaseError("multiple rows affected by update");
            }
        }
    }

    public void update(@NotNull DatabaseConnection conn, @NotNull T record) throws DatabaseError {
        final StatementCreator creator = new StatementCreator(getDataManager().getDialect());
        final List<String> updatingColumns = columns.stream().filter(col -> !primaryKeyColumns.contains(col)).toList();
        if (updatingColumns.isEmpty()) {
            log.warn("update is meaningless on a collection that consists only of its primary key columns - you probably want to call merge directly, and I'm calling it for you");
            merge(conn, record);
            return;
        }
        final UpdateStatement updateStatement = UpdateStatement.of(creator.createUpdateStatement(tableName, updatingColumns, primaryKeyColumns));
        final int changedRows = conn.update(updateStatement, ps -> {
            writeAllColumns(conn, record, updatingColumns, ps);
            writeAllColumns(conn, record, primaryKeyColumns, ps);
        });
        if (verbose) {
            log.info("updated {} rows", changedRows);
        }
        if (changedRows == 0) {
            throw new DatabaseError("no rows affected by update");
        }
        if (changedRows > 1) {
            throw new DatabaseError("multiple rows affected by update");
        }
    }

    public void delete(@NotNull final DatabaseConnection conn, @NotNull final T record) throws DatabaseError {
        final StatementCreator creator = new StatementCreator(getDataManager().getDialect());
        final UpdateStatement updateStatement = UpdateStatement.of(creator.createDeleteStatement(tableName, primaryKeyColumns));
        final int changedRows = conn.update(updateStatement, ps -> writeAllColumns(conn, record, primaryKeyColumns, ps));
        if (verbose) {
            log.info("deleted {} rows", changedRows);
        }
        if (changedRows == 0) {
            throw new DatabaseError("no rows affected by delete");
        }
        if (changedRows > 1) {
            throw new DatabaseError("multiple rows affected by delete");
        }
    }

    public T convertToEntity(final @NotNull JanitorScriptProcess process, final @NotNull JCallArgs arguments, final OrmEntity parent) throws JanitorRuntimeException {
        final JanitorObject param = arguments.require(1).get(0);
        if (param instanceof JMap map) {
            final T instance = newValue.get();
            // we cannot usually call this, because it needs the Source as a parameter, so it cannot be in the dispatch table!
            // final T instance = entityDispatch.getConstructor().call(process, JCallArgs.empty("constructor", process));
            map.applyTo(process, instance);
            return instance;
        } else if (entityClass.isInstance(param)) {
            return entityClass.cast(param);
        } else if (param instanceof ForeignKey<?> fk && parent instanceof ForeignKey<?> parentFk) {
            return createFromEntityPair(process, parentFk, fk);
        }
        throw new JanitorArgumentException(process, "invalid argument " + param + " [" + simpleClassNameOf(param) + "]");
    }


    public T insertForScript(final @NotNull JanitorScriptProcess process,
                                final @NotNull JCallArgs arguments) throws JanitorRuntimeException {
        final JanitorObject param = arguments.require(1).get(0);
        if (param instanceof JMap map) {
            final T instance = newValue.get();
            // we cannot usually call this, because it needs the Source as a parameter, so it cannot be in the dispatch table!
            // final T instance = entityDispatch.getConstructor().call(process, JCallArgs.empty("constructor", process));
            map.applyTo(process, instance);
            try {
                getDataManager().executeTransaction(conn -> insert(conn, instance));
                return instance;
            } catch (DatabaseError e) {
                throw new JanitorNativeException(process, e.getMessage(), e);
            }
        } else if (entityClass.isInstance(param)) {
            final T instance = entityClass.cast(param);
            try {
                getDataManager().executeTransaction(conn -> insert(conn, instance));
                return instance;
            } catch (DatabaseError e) {
                throw new JanitorNativeException(process, e.getMessage(), e);
            }
        }
        throw new JanitorArgumentException(process, "invalid argument " + param + " [" + simpleClassNameOf(param) + "]");
    }

    public T updateForScript(final @NotNull JanitorScriptProcess process,
                                   final @NotNull JCallArgs arguments) throws JanitorRuntimeException {
        final JanitorObject param = arguments.require(1).get(0);
        if (param instanceof JMap map) {
            final T instance = newValue.get();
            // we cannot usually call this, because it needs the Source as a parameter, so it cannot be in the dispatch table!
            // final T instance = entityDispatch.getConstructor().call(process, JCallArgs.empty("constructor", process));
            map.applyTo(process, instance);
            try {
                getDataManager().executeTransaction(conn -> update(conn, instance));
                return instance;
            } catch (DatabaseError e) {
                throw new JanitorNativeException(process, e.getMessage(), e);
            }
        } else if (entityClass.isInstance(param)) {
            final T instance = entityClass.cast(param);
            try {
                getDataManager().executeTransaction(conn -> update(conn, instance));
                return instance;
            } catch (DatabaseError e) {
                throw new JanitorNativeException(process, e.getMessage(), e);
            }
        }
        throw new JanitorArgumentException(process, "invalid argument " + param + " [" + simpleClassNameOf(param) + "]");
    }

    public T mergeForScript(final @NotNull JanitorScriptProcess process,
                                final @NotNull JCallArgs arguments) throws JanitorRuntimeException {
        final JanitorObject param = arguments.require(1).get(0);
        if (param instanceof JMap map) {
            final T instance = newValue.get();
            // we cannot usually call this, because it needs the Source as a parameter, so it cannot be in the dispatch table!
            // final T instance = entityDispatch.getConstructor().call(process, JCallArgs.empty("constructor", process));
            map.applyTo(process, instance);
            try {
                getDataManager().executeTransaction(conn -> merge(conn, instance));
                return instance;
            } catch (DatabaseError e) {
                throw new JanitorNativeException(process, e.getMessage(), e);
            }
        } else if (entityClass.isInstance(param)) {
            final T instance = entityClass.cast(param);
            try {
                getDataManager().executeTransaction(conn -> merge(conn, instance));
                return instance;
            } catch (DatabaseError e) {
                throw new JanitorNativeException(process, e.getMessage(), e);
            }
        }
        throw new JanitorArgumentException(process, "invalid argument " + param + " [" + simpleClassNameOf(param) + "]");
    }


    public void deleteForScript(final @NotNull JanitorScriptProcess process, final @NotNull JCallArgs arguments) throws JanitorRuntimeException {
        final JanitorObject param = arguments.require(1).get(0);
        if (param instanceof JMap map) {
            final T instance = newValue.get();
            // we cannot usually call this, because it needs the Source as a parameter, so it cannot be in the dispatch table!
            // final T instance = entityDispatch.getConstructor().call(process, JCallArgs.empty("constructor", process));
            map.applyTo(process, instance);
            try {
                getDataManager().executeTransaction(conn -> delete(conn, instance));
            } catch (DatabaseError e) {
                throw new JanitorNativeException(process, e.getMessage(), e);
            }
        } else if (entityClass.isInstance(param)) {
            final T instance = entityClass.cast(param);
            try {
                getDataManager().executeTransaction(conn -> delete(conn, instance));
            } catch (DatabaseError e) {
                throw new JanitorNativeException(process, e.getMessage(), e);
            }
        }
        throw new JanitorArgumentException(process, "invalid argument " + param + " [" + simpleClassNameOf(param) + "]");
    }


    protected JList fetchForScript(final @NotNull JanitorScriptProcess process,
                                   final @NotNull JCallArgs arguments,
                                   final @NotNull String columnName,
                                   final @NotNull Class<?> expected) throws JanitorRuntimeException {
        try {
            final long id = toId(process, arguments.require(1).get(0), expected);
            final List<T> results = getDataManager().callTransaction(conn -> findByColumn(conn, columnName, id));
            return Janitor.list(results);
        } catch (DatabaseError e) {
            throw new JanitorNativeException(process, e.getMessage(), e);
        }
    }

    protected JList fetchForScriptDual(final @NotNull JanitorScriptProcess process,
                                   final @NotNull JCallArgs arguments,
                                   final @NotNull String columnName1,
                                   final @NotNull String columnName2,
                                   final @NotNull Class<?> expected) throws JanitorRuntimeException {
        try {
            final long id = toId(process, arguments.require(1).get(0), expected);
            final List<T> results1 = getDataManager().callTransaction(conn -> findByColumn(conn, columnName1, id));
            final List<T> results2 = getDataManager().callTransaction(conn -> findByColumn(conn, columnName2, id));
            return Janitor.list(Stream.concat(results1.stream(), results2.stream()));
        } catch (DatabaseError e) {
            throw new JanitorNativeException(process, e.getMessage(), e);
        }
    }


    protected long toId(final @NotNull JanitorScriptProcess process,
                        final JanitorObject janitorObject,
                        final Class<?> expected) throws JanitorRuntimeException {
        if (janitorObject instanceof JNumber number) {
            return number.toLong();
        }
        if (janitorObject instanceof ForeignKey<?> fk) {
            if (fk.getReferencedEntityClass() != expected) {
                throw new JanitorNativeException(process, "invalid argument: should be " + expected.getSimpleName() + " but is " + fk.getReferencedEntityClassName(), null);
            }
            return fk.getId();
        }
        if (janitorObject instanceof OrmEntity ormEntity) {
            if (ormEntity.getClass() != expected) {
                throw new JanitorNativeException(process, "invalid argument: should be " + expected.getSimpleName() + " but is " + ormEntity.getClass().getSimpleName(), null);
            }
            return ormEntity.getId();
        }
        throw new JanitorNativeException(process, "invalid argument: should be " + expected.getSimpleName() + " or an ID but is " + simpleClassNameOf(janitorObject), null);
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
            } catch (JanitorGlueException e) {
                throw new SQLException("error writing column '" + column + "' / field '" + field + "' into the database", e);
            }
        }
    }


    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(final boolean verbose) {
        this.verbose = verbose;
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

    public <X> X callLazyTransaction(final DatabaseFunction<DatabaseConnection, X> function) throws JanitorError {
        try {
            return getDataManager().callTransaction(function);
        } catch (DatabaseError e) {
            throw new JanitorError(e.getMessage(), e);
        }
    }

}
