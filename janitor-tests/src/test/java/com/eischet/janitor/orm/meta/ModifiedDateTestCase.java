// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.meta;

import com.eischet.dbxs.DataManager;
import com.eischet.dbxs.SimpleDataManager;
import com.eischet.dbxs.TestDb;
import com.eischet.dbxs.exceptions.DatabaseError;
import com.eischet.dbxs.statements.UpdateStatement;
import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.orm.JanitorOrm;
import com.eischet.janitor.orm.dao.GenericDao;
import com.eischet.janitor.orm.dao.OrmDaoCollection;
import com.eischet.janitor.orm.dao.Uplink;
import com.eischet.janitor.orm.entity.HasModifiedDate;
import com.eischet.janitor.orm.entity.OrmEntity;
import com.eischet.janitor.orm.entity.Versioned;
import com.eischet.janitor.orm.ref.ForeignKeyNull;
import com.eischet.janitor.orm.sql.ColumnTypeHint;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

/** The optional modification date and version column: declaring them, and the DAO maintaining the modification date. */
public class ModifiedDateTestCase extends JanitorTest {

    static final LocalDateTime FIRST = LocalDateTime.of(2026, 1, 2, 3, 4, 5);
    static final LocalDateTime SECOND = LocalDateTime.of(2026, 6, 7, 8, 9, 10);

    public static class Note implements OrmEntity, HasModifiedDate, Versioned {
        static final ForeignKeyNull<Note> NULL = new ForeignKeyNull<>(Note.class);
        static final EntityDispatchTable<Note, NoteCollection> DISPATCH = new EntityDispatchTable<>(Note.class, up -> new Note(), NULL, NoteCollection::getNoteDao);

        static {
            DISPATCH.dbTable("test_note", "note_id", "note_key", "note_name", "seq_test_note_id")
                    .modifiedColumn("modified_at")
                    .versionColumn("version_no");
            DISPATCH.addLongColumn("id", "note_id", Note::getId, Note::setId);
            DISPATCH.addStringColumn("key", "note_key", Note::getKey, Note::setKey, 50);
            DISPATCH.addStringColumn("name", "note_name", Note::getName, Note::setName, 100);
            DISPATCH.addDateTimeColumn("modifiedDate", "modified_at", Note::getModifiedDate, Note::setModifiedDate);
            DISPATCH.addLongColumn("version", "version_no", Note::getVersion, Note::setVersion);
        }

        private long id;
        private String key;
        private String name;
        private LocalDateTime modifiedDate;
        private long version;

        @Override public long getId() { return id; }
        @Override public void setId(final long id) { this.id = id; }
        @Override public String getKey() { return key; }
        @Override public void setKey(final String key) { this.key = key; }
        @Override public String getName() { return name; }
        @Override public void setName(final String name) { this.name = name; }
        @Override public boolean isSoftDeleted() { return false; }
        @Override public void setSoftDeleted(final boolean softDeleted) { }
        @Override public @Nullable LocalDateTime getModifiedDate() { return modifiedDate; }
        @Override public void setModifiedDate(final @Nullable LocalDateTime updateDate) { this.modifiedDate = updateDate; }
        @Override public long getVersion() { return version; }
        @Override public void setVersion(final long version) { this.version = version; }
    }

    public static class NoteDao extends GenericDao<Note, NoteCollection> {
        NoteDao(final NoteCollection collection) {
            super(new DispatchTable<NoteDao>(false), collection, Note.class, Note.DISPATCH, Note::new);
        }

        @Override public @NotNull Class<Note> getEntityClass() { return Note.class; }
        @Override public @NotNull String getEntityClassName() { return "Note"; }
    }

    public static class NoteCollection extends OrmDaoCollection<NoteCollection> implements Uplink {
        static final DispatchTable<NoteCollection> DISPATCH = new DispatchTable<>();

        private final DataManager dataManager;
        private final NoteDao noteDao;
        private Clock clock = Clock.fixed(FIRST.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);

        NoteCollection(final DataManager dataManager) {
            super(DISPATCH, dataManager.getDialect(), null, null);
            this.dataManager = dataManager;
            this.noteDao = new NoteDao(this);
        }

        NoteDao getNoteDao() { return noteDao; }
        @Override public DataManager getDataManager() { return dataManager; }
        @Override public @NotNull Clock getClock() { return clock; }
        void setClock(final Instant instant) { this.clock = Clock.fixed(instant, ZoneOffset.UTC); }
    }

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

    @Test
    public void theColumnsAreDeclaredAsMetaData() {
        assertEquals("modified_at", Note.DISPATCH.getMetaData(JanitorOrm.MetaData.MODIFIED_FIELD));
        assertEquals("version_no", Note.DISPATCH.getMetaData(JanitorOrm.MetaData.VERSION_FIELD));
    }

    @Test
    public void decimalPrecisionAndScaleCanBeDeclared() {
        final var scratch = new EntityDispatchTable<>(Note.class, up -> new Note(), Note.NULL, NoteCollection::getNoteDao);
        final var handle = scratch.addDateTimeColumn("scratch", "scratch_col", Note::getModifiedDate, Note::setModifiedDate)
                .setMetaData(JanitorOrm.MetaData.COLUMN_TYPE, ColumnTypeHint.DECIMAL)
                .decimal(10, 2);
        assertEquals(10, handle.getDecimalPrecision());
        assertEquals(2, handle.getDecimalScale());
    }

    @Test
    public void insertSetsTheModifiedDate() throws DatabaseError {
        final Note note = new Note();
        note.setKey("n1");
        manager.executeTransaction(conn -> collection.getNoteDao().insert(conn, note));
        assertEquals(FIRST, note.getModifiedDate());
        final Note loaded = manager.callTransaction(conn -> collection.getNoteDao().findById(conn, note.getId()));
        assertEquals(FIRST, loaded.getModifiedDate());
    }

    @Test
    public void updateSetsTheModifiedDateAgain() throws DatabaseError {
        final Note note = new Note();
        note.setKey("n2");
        manager.executeTransaction(conn -> collection.getNoteDao().insert(conn, note));
        collection.setClock(SECOND.toInstant(ZoneOffset.UTC));
        note.setName("renamed");
        manager.executeTransaction(conn -> collection.getNoteDao().update(conn, note));
        final Note loaded = manager.callTransaction(conn -> collection.getNoteDao().findById(conn, note.getId()));
        assertEquals(SECOND, loaded.getModifiedDate());
        assertEquals("renamed", loaded.getName());
    }
}
