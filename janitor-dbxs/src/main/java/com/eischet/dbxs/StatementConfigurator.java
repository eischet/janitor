/*
 * © Eischet Software e.K., Köln
 */

package com.eischet.dbxs;

import java.sql.SQLException;

@FunctionalInterface
public interface StatementConfigurator {
    void configure(final SimplePreparedStatement ps) throws SQLException;
}
