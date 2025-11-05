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

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Basic Database Operations.
 */
public interface DatabaseConnection {

    long queryForLong(@NotNull final SelectStatement sql) throws DatabaseError;
    long queryForLong(@NotNull final SelectStatement sql, @NotNull final StatementConfigurator sc) throws DatabaseError;
    int queryForInt(@NotNull final SelectStatement sql) throws DatabaseError;
    int queryForInt(@NotNull final SelectStatement sql, @NotNull final StatementConfigurator sc) throws DatabaseError;
    LocalDateTime queryForLocalDateTime(@NotNull final SelectStatement sql) throws DatabaseError;
    LocalDateTime queryForLocalDateTime(@NotNull final SelectStatement sql, @NotNull final StatementConfigurator sc) throws DatabaseError;
    Integer queryForInteger(@NotNull final SelectStatement sql, @NotNull final StatementConfigurator sc) throws DatabaseError;
    Long queryForLongInstance(@NotNull final SelectStatement sql, @NotNull final StatementConfigurator sc) throws DatabaseError;
    String queryForString(@NotNull final SelectStatement sql) throws DatabaseError;
    String queryForString(@NotNull final SelectStatement sql, @NotNull final StatementConfigurator sc) throws DatabaseError;
    <T> T queryForObject(@NotNull final SelectStatement sql, @NotNull final StatementConfigurator sc, @NotNull final ResultSetReader<T> reader) throws DatabaseError;
    <T> T queryForObject(@NotNull final SelectStatement sql, @NotNull final ResultSetReader<T> reader) throws DatabaseError;
    QuerySummary queryForEach(@NotNull final SelectStatement sql, @NotNull final StatementConfigurator sc, @NotNull final ResultSetConsumer consumer) throws DatabaseError;
    QuerySummary queryForEach(@NotNull final SelectStatement sql, @NotNull final ResultSetConsumer consumer) throws DatabaseError;

    QuerySummary queryForEach(@NotNull final SelectStatement sql, @NotNull final StatementConfigurator sc, @NotNull final ResultSetConsumer consumer, @Nullable final Long limit, @Nullable Flag abortFlag) throws DatabaseError;

    default <T> T queryForMappedObject(@NotNull final SelectStatementWithMapper<T> sql,
                                    @NotNull final StatementConfigurator sc) throws DatabaseError {
        return queryForObject(sql, sc, sql.getMapper());
    }

    default <T> void queryForEachObject(@NotNull final SelectStatementWithMapper<T> sql,
                                        @NotNull final DatabaseConsumer<T> consumer) throws DatabaseError {
        final ResultSetReader<T> mapper = sql.getMapper();
        queryForEach(sql, rs -> consumer.accept(mapper.read(rs)));
    }

    class QuerySummary {
        final long readRows;
        final boolean abortedOnLimit;
        private final boolean abortedOnFlag;

        public QuerySummary(final long readRows, final boolean abortedOnLimit, final boolean abortedOnFlag) {
            this.readRows = readRows;
            this.abortedOnLimit = abortedOnLimit;
            this.abortedOnFlag = abortedOnFlag;
        }

        public long getReadRows() {
            return readRows;
        }

        public boolean isAbortedOnLimit() {
            return abortedOnLimit;
        }

        public boolean isAbortedOnFlag() {
            return abortedOnFlag;
        }
    }




    default <T> void queryForEachObject(@NotNull final SelectStatementWithMapper<T> sql,
                                        @NotNull final StatementConfigurator sc,
                                        @NotNull final DatabaseConsumer<T> consumer) throws DatabaseError {
        final ResultSetReader<T> mapper = sql.getMapper();
        queryForEach(sql, sc, rs -> consumer.accept(mapper.read(rs)));
    }

    <T> T insertOneRowAndReturnGeneratedKey(@NotNull final UpdateStatement sql, @NotNull StatementConfigurator sc, @NotNull final ResultSetReader<T> consumer) throws DatabaseError;

    default <T> T insertOneRowAndReturnGeneratedKey(@NotNull final UpdateStatement sql, @NotNull final ResultSetReader<T> consumer) throws DatabaseError {
        return insertOneRowAndReturnGeneratedKey(sql, ps -> {}, consumer);
    }

    int update(@NotNull final UpdateStatement sql, @NotNull StatementConfigurator sc) throws DatabaseError;
    int update(@NotNull final UpdateStatement sql) throws DatabaseError;

    default <T> int update(@NotNull final UpdateStatementWithMapper<T> sql, final T value) throws DatabaseError {
        return update(sql, sql.getMapper(value));
    }


    default Integer queryForInteger(@NotNull final SelectStatement sql) throws DatabaseError {
        return queryForInteger(sql, stmt -> {});
    }

    @NotNull DatabaseDialect getDialect();

    Connection getJdbcConnection();

    static <T>  @NotNull T require(@Nullable T value) throws DatabaseError {
        if (value == null) {
            throw new DatabaseError("required object is null");
        } else {
            return value;
        }
    }

    <T> List<T> queryForList(@NotNull final SelectStatement sql, @NotNull final StatementConfigurator sc, @NotNull final ResultSetReader<T> reader) throws DatabaseError;
    <T> List<T> queryForList(@NotNull final SelectStatement sql, @NotNull final ResultSetReader<T> reader) throws DatabaseError;
    <T> Set<T> queryForSet(@NotNull final SelectStatement sql, @NotNull final StatementConfigurator sc, @NotNull final ResultSetReader<T> reader) throws DatabaseError;
    <T> Set<T> queryForSet(@NotNull final SelectStatement sql, @NotNull final ResultSetReader<T> reader) throws DatabaseError;

    default <T> List<T> queryForList(@NotNull final SelectStatementWithMapper<T> sql) throws DatabaseError {
        return queryForList(sql, sql.getMapper());
    }

    default <T> List<T> queryForMappedList(@NotNull final SelectStatementWithMapper<T> sql, final StatementConfigurator sc) throws DatabaseError {
        return queryForList(sql, sc, sql.getMapper());
    }


}
