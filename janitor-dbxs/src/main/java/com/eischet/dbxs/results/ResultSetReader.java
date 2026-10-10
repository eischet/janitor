/*
 * © Eischet Software e.K., Köln
 */

package com.eischet.dbxs.results;

import com.eischet.dbxs.exceptions.DatabaseError;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

/**
 * Reads one row of a result set, and converts it to an object.
 * @param <T> the type of the object
 */
@FunctionalInterface
public interface ResultSetReader<T> {
    /**
     * Reads the current row.
     * @param rs the result set, positioned at the row
     * @return the object
     * @throws DatabaseError on database errors
     * @throws SQLException on database errors
     */
    T read(final @NotNull SimpleResultSet rs) throws DatabaseError, SQLException;
}
