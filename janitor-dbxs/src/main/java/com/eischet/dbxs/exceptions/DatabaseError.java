/*
 * © Eischet Software e.K., Köln
 */

package com.eischet.dbxs.exceptions;

import com.eischet.dbxs.SimplePreparedStatement;
import com.eischet.dbxs.statements.GenericStatement;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Collectors;

public class DatabaseError extends Exception {

    public DatabaseError(final String message) {
        super(message);
    }

    public DatabaseError(final String message, final Throwable cause) {
        super(message, cause);
    }

    public DatabaseError(@Nullable String connectionName, final SimplePreparedStatement sps, final Throwable cause) {
        super(formatError(connectionName, sps), cause);
    }

    public DatabaseError(final GenericStatement stmt, final Throwable cause) {
        super("error in statement " + stmt, cause);
    }

    public DatabaseError(final Throwable cause) {
        super(cause);
    }

    private static String formatError(@Nullable String connectionName, final SimplePreparedStatement sps) {
        if (connectionName != null) {
            if (sps.getArgs() == null || sps.getArgs().isEmpty()) {
                return String.format("%s: error in statement %s", connectionName, sps.getStatement());
            } else {
                return String.format("%s: error in statement %s with arguments %s", connectionName, sps.getStatement(),
                        sps.getArgs().stream().map(Object::toString).collect(Collectors.joining("\n")));
            }

        } else {
            if (sps.getArgs() == null || sps.getArgs().isEmpty()) {
                return String.format("error in statement %s", sps.getStatement());
            } else {
                return String.format("error in statement %s with arguments %s", sps.getStatement(),
                        sps.getArgs().stream().map(Object::toString).collect(Collectors.joining("\n")));
            }
        }
    }

    /**
     * Returns true if the cause of this error is a timeout, e.g. caused by exceeding a query timeout.
     * @return true if the cause is a timeout
     */
    public boolean isTimeout() {
        return getCause() instanceof java.sql.SQLTimeoutException;
    }

}
