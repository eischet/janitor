// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.meta;

import com.eischet.dbxs.SimpleDataManager;
import com.eischet.dbxs.TestDb;
import com.eischet.dbxs.exceptions.DatabaseError;
import com.eischet.dbxs.statements.UpdateStatement;
import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.orm.dao.OptimisticLockError;
import com.eischet.janitor.orm.meta.ModifiedDateTestCase.Note;
import com.eischet.janitor.orm.meta.ModifiedDateTestCase.NoteCollection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Entities with a version column (see {@code VERSION_FIELD}) are updated and deleted only if nobody else changed them meanwhile. */
public class OptimisticLockingTestCase extends JanitorTest {

    private SimpleDataManager manager;
    private NoteCollection collection;

    @BeforeEach
    void setUpDatabase() throws DatabaseError {
        manager = TestDb.newManager();
        collection = new NoteCollection(manager);
        manager.executeTransaction(conn -> {
            conn.update(UpdateStatement.of("create sequence seq_test_note_id start with 1"));
            conn.update(UpdateStatement.of("""
                    create table test_note (
                      note_id bigint primary key,
                      note_key varchar(50),
                      note_name varchar(100),
                      modified_at timestamp,
                      version_no bigint
                    )"""));
        });
    }

    private Note insert(final String key) throws DatabaseError {
        final Note note = new Note();
        note.setKey(key);
        manager.executeTransaction(conn -> collection.getNoteDao().insert(conn, note));
        return note;
    }

    private Note load(final long id) throws DatabaseError {
        return manager.callTransaction(conn -> collection.getNoteDao().findById(conn, id));
    }

    @Test
    public void everyUpdateCountsTheVersionUp() throws DatabaseError {
        final Note note = insert("a");
        assertEquals(0, note.getVersion());
        note.setName("one");
        manager.executeTransaction(conn -> collection.getNoteDao().update(conn, note));
        assertEquals(1, note.getVersion());
        assertEquals(1, load(note.getId()).getVersion());
        note.setName("two");
        manager.executeTransaction(conn -> collection.getNoteDao().update(conn, note));
        assertEquals(2, load(note.getId()).getVersion());
        assertEquals("two", load(note.getId()).getName());
    }

    @Test
    public void aStaleUpdateIsRefusedAndChangesNothing() throws DatabaseError {
        final Note original = insert("b");
        final Note mine = load(original.getId());
        final Note theirs = load(original.getId());
        theirs.setName("theirs");
        manager.executeTransaction(conn -> collection.getNoteDao().update(conn, theirs));

        mine.setName("mine");
        final OptimisticLockError error = assertThrows(OptimisticLockError.class, () -> manager.executeTransaction(conn -> collection.getNoteDao().update(conn, mine)));
        assertEquals(original.getId(), error.getId());
        assertEquals(0, error.getExpectedVersion());
        assertEquals(0, mine.getVersion(), "the entity is left as it was");
        assertEquals("theirs", load(original.getId()).getName());
        assertEquals(1, load(original.getId()).getVersion());
    }

    @Test
    public void aStaleDeleteIsRefusedAndACurrentOneWorks() throws DatabaseError {
        final Note original = insert("c");
        final Note stale = load(original.getId());
        final Note current = load(original.getId());
        current.setName("changed");
        manager.executeTransaction(conn -> collection.getNoteDao().update(conn, current));

        assertThrows(OptimisticLockError.class, () -> manager.executeTransaction(conn -> collection.getNoteDao().delete(conn, stale)));
        assertNotNull(load(original.getId()));
        manager.executeTransaction(conn -> collection.getNoteDao().delete(conn, current));
        assertNull(load(original.getId()));
    }

    @Test
    public void updatingAnEntityThatIsGoneIsRefused() throws DatabaseError {
        final Note note = insert("d");
        manager.executeTransaction(conn -> collection.getNoteDao().delete(conn, load(note.getId())));
        assertThrows(OptimisticLockError.class, () -> manager.executeTransaction(conn -> collection.getNoteDao().update(conn, note)));
    }
}
