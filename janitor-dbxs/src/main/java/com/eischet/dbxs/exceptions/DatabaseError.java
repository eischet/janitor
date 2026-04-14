/*
 * © Eischet Software e.K., Köln
 */

package com.eischet.dbxs.exceptions;

import com.eischet.dbxs.SimplePreparedStatement;
import com.eischet.dbxs.statements.GenericStatement;

import java.util.stream.Collectors;

public class DatabaseError extends Exception {

    public DatabaseError(final String message) {
        super(message);
    }

    public DatabaseError(final String message, final Throwable cause) {
        super(message, cause);
    }

    public DatabaseError(final SimplePreparedStatement sps, final Throwable cause) {
        super(formatError(sps), cause);
    }

    public DatabaseError(final GenericStatement stmt, final Throwable cause) {
        super("error in statement " + stmt, cause);
    }

    public DatabaseError(final Throwable cause) {
        super(cause);
    }

    private static String formatError(final SimplePreparedStatement sps) {
        if (sps.getArgs() == null || sps.getArgs().isEmpty()) {
            return String.format("error in statement %s", sps.getStatement());
        } else {
            return String.format("error in statement %s with arguments %s", sps.getStatement(),
                sps.getArgs().stream().map(Object::toString).collect(Collectors.joining("\n")));
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
