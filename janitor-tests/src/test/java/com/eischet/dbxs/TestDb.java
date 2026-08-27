package com.eischet.dbxs;

import com.eischet.dbxs.dialects.DatabaseDialectH2;
import org.h2.jdbcx.JdbcDataSource;

import javax.sql.DataSource;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Shared helper for spinning up an isolated, in-memory H2 database per test.
 */
public final class TestDb {

    private static final AtomicInteger counter = new AtomicInteger();

    private TestDb() {
    }

    public static DataSource newDataSource() {
        final JdbcDataSource ds = new JdbcDataSource();
        // A unique DB name per call keeps tests isolated from each other; DB_CLOSE_DELAY=-1 keeps
        // the in-memory DB alive for the lifetime of the JVM instead of vanishing after the first
        // connection closes (SimpleDataManager checks connections in and out repeatedly).
        ds.setURL("jdbc:h2:mem:dbxs_test_" + counter.incrementAndGet() + ";DB_CLOSE_DELAY=-1");
        return ds;
    }

    public static SimpleDataManager newManager() {
        return new SimpleDataManager("test", newDataSource(), new DatabaseDialectH2(), null, null);
    }

}
