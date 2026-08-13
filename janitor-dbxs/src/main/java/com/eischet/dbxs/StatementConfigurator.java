/*
 * © Eischet Software e.K., Köln
 */

package com.eischet.dbxs;

import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

@FunctionalInterface
public interface StatementConfigurator {
    void configure(final @NotNull SimplePreparedStatement ps) throws SQLException;
}
