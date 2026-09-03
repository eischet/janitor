package com.eischet.dbxs.results;

import com.eischet.dbxs.SimpleDataManager;
import com.eischet.dbxs.TestDb;
import com.eischet.dbxs.exceptions.DatabaseError;
import com.eischet.dbxs.statements.SelectStatement;
import com.eischet.dbxs.statements.UpdateStatement;
import com.eischet.janitor.JanitorTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Coverage for SimpleResultSet's query/getter methods -- both the explicit-column-index overloads
 * and the no-arg "auto-advancing" ones (which read/advance an internal column cursor) -- against a
 * real H2 database, using one fully populated row and one all-NULL row.
 * <p>
 * Lives in janitor-tests (rather than janitor-dbxs's own test sources) specifically to reuse
 * JanitorTest's shared unit-test logging setup instead of duplicating it: janitor-dbxs's own tests
 * have no SLF4J provider on their classpath, so they print SLF4J's "No providers were found" warning
 * on every run. janitor-tests already depends on janitor-logging (which supplies a real provider,
 * bridging to java.util.logging) and wires it up once via JanitorTest's @BeforeAll.
 */
public class SimpleResultSetQueryMethodsTestCase extends JanitorTest {

    private static final byte[] SAMPLE_BYTES = {1, 2, 3, 4, 5};
    private static final LocalDateTime SAMPLE_TIMESTAMP = LocalDateTime.of(2024, 3, 17, 13, 45, 30);
    private static final LocalDate SAMPLE_DATE = LocalDate.of(2024, 3, 17);

    private SimpleDataManager manager;

    @BeforeEach
    void setUpDatabase() throws DatabaseError {
        manager = TestDb.newManager();
        manager.executeTransaction(conn -> conn.update(new UpdateStatement("""
                create table t (
                    id int,
                    s varchar(50),
                    i int,
                    l bigint,
                    fd double,
                    ts timestamp,
                    dt date,
                    c clob,
                    bin varbinary(100)
                )
                """)));
        manager.executeTransaction(conn -> conn.update(
                new UpdateStatement("insert into t (id, s, i, l, fd, ts, dt, c, bin) values (1, ?, ?, ?, ?, ?, ?, ?, ?)"),
                ps -> ps.addString("hello")
                        .addInt(17)
                        .addLong(4200000000L)
                        .addDoubleInstance(1.5)
                        .addNullableDateTime(SAMPLE_TIMESTAMP)
                        .addNullableDate(SAMPLE_DATE)
                        .addClob("clob content")
                        .addBytes(SAMPLE_BYTES)));
        manager.executeTransaction(conn -> conn.update(new UpdateStatement("insert into t (id) values (2)")));
    }

    // --- indexed getters, populated row -------------------------------------------------------

    @Test
    public void indexedGettersReadThePopulatedRow() throws DatabaseError {
        manager.executeTransaction(conn -> conn.queryForEach(SelectStatement.of(
                "select s, i, l, fd, ts, c, bin from t where id = 1"), ps -> {}, rs -> {
            assertEquals("hello", rs.getString(1));
            assertEquals(17, rs.getInt(2));
            assertEquals(17, rs.getIntegerInstance(2));
            assertEquals(4200000000L, rs.getLong(3));
            assertEquals(4200000000L, rs.getLongInstance(3));
            assertEquals(1.5, rs.getDouble(4));
            assertEquals(1.5, rs.getDoubleInstance(4));
            assertEquals(Timestamp.valueOf(SAMPLE_TIMESTAMP), rs.getTimestamp(5));
            assertEquals(SAMPLE_TIMESTAMP, rs.getLocalDateTime(5));
            assertEquals("clob content", rs.readClob(6));
            assertEquals("clob content", rs.readNationalClob(6));
            assertArrayEquals(SAMPLE_BYTES, rs.readBlob(7));
        }));
    }

    // --- indexed getters, all-NULL row ---------------------------------------------------------

    @Test
    public void indexedGettersReadTheAllNullRow() throws DatabaseError {
        manager.executeTransaction(conn -> conn.queryForEach(SelectStatement.of(
                "select s, i, l, fd, ts, c, bin from t where id = 2"), ps -> {}, rs -> {
            assertNull(rs.getString(1));
            // Note the asymmetry: getInt() already returns a nullable Integer (wasNull()-aware), but
            // getLong()/getDouble() return primitive long/double and are NOT nullable-aware -- NULL
            // comes back as 0/0.0 for those two, by design (see DatabaseConnection.queryForInt()'s
            // Javadoc for the same "NULL becomes 0" contract on the primitive-returning query methods).
            // getLongInstance()/getDoubleInstance() are their nullable-aware siblings.
            assertNull(rs.getIntegerInstance(2));
            assertNull(rs.getIntegerInstance(2));
            assertEquals(0, rs.getInt(2));
            assertEquals(0L, rs.getLong(3));
            assertNull(rs.getLongInstance(3));
            assertEquals(0.0, rs.getDouble(4));
            assertNull(rs.getDoubleInstance(4));
            assertNull(rs.getTimestamp(5));
            assertNull(rs.getLocalDateTime(5));
            assertNull(rs.readClob(6));
            assertNull(rs.readNationalClob(6));
            assertNull(rs.readBlob(7));
        }));
    }

    // --- no-arg, auto-advancing getters ---------------------------------------------------------

    @Test
    public void noArgGettersAdvanceThroughColumnsInOrder() throws DatabaseError {
        manager.executeTransaction(conn -> conn.queryForEach(SelectStatement.of(
                "select s, i, l, fd from t where id = 1"), ps -> {}, rs -> {
            assertEquals("hello", rs.getString());
            assertEquals(17, rs.getInt());
            assertEquals(4200000000L, rs.getLong());
            assertEquals(1.5, rs.getDouble());
        }));
    }

    @Test
    public void noArgGettersResetToColumnOneOnEachRow() throws DatabaseError {
        // next() resets the column cursor to 1, so the same no-arg call sequence must work
        // identically for every row, not just the first.
        manager.executeTransaction(conn -> conn.queryForEach(SelectStatement.of(
                "select id, s from t order by id"), ps -> {}, rs -> {
            final int id = rs.getInt();
            final String s = rs.getString();
            if (id == 1) {
                assertEquals("hello", s);
            } else {
                assertNull(s);
            }
        }));
    }

    // --- row/column bookkeeping -----------------------------------------------------------------

    @Test
    public void rowNumberAndFirstRowAreTrackedAcrossNext() throws DatabaseError {
        manager.executeTransaction(conn -> {
            final int[] seenRows = {0};
            conn.queryForEach(SelectStatement.of("select id from t order by id"), ps -> {}, rs -> {
                if (seenRows[0] == 0) {
                    assertTrue(rs.isFirstRow(), "the first row read must report isFirstRow() == true");
                    assertEquals(0, rs.getRowNumber());
                } else {
                    assertFalse(rs.isFirstRow(), "later rows must not report isFirstRow()");
                    assertEquals(seenRows[0], rs.getRowNumber());
                }
                ++seenRows[0];
            });
            assertEquals(2, seenRows[0], "both rows must have been visited");
        });
    }

    @Test
    public void getNumberOfColumnsMatchesTheSelectedColumns() throws DatabaseError {
        manager.executeTransaction(conn -> conn.queryForEach(
                SelectStatement.of("select s, i, l from t where id = 1"), ps -> {}, rs ->
                        assertEquals(3, rs.getNumberOfColumns())));
    }

    @Test
    public void getOptionalTimestampReturnsNullBeyondTheLastColumn() throws DatabaseError {
        manager.executeTransaction(conn -> conn.queryForEach(
                SelectStatement.of("select ts from t where id = 1"), ps -> {}, rs -> {
            assertEquals(Timestamp.valueOf(SAMPLE_TIMESTAMP), rs.getOptionalTimestamp(1));
            assertNull(rs.getOptionalTimestamp(2), "there is no second column to read");
        }));
    }

    @Test
    public void getLocalDateReadsFromTheCurrentCursorColumn() throws DatabaseError {
        // getLocalDate(), unlike most other getters here, has no explicit-column-index overload --
        // it only reads from (and advances) the internal column cursor, so it needs a single-column
        // query to test in isolation.
        manager.executeTransaction(conn -> conn.queryForEach(
                SelectStatement.of("select dt from t where id = 1"), ps -> {}, rs ->
                        assertEquals(SAMPLE_DATE, rs.getLocalDate())));
        manager.executeTransaction(conn -> conn.queryForEach(
                SelectStatement.of("select dt from t where id = 2"), ps -> {}, rs ->
                        assertNull(rs.getLocalDate())));
    }

    @Test
    public void getDateReadsAJavaUtilDate() throws DatabaseError {
        manager.executeTransaction(conn -> conn.queryForEach(
                SelectStatement.of("select dt from t where id = 1"), ps -> {}, rs -> {
            final Date date = rs.getDate(1);
            assertEquals(java.sql.Date.valueOf(SAMPLE_DATE), date);
        }));
    }

    @Test
    public void getBinaryStreamReadsTheSameBytesAsReadBlob() throws DatabaseError {
        manager.executeTransaction(conn -> conn.queryForEach(
                SelectStatement.of("select bin from t where id = 1"), ps -> {}, rs -> {
            try {
                assertArrayEquals(SAMPLE_BYTES, rs.getBinaryStream(1).readAllBytes());
            } catch (java.io.IOException e) {
                throw new DatabaseError(e);
            }
        }));
    }

}
