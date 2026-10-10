// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.dao;

import com.eischet.dbxs.*;
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
import com.eischet.janitor.logging.JanitorLogger;
import com.eischet.janitor.orm.meta.OrmPropertyHandle;
import com.eischet.janitor.orm.JanitorOrm;
import com.eischet.janitor.orm.entity.OrmEntity;
import com.eischet.janitor.orm.entity.OrmJoined;
import com.eischet.janitor.orm.ref.ForeignKey;
import com.eischet.janitor.orm.sql.ColumnTypeHint;
import com.eischet.janitor.orm.sql.StatementCreator;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.listeners.ListenerRegistration;
import com.eischet.janitor.toolbox.listeners.ListenerSet;
import com.eischet.janitor.toolbox.listeners.ListenerSetStandard;
import com.eischet.janitor.versioning.Version;
import com.eischet.janitor.versioning.VersionRange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.sql.SQLException;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.eischet.janitor.api.util.ObjectUtilities.simpleClassNameOf;

/**
 * A data access object for the records of a join table, which connect two entities.
 * Join records are identified by several primary key columns, not by a single ID.
 * @param <T> the type of the join records
 */
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
        DISPATCH.addStringProperty("jsonSchema", self -> Janitor.current().writeJson(self.entityDispatchTable::writeSchemaToJson));
    }

    protected final ListenerSet<EntityChangeListener<T>> entityChangeListeners= new ListenerSetStandard<>();
    protected final JanitorLogger log = JanitorLogger.getLogger(getClass());
    protected final @NotNull String tableName;
    protected final @NotNull
    @Unmodifiable List<String> columns;
    protected final DispatchTable<T> entityDispatchTable;
    protected final Supplier<T> newValue;
    protected final Map<String, String> columnForField = new HashMap<>();
    protected final Map<String, String> fieldForColumn = new HashMap<>();
    protected final OrmDaoCollection<?> collection;
    protected final String className;
    protected final @NotNull List<String> primaryKeyColumns;
    protected final Class<T> entityClass;
    protected boolean verbose = false;
    protected @Nullable DaoLogging logging;

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
     * @return the simple name of the entity class
     */
    public @NotNull String getEntityClassName() {
        return entityClass.getSimpleName();
    }

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
            final OrmDaoCollection<?> collection,
            final DispatchTable<T> entityDispatchTable,
            final Supplier<T> newValue) {

        super(Dispatcher.inherit(DISPATCH, childDispatch));
        this.entityClass = entityClass;
        this.collection = collection;
        this.entityDispatchTable = entityDispatchTable;
        this.newValue = newValue;
        this.className = Objects.requireNonNull(entityDispatchTable.getMetaData(Janitor.MetaData.CLASS), "missing required CLASS");
        this.tableName = Objects.requireNonNull(entityDispatchTable.getMetaData(JanitorOrm.MetaData.TABLE_NAME), "missing required TABLE_NAME");
        this.primaryKeyColumns = List.copyOf(Objects.requireNonNull(entityDispatchTable.getMetaData(JanitorOrm.MetaData.JOIN_TABLE_PK), "missing required JOIN_TABLE_PK columns"));

        if (log.isDebugEnabled()) {
            log.debug("initializing dao for joined {} in table {}", className, tableName);
        }
        final List<String> databaseBackedFields = new ArrayList<>();
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
            }
        }
        this.columns = List.copyOf(databaseBackedFields);
        collection.registerJoinDao(this);

    }

    /**
     * Reads all columns of the current row into a new join record.
     * @param conn the database connection
     * @param rs the result set, positioned at the row to read
     * @return the new join record
     * @throws DatabaseError if a column cannot be read
     */
    protected T readAllProperties(final DatabaseConnection conn, final SimpleResultSet rs) throws DatabaseError {
        final T value = newValue.get();
        int columnIndex = 0;
        for (final String column : columns) {
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
                log.warn("SQL exception on class '{}', column #{} = '{}', field '{}', type hint '{}', column order: {}", className, columnIndex, column, field, columnTypeHint, columns, e);
                final String message = String.format("SQL exception on class '%s', column '%s', field '%s', type hint '%s'", className, column, field, columnTypeHint);
                throw new DatabaseError(message, e);
            } catch (Exception e) {
                throw new DatabaseError("invalid field '" + field + "' caused an exception", e);
            }
        }
        return value;
    }

    /**
     * Finds join records using a custom query.
     * @param conn the database connection
     * @param query the SQL text
     * @param statementConfigurator sets the parameters of the query
     * @return the join records
     * @throws DatabaseError on database errors
     */
    public @NotNull @Unmodifiable List<T> findByQuery(@NotNull final DatabaseConnection conn, @NotNull final String query, @NotNull final StatementConfigurator statementConfigurator) throws DatabaseError {
        final SelectStatement select = SelectStatement.of(query);
        return conn.queryForList(select, statementConfigurator, rs -> readAllProperties(conn, rs));
    }

    /**
     * Finds all join records where a column has the given value.
     * @param conn the database connection
     * @param columnName the column
     * @param id the value
     * @return the join records
     * @throws DatabaseError on database errors
     */
    protected List<T> findByColumn(final DatabaseConnection conn, final String columnName, final long id) throws DatabaseError {
        final StatementCreator creator = new StatementCreator(getDataManager().getDialect());
        final SelectStatement select = SelectStatement.of(creator.createSelectStatement(tableName, columns, columnName));
        return conn.queryForList(select, stmt -> stmt.addLong(id), rs -> readAllProperties(conn, rs));
    }



    /**
     * Inserts a join record.
     * @param conn the database connection
     * @param record the join record
     * @throws DatabaseError if the insert fails, or does not affect exactly one row
     */
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
        entityChangeListeners.fire(listener -> listener.onChange(EntityChangeListener.Type.INSERT, record));
    }

    /**
     * Updates a join record, or inserts it if it does not exist yet.
     * @param conn the database connection
     * @param record the join record
     * @throws DatabaseError if the update affects more than one row, or on database errors
     */
    public void merge(@NotNull DatabaseConnection conn, @NotNull T record) throws DatabaseError {
        final StatementCreator creator = new StatementCreator(getDataManager().getDialect());
        final List<String> updatingColumns = columns.stream().filter(col -> !primaryKeyColumns.contains(col)).toList();
        if (updatingColumns.isEmpty()) {
            final List<String> countingColumns = columns.stream().toList();
            final SelectStatement countStatement = SelectStatement.of(creator.createCountStatement(tableName, countingColumns));
            final int count = conn.queryForInt(countStatement, ps -> writeAllColumns(conn, record, countingColumns, ps));
            log.info("merge: searching via {} -> {} rows", countStatement.getSql(), count);
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
                insert(conn, record);
            }
            if (changedRows > 1) {
                throw new DatabaseError("multiple rows affected by update");
            }
        }
    }

    /**
     * Updates a join record.
     * @param conn the database connection
     * @param record the join record
     * @throws DatabaseError if the update does not affect exactly one row
     */
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
        entityChangeListeners.fire(listener -> listener.onChange(EntityChangeListener.Type.UPDATE, record));
    }

    /**
     * Deletes a join record.
     * @param conn the database connection
     * @param record the join record
     * @throws DatabaseError if the delete does not affect exactly one row
     */
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
        entityChangeListeners.fire(listener -> listener.onChange(EntityChangeListener.Type.DELETE, record));
    }

    /**
     * Converts a script value to a join record: a map is applied to a new record, an existing record is returned as it is,
     * and a foreign key is combined with the parent entity.
     * @param process the running script process
     * @param arguments the call arguments
     * @param parent the parent entity
     * @return the join record
     * @throws JanitorRuntimeException if the argument cannot be converted
     */
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


    /**
     * Script method {@code dao.insert(x)}: inserts a join record, given as a map or as a record.
     * @param process the running script process
     * @param arguments the call arguments
     * @return the inserted record
     * @throws JanitorRuntimeException if the argument is invalid or the insert fails
     */
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

    /**
     * Script method {@code dao.update(x)}: updates a join record, given as a map or as a record.
     * @param process the running script process
     * @param arguments the call arguments
     * @return the updated record
     * @throws JanitorRuntimeException if the argument is invalid or the update fails
     */
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

    /**
     * Script method {@code dao.merge(x)}: updates a join record, or inserts it if it does not exist yet.
     * @param process the running script process
     * @param arguments the call arguments
     * @return the merged record
     * @throws JanitorRuntimeException if the argument is invalid or the merge fails
     */
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


    /**
     * Script method {@code dao.delete(x)}: deletes a join record, given as a map or as a record.
     * @param process the running script process
     * @param arguments the call arguments
     * @throws JanitorRuntimeException if the argument is invalid or the delete fails
     */
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
        } else {
            throw new JanitorArgumentException(process, "invalid argument " + param + " [" + simpleClassNameOf(param) + "], expected Map or " + entityClass.getName() + " instead");
        }
    }


    /**
     * Finds the join records that refer to an entity via a column, for use in script methods.
     * @param process the running script process
     * @param arguments the call arguments; the first one is the entity or its ID
     * @param columnName the column that holds the reference
     * @param expected the expected type of the entity
     * @return a list of the join records
     * @throws JanitorRuntimeException if the argument is invalid or the query fails
     */
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

    /**
     * Finds the join records that refer to an entity via either of two columns, for use in script methods.
     * @param process the running script process
     * @param arguments the call arguments; the first one is the entity or its ID
     * @param columnName1 the first column that may hold the reference
     * @param columnName2 the second column that may hold the reference
     * @param expected the expected type of the entity
     * @return a list of the join records
     * @throws JanitorRuntimeException if the argument is invalid or the query fails
     */
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


    /**
     * Converts a script value to an ID: it can be a number, a foreign key or an entity.
     * @param process the running script process
     * @param janitorObject the value
     * @param expected the expected type of the entity
     * @return the ID
     * @throws JanitorRuntimeException if the value is not valid
     */
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
                final JanitorObject propertyValue = Objects.requireNonNull(entityDispatchTable.get(field).lookupAttribute(record), "no value for field '" + field + "' in record " + record + " / column '" + column + "'");
                final @NotNull ColumnTypeHint columnTypeHint = Objects.requireNonNull(entityDispatchTable.getMetaData(field, JanitorOrm.MetaData.COLUMN_TYPE), "no column type hint for field '" + field + "' in record " + record + " / column '" + column + "'");
                CommonDao.writeProperty(conn, className, column, field, propertyValue.janitorUnpack(), ps, columnTypeHint);
            } catch (Exception e) {
                throw new SQLException("error writing column '" + column + "' / field '" + field + "' into the database", e);
            }
        }
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
     * Runs a function in a transaction, for lazy loading.
     * @param function the function to run
     * @param <X> the type of the result
     * @return the result of the function
     * @throws JanitorError if the transaction fails
     */
    public <X> X callLazyTransaction(final DatabaseFunction<DatabaseConnection, X> function) throws JanitorError {
        try {
            return getDataManager().callTransaction(function);
        } catch (DatabaseError e) {
            throw new JanitorError(e.getMessage(), e);
        }
    }

    /**
     * Adds a listener that is notified about inserts, updates and deletes.
     * @param listener the listener
     * @return a registration that can be used to remove the listener
     */
    public ListenerRegistration addChangeListener(final EntityChangeListener<T> listener) {
        return entityChangeListeners.add(listener);
    }

    /**
     * @return the dispatch table of the join records
     */
    public DispatchTable<T> getEntityDispatchTable() {
        return entityDispatchTable;
    }
}
