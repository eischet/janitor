package com.eischet.dbxs;

import com.eischet.dbxs.exceptions.DatabaseError;
import com.eischet.dbxs.statements.SelectStatement;
import com.eischet.dbxs.statements.UpdateStatement;
import com.eischet.janitor.JanitorTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Regression tests for DatabaseConnection.queryForInteger()/queryForLongInstance(): both are
 * documented to return null when the database value is NULL, but used to call
 * SimpleResultSet.getInt()/getLong() (the primitive-returning readers, which per the JDBC contract
 * return 0 for NULL and don't check wasNull()) instead of getInteger()/getLongInstance() (the
 * existing, correctly wasNull()-aware siblings meant for exactly this purpose). So a NULL column
 * value used to silently come back as 0/0L instead of null.
 */
public class AbstractDatabaseConnectionNullableGettersTestCase extends JanitorTest {

    private void setUp(final SimpleDataManager manager) throws DatabaseError {
        manager.executeTransaction(conn -> {
            conn.update(new UpdateStatement("create table t (i int, l bigint)"));
            conn.update(new UpdateStatement("insert into t (i, l) values (null, null)"));
            conn.update(new UpdateStatement("insert into t (i, l) values (42, 4200000000)"));
        });
    }

    @Test
    public void queryForIntegerReturnsNullForANullColumn() throws DatabaseError {
        final SimpleDataManager manager = TestDb.newManager();
        setUp(manager);
        final Integer result = manager.callTransaction(conn ->
                conn.queryForInteger(new SelectStatement("select i from t where i is null")));
        assertNull(result, "queryForInteger() must return null for a NULL column, not 0");
    }

    @Test
    public void queryForIntegerReturnsTheActualValueWhenNotNull() throws DatabaseError {
        final SimpleDataManager manager = TestDb.newManager();
        setUp(manager);
        final Integer result = manager.callTransaction(conn ->
                conn.queryForInteger(new SelectStatement("select i from t where i is not null")));
        assertEquals(42, result);
    }

    @Test
    public void queryForLongInstanceReturnsNullForANullColumn() throws DatabaseError {
        final SimpleDataManager manager = TestDb.newManager();
        setUp(manager);
        final Long result = manager.callTransaction(conn ->
                conn.queryForLongInstance(new SelectStatement("select l from t where l is null"), ps -> {}));
        assertNull(result, "queryForLongInstance() must return null for a NULL column, not 0");
    }

    @Test
    public void queryForLongInstanceReturnsTheActualValueWhenNotNull() throws DatabaseError {
        final SimpleDataManager manager = TestDb.newManager();
        setUp(manager);
        final Long result = manager.callTransaction(conn ->
                conn.queryForLongInstance(new SelectStatement("select l from t where l is not null"), ps -> {}));
        assertEquals(4200000000L, result);
    }

    @Test
    public void queryForIntStillReturnsZeroForANullColumn() throws DatabaseError {
        // The primitive-returning queryForInt() is documented to return 0 for NULL -- must be
        // unaffected by the queryForInteger() fix above.
        final SimpleDataManager manager = TestDb.newManager();
        setUp(manager);
        final int result = manager.callTransaction(conn ->
                conn.queryForInt(new SelectStatement("select i from t where i is null")));
        assertEquals(0, result);
    }

}
