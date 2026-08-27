package com.eischet.dbxs.metadata;

import com.eischet.dbxs.TestDb;
import com.eischet.janitor.JanitorTest;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Regression tests for SqlTypeInterpreter's INTEGER/FLOAT/DOUBLE/REAL extractors: they used to read
 * via the JDBC primitive getters (getInt()/getFloat()/getDouble()) without ever calling wasNull()
 * afterward, so a SQL NULL silently came back as 0/0.0/0.0f instead of null -- readIntegerAndIntern()
 * backs SimpleResultSet.getInt() directly, and all four are wired up as the extractors for their
 * respective java.sql.Types entries.
 */
public class SqlTypeInterpreterNullHandlingTestCase extends JanitorTest {

    private interface RowTest {
        void run(ResultSet rs) throws SQLException;
    }

    private void withRow(final String createColumn, final String insertValue, final RowTest test) throws SQLException {
        final DataSource ds = TestDb.newDataSource();
        try (Connection c = ds.getConnection(); Statement stmt = c.createStatement()) {
            stmt.execute("create table t (v " + createColumn + ")");
            stmt.execute("insert into t (v) values (" + insertValue + ")");
            try (ResultSet rs = stmt.executeQuery("select v from t")) {
                rs.next();
                test.run(rs);
            }
        }
    }

    @Test
    public void readIntegerAndInternReturnsNullForSqlNull() throws SQLException {
        withRow("int", "null", rs -> assertNull(SqlTypeInterpreter.readIntegerAndIntern(rs, 1)));
    }

    @Test
    public void readIntegerAndInternReturnsTheActualValue() throws SQLException {
        withRow("int", "17", rs -> assertEquals(17, SqlTypeInterpreter.readIntegerAndIntern(rs, 1)));
    }

    @Test
    public void readFloatInstanceReturnsNullForSqlNull() throws SQLException {
        withRow("real", "null", rs -> assertNull(SqlTypeInterpreter.readFloatInstance(rs, 1)));
    }

    @Test
    public void readFloatInstanceReturnsTheActualValue() throws SQLException {
        withRow("real", "1.5", rs -> assertEquals(1.5f, SqlTypeInterpreter.readFloatInstance(rs, 1)));
    }

    @Test
    public void readDoubleInstanceReturnsNullForSqlNull() throws SQLException {
        withRow("double", "null", rs -> assertNull(SqlTypeInterpreter.readDoubleInstance(rs, 1)));
    }

    @Test
    public void readDoubleInstanceReturnsTheActualValue() throws SQLException {
        withRow("double", "1.5", rs -> assertEquals(1.5d, SqlTypeInterpreter.readDoubleInstance(rs, 1)));
    }

}
