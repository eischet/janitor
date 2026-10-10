/*
 * © Eischet Software e.K., Köln
 */

package com.eischet.dbxs;

import java.sql.SQLException;

/**
 * Sets the parameters of a prepared statement from the fields of a record.
 * @param <T> the type of the record
 */
@FunctionalInterface
public interface GenericStatementConfigurator<T> {
    /**
     * Sets the parameters of the statement.
     * @param record the object to take the values from
     * @param ps the statement
     * @throws SQLException if a value cannot be set
     */
    void configure(final T record, final SimplePreparedStatement ps) throws SQLException;
}
