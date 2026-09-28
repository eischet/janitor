package com.eischet.janitor.orm.entity;

import com.eischet.dbxs.DataManager;
import com.eischet.dbxs.exceptions.DatabaseError;
import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.orm.dao.GenericDao;
import com.eischet.janitor.orm.dao.OrmDaoCollection;
import com.eischet.janitor.orm.dao.Uplink;
import com.eischet.janitor.orm.meta.EntityDispatchTable;
import com.eischet.janitor.orm.ref.ForeignKeyNull;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Covers LazyLoadedString itself, plus the two GenericDao-level behaviors it depends on: excluding
 * LAZY_LOAD-marked columns from the default SELECT column list (see GenericDao#selectColumns) while
 * still writing them normally on insert/update, and GenericDao#fetchLazyColumn as the on-demand fetch
 * path. Deliberately does not use a real database -- LazyThingDao overrides fetchLazyColumn to return
 * canned values and count invocations instead.
 */
public class LazyLoadedStringTestCase extends JanitorTest {

    static class LazyThing implements OrmEntity {
        static final ForeignKeyNull<LazyThing> NULL = new ForeignKeyNull<>();
        static final EntityDispatchTable<LazyThing, TestCollection> DISPATCH =
                new EntityDispatchTable<>(LazyThing.class, LazyThing::new, NULL, up -> null);

        static {
            DISPATCH.dbTable("lazy_thing", "lazy_thing_id", "lazy_thing_key", "lazy_thing_n", "seq_lazy_thing_id");
            DISPATCH.addLongColumn("id", "lazy_thing_id", LazyThing::getId, LazyThing::setId);
            DISPATCH.addStringColumn("label", "label", LazyThing::getLabel, LazyThing::setLabel, 50);
            DISPATCH.addLazyTextColumn("bigText", "big_text", it -> it.bigText);
        }

        private final TestCollection source;
        final LazyLoadedString bigText;
        private long id;
        private String label;
        private String key;
        private String name;
        private boolean softDeleted;

        LazyThing(final TestCollection source) {
            this.source = source;
            this.bigText = new LazyLoadedString(source::getLazyThingDao, this::getId, "big_text");
        }

        @Override
        public long getId() {
            return id;
        }

        @Override
        public void setId(final long id) {
            this.id = id;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public void setKey(final String key) {
            this.key = key;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public void setName(final String name) {
            this.name = name;
        }

        @Override
        public boolean isSoftDeleted() {
            return softDeleted;
        }

        @Override
        public void setSoftDeleted(final boolean softDeleted) {
            this.softDeleted = softDeleted;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(final String label) {
            this.label = label;
        }

        public String getBigText() {
            return bigText.getValue();
        }

        public void setBigText(final String value) {
            bigText.setValue(value);
        }
    }

    /**
     * Counts fetchLazyColumn calls and returns a canned value, instead of touching a real database --
     * this is the only method LazyLoadedString ever calls to resolve itself, so overriding it is enough
     * to test the whole lazy-loading contract without a DataManager.
     */
    static class LazyThingDao extends GenericDao<LazyThing, TestCollection> {
        final AtomicInteger fetchCount = new AtomicInteger();
        String canned = "fetched-from-db";

        LazyThingDao(final TestCollection collection, final EntityDispatchTable<LazyThing, TestCollection> dispatch) {
            super(new DispatchTable<LazyThingDao>(false), collection, LazyThing.class, dispatch, () -> new LazyThing(collection));
        }

        @Override
        public String fetchLazyColumn(final long id, final @NotNull String column) throws DatabaseError {
            fetchCount.incrementAndGet();
            assertEquals("big_text", column);
            return canned;
        }

        @Override
        public @NotNull Class<LazyThing> getEntityClass() {
            return LazyThing.class;
        }

        @Override
        public @NotNull String getEntityClassName() {
            return "LazyThing";
        }

        List<String> selectColumnsForTest() {
            return selectColumns;
        }

        List<String> columnsForTest() {
            return columns;
        }
    }

    static class TestCollection extends OrmDaoCollection<TestCollection> implements Uplink {
        static final DispatchTable<TestCollection> DISPATCH = new DispatchTable<>();

        static {
            OrmDaoCollection.addRegistryProperties(DISPATCH);
        }

        private final LazyThingDao lazyThingDao;

        TestCollection() {
            super(DISPATCH);
            lazyThingDao = new LazyThingDao(this, LazyThing.DISPATCH);
        }

        LazyThingDao getLazyThingDao() {
            return lazyThingDao;
        }

        @Override
        public DataManager getDataManager() {
            return null;
        }
    }

    @Test
    void aBrandNewEntityWithIdZeroNeverFetches() {
        final TestCollection collection = new TestCollection();
        final LazyThing thing = new LazyThing(collection);
        assertNull(thing.getBigText());
        assertEquals(0, collection.getLazyThingDao().fetchCount.get(), "id 0 means not persisted yet -- there's nothing to fetch");
    }

    @Test
    void explicitlySettingTheValueNeverFetches() {
        final TestCollection collection = new TestCollection();
        final LazyThing thing = new LazyThing(collection);
        thing.setId(7);
        thing.setBigText("set directly");
        assertEquals("set directly", thing.getBigText());
        assertEquals(0, collection.getLazyThingDao().fetchCount.get(), "an explicitly set value must never trigger a fetch");
    }

    @Test
    void aPersistedEntityFetchesLazilyOnFirstAccessAndThenCaches() {
        final TestCollection collection = new TestCollection();
        final LazyThing thing = new LazyThing(collection);
        thing.setId(42); // simulates what readAllProperties would have done after loading the row

        assertFalse(thing.bigText.isLoaded());
        assertEquals("fetched-from-db", thing.getBigText());
        assertEquals(1, collection.getLazyThingDao().fetchCount.get());
        assertTrue(thing.bigText.isLoaded());

        // a second read must be served from the cached value, not fetched again
        assertEquals("fetched-from-db", thing.getBigText());
        assertEquals(1, collection.getLazyThingDao().fetchCount.get());
    }

    @Test
    void aNegativeIdIsTreatedAsPersistedAndStillFetches() {
        // explicit modification requested: id == 0 is the only "not persisted yet" sentinel; negative
        // IDs are legitimate (some existing data uses them) and must still be fetched normally.
        final TestCollection collection = new TestCollection();
        final LazyThing thing = new LazyThing(collection);
        thing.setId(-5);

        assertEquals("fetched-from-db", thing.getBigText());
        assertEquals(1, collection.getLazyThingDao().fetchCount.get(), "a negative id must still trigger a fetch, only id == 0 skips it");
    }

    @Test
    void theDaoExcludesTheLazyColumnFromSelectButKeepsItForWrites() {
        final TestCollection collection = new TestCollection();
        final LazyThingDao dao = collection.getLazyThingDao();

        assertTrue(dao.columnsForTest().contains("big_text"), "big_text must still be written on insert/update");
        assertFalse(dao.selectColumnsForTest().contains("big_text"), "big_text must be excluded from the default SELECT column list");
        assertTrue(dao.selectColumnsForTest().contains("label"), "an ordinary column must still be selected normally");
    }

}
