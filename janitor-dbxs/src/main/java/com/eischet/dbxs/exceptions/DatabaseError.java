// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.dbxs.exceptions;

import com.eischet.dbxs.SimplePreparedStatement;
import com.eischet.dbxs.statements.GenericStatement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/**
 * Checked exception for all errors that occur while working with a database.
 * <p>
 * For a failed statement, the message is kept short enough for a log: what the database said, the statement in a shortened form, and its
 * arguments, labeled with their columns where possible. The full statement and the arguments are available from {@link #getSql()} and
 * {@link #getArguments()}.
 * </p>
 */
public class DatabaseError extends Exception {

    private final @Nullable String sql;
    private final @NotNull @Unmodifiable List<SimplePreparedStatement.Arg> arguments;

    public DatabaseError(final String message) {
        super(message);
        this.sql = null;
        this.arguments = List.of();
    }

    public DatabaseError(final String message, final Throwable cause) {
        super(message, cause);
        this.sql = null;
        this.arguments = List.of();
    }

    public DatabaseError(@Nullable String connectionName, final SimplePreparedStatement sps, final Throwable cause) {
        this(connectionName, sps.getStatement().getSql(), sps.getArgs(), cause);
    }

    public DatabaseError(final GenericStatement stmt, final Throwable cause) {
        this(null, stmt.getSql(), List.of(), cause);
    }

    public DatabaseError(final Throwable cause) {
        super(cause);
        this.sql = null;
        this.arguments = List.of();
    }

    private DatabaseError(final @Nullable String connectionName, final @NotNull String sql, final @NotNull List<SimplePreparedStatement.Arg> arguments, final Throwable cause) {
        super(StatementErrorFormat.format(connectionName, sql, arguments, cause), cause);
        this.sql = sql;
        this.arguments = List.copyOf(arguments);
    }

    /**
     * @return the full text of the statement that failed, or null if the error is not about a statement
     */
    public @Nullable String getSql() {
        return sql;
    }

    /**
     * @return the arguments that were bound to the statement that failed, up to the failure; empty if there were none, or if the error is
     * not about a statement
     */
    public @NotNull @Unmodifiable List<SimplePreparedStatement.Arg> getArguments() {
        return arguments;
    }

    /**
     * Returns true if the cause of this error is a timeout, e.g. caused by exceeding a query timeout.
     * @return true if the cause is a timeout
     */
    public boolean isTimeout() {
        return getCause() instanceof java.sql.SQLTimeoutException;
    }

}
