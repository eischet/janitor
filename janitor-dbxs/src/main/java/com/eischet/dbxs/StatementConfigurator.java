// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.dbxs;

import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

/** Sets the parameters of a prepared statement. */
@FunctionalInterface
public interface StatementConfigurator {
    /**
     * Sets the parameters of the statement.
     * @param ps the statement
     * @throws SQLException if a value cannot be set
     */
    void configure(final @NotNull SimplePreparedStatement ps) throws SQLException;
}
