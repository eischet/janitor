package com.eischet.janitor.orm.meta;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.metadata.PropertyHandle;
import com.eischet.janitor.orm.JanitorOrm;
import com.eischet.janitor.orm.dao.Uplink;
import com.eischet.janitor.orm.entity.LazyLoadedString;
import com.eischet.janitor.orm.entity.OrmEntity;
import com.eischet.janitor.orm.ref.ForeignKey;
import com.eischet.janitor.orm.ref.ForeignKeyNull;
import com.eischet.janitor.orm.sql.ColumnTypeHint;
import com.eischet.janitor.versioning.Version;
import com.eischet.janitor.versioning.VersionRange;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class OrmPropertyHandleTestCase extends JanitorTest {

    static class Target implements OrmEntity {
        static final ForeignKeyNull<Target> NULL = new ForeignKeyNull<>(Target.class);
        static final EntityDispatchTable<Target, Uplink> DISPATCH = new EntityDispatchTable<>(Target.class, up -> new Target(), NULL, up -> null);

        private long id;
        private String key;
        private String name;
        private boolean softDeleted;

        @Override public long getId() { return id; }
        @Override public void setId(final long id) { this.id = id; }
        @Override public String getKey() { return key; }
        @Override public void setKey(final String key) { this.key = key; }
        @Override public String getName() { return name; }
        @Override public void setName(final String name) { this.name = name; }
        @Override public boolean isSoftDeleted() { return softDeleted; }
        @Override public void setSoftDeleted(final boolean softDeleted) { this.softDeleted = softDeleted; }
    }

    static class Thing implements OrmEntity {
        static final ForeignKeyNull<Thing> NULL = new ForeignKeyNull<>(Thing.class);
        static final EntityDispatchTable<Thing, Uplink> DISPATCH = new EntityDispatchTable<>(Thing.class, up -> new Thing(), NULL, up -> null);

        static final OrmPropertyHandle<Thing, String> LABEL = DISPATCH.addStringColumn("label", "label_col", Thing::getLabel, Thing::setLabel, 42);
        static final OrmPropertyHandle<Thing, String> NOTES = DISPATCH.addTextColumn("notes", "notes_col", Thing::getNotes, Thing::setNotes);
        static final OrmPropertyHandle<Thing, String> LAZY = DISPATCH.addLazyTextColumn("lazy", "lazy_col", it -> it.lazy).since(Version.ofWithRuntimeError("2.0.0"));
        static final OrmPropertyHandle<Thing, Integer> COUNT = DISPATCH.addIntegerColumn("count", "count_col", Thing::getCount, Thing::setCount)
                .until(Version.ofWithRuntimeError("5.0.0"));
        static final OrmPropertyHandle<Thing, ForeignKey<Target>> TARGET = DISPATCH.addReference("target", "target_id", Target.DISPATCH, Thing::getTarget, Thing::setTarget, it -> null);

        private long id;
        private String key;
        private String name;
        private boolean softDeleted;
        private String label;
        private String notes;
        private int count;
        private ForeignKey<Target> target = Target.NULL;
        final LazyLoadedString lazy = new LazyLoadedString(() -> null, () -> id, "lazy_col");

        @Override public long getId() { return id; }
        @Override public void setId(final long id) { this.id = id; }
        @Override public String getKey() { return key; }
        @Override public void setKey(final String key) { this.key = key; }
        @Override public String getName() { return name; }
        @Override public void setName(final String name) { this.name = name; }
        @Override public boolean isSoftDeleted() { return softDeleted; }
        @Override public void setSoftDeleted(final boolean softDeleted) { this.softDeleted = softDeleted; }
        public String getLabel() { return label; }
        public void setLabel(final String label) { this.label = label; }
        public String getNotes() { return notes; }
        public void setNotes(final String notes) { this.notes = notes; }
        public int getCount() { return count; }
        public void setCount(final int count) { this.count = count; }
        public ForeignKey<Target> getTarget() { return target; }
        public void setTarget(final ForeignKey<Target> target) { this.target = target; }
    }

    @Test
    void stringColumn() {
        assertEquals("label", Thing.LABEL.getName());
        assertEquals("label_col", Thing.LABEL.getColumnName());
        assertEquals(ColumnTypeHint.NVARCHAR, Thing.LABEL.getColumnType());
        assertEquals(42, Thing.LABEL.getMaxLength());
        assertFalse(Thing.LABEL.isLazyLoaded());
        assertNull(Thing.LABEL.getReferencedClassName());
        assertNull(Thing.LABEL.getVersionRange());
    }

    @Test
    void textColumnHasNoMaxLength() {
        assertEquals(ColumnTypeHint.NCLOB, Thing.NOTES.getColumnType());
        assertNull(Thing.NOTES.getMaxLength());
    }

    @Test
    void lazyColumn() {
        assertTrue(Thing.LAZY.isLazyLoaded());
        assertEquals("lazy_col", Thing.LAZY.getColumnName());
    }

    @Test
    void referenceColumn() {
        assertEquals("target_id", Thing.TARGET.getColumnName());
        assertEquals(ColumnTypeHint.INT, Thing.TARGET.getColumnType());
        assertEquals("Target", Thing.TARGET.getReferencedClassName());
    }

    @Test
    void versionRange() {
        final Version v1 = Version.ofWithRuntimeError("1.0.0");
        final Version v2 = Version.ofWithRuntimeError("2.0.0");
        final Version v5 = Version.ofWithRuntimeError("5.0.0");
        final Version v6 = Version.ofWithRuntimeError("6.0.0");

        // since
        assertNotNull(Thing.LAZY.getVersionRange());
        assertFalse(Thing.LAZY.isAvailableIn(v1));
        assertTrue(Thing.LAZY.isAvailableIn(v2));
        assertTrue(Thing.LAZY.isAvailableIn(v6));

        // until
        assertTrue(Thing.COUNT.isAvailableIn(v1));
        assertTrue(Thing.COUNT.isAvailableIn(v5));
        assertFalse(Thing.COUNT.isAvailableIn(v6));

        // no range, or unknown schema version: always available
        assertTrue(Thing.LABEL.isAvailableIn(v1));
        assertTrue(Thing.LAZY.isAvailableIn(null));
        assertTrue(OrmPropertyHandle.isAvailableIn(null, v1));
    }

    @Test
    void handleReadsThroughToTable() {
        // the same metadata is visible through the table and through the handle, in both directions
        assertEquals("label_col", Thing.DISPATCH.getMetaData("label", JanitorOrm.MetaData.COLUMN_NAME));
        final VersionRange range = VersionRange.startingWith(Version.ofWithRuntimeError("9.0.0"));
        Thing.NOTES.setMetaData(JanitorOrm.MetaData.VERSION_RANGE, range);
        assertSame(range, Thing.DISPATCH.getMetaData("notes", JanitorOrm.MetaData.VERSION_RANGE));
        assertSame(range, Thing.NOTES.getVersionRange());
    }

    @Test
    void ofWrapsPlainHandles() {
        final EntityDispatchTable<Thing, Uplink> table = Thing.DISPATCH.extend();
        final PropertyHandle<Thing, String> plain = table.addStringProperty("plain", Thing::getLabel, Thing::setLabel);
        plain.setMetaData(JanitorOrm.MetaData.MAX_LENGTH, 7);
        final OrmPropertyHandle<Thing, String> wrapped = OrmPropertyHandle.of(plain);
        assertEquals(7, wrapped.getMaxLength());
        assertSame(wrapped, OrmPropertyHandle.of(wrapped));
    }

}
