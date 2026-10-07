package com.eischet.dbxs;

import com.eischet.dbxs.dialects.DatabaseDialect;
import com.eischet.dbxs.dialects.DatabaseDialectH2;
import com.eischet.dbxs.dialects.DatabaseDialectMicrosoft;
import com.eischet.dbxs.dialects.DatabaseDialectOracle;
import com.eischet.dbxs.exceptions.DatabaseError;
import com.eischet.dbxs.statements.SelectStatement;
import com.eischet.dbxs.statements.UpdateStatement;
import com.eischet.janitor.JanitorTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The text-search related parts of the dialects: SQL text for Oracle and MS SQL Server (without a
 * database), and the actual semantics of the generated conditions on H2.
 */
public class DialectLikeTestCase extends JanitorTest {

    private final DatabaseDialect oracle = new DatabaseDialectOracle();
    private final DatabaseDialect mssql = new DatabaseDialectMicrosoft();

    @Test
    void oracleIgnoresCaseExplicitly() {
        assertEquals("lower(name) like lower(?) escape '\\'", oracle.likeCondition("name", false, true));
        assertEquals("lower(name) not like lower(?) escape '\\'", oracle.likeCondition("name", true, true));
        assertEquals("name like ? escape '\\'", oracle.likeCondition("name", false, false));
    }

    @Test
    void msSqlKeepsRelyingOnTheCollation() {
        assertEquals("name like ? escape '\\'", mssql.likeCondition("name", false, true));
        assertEquals("name not like ? escape '\\'", mssql.likeCondition("name", true, true));
    }

    @Test
    void likeWildcardsAreEscaped() {
        assertEquals("100\\% a\\_b \\\\ [x]", oracle.escapeLikePattern("100% a_b \\ [x]"));
        assertEquals("100\\% a\\_b \\\\ \\[x]", mssql.escapeLikePattern("100% a_b \\ [x]"));
    }

    @Test
    void oracleTreatsEmptyStringAsNull() {
        assertEquals("c is null", oracle.isEmptyCondition("c"));
        assertEquals("c is not null", oracle.isNotEmptyCondition("c"));
        assertEquals("(c is null or c = '')", mssql.isEmptyCondition("c"));
        assertEquals("(c is not null and c != '')", mssql.isNotEmptyCondition("c"));
    }

    @Test
    void oracleQuotesReservedWordsInUpperCase() {
        assertEquals("\"COMMENT\"", oracle.quoteColumn("comment"));
        assertEquals("\"LEVEL\"", oracle.quoteColumn("Level"));
        assertEquals("short_code", oracle.quoteColumn("short_code"));
    }

    private List<String> search(final DatabaseConnection conn, final DatabaseDialect dialect, final boolean negate, final boolean ignoreCase, final String pattern) throws DatabaseError {
        return conn.queryForList(
                SelectStatement.of("select name from thing where " + dialect.likeCondition("name", negate, ignoreCase) + " order by name"),
                ps -> ps.addString(pattern),
                rs -> rs.getString());
    }

    @Test
    void likeConditionsBehaveOnH2() throws DatabaseError {
        final SimpleDataManager manager = TestDb.newManager();
        final DatabaseDialect h2 = new DatabaseDialectH2();
        manager.callTransaction(conn -> {
            conn.update(UpdateStatement.of("create table thing (name varchar(50))"));
            for (final String name : List.of("Alpha", "alpha beta", "100% done", "100 percent", "a_c", "abc")) {
                conn.update(UpdateStatement.of("insert into thing (name) values (?)"), ps -> ps.addString(name));
            }

            // case-insensitive vs. case-sensitive
            assertEquals(List.of("Alpha", "alpha beta"), search(conn, h2, false, true, "%" + h2.escapeLikePattern("ALPHA") + "%"));
            assertEquals(List.of(), search(conn, h2, false, false, "%" + h2.escapeLikePattern("ALPHA") + "%"));
            assertEquals(List.of("Alpha"), search(conn, h2, false, false, h2.escapeLikePattern("Alpha") + "%"));
            assertEquals(List.of("alpha beta"), search(conn, h2, false, true, "%" + h2.escapeLikePattern("BETA")));

            // wildcards in the search value are matched literally
            assertEquals(List.of("100% done"), search(conn, h2, false, true, "%" + h2.escapeLikePattern("100%") + "%"));
            assertEquals(List.of("a_c"), search(conn, h2, false, true, "%" + h2.escapeLikePattern("a_c") + "%"));

            // negation
            assertEquals(List.of("100 percent", "100% done", "a_c", "abc"), search(conn, h2, true, true, "%" + h2.escapeLikePattern("ALPHA") + "%"));
            return null;
        });
    }

}
