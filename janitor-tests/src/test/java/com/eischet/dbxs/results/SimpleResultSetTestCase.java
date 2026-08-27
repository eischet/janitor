package com.eischet.dbxs.results;

import com.eischet.dbxs.SimpleDataManager;
import com.eischet.dbxs.TestDb;
import com.eischet.dbxs.exceptions.DatabaseError;
import com.eischet.dbxs.metadata.SqlTypes;
import com.eischet.dbxs.statements.SelectStatement;
import com.eischet.dbxs.statements.UpdateStatement;
import com.eischet.janitor.JanitorTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Regression test for SimpleResultSet.typeOf(): it only guarded the upper bound (column > types
 * .size()), so a column index below 1 (e.g. 0, or a negative index) fell through to
 * types.get(column-1) and threw an unguarded IndexOutOfBoundsException instead of gracefully
 * returning UNKNOWN like the upper-bound case already did.
 * <p>
 * Goes through DatabaseConnection.queryForEach() -- the actual, intended way to obtain a
 * SimpleResultSet -- rather than constructing one directly, so this exercises the real query path.
 */
public class SimpleResultSetTestCase extends JanitorTest {

    @Test
    public void typeOfBelowOneReturnsUnknownInsteadOfThrowing() throws DatabaseError {
        final SimpleDataManager manager = TestDb.newManager();
        manager.executeTransaction(conn -> conn.update(new UpdateStatement("create table t (n int)")));
        manager.executeTransaction(conn -> conn.update(new UpdateStatement("insert into t (n) values (1)")));

        manager.executeTransaction(conn -> conn.queryForEach(SelectStatement.of("select n from t"), ps -> {}, rs -> {
            assertEquals(SqlTypes.UNKNOWN, rs.typeOf(0));
            assertEquals(SqlTypes.UNKNOWN, rs.typeOf(-1));
            assertEquals(SqlTypes.INTEGER, rs.typeOf(1));
        }));
    }

}
