// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.dbxs;

import com.eischet.dbxs.dialects.DatabaseDialectH2;
import com.eischet.dbxs.exceptions.DatabaseError;
import com.eischet.dbxs.statements.SelectStatement;
import com.eischet.dbxs.statements.UpdateStatement;
import com.eischet.janitor.JanitorTest;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A read-only data manager must not change the database, whatever the JDBC driver makes of {@code setReadOnly}:
 * neither updates, inserts nor deletes get through, while reading works as usual.
 */
public class SimpleDataManagerReadOnlyTestCase extends JanitorTest {

    private static final int ROWS = 5000;

    @Test
    public void aReadOnlyManagerRefusesEveryWriteAndLeavesTheDataAlone() throws DatabaseError {
        final DataSource ds = TestDb.newDataSource();
        final SimpleDataManager writer = new SimpleDataManager("writer", ds, new DatabaseDialectH2(), null, null);
        writer.executeTransaction(conn -> {
            conn.update(new UpdateStatement("create table items (id int primary key, name varchar(40))"));
            for (int i = 1; i <= ROWS; i++) {
                final int id = i;
                conn.update(new UpdateStatement("insert into items (id, name) values (?, ?)"), ps -> ps.addLong(id).addString("item " + id));
            }
        });
        assertFalse(writer.isReadOnly());

        final SimpleDataManager reader = new SimpleDataManager("reader", ds, new DatabaseDialectH2(), null, null, true);
        assertTrue(reader.isReadOnly());

        assertThrows(DatabaseError.class, () -> reader.executeTransaction(conn -> conn.update(new UpdateStatement("update items set name = 'changed'"))));
        assertThrows(DatabaseError.class, () -> reader.executeTransaction(conn -> conn.update(new UpdateStatement("delete from items where id > 10"))));
        assertThrows(DatabaseError.class, () -> reader.executeTransaction(conn -> conn.update(new UpdateStatement("insert into items (id, name) values (?, ?)"),
                ps -> ps.addLong(ROWS + 1).addString("new"))));
        assertThrows(DatabaseError.class, () -> reader.executeTransaction(conn -> conn.insertOneRowAndReturnGeneratedKey(
                new UpdateStatement("insert into items (id, name) values (" + (ROWS + 2) + ", 'new')"), rs -> rs.getLong(1))));

        assertEquals((long) ROWS, (long) reader.callTransaction(conn -> (long) conn.queryForInt(new SelectStatement("select count(*) from items"))));
        assertEquals(0L, (long) reader.callTransaction(conn -> (long) conn.queryForInt(new SelectStatement("select count(*) from items where name not like 'item %'"))));
    }

}
