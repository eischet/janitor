// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.dao;

import com.eischet.dbxs.DatabaseConnection;
import com.eischet.dbxs.SimplePreparedStatement;

import java.sql.SQLException;

/** A {@link Prepper} with a description, which is used when the prepper is printed, e.g. in logs. */
public class NamedPrepper implements Prepper {
    private final Prepper wrapped;
    private final String description;

    public NamedPrepper(final Prepper wrapped, final String description) {
        this.wrapped = wrapped;
        this.description = description;
    }

    @Override
    public String toString() {
        return description;
    }

    @Override
    public void prepare(final DatabaseConnection conn, final SimplePreparedStatement stmt) throws SQLException {
        wrapped.prepare(conn, stmt);
    }
}
