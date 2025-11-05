package com.eischet.janitor.orm.dao;

import com.eischet.dbxs.DatabaseConnection;
import com.eischet.dbxs.SimplePreparedStatement;

import java.sql.SQLException;

public interface Prepper {
    void prepare(final DatabaseConnection conn, SimplePreparedStatement stmt) throws SQLException;
}
