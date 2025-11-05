/*
 * © Eischet Software e.K., Köln
 */

package com.eischet.dbxs.results;

import com.eischet.dbxs.exceptions.DatabaseError;

import java.sql.SQLException;

@FunctionalInterface
public interface ResultSetReader<T> {
    T read(final SimpleResultSet rs) throws DatabaseError, SQLException;
}
