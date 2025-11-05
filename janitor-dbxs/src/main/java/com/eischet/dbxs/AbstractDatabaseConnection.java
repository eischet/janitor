/*
 * © Eischet Software e.K., Köln
 */

package com.eischet.dbxs;

import com.eischet.dbxs.exceptions.DatabaseError;
import com.eischet.dbxs.results.ResultSetConsumer;
import com.eischet.dbxs.results.ResultSetReader;
import com.eischet.dbxs.statements.SelectStatement;
import com.eischet.dbxs.statements.UpdateStatement;
import com.eischet.janitor.toolbox.memory.Keeper;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.*;

public abstract class AbstractDatabaseConnection implements DatabaseConnection {

    @Override
    public long queryForLong(final @NotNull SelectStatement sql) throws DatabaseError {
        return queryForLong(sql, ps -> {});
    }

    @Override
    public long queryForLong(final @NotNull SelectStatement sql, final @NotNull StatementConfigurator sc) throws DatabaseError {
        return queryForList(sql, sc, rs -> rs.getLong(1)).stream().filter(Objects::nonNull).findFirst().orElse(0L);
    }


    @Override
    public Long queryForLongInstance(final @NotNull SelectStatement sql, final @NotNull StatementConfigurator sc) throws DatabaseError {
        return queryForList(sql, sc, rs -> rs.getLong(1)).stream().filter(Objects::nonNull).findFirst().orElse(null);
    }


    @Override
    public int queryForInt(final @NotNull SelectStatement sql) throws DatabaseError {
        return queryForInt(sql, ps -> {});
    }

    @Override
    public LocalDateTime queryForLocalDateTime(final @NotNull SelectStatement sql) throws DatabaseError {
        return queryForObject(sql, rs -> rs.getLocalDateTime(1));
    }

    @Override
    public LocalDateTime queryForLocalDateTime(final @NotNull SelectStatement sql, final @NotNull StatementConfigurator sc) throws DatabaseError {
        return queryForObject(sql, sc, rs -> rs.getLocalDateTime(1));
    }

    @Override
    public int queryForInt(final @NotNull SelectStatement sql, final @NotNull StatementConfigurator sc) throws DatabaseError {
        return queryForList(sql, sc, rs -> rs.getInt(1)).stream().filter(Objects::nonNull).findFirst().orElse(0);
    }

    @Override
    public Integer queryForInteger(final @NotNull SelectStatement sql, final @NotNull StatementConfigurator sc) throws DatabaseError {
        return queryForList(sql, sc, rs -> rs.getInt(1)).stream().filter(Objects::nonNull).findFirst().orElse(null);
    }

    @Override
    public String queryForString(final @NotNull SelectStatement sql) throws DatabaseError {
        return queryForString(sql, ps -> {});
    }

    @Override
    public String queryForString(final @NotNull SelectStatement sql, final @NotNull StatementConfigurator sc) throws DatabaseError {
        // .filter(Objects::nonNull) ist entscheidend wichtig, denn wenn der String NULL ist, gibt es sonst in findFirst() einen NPE!!!
        return queryForList(sql, sc, rs -> rs.getString(1)).stream().filter(Objects::nonNull).findFirst().orElse(null);
    }


    @Override
    public QuerySummary queryForEach(final @NotNull SelectStatement sql, @NotNull final ResultSetConsumer consumer) throws DatabaseError {
        return queryForEach(sql, ps -> {}, consumer);
    }

    @Override
    public <T> T queryForObject(final @NotNull SelectStatement sql, final @NotNull StatementConfigurator sc, final @NotNull ResultSetReader<T> reader) throws DatabaseError {
        final Keeper<T> result = new Keeper<>();
        queryForEach(sql, sc, rs -> result.setValue(reader.read(rs)));
        return result.getValue();
    }

    @Override
    public <T> T queryForObject(final @NotNull SelectStatement sql, final @NotNull ResultSetReader<T> reader) throws DatabaseError {
        return queryForObject(sql, ps -> {}, reader);
    }

    @Override
    public int update(final @NotNull UpdateStatement sql) throws DatabaseError {
        return update(sql, ps -> {});
    }

    @Override
    public <T> List<T> queryForList(final @NotNull SelectStatement sql, final @NotNull ResultSetReader<T> reader) throws DatabaseError {
        return queryForList(sql, ps -> {}, reader);
    }

    @Override
    public <T> List<T> queryForList(final @NotNull SelectStatement sql, final @NotNull StatementConfigurator sc, final @NotNull ResultSetReader<T> reader) throws DatabaseError {
        final List<T> results = new ArrayList<>();
        queryForEach(sql, sc, rs -> results.add(reader.read(rs)));
        return results;
    }

    @Override
    public <T> Set<T> queryForSet(final @NotNull SelectStatement sql, final @NotNull ResultSetReader<T> reader) throws DatabaseError {
        return queryForSet(sql, ps -> {}, reader);
    }

    @Override
    public <T> Set<T> queryForSet(final @NotNull SelectStatement sql, final @NotNull StatementConfigurator sc, final @NotNull ResultSetReader<T> reader) throws DatabaseError {
        final Set<T> results = new HashSet<>();
        queryForEach(sql, sc, rs -> results.add(reader.read(rs)));
        return results;
    }

}
