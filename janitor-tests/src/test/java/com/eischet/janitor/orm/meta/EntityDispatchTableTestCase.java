package com.eischet.janitor.orm.meta;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.orm.JanitorOrm;
import com.eischet.janitor.orm.dao.Dao;
import com.eischet.janitor.orm.dao.Uplink;
import com.eischet.janitor.orm.entity.OrmEntity;
import com.eischet.janitor.orm.ref.ForeignKeyNull;
import com.eischet.janitor.orm.sql.ColumnTypeHint;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The entity dispatch table is its own wrangler: class, constructor, null reference and ORM meta-data live in one object.
 */
public class EntityDispatchTableTestCase extends JanitorTest {

    static class Thing implements OrmEntity {
        static final ForeignKeyNull<Thing> NULL = new ForeignKeyNull<>();
        static final EntityDispatchTable<Thing, TestUplink> DISPATCH =
                new EntityDispatchTable<>(Thing.class, up -> new Thing(), NULL, up -> null);

        static {
            DISPATCH.dbTable("thing", "thing_id", "thing_key", "thing_n", "seq_thing_id");
            DISPATCH.addLongColumn("id", "thing_id", Thing::getId, Thing::setId);
            DISPATCH.addStringColumn("label", "label", Thing::getLabel, Thing::setLabel, 50);
        }

        private long id;
        private String label;
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
        public String getLabel() { return label; }
        public void setLabel(final String label) { this.label = label; }
    }

    static class TestUplink implements Uplink {
    }

    @Test
    void tableIsItsOwnWrangler() {
        final EntityWrangler<Thing, TestUplink> wrangler = Thing.DISPATCH;
        assertSame(Thing.DISPATCH, wrangler.getDispatchTable());
        assertSame(Thing.NULL, wrangler.getNullReference());
        assertEquals(Thing.class, wrangler.getWrangledClass());
        assertEquals("Thing", wrangler.getSimpleClassName());
        assertNotNull(wrangler.createNewInstance(new TestUplink()));
    }

    @Test
    void ormMetaData() {
        assertEquals("Thing", Thing.DISPATCH.getMetaData(Janitor.MetaData.CLASS));
        assertEquals("thing", Thing.DISPATCH.getMetaData(JanitorOrm.MetaData.TABLE_NAME));
        assertEquals("thing_id", Thing.DISPATCH.getMetaData(JanitorOrm.MetaData.ID_FIELD));
        assertEquals("label", Thing.DISPATCH.getMetaData("label", JanitorOrm.MetaData.COLUMN_NAME));
        assertEquals(ColumnTypeHint.NVARCHAR, Thing.DISPATCH.getMetaData("label", JanitorOrm.MetaData.COLUMN_TYPE));
        assertEquals(50, Thing.DISPATCH.getMetaData("label", JanitorOrm.MetaData.MAX_LENGTH));
    }

    @Test
    void duplicateCopiesAttributes() {
        final Thing original = new Thing();
        original.setId(7);
        original.setLabel("hello");
        final Thing copy = Thing.DISPATCH.duplicate(new TestUplink(), original);
        assertNotSame(original, copy);
        assertEquals(7, copy.getId());
        assertEquals("hello", copy.getLabel());
    }

    @Test
    void extendKeepsTheSubclass() {
        final EntityDispatchTable<Thing, TestUplink> child = Thing.DISPATCH.extend();
        child.addStringColumn("extra", "extra", Thing::getLabel, Thing::setLabel, 10);
        assertSame(Thing.NULL, child.getNullReference());
        assertEquals(Thing.class, child.getWrangledClass());
        assertEquals("thing", child.getMetaData(JanitorOrm.MetaData.TABLE_NAME), "inherited from parent");
        assertTrue(child.has("extra"));
        assertFalse(Thing.DISPATCH.has("extra"));
        assertNotNull(child.createNewInstance(new TestUplink()));
    }

    @Test
    void extendHonorsApplyFlag() {
        final DispatchTable<Thing> without = new DispatchTable<Thing>(false).extend(false);
        assertFalse(without.has("apply"));
        assertTrue(new DispatchTable<Thing>(false).extend(true).has("apply"));
    }

    @Test
    void entityIndexServesTableAndNullReference() {
        final EntityIndex index = new EntityIndex().addEntity(Thing.DISPATCH);
        assertSame(Thing.DISPATCH, index.getEntity("Thing"));
        assertSame(Thing.DISPATCH, index.getOrmDispatchTable("Thing"));
        assertSame(Thing.DISPATCH, index.getEntityDispatchTable("Thing"));
        assertSame(Thing.DISPATCH, index.getEntityDispatchTable(Thing.class));
        assertSame(Thing.NULL, index.getNullReference(Thing.class));
        assertNull(index.getDaoFor(Thing.class), "no dao registered yet");
        assertNull(index.getEntityDispatchTable("Nope"));
    }

    @Test
    void entityIndexIgnoresPlainDispatchTablesForOrmLookups() {
        final EntityIndex index = new EntityIndex().addEntity(Thing.class, new DispatchTable<Thing>(false));
        assertNotNull(index.getEntity("Thing"));
        assertNull(index.getEntityDispatchTable(Thing.class));
        assertNull(index.getNullReference(Thing.class));
    }
}
