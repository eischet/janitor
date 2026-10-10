/*
 * © Eischet Software e.K., Köln
 */

package com.eischet.dbxs.results;

import com.eischet.dbxs.exceptions.DatabaseError;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

/** Consumes each row of a result set. */
@FunctionalInterface
public interface ResultSetConsumer {
    /**
     * Consumes the current row.
     * @param rs the result set, positioned at the row
     * @throws SQLException on database errors
     * @throws DatabaseError on database errors
     */
    void consume(final @NotNull SimpleResultSet rs) throws SQLException, DatabaseError;
}
