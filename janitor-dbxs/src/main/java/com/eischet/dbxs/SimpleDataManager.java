/*
 * © Eischet Software e.K., Köln
 */

package com.eischet.dbxs;

import com.eischet.dbxs.dialects.DatabaseDialect;
import com.eischet.dbxs.exceptions.DatabaseError;
import com.eischet.dbxs.results.ResultSetConsumer;
import com.eischet.dbxs.results.ResultSetReader;
import com.eischet.dbxs.results.SimpleResultSet;
import com.eischet.dbxs.statements.SelectStatement;
import com.eischet.dbxs.statements.UpdateStatement;
import com.eischet.janitor.toolbox.memory.Flag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.Closeable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@SuppressWarnings("unused") // this is not unused, it's our main API, currently all the tests are hidden from the public in an app though...
public class SimpleDataManager implements DataManager {

    protected static final Logger log = LoggerFactory.getLogger(SimpleDataManager.class);
    protected final ThreadLocal<ConnectionWrapper> connHolder = new ThreadLocal<>();
    protected final String defaultSchema;
    protected final AtomicLong transId = new AtomicLong();

    protected final String name;
    protected final DataSource dataSource;
    protected final List<String> initStatements;
    protected final DatabaseDialect dialect;

    public SimpleDataManager(final @NotNull String name,
                             final @NotNull DataSource dataSource,
                             final @NotNull DatabaseDialect dialect,
                             final @Nullable String defaultSchema,
                             final @Nullable List<String> initStatements) {
        this.name = name;
        this.dataSource = dataSource;
        this.dialect = dialect;
        this.defaultSchema = defaultSchema;
        this.initStatements = initStatements == null ? Collections.emptyList() : List.copyOf(initStatements);
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    @Override
    public String getDefaultSchema() {
        return defaultSchema;
    }

    @Override
    public @NotNull DatabaseDialect getDialect() {
        return dialect;
    }

    @Override
    public String getStatistics() {
        return "none"; // LATER get statistics
    }

    private void runInitStatements(final ConnectionWrapper newConn) {
        if (initStatements != null && !initStatements.isEmpty()) {
            for (final String initStatement : initStatements) {
                try (final Statement stmt = newConn.getConn().createStatement()) {
                    log.debug("executing init statement: {}", initStatement);
                    stmt.execute(initStatement);
                } catch (SQLException e) {
                    log.error("error executing init statement", e);
                }
            }
        }
    }

    @Override
    public void scheduleTransaction(final DatabaseTransaction transaction) {
        // war früher im Hintergrund, hatte sich aber nicht bewährt...
        try {
            executeTransaction(transaction);
        } catch (DatabaseError e) {
            log.error("error in background transaction {}", transaction, e);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public <T> T callTransaction(final DatabaseFunction<DatabaseConnection, T> callable) throws DatabaseError {
        final long transId = this.transId.incrementAndGet();
        log.debug("{} executing transaction {}", name, transId);
        try (final ClosableDatabaseConnection conn = new ClosableDatabaseConnection()) {
            try {
                T result = callable.apply(conn);
                conn.commit();
                return result;
            } catch (DatabaseError e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            log.error("{}: SQL Exception connecting to database for transaction {}", name, transId, e);
            if (exceptionConsumer != null) {
                exceptionConsumer.accept(this, e);
            }
            throw new DatabaseError(e);
        } catch (DatabaseError e) {
            if (exceptionConsumer != null) {
                exceptionConsumer.accept(this, e);
            }
            throw e;
        }
    }

    public interface ExceptionConsumer {
        void accept(SimpleDataManager self, Throwable e);
    }

    protected static @Nullable ExceptionConsumer exceptionConsumer = null;

    public static void setExceptionConsumer(@Nullable final ExceptionConsumer exceptionConsumer) {
        SimpleDataManager.exceptionConsumer = exceptionConsumer;
    }

    @Override
    public void executeTransaction(final DatabaseTransaction transaction) throws DatabaseError {
        log.debug("executing transaction: {}", transaction);
        callTransaction(conn -> {
            transaction.apply(conn);
            return null;
        });
    }

    @Override
    public String getSchema() {
        return defaultSchema;
    }

    public static class ConnectionWrapper {
        private final Connection conn;
        private final long checkoutTime;

        private long updates;
        private boolean orphaned;

        public ConnectionWrapper(final Connection conn) {
            this.conn = conn;
            this.checkoutTime = System.currentTimeMillis();
        }

        public Connection getConn() {
            return conn;
        }

        public long getCheckoutTime() {
            return checkoutTime;
        }

        public long getUpdates() {
            return updates;
        }

        public void countUpdate() {
            ++updates;
        }

        @Override
        public String toString() {
            return "ConnectionWrapper{" +
                   "conn=" + conn +
                   ", orphaned=" + orphaned +
                   ", checkoutTime=" + new Date(checkoutTime) +
                   ", updates=" + updates +
                   '}';
        }

        public boolean getOrphaned() {
            return orphaned;
        }

        public void setOrphaned(final boolean orphaned) {
            this.orphaned = orphaned;
        }

    }

    @SuppressWarnings("Duplicates")
    private class ClosableDatabaseConnection extends AbstractDatabaseConnection implements Closeable {

        private final boolean borrowed;
        private ConnectionWrapper conn;

        public ClosableDatabaseConnection() throws SQLException {
            final long checkoutStart = System.currentTimeMillis();
            log.debug("{} checking out a database connection", getName());

            final ConnectionWrapper existing = connHolder.get();
            boolean orphaned = false;
            if (existing != null && existing.getCheckoutTime() < checkoutStart - 1000 * 180) {
                log.warn("orphaned connection detected: {}", existing);
                existing.setOrphaned(true);
                orphaned = true;
            }

            if (orphaned) {
                log.warn("removing orphaned connection {}", existing);
                // log.warn("removing orphaned connection {} created by {}", existing, existing.getFullStackTrace());
                // existing.getConn().close();
                connHolder.remove();
            }


            if (!orphaned && existing != null) {
                log.debug("{} reusing existing connection", name);
                conn = existing;
                borrowed = true;
            } else {
                log.debug("{} fetching new connection", name);
                ConnectionWrapper newConn = null;
                while (newConn == null) {
                    newConn = new ConnectionWrapper(dataSource.getConnection());
                    connHolder.set(newConn);
                    final long delta = newConn.getCheckoutTime() - checkoutStart;
                    if (delta > 1000) {
                        log.info("{} - database connection checkout time: {} ms", name, delta);
                    }
                    newConn.getConn().setAutoCommit(false);

                    runInitStatements(newConn);

                }
                this.conn = newConn;
                borrowed = false;
            }
        }

        private void close(final Connection conn) {
            if (borrowed) {
                log.debug("{}: not closing 'borrowed' connection", name);
            } else if (conn != null) {
                log.debug("{}: closing my own connection", name);
                try {
                    conn.close();
                } catch (SQLException e) {
                    log.error("error closing connection", e);
                }
                connHolder.remove();
            }
        }

        private void close(final Statement stmt) {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    log.error("error closing statement", e);
                }
            }
        }

        private void close(final SimpleResultSet rs) {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    log.error("error closing result set", e);
                }
            }
        }

        public void rollback() {
            if (conn.getUpdates() > 0) {
                log.info("rolling back on {}", this);
                try {
                    if (conn != null) {
                        conn.getConn().rollback();
                    }
                } catch (SQLException e1) {
                    log.error("error rolling back transaction", e1);
                }
            }
        }

        public void commit() {
            if (conn.getUpdates() > 0) {
                log.debug("committing on {}", this);
                try {
                    if (conn != null) {
                        conn.getConn().commit();
                    }
                } catch (SQLException e1) {
                    log.error("error rolling back transaction", e1);
                }
            }
        }

        @Override
        public QuerySummary queryForEach(final @NotNull SelectStatement sql, final @NotNull StatementConfigurator sc, final @NotNull ResultSetConsumer consumer) throws DatabaseError {
            return queryForEach(sql, sc, consumer, null, null);
        }

        @Override
        public QuerySummary queryForEach(final @NotNull SelectStatement sql,
                                         final @NotNull StatementConfigurator sc,
                                         final @NotNull ResultSetConsumer consumer,
                                         final @Nullable Long limit,
                                         final @Nullable Flag abortFlag)
            throws DatabaseError {
            log.debug("{} starting query {}, limit={}", getName(), sql, limit);
            long readRows = 0;
            boolean abortedOnLimit = false;
            boolean abortedOnFlag = false;
            PreparedStatement stmt = null;
            SimpleResultSet rs = null;
            SimplePreparedStatement sps = null;
            try {
                log.debug("{} preparing sql statement", getName());

                //noinspection SqlSourceToSinkFlow
                stmt = conn.getConn().prepareStatement(sql.getSql());
                // The risk is tolerable.

                sps = new SimplePreparedStatement(getDialect(), sql, stmt);
                sc.configure(sps);
                log.debug("{} executing query", getName());
                rs = new SimpleResultSet(getDialect(), stmt.executeQuery(), this);

                while (rs.next()) {
                    if (limit != null && readRows >= limit) {
                        abortedOnLimit = true;
                        break;
                    }
                    if (abortFlag != null && abortFlag.isFlag()) {
                        abortedOnFlag = true;
                        break;
                    }
                    consumer.consume(rs);
                    ++readRows;
                }
                final long queryEnd = System.currentTimeMillis();
                log.debug("{} - database query duration: {} ms for {}", name, queryEnd - conn.getCheckoutTime(), sql.getSql().replace("\n", " "));
            } catch (final SQLException e) {
                if (sps == null) {
                    throw new DatabaseError(sql, e);
                } else {
                    throw new DatabaseError(sps, e);
                }
            } finally {
                close(rs);
                close(stmt);
                log.debug("{} finished query {}", getName(), sql);
            }
            return new QuerySummary(readRows, abortedOnLimit, abortedOnFlag);
        }

        @Override
        public int update(final @NotNull UpdateStatement sql, @NotNull final StatementConfigurator sc) throws DatabaseError {
            conn.countUpdate();
            PreparedStatement stmt = null;
            SimplePreparedStatement sps = null;
            try {
                //noinspection SqlSourceToSinkFlow
                stmt = conn.getConn().prepareStatement(sql.getSql());
                sps = new SimplePreparedStatement(getDialect(), sql, stmt);
                sc.configure(sps);
                return stmt.executeUpdate();
            } catch (final Throwable e) { // should catch RuntimeException, too, e.g. NPEs in client code
                if (sps == null) {
                    throw new DatabaseError(e);
                } else {
                    throw new DatabaseError(sps, e);
                }
            } finally {
                close(stmt);
            }
        }

        @Override
        public <T> T insertOneRowAndReturnGeneratedKey(final @NotNull UpdateStatement sql,
                                                       final @NotNull StatementConfigurator sc,
                                                       final @NotNull ResultSetReader<T> consumer) throws DatabaseError {
            conn.countUpdate();
            PreparedStatement stmt = null;
            SimplePreparedStatement sps = null;
            try {
                //noinspection SqlSourceToSinkFlow
                stmt = conn.getConn().prepareStatement(sql.getSql(), Statement.RETURN_GENERATED_KEYS);
                sps = new SimplePreparedStatement(getDialect(), sql, stmt);
                sc.configure(sps);
                stmt.executeUpdate();
                final SimpleResultSet rs = new SimpleResultSet(getDialect(), stmt.getGeneratedKeys(), this);
                if (rs.next()) {
                    return consumer.read(rs);
                } else {
                    return null;
                }
            } catch (final Throwable e) { // should catch RuntimeException, too, e.g. NPEs in client code
                if (sps == null) {
                    throw new DatabaseError(e);
                } else {
                    throw new DatabaseError(sps, e);
                }
            } finally {
                close(stmt);
            }
        }

        @Override
        public @NotNull DatabaseDialect getDialect() {
            return SimpleDataManager.this.getDialect();
        }

        @Override
        public Connection getJdbcConnection() {
            return conn.getConn();
        }

        @Override
        public void close() {
            log.debug("closing transaction {} #{}", name, transId);
            close(conn.getConn());
        }
    }

}
