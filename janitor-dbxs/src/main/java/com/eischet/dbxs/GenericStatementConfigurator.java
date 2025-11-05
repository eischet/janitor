/*
 * © Eischet Software e.K., Köln
 */

package com.eischet.dbxs;

import java.sql.SQLException;

@FunctionalInterface
public interface GenericStatementConfigurator<T> {
    void configure(final T record, final SimplePreparedStatement ps) throws SQLException;
}
