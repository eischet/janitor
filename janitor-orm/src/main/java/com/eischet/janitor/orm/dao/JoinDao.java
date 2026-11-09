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
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JList;
import com.eischet.janitor.api.types.builtin.JMap;
import com.eischet.janitor.api.types.builtin.JNumber;
import com.eischet.janitor.api.types.builtin.JString;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.api.types.dispatch.Dispatcher;
import com.eischet.janitor.api.metadata.MetaDataMap;
import com.eischet.janitor.api.metadata.ScriptType;
import com.eischet.janitor.api.types.functions.JCallArgs;
import com.eischet.janitor.api.types.functions.JCallable;
import com.eischet.janitor.api.types.functions.tree.JInspectableFunction;
import com.eischet.janitor.logging.JanitorLogger;
import com.eischet.janitor.orm.meta.OrmPropertyHandle;
import com.eischet.janitor.orm.JanitorOrm;
import com.eischet.janitor.orm.entity.OrmEntity;
import com.eischet.janitor.orm.entity.OrmJoined;
import com.eischet.janitor.orm.filter.FilterExpression;
import com.eischet.janitor.orm.filter.FilterOperator;
import com.eischet.janitor.orm.filter.LambdaFilter;
import com.eischet.janitor.orm.filter.MalformedExpression;
import com.eischet.janitor.orm.filter.FilterFields;
import com.eischet.janitor.orm.filter.FilterRewrite;
import com.eischet.janitor.orm.filter.ScriptFilter;
import com.eischet.janitor.orm.ref.ForeignKey;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
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
        DISPATCH.addBooleanProperty("verbose", JoinDao::isVerbose, JoinDao::setVerbose)
                .setMetaData(Janitor.MetaData.HELP, "verbose: True if the DAO logs what it does in detail; writable.");
        DISPATCH.addStringProperty("tableName", dao -> dao.tableName)
                .setMetaData(Janitor.MetaData.HELP, "tableName: The name of the join table; read-only.");
        DISPATCH.addListProperty("columns", dao -> Janitor.list(dao.columns.stream().map(Janitor::string)))
                .setMetaData(Janitor.MetaData.HELP, "columns: The names of the database columns the DAO reads and writes, as a list of strings; read-only.");
        DISPATCH.addStringProperty("className", dao -> dao.className)
                .setMetaData(Janitor.MetaData.HELP, "className: The class name of the join records; read-only.");
        DISPATCH.addMethod("insert", JoinDao::insertForScript)
                .setMetaData(Janitor.MetaData.HELP, "insert(record): Inserts a join record, given as a record or a map, into the database and returns it.")
                .setMetaData(Janitor.MetaData.PARAMETERS, DaoScriptTypes.ONE_ENTITY)
                .setMetaData(Janitor.MetaData.RETURNS, ScriptType.ENTITY);
        DISPATCH.addMethod("update", JoinDao::updateForScript)
                .setMetaData(Janitor.MetaData.HELP, "update(record): Updates a join record, given as a record or a map, in the database and returns it.")
                .setMetaData(Janitor.MetaData.PARAMETERS, DaoScriptTypes.ONE_ENTITY)
                .setMetaData(Janitor.MetaData.RETURNS, ScriptType.ENTITY);
        DISPATCH.addMethod("merge", JoinDao::mergeForScript)
                .setMetaData(Janitor.MetaData.HELP, "merge(record): Updates a join record, given as a record or a map, or inserts it if it does not exist yet; returns it.")
                .setMetaData(Janitor.MetaData.PARAMETERS, DaoScriptTypes.ONE_ENTITY)
                .setMetaData(Janitor.MetaData.RETURNS, ScriptType.ENTITY);
        DISPATCH.addVoidMethod("delete", JoinDao::deleteForScript)
                .setMetaData(Janitor.MetaData.HELP, "delete(record): Deletes a join record, given as a record or a map, from the database.")
                .setMetaData(Janitor.MetaData.PARAMETERS, DaoScriptTypes.ONE_ENTITY);
        DISPATCH.addStringProperty("jsonSchema", self -> Janitor.current().writeJson(self.entityDispatchTable::writeSchemaToJson))
                .setMetaData(Janitor.MetaData.HELP, "jsonSchema: The JSON schema of the join records as a string; read-only.");
        DISPATCH.addMethod("select", JoinDao::scriptSelect)
                .setMetaData(Janitor.MetaData.HELP, "select(condition): Returns the list of the join records for which the condition, a lambda like 'link -> link.reason.key == \"A\" and link.ticket.status == \"o\"' or a filter map like {field: \"reason.key\", operator: \"eq\", valueString: \"A\"}, holds; runs it as a query.")
                .setMetaData(Janitor.MetaData.PARAMETERS, DaoScriptTypes.CONDITION)
                .setMetaData(Janitor.MetaData.RETURNS, DaoScriptTypes.ENTITIES);
        DISPATCH.addMethod("count", JoinDao::scriptCount)
                .setMetaData(Janitor.MetaData.HELP, "count(condition): Returns the number of the join records for which the condition, a lambda or a filter map like for select, holds; runs it as a query.")
                .setMetaData(Janitor.MetaData.PARAMETERS, DaoScriptTypes.CONDITION)
                .setMetaData(Janitor.MetaData.RETURNS, ScriptType.INT);
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

    // The SQL that only depends on the table, the columns and the dialect is built once, in the constructor, so that a DAO that cannot work fails
    // early instead of on first use. Statement objects are immutable and can be shared between threads.
    private final @NotNull @Unmodifiable List<String> updateColumns;
    private final @NotNull UpdateStatement insertStatement;
    /** Null if the table consists only of primary key columns, i.e. there is nothing to update; {@link #merge} and {@link #update} handle that case. */
    private final @Nullable UpdateStatement updateStatement;
    private final @NotNull UpdateStatement deleteStatement;
    private final @NotNull SelectStatement countStatement;
    private final @NotNull String selectAllSql;
    private final @NotNull String countAllSql;
    private final @NotNull String orderByPrimaryKey;
    private final Map<String, SelectStatement> selectByColumn = new ConcurrentHashMap<>();
    private final @NotNull @Unmodifiable Map<String, ColumnMapping<T>> mappingForColumn;
    /** The mappings of {@link #columns}, in that order. */
    private final @NotNull ColumnMapping<T>[] columnMappings;

    /**
     * @return the dialect of the database, as defined by the collection that this DAO belongs to
     */
    protected final @NotNull DatabaseDialect getDialect() {
        return collection.getDialect();
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

    /**
     * The dispatcher that a DAO with these script methods has: those of every join DAO and these. It is the same for every
     * instance of a DAO class, so it can describe the class, e.g. for editors (see {@link DaoProperties}).
     *
     * @param daoMethods the script methods of the DAO class, as handed to the constructor
     * @return the dispatcher of the DAOs
     */
    public static @NotNull Dispatcher<?> dispatcherFor(final @NotNull Dispatcher<?> daoMethods) {
        return Dispatcher.inherit(DISPATCH, daoMethods);
    }

    /**
     * Like {@link #dispatcherFor(Dispatcher)}, and names the dispatcher of the join records, so that editors know what
     * {@code select} returns and what its lambda gets ({@link Janitor.MetaData#ENTITY}).
     *
     * @param daoMethods the script methods of the DAO class, as handed to the constructor
     * @param records    the dispatch table of the join records
     * @return the dispatcher of the DAOs
     */
    public static @NotNull Dispatcher<?> dispatcherFor(final @NotNull Dispatcher<?> daoMethods, final @NotNull Dispatcher<?> records) {
        final MetaDataMap metaData = new MetaDataMap();
        metaData.put(Janitor.MetaData.ENTITY, records);
        return Dispatcher.inherit(DISPATCH, daoMethods, metaData);
    }

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

        final Map<String, ColumnMapping<T>> mappings = new HashMap<>();
        for (final String column : columns) {
            mappings.put(column, ColumnMapping.of(entityDispatchTable, fieldForColumn.get(column), column, collection.getNullReferenceId()));
        }
        this.mappingForColumn = Map.copyOf(mappings);
        @SuppressWarnings("unchecked") final ColumnMapping<T>[] mapped = new ColumnMapping[columns.size()];
        for (int i = 0; i < mapped.length; i++) {
            mapped[i] = mappings.get(columns.get(i));
        }
        this.columnMappings = mapped;

        final StatementCreator creator = new StatementCreator(getDialect());
        this.updateColumns = columns.stream().filter(col -> !primaryKeyColumns.contains(col)).toList();
        this.insertStatement = UpdateStatement.of(creator.createInsertStatement(tableName, columns));
        this.updateStatement = updateColumns.isEmpty() ? null : UpdateStatement.of(creator.createUpdateStatement(tableName, updateColumns, primaryKeyColumns));
        this.deleteStatement = UpdateStatement.of(creator.createDeleteStatement(tableName, primaryKeyColumns));
        this.countStatement = SelectStatement.of(creator.createCountStatement(tableName, columns));
        this.selectAllSql = creator.createSelectAllStatement(tableName, columns);
        this.countAllSql = creator.createCountStatement(tableName);
        this.orderByPrimaryKey = "order by " + String.join(", ", primaryKeyColumns.stream().map(getDialect()::quoteColumn).toList());

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
        for (int i = 0; i < columnMappings.length; i++) {
            final ColumnMapping<T> mapping = columnMappings[i];
            try {
                final JanitorObject propertyValue = Objects.requireNonNull(mapping.handler().lookupAttribute(value));
                if (propertyValue instanceof JAssignable assignableProperty) {
                    CommonDao.readColumn(collection, conn, mapping, assignableProperty, rs);
                } else {
                    throw new DatabaseError("invalid field '" + mapping.field() + "' / column '" + mapping.column() + "' is not assignable");
                }
            } catch (SQLException e) {
                log.warn("SQL exception on class '{}', column #{} = '{}', field '{}', type hint '{}', column order: {}", className, i + 1, mapping.column(), mapping.field(), mapping.type(), columns, e);
                final String message = String.format("SQL exception on class '%s', column '%s', field '%s', type hint '%s'", className, mapping.column(), mapping.field(), mapping.type());
                throw new DatabaseError(message, e);
            } catch (Exception e) {
                throw new DatabaseError("invalid field '" + mapping.field() + "' caused an exception", e);
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
        final SelectStatement select = selectByColumn.computeIfAbsent(columnName, c -> SelectStatement.of(new StatementCreator(getDialect()).createSelectStatement(tableName, columns, c)));
        return conn.queryForList(select, stmt -> stmt.addLong(id), rs -> readAllProperties(conn, rs));
    }



    /**
     * Finds all join records where a column has the given value, e.g. all memberships of one user.
     * @param conn the database connection
     * @param columnName the column
     * @param id the value
     * @return the join records
     * @throws DatabaseError on database errors
     */
    public @NotNull @Unmodifiable List<T> findByColumnValue(final @NotNull DatabaseConnection conn, final @NotNull String columnName, final long id) throws DatabaseError {
        return List.copyOf(findByColumn(conn, columnName, id));
    }

    /**
     * Finds the join records that match a filter expression, ordered by the primary key. Fields of the join records are compared directly,
     * paths like "ticket.status" through a reference with a sub-query on the referenced entity's table, as in {@link GenericDao}.
     * @param conn the database connection
     * @param filterExpression the filter
     * @param queryTimeout the query timeout in seconds, or null for no timeout
     * @return the join records
     * @throws DatabaseError on database errors, including a query that ran into its timeout
     * @throws MalformedExpression if the filter names unknown fields or cannot be turned into SQL
     */
    public @NotNull @Unmodifiable List<T> findByFilter(final @NotNull DatabaseConnection conn, final @NotNull FilterExpression filterExpression,
                                                       final @Nullable Integer queryTimeout) throws DatabaseError {
        final List<Prepper> preppers = new ArrayList<>();
        @Language("SQL") final String sql = selectAllSql + "\nWHERE\n  " + expressionToSql(filterExpression, preppers::add) + "\n" + orderByPrimaryKey;
        if (verbose) {
            log.info("findByFilter, sql: {}, preppers: {}", sql, preppers);
        }
        return List.copyOf(conn.queryForList(new SelectStatement(sql), prepare(conn, preppers, queryTimeout), rs -> readAllProperties(conn, rs)));
    }

    /**
     * Counts the join records that match a filter expression, like {@link #findByFilter}.
     * @param conn the database connection
     * @param filterExpression the filter
     * @param queryTimeout the query timeout in seconds, or null for no timeout
     * @return the number of join records
     * @throws DatabaseError on database errors, including a query that ran into its timeout
     * @throws MalformedExpression if the filter names unknown fields or cannot be turned into SQL
     */
    public int countByFilter(final @NotNull DatabaseConnection conn, final @NotNull FilterExpression filterExpression,
                             final @Nullable Integer queryTimeout) throws DatabaseError {
        final List<Prepper> preppers = new ArrayList<>();
        @Language("SQL") final String sql = countAllSql + "\nWHERE\n  " + expressionToSql(filterExpression, preppers::add);
        if (verbose) {
            log.info("countByFilter, sql: {}, preppers: {}", sql, preppers);
        }
        return conn.queryForInt(new SelectStatement(sql), prepare(conn, preppers, queryTimeout));
    }

    private static @NotNull StatementConfigurator prepare(final @NotNull DatabaseConnection conn, final @NotNull List<Prepper> preppers,
                                                          final @Nullable Integer queryTimeout) {
        return stmt -> {
            if (queryTimeout != null) {
                stmt.setQueryTimeout(queryTimeout);
            }
            for (final Prepper prepper : preppers) {
                prepper.prepare(conn, stmt);
            }
        };
    }

    /**
     * Converts a filter expression to an SQL condition on the join table, collecting the preppers that bind its values.
     * Conditions on pseudo fields are rewritten first, see {@link FilterRewrite}.
     * @param filterExpression the expression
     * @param prepperConsumer receives the preppers, in the order of the parameters in the SQL
     * @return the SQL condition
     * @throws MalformedExpression if the expression is invalid
     */
    protected String expressionToSql(final @NotNull FilterExpression filterExpression, final @NotNull Consumer<Prepper> prepperConsumer) throws MalformedExpression {
        final @Nullable String field = filterExpression.getField();
        if (field != null && FilterSql.INVALID_FIELD.test(field)) {
            throw new MalformedExpression("invalid field '" + field + "'");
        }
        if (filterExpression.isGroup()) {
            return filterExpression.getFilters().stream()
                    .map((FilterExpression element) -> expressionToSql(element, prepperConsumer))
                    .collect(Collectors.joining(" " + filterExpression.getLogic() + " ", "(", ")"));
        }
        if (!filterExpression.isExpression() || field == null) {
            throw new MalformedExpression("part is neither group nor expression");
        }
        final int dot = field.indexOf('.');
        final @Nullable FilterRewrite rewrite = dot >= 0 ? null : FilterFields.find(entityDispatchTable, field);
        if (rewrite != null) {
            return expressionToSql(rewrite.rewrite(filterExpression), prepperConsumer);
        }
        if (dot >= 0) {
            final String reference = field.substring(0, dot);
            final GenericDao<?, ?> referencedDao = FilterSql.referencedDao(collection, columnForField, mappingForColumn, reference, field);
            return FilterSql.pathToSql(getDialect(), columnForField.get(reference), referencedDao, filterExpression, prepperConsumer);
        }
        final String column = columnForField.get(field);
        if (column == null) {
            throw new MalformedExpression("missing column for field '" + field + "'");
        }
        final @Nullable FilterOperator operator = filterExpression.getOperator();
        if (operator == null) {
            throw new MalformedExpression("missing operator in expression " + filterExpression);
        }
        final ColumnMapping<T> mapping = Objects.requireNonNull(mappingForColumn.get(column));
        final String quotedColumn = getDialect().quoteColumn(column);
        final @Nullable String nullReferenceCondition = FilterSql.nullReferenceCondition(operator, quotedColumn, mapping.nullReferenceId());
        if (nullReferenceCondition != null) {
            return nullReferenceCondition;
        }
        final @Nullable ColumnCase columnCase = entityDispatchTable.getMetaData(field, JanitorOrm.MetaData.COLUMN_CASE);
        return FilterSql.applyToColumn(getDialect(), filterExpression, quotedColumn, mapping.type(), columnCase, FilterSql::prepperFor, prepperConsumer);
    }

    /**
     * Checks whether a field of the join records, or a path like "ticket.status" through a reference, can be used in a filter expression.
     * @param path the field or path
     * @return true if the field exists
     */
    public boolean isFilterField(final @NotNull String path) {
        final int dot = path.indexOf('.');
        if (dot < 0) {
            return columnForField.containsKey(path) || FilterFields.find(entityDispatchTable, path) != null;
        }
        try {
            return FilterSql.referencedDao(collection, columnForField, mappingForColumn, path.substring(0, dot), path).isFilterField(path.substring(dot + 1));
        } catch (MalformedExpression e) {
            return false;
        }
    }

    /**
     * Converts a script lambda to a filter expression on the fields of the join records, see {@link LambdaFilter}.
     * @param process the running script process
     * @param function the lambda, whose parameter is the join record
     * @return the filter expression
     * @throws MalformedExpression if the lambda cannot be used as a filter
     * @throws JanitorRuntimeException if evaluating a value of the lambda fails
     */
    public @NotNull FilterExpression filterFromLambda(final @NotNull JanitorScriptProcess process, final @NotNull JInspectableFunction function) throws JanitorRuntimeException {
        return LambdaFilter.toFilter(process, function, this::isFilterField);
    }

    /**
     * Script method {@code dao.select(link -> condition)}: finds the join records for which the condition holds, by running it as a query.
     * The condition is a lambda (see {@link LambdaFilter}), a filter map or a filter expression, see {@link ScriptFilter}.
     * @param process the running script process
     * @param arguments the call arguments
     * @return a list of the join records
     * @throws JanitorRuntimeException if the condition cannot be used as a query, or the query fails
     */
    public JanitorObject scriptSelect(final @NotNull JanitorScriptProcess process, final @NotNull JCallArgs arguments) throws JanitorRuntimeException {
        final FilterExpression filter = scriptFilter(process, arguments);
        final Integer queryTimeout = GenericDao.queryTimeoutFor(process);
        try {
            final List<T> found = getDataManager().callTransaction(conn -> findByFilter(conn, filter, queryTimeout));
            return Janitor.list(found);
        } catch (DatabaseError e) {
            throw new JanitorNativeException(process, e.getMessage(), e);
        }
    }

    /**
     * Script method {@code dao.count(link -> condition)}: counts the join records for which the condition holds, like {@link #scriptSelect}.
     * @param process the running script process
     * @param arguments the call arguments
     * @return the number of join records
     * @throws JanitorRuntimeException if the condition cannot be used as a query, or the query fails
     */
    public JanitorObject scriptCount(final @NotNull JanitorScriptProcess process, final @NotNull JCallArgs arguments) throws JanitorRuntimeException {
        final FilterExpression filter = scriptFilter(process, arguments);
        final Integer queryTimeout = GenericDao.queryTimeoutFor(process);
        try {
            return Janitor.integer(getDataManager().callTransaction(conn -> countByFilter(conn, filter, queryTimeout)));
        } catch (DatabaseError e) {
            throw new JanitorNativeException(process, e.getMessage(), e);
        }
    }

    private @NotNull FilterExpression scriptFilter(final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        final JanitorObject argument = arguments.require(1).get(0).janitorUnpack();
        try {
            final FilterExpression filter = ScriptFilter.toFilter(process, argument, this::isFilterField, "link -> link.reason.key == \"A\"");
            expressionToSql(filter, prepper -> { }); // reports what cannot be a query before the query runs
            return filter;
        } catch (MalformedExpression e) {
            throw new JanitorArgumentException(process, arguments.getFunctionName() + ": " + e.getMessage(), e);
        }
    }

    /**
     * @return a new, empty join record, as {@code dao.insert(map)} starts from in scripts
     */
    public @NotNull T newInstance() {
        return newValue.get();
    }

    /**
     * @return the columns that make up the primary key of the join table
     */
    public @NotNull @Unmodifiable List<String> getPrimaryKeyColumns() {
        return primaryKeyColumns;
    }

    /**
     * @param field the name of a field of the join records
     * @return the column that the field is mapped to, or null if there is none (also: not in the schema version of the collection)
     */
    public @Nullable String getColumnForField(final @NotNull String field) {
        return columnForField.get(field);
    }

    /**
     * Inserts a join record.
     * @param conn the database connection
     * @param record the join record
     * @throws DatabaseError if the insert fails, or does not affect exactly one row
     */
    public void insert(@NotNull DatabaseConnection conn, @NotNull T record) throws DatabaseError {
        final List<String> insertingColumns = columns;
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
        final List<String> updatingColumns = updateColumns;
        if (updatingColumns.isEmpty()) {
            final List<String> countingColumns = columns;
            final int count = conn.queryForInt(countStatement, ps -> writeAllColumns(conn, record, countingColumns, ps));
            log.info("merge: searching via {} -> {} rows", countStatement.getSql(), count);
            if (count == 0) {
                insert(conn, record);
            }
        } else {
            final UpdateStatement updateStatement = Objects.requireNonNull(this.updateStatement);
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
        final List<String> updatingColumns = updateColumns;
        if (updatingColumns.isEmpty()) {
            log.warn("update is meaningless on a collection that consists only of its primary key columns - you probably want to call merge directly, and I'm calling it for you");
            merge(conn, record);
            return;
        }
        final UpdateStatement updateStatement = Objects.requireNonNull(this.updateStatement);
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
        final UpdateStatement updateStatement = deleteStatement;
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
        collection.checkScriptWrite(process);
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
        collection.checkScriptWrite(process);
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
        collection.checkScriptWrite(process);
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
        collection.checkScriptWrite(process);
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
