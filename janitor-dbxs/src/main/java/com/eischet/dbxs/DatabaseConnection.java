/*
 * © Eischet Software e.K., Köln
 */

package com.eischet.dbxs;

import com.eischet.dbxs.dialects.DatabaseDialect;
import com.eischet.dbxs.exceptions.DatabaseError;
import com.eischet.dbxs.results.ResultSetConsumer;
import com.eischet.dbxs.results.ResultSetReader;
import com.eischet.dbxs.statements.SelectStatement;
import com.eischet.dbxs.statements.SelectStatementWithMapper;
import com.eischet.dbxs.statements.UpdateStatement;
import com.eischet.dbxs.statements.UpdateStatementWithMapper;
import com.eischet.janitor.toolbox.memory.Flag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Basic Database Operations.
 */
public interface DatabaseConnection {

    /**
     * Retrieve a long value from the database.
     * This will only look at the first column of the first row.
     * NULL will be returned as 0.
     *
     * @param sql the query
     * @return the long value
     * @throws DatabaseError on errors
     */
    long queryForLong(@NotNull final SelectStatement sql) throws DatabaseError;

    /**
     * Retrieve a long value from the database.
     * This will only look at the first column of the first row.
     * NULL will be returned as 0.
     *
     * @param sql the query
     * @param sc the statement configurator
     * @return the long value
     * @throws DatabaseError on errors
     */
    long queryForLong(@NotNull final SelectStatement sql,
                      @NotNull final StatementConfigurator sc) throws DatabaseError;

    /**
     * Retrieve an integer value from the database.
     * This will only look at the first column of the first row.
     * NULL will be returned as 0.
     *
     * @param sql the query
     * @return the integer value
     * @throws DatabaseError on errors
     */
    int queryForInt(@NotNull final SelectStatement sql) throws DatabaseError;

    /**
     * Retrieve an integer value from the database.
     * This will only look at the first column of the first row.
     * NULL will be returned as 0.
     *
     * @param sql the query
     * @param sc the statement configurator
     * @return the integer value
     * @throws DatabaseError on errors
     */
    int queryForInt(@NotNull final SelectStatement sql,
                    @NotNull final StatementConfigurator sc) throws DatabaseError;

    /**
     * Retrieve a LocalDateTime value from the database.
     * This will only look at the first column of the first row.
     *
     * @param sql the query
     * @return the LocalDateTime value or null if the value is null
     * @throws DatabaseError on errors
     */
    @Nullable LocalDateTime queryForLocalDateTime(@NotNull final SelectStatement sql) throws DatabaseError;

    /**
     * Retrieve a LocalDateTime value from the database.
     * This will only look at the first column of the first row.
     *
     * @param sql the query
     * @param sc the statement configurator
     * @return the LocalDateTime value or null if the value is null
     * @throws DatabaseError on errors
     */
    @Nullable LocalDateTime queryForLocalDateTime(@NotNull final SelectStatement sql,
                                                  @NotNull final StatementConfigurator sc) throws DatabaseError;

    /**
     * Retrieve an Integer value from the database.
     * This will only look at the first column of the first row.
     *
     * @param sql the query
     * @param sc the statement configurator
     * @return the Integer value or null if the value is null
     * @throws DatabaseError on errors
     */
    @Nullable Integer queryForInteger(@NotNull final SelectStatement sql,
                                      @NotNull final StatementConfigurator sc) throws DatabaseError;

    /**
     * Retrieve a Long value from the database.
     * This will only look at the first column of the first row.
     *
     * @param sql the query
     * @param sc the statement configurator
     * @return the Long value or null if the value is null
     * @throws DatabaseError on errors
     */
    @Nullable Long queryForLongInstance(@NotNull final SelectStatement sql,
                                        @NotNull final StatementConfigurator sc) throws DatabaseError;

    /**
     * Retrieve a String value from the database.
     * This will only look at the first column of the first row.
     *
     * @param sql the query
     * @return the String value or null if the value is null
     * @throws DatabaseError on errors
     */
    @Nullable String queryForString(@NotNull final SelectStatement sql) throws DatabaseError;

    /**
     * Retrieve a String value from the database.
     * This will only look at the first column of the first row.
     *
     * @param sql the query
     * @param sc the statement configurator
     * @return the String value or null if the value is null
     * @throws DatabaseError on errors
     */
    @Nullable String queryForString(@NotNull final SelectStatement sql,
                                    @NotNull final StatementConfigurator sc) throws DatabaseError;

    /**
     * Retrieve an object from the database.
     * This will only look at the first column of the first row.
     *
     * @param sql the query
     * @param sc the statement configurator
     * @param reader the reader to use to convert the result set to an object
     * @return the object or null if the value is null
     * @param <T> the type of the object
     * @throws DatabaseError on errors
     */
    @Nullable <T> T queryForObject(@NotNull final SelectStatement sql,
                                   @NotNull final StatementConfigurator sc,
                                   @NotNull final ResultSetReader<T> reader) throws DatabaseError;

    /**
     * Retrieve an object from the database.
     * This will only look at the first column of the first row.
     *
     * @param sql the query
     * @param reader the reader to use to convert the result set to an object
     * @return the object or null if the value is null
     * @param <T> the type of the object
     * @throws DatabaseError on errors
     */
    @Nullable <T> T queryForObject(@NotNull final SelectStatement sql,
                                   @NotNull final ResultSetReader<T> reader) throws DatabaseError;

    /**
     * Query the database, calling the consumer for each row.
     *
     * @param sql the query
     * @param sc the statement configurator
     * @param consumer the consumer to use to process the result set
     * @return the query summary
     * @throws DatabaseError on errors
     */
    @NotNull QuerySummary queryForEach(@NotNull final SelectStatement sql,
                                       @NotNull final StatementConfigurator sc,
                                       @NotNull final ResultSetConsumer consumer) throws DatabaseError;

    /**
     * Query the database, calling the consumer for each row.
     *
     * @param sql the query
     * @param consumer the consumer to use to process the result set
     * @return the query summary
     * @throws DatabaseError on errors
     */
    @NotNull QuerySummary queryForEach(@NotNull final SelectStatement sql,
                                       @NotNull final ResultSetConsumer consumer) throws DatabaseError;

    /**
     * Query the database, calling the consumer for each row.
     *
     * @param sql the query
     * @param sc the statement configurator
     * @param consumer the consumer to use to process the result set
     * @param limit the maximum number of rows to return
     * @param abortFlag the flag to use to abort the query
     * @return the query summary
     * @throws DatabaseError on errors
     */
    @NotNull QuerySummary queryForEach(@NotNull final SelectStatement sql,
                                       @NotNull final StatementConfigurator sc,
                                       @NotNull final ResultSetConsumer consumer,
                                       @Nullable final Long limit,
                                       @Nullable Flag abortFlag) throws DatabaseError;

    /**
     * Retrieve an object from the database.
     * This will only look at the first row.
     *
     * @param sql the query
     * @param sc the statement configurator
     * @return the object
     * @param <T> the type of the object
     * @throws DatabaseError on errors
     */
    @Nullable default <T> T queryForMappedObject(@NotNull final SelectStatementWithMapper<T> sql,
                                                 @NotNull final StatementConfigurator sc) throws DatabaseError {
        return queryForObject(sql, sc, sql.getMapper());
    }

    /**
     * Query the database, calling the consumer for each row, with the mapped object.
     * @param sql the query
     * @param consumer the consumer
     * @param <T> the type of the object
     * @throws DatabaseError on errors
     */
    default <T> void queryForEachObject(@NotNull final SelectStatementWithMapper<T> sql,
                                        @NotNull final DatabaseConsumer<T> consumer) throws DatabaseError {
        final ResultSetReader<T> mapper = sql.getMapper();
        queryForEach(sql, rs -> consumer.accept(mapper.read(rs)));
    }

    /**
     * Query Summary: Provides information about a query.
     */
    class QuerySummary {
        final long readRows;
        final boolean abortedOnLimit;
        private final boolean abortedOnFlag;

        /**
         * Create a new QuerySummary.
         * @param readRows the number of rows read
         * @param abortedOnLimit whether the query was aborted due to a limit
         * @param abortedOnFlag whether the query was aborted due to a flag
         */
        public QuerySummary(final long readRows,
                            final boolean abortedOnLimit,
                            final boolean abortedOnFlag) {
            this.readRows = readRows;
            this.abortedOnLimit = abortedOnLimit;
            this.abortedOnFlag = abortedOnFlag;
        }

        /**
         * Get the number of rows read.
         * @return the number of rows read
         */
        public long getReadRows() {
            return readRows;
        }

        /**
         * Return whether the query was aborted due to a limit.
         * @return whether the query was aborted due to a limit
         */
        public boolean isAbortedOnLimit() {
            return abortedOnLimit;
        }

        /**
         * Return whether the query was aborted due to a flag.
         * @return whether the query was aborted due to a flag
         */
        public boolean isAbortedOnFlag() {
            return abortedOnFlag;
        }
    }


    /**
     * Run the query, configured by the statement configurator, pass each row through the statement's mapper, and then
     * feed each value object to the consumer.
     *
     * @param sql the query
     * @param sc the statement configurator
     * @param consumer the row value consumer
     * @param <T> the type of the row's value object
     * @throws DatabaseError on errors
     */
    default <T> void queryForEachObject(@NotNull final SelectStatementWithMapper<T> sql,
                                        @NotNull final StatementConfigurator sc,
                                        @NotNull final DatabaseConsumer<T> consumer) throws DatabaseError {
        final ResultSetReader<T> mapper = sql.getMapper();
        queryForEach(sql, sc, rs -> consumer.accept(mapper.read(rs)));
    }

    /**
     * Insert a single row and retrieve the generated key.
     *
     * @param sql the query
     * @param sc the statement configurator
     * @param consumer the row value consumer
     * @return the generated key
     * @param <T> the type of the row's value object
     * @throws DatabaseError on errors
     */
    <T> T insertOneRowAndReturnGeneratedKey(@NotNull final UpdateStatement sql,
                                            @NotNull StatementConfigurator sc,
                                            @NotNull final ResultSetReader<T> consumer) throws DatabaseError;

    /**
     * Insert a single row and retrieve the generated key.
     *
     * @param sql the query
     * @param consumer the row value consumer
     * @return the generated key
     * @param <T> the type of the row's value object
     * @throws DatabaseError on errors
     */
    default <T> T insertOneRowAndReturnGeneratedKey(@NotNull final UpdateStatement sql,
                                                    @NotNull final ResultSetReader<T> consumer) throws DatabaseError {
        return insertOneRowAndReturnGeneratedKey(sql, ps -> {}, consumer);
    }

    /**
     * Execute an update statement.
     *
     * @param sql the update statement
     * @param sc the statement configurator
     * @return the number of affected rows
     * @throws DatabaseError on errors
     */
    int update(@NotNull final UpdateStatement sql,
               @NotNull StatementConfigurator sc) throws DatabaseError;

    /**
     * Execute an update statement.
     *
     * @param sql the update statement
     * @return the number of affected rows
     * @throws DatabaseError on errors
     */
    int update(@NotNull final UpdateStatement sql) throws DatabaseError;

    /**
     * Execute an update statement with a value object.
     *
     * @param sql the update statement
     * @param value the value object
     * @return the number of affected rows
     * @param <T> the type of the value object
     * @throws DatabaseError on errors
     */
    default <T> int update(@NotNull final UpdateStatementWithMapper<T> sql, final T value) throws DatabaseError {
        return update(sql, sql.getMapper(value));
    }

    /**
     * Execute a query statement and return the first column of the first row as an integer.
     *
     * @param sql the query statement
     * @return the integer value, or null if no rows were returned
     * @throws DatabaseError on errors
     */
    default @Nullable Integer queryForInteger(@NotNull final SelectStatement sql) throws DatabaseError {
        return queryForInteger(sql, stmt -> {});
    }

    /**
     * Get the database dialect.
     *
     * @return the database dialect
     */
    @NotNull DatabaseDialect getDialect();

    /**
     * Get the JDBC connection.
     *
     * @return the JDBC connection
     */
    @NotNull Connection getJdbcConnection();

    /**
     * Require a non-null value.
     *
     * @param value the value
     * @param <T> the type of the value
     * @return the value
     * @throws DatabaseError if the value is null
     */
    static <T>  @NotNull T require(@Nullable T value) throws DatabaseError {
        if (value == null) {
            throw new DatabaseError("required object is null");
        } else {
            return value;
        }
    }

    /**
     * Query for a list of objects.
     *
     * @param sql the select statement
     * @return the list of objects
     * @param <T> the type of the objects
     * @throws DatabaseError if an error occurs
     */
    default <T> List<T> queryForList(@NotNull final SelectStatementWithMapper<T> sql) throws DatabaseError {
        return queryForList(sql, sql.getMapper());
    }

    /**
     * Query for a list of objects.
     *
     * @param sql the select statement
     * @param sc the statement configurator
     * @return the list of objects
     * @param <T> the type of the objects
     * @throws DatabaseError if an error occurs
     */
    default <T> List<T> queryForMappedList(@NotNull final SelectStatementWithMapper<T> sql,
                                           final StatementConfigurator sc) throws DatabaseError {
        return queryForList(sql, sc, sql.getMapper());
    }

    /**
     * Query for a list of objects.
     *
     * @param sql the select statement
     * @param sc the statement configurator
     * @param reader the result set reader
     * @return the list of objects
     * @param <T> the type of the objects
     * @throws DatabaseError if an error occurs
     */
    @NotNull @Unmodifiable
    <T> List<T> queryForList(@NotNull final SelectStatement sql,
                             @NotNull final StatementConfigurator sc,
                             @NotNull final ResultSetReader<T> reader) throws DatabaseError;

    /**
     * Query for a list of objects.
     *
     * @param sql the select statement
     * @param reader the result set reader
     * @return the list of objects
     * @param <T> the type of the objects
     * @throws DatabaseError if an error occurs
     */
    @NotNull @Unmodifiable
    <T> List<T> queryForList(@NotNull final SelectStatement sql,
                             @NotNull final ResultSetReader<T> reader) throws DatabaseError;

    /**
     * Query for a set of objects.
     *
     * @param sql the select statement
     * @param sc the statement configurator
     * @param reader the result set reader
     * @return the set of objects
     * @param <T> the type of the objects
     * @throws DatabaseError if an error occurs
     */
    @NotNull @Unmodifiable
    <T> Set<T> queryForSet(@NotNull final SelectStatement sql,
                           @NotNull final StatementConfigurator sc,
                           @NotNull final ResultSetReader<T> reader) throws DatabaseError;

    /**
     * Query for a set of objects.
     *
     * @param sql the select statement
     * @param reader the result set reader
     * @return the set of objects
     * @param <T> the type of the objects
     * @throws DatabaseError if an error occurs
     */
    @NotNull @Unmodifiable
    <T> Set<T> queryForSet(@NotNull final SelectStatement sql,
                           @NotNull final ResultSetReader<T> reader) throws DatabaseError;



}
