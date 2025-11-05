package com.eischet.janitor.orm.dao;

import com.eischet.dbxs.DatabaseConnection;
import com.eischet.dbxs.SimplePreparedStatement;

import java.sql.SQLException;

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
