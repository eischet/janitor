package com.eischet.dbxs;

import com.eischet.dbxs.exceptions.DatabaseError;
import com.eischet.dbxs.statements.SelectStatement;
import com.eischet.dbxs.statements.UpdateStatement;
import com.eischet.janitor.JanitorTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Regression tests for SimplePreparedStatement.addBytes()/addBlob(): they used to treat an
 * explicitly empty (but non-null) byte[] exactly like null, binding SQL NULL instead of an empty
 * blob -- silently discarding the distinction between "no data" and "zero-length data" that most
 * client code relies on.
 */
public class SimplePreparedStatementBlobNullHandlingTestCase extends JanitorTest {

    @Test
    public void addBytesDistinguishesEmptyArrayFromNull() throws DatabaseError {
        final SimpleDataManager manager = TestDb.newManager();
        manager.executeTransaction(conn -> conn.update(new UpdateStatement("create table t (id int, data blob)")));
        manager.executeTransaction(conn -> {
            conn.update(new UpdateStatement("insert into t (id, data) values (1, ?)"), ps -> ps.addBytes(new byte[0]));
            conn.update(new UpdateStatement("insert into t (id, data) values (2, ?)"), ps -> ps.addBytes(null));
        });

        final byte[] empty = manager.callTransaction(conn ->
                conn.queryForObject(new SelectStatement("select data from t where id = 1"), rs -> rs.readBlob(1)));
        final byte[] nullValue = manager.callTransaction(conn ->
                conn.queryForObject(new SelectStatement("select data from t where id = 2"), rs -> rs.readBlob(1)));

        assertNotNull(empty, "an explicitly empty byte[] must round-trip as an empty blob, not NULL");
        assertEquals(0, empty.length);
        assertNull(nullValue, "a null byte[] must still round-trip as SQL NULL");
    }

    @Test
    public void addBlobDistinguishesEmptyArrayFromNull() throws DatabaseError {
        final SimpleDataManager manager = TestDb.newManager();
        manager.executeTransaction(conn -> conn.update(new UpdateStatement("create table t2 (id int, data blob)")));
        manager.executeTransaction(conn -> {
            conn.update(new UpdateStatement("insert into t2 (id, data) values (1, ?)"), ps -> ps.addBlob(new byte[0]));
            conn.update(new UpdateStatement("insert into t2 (id, data) values (2, ?)"), ps -> ps.addBlob(null));
        });

        final byte[] empty = manager.callTransaction(conn ->
                conn.queryForObject(new SelectStatement("select data from t2 where id = 1"), rs -> rs.readBlob(1)));
        final byte[] nullValue = manager.callTransaction(conn ->
                conn.queryForObject(new SelectStatement("select data from t2 where id = 2"), rs -> rs.readBlob(1)));

        assertNotNull(empty, "an explicitly empty byte[] must round-trip as an empty blob, not NULL");
        assertEquals(0, empty.length);
        assertNull(nullValue, "a null byte[] must still round-trip as SQL NULL");
    }

}
