package com.eischet.dbxs.dialects;


import com.eischet.dbxs.SimplePreparedStatement;
import com.eischet.dbxs.metadata.DatabaseVersion;
import com.eischet.dbxs.statements.SelectStatement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;

/** Base class for database dialects, with defaults that suit most databases. */
public abstract class DatabaseDialectCommon implements DatabaseDialect {

    @Override
    public boolean limitAndOffsetRequiresOrderBy() {
        return false; // usually, no, I think
    }

    @Override
    public boolean canLimitAndOffset(final DatabaseVersion databaseVersion) {
        return false; // LATER: implement for other database types
    }

    @Override
    public @NotNull SelectStatement addLimitAndOffset(final @NotNull SelectStatement selectStatement) {
        return selectStatement;
    }

    @Override
    public @NotNull SimplePreparedStatement addLimitAndOffset(final @NotNull SimplePreparedStatement statement, final int limit, final int offset) throws SQLException {
        return statement;
    }

    /**
     * For dialects with the {@code LIMIT ? OFFSET ?} syntax (PostgreSQL, H2, MariaDB/MySQL, SQLite ...): appends the clause to the statement.
     * Use together with {@link #bindLimitThenOffset}, which binds the two parameters in this clause's order.
     */
    protected static @NotNull SelectStatement appendLimitThenOffset(final @NotNull SelectStatement selectStatement) {
        return new SelectStatement(selectStatement.getSql() + " LIMIT ? OFFSET ?");
    }

    /** Binds the parameters of {@link #appendLimitThenOffset}: first the limit, then the offset (the other way round than {@code OFFSET ? ROWS FETCH NEXT ? ROWS ONLY}). */
    protected static @NotNull SimplePreparedStatement bindLimitThenOffset(final @NotNull SimplePreparedStatement statement, final int limit, final int offset) throws SQLException {
        return statement.addInt(limit).addInt(offset);
    }

    @Override
    public @Nullable SelectStatement getNextValueQuery(final @Nullable String schema, final @NotNull String seq) {
        return null;
    }

    @Override
    public @Nullable SelectStatement getCurrentValueQuery(final @Nullable String schema, final @NotNull String seq) {
        return null;
    }
}
