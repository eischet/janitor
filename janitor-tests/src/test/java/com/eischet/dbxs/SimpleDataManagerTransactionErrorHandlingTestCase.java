package com.eischet.dbxs;

import com.eischet.dbxs.exceptions.DatabaseError;
import com.eischet.dbxs.statements.SelectStatement;
import com.eischet.dbxs.statements.UpdateStatement;
import com.eischet.janitor.JanitorTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Regression tests for two related fixes in SimpleDataManager:
 * <p>
 * 1. callTransaction() used to only roll back and notify the (static) exceptionConsumer hook for
 * DatabaseError/SQLException. Any other unchecked exception thrown by the caller's transaction
 * callback (a plain bug, e.g. an NPE) skipped both, relying only on whatever the JDBC driver happens
 * to do with an open transaction when the connection is close()d.
 * <p>
 * 2. update()/insertOneRowAndReturnGeneratedKey() used to catch Throwable, not just Exception, so an
 * Error thrown by client code (e.g. inside a StatementConfigurator) got wrapped into a DatabaseError
 * instead of propagating as itself.
 */
public class SimpleDataManagerTransactionErrorHandlingTestCase extends JanitorTest {

    @AfterEach
    void resetExceptionConsumer() {
        // exceptionConsumer is a static field on SimpleDataManager, shared across all instances --
        // must reset it so this test doesn't leak into others.
        SimpleDataManager.setExceptionConsumer(null);
    }

    @Test
    public void aRuntimeExceptionFromTheCallbackRollsBackAndNotifiesTheExceptionConsumer() throws DatabaseError {
        final SimpleDataManager manager = TestDb.newManager();
        manager.executeTransaction(conn -> conn.update(new UpdateStatement("create table t (n int)")));

        final AtomicReference<Throwable> notified = new AtomicReference<>();
        SimpleDataManager.setExceptionConsumer((self, e) -> notified.set(e));

        final IllegalStateException boom = new IllegalStateException("boom");
        final RuntimeException caught = assertThrows(RuntimeException.class, () -> manager.executeTransaction(conn -> {
            conn.update(new UpdateStatement("insert into t (n) values (1)"));
            throw boom;
        }));
        assertSame(boom, caught, "the original RuntimeException must propagate unchanged, not get wrapped into a DatabaseError");
        assertSame(boom, notified.get(), "exceptionConsumer must be notified of the RuntimeException");

        final int count = manager.callTransaction(conn -> conn.queryForInt(new SelectStatement("select count(*) from t")));
        assertEquals(0, count, "the insert must have been rolled back, not silently committed via close()");
    }

    @Test
    public void anErrorFromTheConfiguratorPropagatesUnwrapped() throws DatabaseError {
        final SimpleDataManager manager = TestDb.newManager();
        manager.executeTransaction(conn -> conn.update(new UpdateStatement("create table t2 (n int)")));

        final AssertionError boom = new AssertionError("boom");
        final AssertionError caught = assertThrows(AssertionError.class, () -> manager.executeTransaction(conn ->
                conn.update(new UpdateStatement("insert into t2 (n) values (1)"), ps -> {
                    throw boom;
                })));
        assertSame(boom, caught, "an Error thrown by client code must propagate unwrapped, not become a DatabaseError");
    }

}
