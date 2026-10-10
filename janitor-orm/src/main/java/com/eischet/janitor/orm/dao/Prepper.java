// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.dao;

import com.eischet.dbxs.DatabaseConnection;
import com.eischet.dbxs.SimplePreparedStatement;

import java.sql.SQLException;

/** Binds a value to the next parameter of a prepared statement. */
public interface Prepper {
    /**
     * Binds the value to the next parameter of the statement.
     * @param conn the database connection
     * @param stmt the statement
     * @throws SQLException if the value cannot be bound
     */
    void prepare(final DatabaseConnection conn, SimplePreparedStatement stmt) throws SQLException;
}
