// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.dbxs;

import com.eischet.dbxs.exceptions.DatabaseError;
import com.eischet.dbxs.statements.UpdateStatement;
import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.errors.runtime.JanitorNativeException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.lang.JNativeMethod;
import com.eischet.janitor.runtime.OutputCatchingTestRuntime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The message of a {@link DatabaseError} for a failed statement is short enough for a log: what the database said first, then the
 * statement in a shortened form, then the arguments labeled with their columns; and a script error that wraps it lists the statement
 * only once.
 */
public class DatabaseErrorMessageTestCase extends JanitorTest {

    private SimpleDataManager manager;

    @BeforeEach
    void setUpDatabase() throws DatabaseError {
        manager = TestDb.newManager();
        manager.executeTransaction(conn -> {
            conn.update(UpdateStatement.of("create table e_item (item_id bigint primary key, item_sc varchar(20) not null, item_n varchar(200), item_rmk varchar(200))"));
            conn.update(UpdateStatement.of("create unique index e_item_ux on e_item (item_sc)"));
            conn.update(UpdateStatement.of("insert into e_item (item_id, item_sc, item_n) values (1, 'A', 'first')"));
            conn.update(UpdateStatement.of("insert into e_item (item_id, item_sc, item_n) values (2, 'B', 'second')"));
        });
    }

    private DatabaseError insertDuplicate() {
        return assertThrows(DatabaseError.class, () -> manager.executeTransaction(conn -> conn.update(
                UpdateStatement.of("insert into e_item (item_id, item_sc, item_n, item_rmk) values (?, ?, ?, ?)"),
                ps -> ps.addLong(3L).addString("A").addString("x".repeat(100)).addString(null))));
    }

    @Test
    void anInsertIsShortenedAndItsValuesAreLabeled() {
        final DatabaseError error = insertDuplicate();
        final List<String> lines = error.getMessage().lines().toList();
        assertEquals(4, lines.size(), error.getMessage());
        assertTrue(lines.get(0).contains("E_ITEM_UX"), "what the database said comes first: " + lines.get(0));
        assertFalse(lines.get(0).contains("http"), "only the first line of the driver's message");
        assertEquals("  statement: insert into e_item (4 columns)", lines.get(1));
        assertEquals("  values: item_id=3, item_sc='A', item_n='" + "x".repeat(60) + "... (100 chars)'", lines.get(2));
        assertEquals("  null: item_rmk", lines.get(3));
        assertFalse(error.getMessage().contains("Arg{"));

        assertEquals("insert into e_item (item_id, item_sc, item_n, item_rmk) values (?, ?, ?, ?)", error.getSql(), "the full statement is still there");
        assertEquals(4, error.getArguments().size());
    }

    @Test
    void anUpdateIsShortenedAndItsValuesAreLabeled() {
        final DatabaseError error = assertThrows(DatabaseError.class, () -> manager.executeTransaction(conn -> conn.update(
                UpdateStatement.of("update e_item set item_sc = ?, item_n = ? where item_id = ?"),
                ps -> ps.addString("A").addString("renamed").addLong(2L))));
        final List<String> lines = error.getMessage().lines().toList();
        assertEquals(3, lines.size(), error.getMessage());
        assertEquals("  statement: update e_item set (2 columns) where item_id = ?", lines.get(1));
        assertEquals("  values: item_sc='A', item_n='renamed', item_id=2", lines.get(2));
    }

    @Test
    void otherStatementsLabelTheirValuesByComparison() {
        final DatabaseError error = assertThrows(DatabaseError.class, () -> manager.executeTransaction(conn -> conn.update(
                UpdateStatement.of("insert into e_item (item_id, item_sc) select item_id + ?, ? from e_item where item_id = ?"),
                ps -> ps.addLong(10L).addString("A").addLong(1L))));
        final List<String> lines = error.getMessage().lines().toList();
        assertEquals("  statement: insert into e_item (item_id, item_sc) select item_id + ?, ? from e_item where item_id = ?", lines.get(1));
        assertEquals("  values: #1=10, #2='A', item_id=1", lines.get(2), "values without a comparison are labeled by their position");
    }

    @Test
    void aScriptErrorListsTheStatementOnce() {
        final DatabaseError dbError = insertDuplicate();
        // as a host would report it: the script error names the operation and what the driver said
        final String reason = dbError.getCause().getMessage().lines().findFirst().orElseThrow();
        final JNativeMethod merge = new JNativeMethod((process, args) -> {
            throw new JanitorNativeException(process, "merge: " + reason, dbError);
        });
        final OutputCatchingTestRuntime runtime = OutputCatchingTestRuntime.fresh();
        final JanitorRuntimeException error = assertThrows(JanitorRuntimeException.class,
                () -> runtime.compile("mergeScript", "merge();").run(g -> g.bind("merge", merge)));
        final String message = error.getMessage();
        assertEquals(1, count(message, "statement: insert into e_item"), message);
        assertEquals(1, count(message, "caused by DatabaseError"), message);
        assertEquals(2, count(message, "E_ITEM_UX"), "named by the script error and by the database error, not again by the driver's exceptions: " + message);
    }

    private static int count(final String text, final String part) {
        int count = 0;
        for (int i = text.indexOf(part); i >= 0; i = text.indexOf(part, i + part.length())) {
            count++;
        }
        return count;
    }
}
