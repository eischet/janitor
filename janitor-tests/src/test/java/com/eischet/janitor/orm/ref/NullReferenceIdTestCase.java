// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.ref;

import com.eischet.dbxs.DataManager;
import com.eischet.dbxs.SimpleDataManager;
import com.eischet.dbxs.TestDb;
import com.eischet.dbxs.exceptions.DatabaseError;
import com.eischet.dbxs.statements.SelectStatement;
import com.eischet.dbxs.statements.UpdateStatement;
import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.orm.JanitorOrm;
import com.eischet.janitor.orm.dao.FilterQuery;
import com.eischet.janitor.orm.dao.GenericDao;
import com.eischet.janitor.orm.dao.OrmDaoCollection;
import com.eischet.janitor.orm.dao.Uplink;
import com.eischet.janitor.orm.entity.OrmEntity;
import com.eischet.janitor.orm.filter.FilterExpression;
import com.eischet.janitor.orm.filter.FilterOperator;
import com.eischet.janitor.orm.meta.EntityDispatchTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * References to nothing in schemas that do not use NULL for them, see {@link JanitorOrm.MetaData#NULL_REFERENCE_ID}: Assyst declares
 * its reference columns {@code NOT NULL} and points "nowhere" to a system row with the id 0.
 * <p>
 * A thing has an owner (configured by the collection) and a deputy (configured on the column itself, always 0).
 * </p>
 */
public class NullReferenceIdTestCase extends JanitorTest {

    public static class Owner implements OrmEntity {
        static final ForeignKeyNull<Owner> NULL = new ForeignKeyNull<>(Owner.class);
        static final EntityDispatchTable<Owner, Things> DISPATCH = new EntityDispatchTable<>(Owner.class, up -> new Owner(), NULL, Things::getOwnerDao);

        static {
            DISPATCH.dbTable("n_owner", "owner_id", "owner_key", "owner_name", "seq_n_owner");
            DISPATCH.addLongColumn("id", "owner_id", Owner::getId, Owner::setId);
            DISPATCH.addStringColumn("key", "owner_key", Owner::getKey, Owner::setKey, 20);
            DISPATCH.addStringColumn("name", "owner_name", Owner::getName, Owner::setName, 50);
        }

        private long id;
        private String key;
        private String name;

        @Override public long getId() { return id; }
        @Override public void setId(final long id) { this.id = id; }
        @Override public String getKey() { return key; }
        @Override public void setKey(final String key) { this.key = key; }
        @Override public String getName() { return name; }
        @Override public void setName(final String name) { this.name = name; }
        @Override public boolean isSoftDeleted() { return false; }
        @Override public void setSoftDeleted(final boolean softDeleted) { }
    }

    public static class Thing implements OrmEntity {
        static final ForeignKeyNull<Thing> NULL = new ForeignKeyNull<>(Thing.class);
        static final EntityDispatchTable<Thing, Things> DISPATCH = new EntityDispatchTable<>(Thing.class, Thing::new, NULL, Things::getThingDao);

        static {
            DISPATCH.dbTable("n_thing", "thing_id", "thing_key", "thing_name", "seq_n_thing");
            DISPATCH.addLongColumn("id", "thing_id", Thing::getId, Thing::setId);
            DISPATCH.addStringColumn("key", "thing_key", Thing::getKey, Thing::setKey, 20);
            DISPATCH.addStringColumn("name", "thing_name", Thing::getName, Thing::setName, 50);
            DISPATCH.addReference("owner", "owner_id", Owner.DISPATCH, Thing::getOwner, Thing::setOwner, thing -> thing.source);
            DISPATCH.addReference("deputy", "deputy_id", Owner.DISPATCH, Thing::getDeputy, Thing::setDeputy, thing -> thing.source)
                    .setMetaData(JanitorOrm.MetaData.NULL_REFERENCE_ID, 0L);
        }

        private final Things source;
        private long id;
        private String key;
        private String name;
        private ForeignKey<Owner> owner = Owner.NULL;
        private ForeignKey<Owner> deputy = Owner.NULL;

        Thing(final Things source) { this.source = source; }

        @Override public long getId() { return id; }
        @Override public void setId(final long id) { this.id = id; }
        @Override public String getKey() { return key; }
        @Override public void setKey(final String key) { this.key = key; }
        @Override public String getName() { return name; }
        @Override public void setName(final String name) { this.name = name; }
        @Override public boolean isSoftDeleted() { return false; }
        @Override public void setSoftDeleted(final boolean softDeleted) { }
        ForeignKey<Owner> getOwner() { return owner; }
        void setOwner(final ForeignKey<Owner> owner) { this.owner = owner; }
        ForeignKey<Owner> getDeputy() { return deputy; }
        void setDeputy(final ForeignKey<Owner> deputy) { this.deputy = deputy; }
    }

    public static class OwnerDao extends GenericDao<Owner, Things> {
        OwnerDao(final Things c) { super(new DispatchTable<OwnerDao>(false), c, Owner.class, Owner.DISPATCH, Owner::new); }
        @Override public @NotNull Class<Owner> getEntityClass() { return Owner.class; }
        @Override public @NotNull String getEntityClassName() { return "Owner"; }
    }

    public static class ThingDao extends GenericDao<Thing, Things> {
        ThingDao(final Things c) { super(new DispatchTable<ThingDao>(false), c, Thing.class, Thing.DISPATCH, () -> new Thing(c)); }
        @Override public @NotNull Class<Thing> getEntityClass() { return Thing.class; }
        @Override public @NotNull String getEntityClassName() { return "Thing"; }
    }

    /** A collection that writes NULL for references to nothing, unless a column says otherwise. */
    public static class Things extends OrmDaoCollection<Things> implements Uplink {
        static final DispatchTable<Things> DISPATCH = new DispatchTable<>();
        private final DataManager dataManager;
        private final OwnerDao ownerDao;
        private final ThingDao thingDao;

        Things(final DataManager dataManager) {
            super(DISPATCH, dataManager.getDialect(), null, null);
            this.dataManager = dataManager;
            this.ownerDao = new OwnerDao(this);
            this.thingDao = new ThingDao(this);
        }

        OwnerDao getOwnerDao() { return ownerDao; }
        ThingDao getThingDao() { return thingDao; }
        @Override public DataManager getDataManager() { return dataManager; }
    }

    /** A collection like Assyst: all references to nothing are the system row 0. */
    public static class SystemRowThings extends Things {
        SystemRowThings(final DataManager dataManager) {
            super(dataManager);
        }

        @Override
        public @Nullable Long getNullReferenceId() {
            return 0L;
        }
    }

    private static SimpleDataManager newDatabase(final boolean ownerNullable) throws DatabaseError {
        final SimpleDataManager manager = TestDb.newManager();
        manager.executeTransaction(conn -> {
            conn.update(UpdateStatement.of("create sequence seq_n_owner start with 1"));
            conn.update(UpdateStatement.of("create sequence seq_n_thing start with 1"));
            conn.update(UpdateStatement.of("create table n_owner (owner_id bigint primary key, owner_key varchar(20), owner_name varchar(50))"));
            conn.update(UpdateStatement.of("insert into n_owner (owner_id, owner_key, owner_name) values (0, null, 'system row')"));
            conn.update(UpdateStatement.of("create table n_thing (thing_id bigint primary key, thing_key varchar(20), thing_name varchar(50), "
                    + "owner_id bigint " + (ownerNullable ? "" : "not null ") + "references n_owner(owner_id), "
                    + "deputy_id bigint not null references n_owner(owner_id))"));
        });
        return manager;
    }

    private static Thing newThing(final Things things, final String key) {
        final Thing thing = new Thing(things);
        thing.setKey(key);
        thing.setName(key);
        return thing;
    }

    /** The raw value of a column of a thing, as it is in the database. */
    private static @Nullable Long column(final Things things, final String column, final long thingId) throws DatabaseError {
        return things.getDataManager().callTransaction(conn -> conn.queryForObject(
                SelectStatement.of("select " + column + " from n_thing where thing_id = ?"), ps -> ps.addLong(thingId), rs -> rs.getLongInstance()));
    }

    private static List<String> keys(final Things things, final FilterExpression filter) throws DatabaseError {
        return things.getDataManager().callTransaction(conn -> things.getThingDao().findByFilter(conn, FilterQuery.from(filter).withOrderByClause("order by thing_key")))
                .stream().map(Thing::getKey).toList();
    }

    @Test
    void referencesToNothingAreWrittenAsTheSystemRow() throws DatabaseError {
        final Things things = new SystemRowThings(newDatabase(false));
        final Thing thing = newThing(things, "T1"); // neither owner nor deputy given, like a new Assyst item without cost centre
        things.getDataManager().executeTransaction(conn -> things.getThingDao().insert(conn, thing));
        assertEquals(0L, column(things, "owner_id", thing.getId()));
        assertEquals(0L, column(things, "deputy_id", thing.getId()));

        thing.setOwner(new ForeignKeyInteger<>(0, things.getOwnerDao())); // an explicit 0, as scripts did as a workaround
        things.getDataManager().executeTransaction(conn -> things.getThingDao().update(conn, thing));
        assertEquals(0L, column(things, "owner_id", thing.getId()));
    }

    @Test
    void theSystemRowIsReadAsNullReference() throws DatabaseError {
        final Things things = new SystemRowThings(newDatabase(false));
        final Thing thing = newThing(things, "T1");
        things.getDataManager().executeTransaction(conn -> things.getThingDao().insert(conn, thing));
        final Thing loaded = things.getDataManager().callTransaction(conn -> things.getThingDao().findById(conn, thing.getId()));
        assertNotNull(loaded);
        assertSame(Owner.NULL, loaded.getOwner());
        assertSame(Owner.NULL, loaded.getDeputy());
    }

    @Test
    void realReferencesAreWrittenAsTheirId() throws DatabaseError {
        final Things things = new SystemRowThings(newDatabase(false));
        final Thing thing = newThing(things, "T1");
        things.getDataManager().executeTransaction(conn -> {
            final Owner owner = new Owner();
            owner.setKey("ACME");
            owner.setName("Acme");
            things.getOwnerDao().insert(conn, owner);
            // by short code, in the same transaction that created the owner: must be looked up on this connection
            thing.setOwner(new ForeignKeyString<>("ACME", things.getOwnerDao()));
            thing.setDeputy(new ForeignKeyInteger<>(owner.getId(), things.getOwnerDao()));
            things.getThingDao().insert(conn, thing);
        });
        final long ownerId = things.getDataManager().callTransaction(conn -> things.getOwnerDao().findByKey(conn, "ACME")).getId();
        assertNotEquals(0L, ownerId);
        assertEquals(ownerId, column(things, "owner_id", thing.getId()));
        assertEquals(ownerId, column(things, "deputy_id", thing.getId()));
        final Thing loaded = things.getDataManager().callTransaction(conn -> things.getThingDao().findById(conn, thing.getId()));
        assertNotNull(loaded);
        assertEquals(ownerId, loaded.getOwner().getId());
        assertFalse(loaded.getOwner().isNull());
    }

    @Test
    void anUnknownShortCodeIsAnErrorAndNotNull() throws DatabaseError {
        final Things things = new Things(newDatabase(true));
        final Thing thing = newThing(things, "T1");
        thing.setOwner(new ForeignKeyString<>("NOPE", things.getOwnerDao()));
        assertThrows(DatabaseError.class, () -> things.getDataManager().executeTransaction(conn -> things.getThingDao().insert(conn, thing)));
        assertEquals(List.of(), keys(things, new FilterExpression("key", FilterOperator.ISNOTNULL)));
    }

    @Test
    void withoutConfigurationReferencesToNothingAreNull() throws DatabaseError {
        final Things things = new Things(newDatabase(true));
        final Thing thing = newThing(things, "T1");
        things.getDataManager().executeTransaction(conn -> things.getThingDao().insert(conn, thing));
        assertNull(column(things, "owner_id", thing.getId()));
        assertEquals(0L, column(things, "deputy_id", thing.getId()), "the column's own configuration applies without a collection default");
    }

    @Test
    void filtersForNullFindTheSystemRow() throws DatabaseError {
        final Things things = new SystemRowThings(newDatabase(false));
        things.getDataManager().executeTransaction(conn -> {
            final Owner owner = new Owner();
            owner.setKey("ACME");
            things.getOwnerDao().insert(conn, owner);
            things.getThingDao().insert(conn, newThing(things, "WITHOUT"));
            final Thing with = newThing(things, "WITH");
            with.setOwner(new ForeignKeyInteger<>(owner.getId(), things.getOwnerDao()));
            things.getThingDao().insert(conn, with);
        });
        assertEquals(List.of("WITHOUT"), keys(things, new FilterExpression("owner", FilterOperator.ISNULL)));
        assertEquals(List.of("WITH"), keys(things, new FilterExpression("owner", FilterOperator.ISNOTNULL)));
    }
}
