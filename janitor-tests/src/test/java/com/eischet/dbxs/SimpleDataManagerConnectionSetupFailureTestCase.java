package com.eischet.dbxs;

import com.eischet.dbxs.dialects.DatabaseDialectH2;
import com.eischet.dbxs.exceptions.DatabaseError;
import com.eischet.janitor.JanitorTest;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Regression test for a connection-setup failure inside ClosableDatabaseConnection's constructor
 * (e.g. setAutoCommit(false) throwing on a dead pooled connection): since the constructor never
 * finishes in that case, callTransaction()'s try-with-resources never gets a resource to close(), so
 * both the freshly checked-out physical connection and the connHolder ThreadLocal entry used to leak.
 * <p>
 * A real dead connection is hard to simulate against H2 directly, so this wraps a real H2 DataSource
 * with dynamic proxies that intercept setAutoCommit() to throw, while delegating everything else
 * (including close()) to the real connection -- letting us both trigger the failure and observe that
 * the connection actually gets closed afterward.
 * <p>
 * Stays in package com.eischet.dbxs (matching SimpleDataManager's own package) so it can read the
 * protected connHolder field directly -- that only works for same-package access, not from
 * com.eischet.janitor.*.
 */
public class SimpleDataManagerConnectionSetupFailureTestCase extends JanitorTest {

    private static DataSource failOnceOnSetAutoCommit(final DataSource real, final AtomicInteger closeCalls) {
        return (DataSource) Proxy.newProxyInstance(
                DataSource.class.getClassLoader(),
                new Class<?>[]{DataSource.class},
                (proxy, method, args) -> {
                    if ("getConnection".equals(method.getName()) && (args == null || args.length == 0)) {
                        return wrapConnection(real.getConnection(), closeCalls);
                    }
                    return method.invoke(real, args);
                });
    }

    private static Connection wrapConnection(final Connection real, final AtomicInteger closeCalls) {
        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                (proxy, method, args) -> {
                    if ("setAutoCommit".equals(method.getName())) {
                        throw new SQLException("simulated dead connection");
                    }
                    if ("close".equals(method.getName())) {
                        closeCalls.incrementAndGet();
                    }
                    return method.invoke(real, args);
                });
    }

    @Test
    public void aSetupFailureClosesTheConnectionAndClearsTheThreadLocal() {
        final AtomicInteger closeCalls = new AtomicInteger();
        final DataSource ds = failOnceOnSetAutoCommit(TestDb.newDataSource(), closeCalls);
        final SimpleDataManager manager = new SimpleDataManager("test", ds, new DatabaseDialectH2(), null, null);

        final DatabaseError error = assertThrows(DatabaseError.class, () -> manager.executeTransaction(conn -> {
        }));
        assertInstanceOf(SQLException.class, error.getCause());

        assertNull(manager.connHolder.get(), "the connHolder ThreadLocal must not keep a reference to a connection nobody will ever close");
        assertEquals(1, closeCalls.get(), "the leaked physical connection must have been closed");
    }

}
