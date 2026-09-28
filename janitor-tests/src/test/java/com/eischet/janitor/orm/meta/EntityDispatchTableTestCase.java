package com.eischet.janitor.orm.meta;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.orm.JanitorOrm;
import com.eischet.dbxs.DataManager;
import com.eischet.janitor.orm.dao.GenericDao;
import com.eischet.janitor.orm.dao.OrmDaoCollection;
import com.eischet.janitor.orm.dao.Uplink;
import com.eischet.janitor.orm.entity.OrmEntity;
import com.eischet.janitor.orm.ref.ForeignKeyNull;
import com.eischet.janitor.orm.sql.ColumnTypeHint;
import org.jetbrains.annotations.NotNull;
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

    static class ThingDao extends GenericDao<Thing> {
        ThingDao(final OrmDaoCollection<?> collection, final DispatchTable<Thing> dispatch) {
            super(new DispatchTable<ThingDao>(false), collection, Thing.class, dispatch, Thing::new);
        }

        @Override
        public @NotNull Class<Thing> getEntityClass() {
            return Thing.class;
        }

        @Override
        public @NotNull String getEntityClassName() {
            return "Thing";
        }
    }

    static class TestCollection extends OrmDaoCollection<TestCollection> implements Uplink {
        static final DispatchTable<TestCollection> DISPATCH = new DispatchTable<>();

        static {
            OrmDaoCollection.addRegistryProperties(DISPATCH);
        }

        private final ThingDao thingDao;

        TestCollection(final DispatchTable<Thing> thingDispatch) {
            super(DISPATCH);
            thingDao = new ThingDao(this, thingDispatch);
        }

        ThingDao getThingDao() {
            return thingDao;
        }

        @Override
        public DataManager getDataManager() {
            return null;
        }
    }

    @Test
    void daoRegistersItselfAndItsDispatchTable() {
        final TestCollection collection = new TestCollection(Thing.DISPATCH);
        assertSame(collection.getThingDao(), collection.getDao("Thing"));
        assertSame(Thing.DISPATCH, collection.getEntity("Thing"));
        assertSame(Thing.DISPATCH, collection.getOrmDispatchTable("Thing"));
        assertSame(Thing.DISPATCH, collection.getEntityDispatchTable("Thing"));
        assertSame(Thing.DISPATCH, collection.getEntityDispatchTable(Thing.class));
        assertSame(Thing.NULL, collection.getNullReference(Thing.class));
        assertEquals(java.util.Set.of("Thing"), collection.getEntityNames());
        assertTrue(collection.getJoinNames().isEmpty());
        assertNull(collection.getEntityDispatchTable("Nope"));
        assertNull(collection.getDao("Nope"));
    }

    @Test
    void collectionsAreIndependent() {
        final TestCollection one = new TestCollection(Thing.DISPATCH);
        final TestCollection two = new TestCollection(Thing.DISPATCH);
        assertNotSame(one.getDao("Thing"), two.getDao("Thing"));
    }

    @Test
    void plainDispatchTablesAreIgnoredForOrmLookups() {
        final DispatchTable<Thing> plain = new DispatchTable<>(false);
        plain.setMetaData(Janitor.MetaData.CLASS, "Thing");
        plain.setMetaData(JanitorOrm.MetaData.TABLE_NAME, "thing");
        plain.setMetaData(JanitorOrm.MetaData.ID_FIELD, "thing_id");
        final TestCollection collection = new TestCollection(plain);
        assertSame(plain, collection.getEntity("Thing"));
        assertNull(collection.getOrmDispatchTable("Thing"));
        assertNull(collection.getEntityDispatchTable(Thing.class));
        assertNull(collection.getNullReference(Thing.class));
    }

    @Test
    void registryScriptProperties() {
        final TestCollection collection = new TestCollection(Thing.DISPATCH);
        assertTrue(TestCollection.DISPATCH.has("entities"));
        assertTrue(TestCollection.DISPATCH.has("joins"));
        assertNotNull(collection);
    }
}
