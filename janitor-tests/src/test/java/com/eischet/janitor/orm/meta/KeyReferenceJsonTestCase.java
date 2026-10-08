package com.eischet.janitor.orm.meta;

import com.eischet.dbxs.DatabaseConnection;
import com.eischet.dbxs.StatementConfigurator;
import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.orm.dao.Dao;
import com.eischet.janitor.orm.dao.DaoLogging;
import com.eischet.janitor.orm.dao.EntityChangeListener;
import com.eischet.janitor.orm.dao.FilterQuery;
import com.eischet.janitor.orm.dao.Uplink;
import com.eischet.janitor.orm.entity.OrmEntity;
import com.eischet.janitor.orm.filter.FilterExpression;
import com.eischet.janitor.orm.ref.ForeignKey;
import com.eischet.janitor.orm.ref.ForeignKeyInteger;
import com.eischet.janitor.orm.ref.ForeignKeyNull;
import com.eischet.janitor.orm.ref.ForeignKeyString;
import com.eischet.janitor.orm.ref.ForeignKeys;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.toolbox.listeners.ListenerRegistration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * References that live in a plain scriptable object (not an entity), as in a JSON configuration class inside an entity's
 * column: {@code addKeyReference} must read and write them as short codes, whatever kind of {@code ForeignKey} they hold.
 */
public class KeyReferenceJsonTestCase extends JanitorTest {

    static class Target implements OrmEntity {
        static final ForeignKeyNull<Target> NULL = new ForeignKeyNull<>(Target.class);
        static final EntityDispatchTable<Target, TestUplink> DISPATCH = new EntityDispatchTable<>(Target.class, up -> new Target(), NULL, up -> up.dao);

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

    static class TestUplink implements Uplink {
        final StubDao dao;

        TestUplink(final StubDao dao) {
            this.dao = dao;
        }
    }

    /** Some object that holds a reference, but is not an entity -- like a JSON settings class. */
    static class Holder extends JanitorComposed<Holder> {
        static final DispatchTable<Holder> DISPATCH = new DispatchTable<>();
        static final TestUplink UPLINK = new TestUplink(new StubDao("FROM-ID"));
        static final OrmPropertyHandle<Holder, ForeignKey<Target>> TARGET = Target.DISPATCH.addKeyReference(DISPATCH, "target", Holder::getTarget, Holder::setTarget, holder -> UPLINK);

        static final OrmPropertyHandle<Holder, List<ForeignKey<Target>>> TARGETS = Target.DISPATCH.addKeyReferenceList(DISPATCH, "targets", Holder::getTargets, Holder::setTargets, holder -> UPLINK);

        private ForeignKey<Target> target = Target.NULL;
        private List<ForeignKey<Target>> targets = new ArrayList<>();

        Holder() {
            super(DISPATCH);
        }

        ForeignKey<Target> getTarget() {
            return target;
        }

        void setTarget(final ForeignKey<Target> target) {
            this.target = target;
        }

        List<ForeignKey<Target>> getTargets() {
            return targets;
        }

        void setTargets(final List<ForeignKey<Target>> targets) {
            this.targets = targets;
        }
    }

    /** Dao stand-in that only knows how to hand out entities, see ForeignKeyHashCodeTestCase. Ids resolve to an entity with {@code keyOfIds}. */
    static class StubDao implements Dao<Target> {
        private final @Nullable String keyOfIds;

        StubDao(final @Nullable String keyOfIds) {
            this.keyOfIds = keyOfIds;
        }

        @Override public @NotNull Class<Target> getEntityClass() { return Target.class; }
        @Override public @NotNull String getEntityClassName() { return Target.class.getSimpleName(); }
        @Override public @Nullable Target findByKey(@NotNull final DatabaseConnection conn, @Nullable final String key) { throw new UnsupportedOperationException(); }
        @Override public @Nullable Target findById(@NotNull final DatabaseConnection conn, final long id) { throw new UnsupportedOperationException(); }
        @Override public @NotNull List<Target> findAll(@NotNull final DatabaseConnection conn, @Nullable final Integer limit) { throw new UnsupportedOperationException(); }
        @Override public @NotNull List<Target> findByQuery(@NotNull final DatabaseConnection conn, @NotNull final String query, @NotNull final StatementConfigurator statementConfigurator) { throw new UnsupportedOperationException(); }
        @Override public @NotNull List<Target> findByFilter(@NotNull final DatabaseConnection conn, @NotNull final FilterQuery filterQuery) { throw new UnsupportedOperationException(); }
        @Override public int countByFilter(@NotNull final DatabaseConnection conn, @Nullable final FilterExpression filterExpression) { throw new UnsupportedOperationException(); }
        @Override public void insert(@NotNull final DatabaseConnection conn, @NotNull final Target record) { throw new UnsupportedOperationException(); }
        @Override public void update(@NotNull final DatabaseConnection conn, @NotNull final Target record) { throw new UnsupportedOperationException(); }
        @Override public void delete(@NotNull final DatabaseConnection conn, @NotNull final Target record) { throw new UnsupportedOperationException(); }
        @Override public @NotNull List<Target> findByAssociation(@NotNull final DatabaseConnection conn, final String foreignKeyColumn, final long foreignKeyValue) { throw new UnsupportedOperationException(); }
        @Override public @NotNull List<Target> lazyLoadByAssociation(final String foreignKeyColumn, final OrmEntity parentEntity) { throw new UnsupportedOperationException(); }

        @Override
        public @Nullable Target lazyLoadById(final long id) {
            final Target entity = new Target();
            entity.setId(id);
            entity.setKey(keyOfIds);
            return entity;
        }

        @Override
        public @Nullable Target lazyLoadByKey(final String key) {
            final Target entity = new Target();
            entity.setKey(key);
            return entity;
        }

        @Override public void setLogging(final DaoLogging logging) { }
        @Override public ListenerRegistration addChangeListener(final EntityChangeListener<Target> listener) { throw new UnsupportedOperationException(); }
        @Override public DispatchTable<Target> getEntityDispatchTable() { return null; }
    }

    @Test
    public void theHandleKnowsWhatItReferences() {
        assertEquals("Target", Holder.TARGET.getMetaData(Janitor.MetaData.REF));
        assertNull(Holder.TARGET.getColumnName(), "a reference inside a JSON object is not backed by a column");
    }

    @Test
    public void aReferenceByKeyIsWrittenAsItsKey() throws Exception {
        final Holder holder = new Holder();
        holder.setTarget(new ForeignKeyString<>("K1", Holder.UPLINK.dao));
        assertTrue(holder.toJson().contains("\"K1\""), holder.toJson());
    }

    @Test
    public void aReferenceByIdIsWrittenAsTheKeyOfWhatItPointsTo() throws Exception {
        final Holder holder = new Holder();
        holder.setTarget(new ForeignKeyInteger<>(7, Holder.UPLINK.dao));
        final String json = holder.toJson();
        assertTrue(json.contains("\"FROM-ID\""), json);
        assertFalse(json.contains("7"), "ids must not show up in JSON: " + json);
    }

    @Test
    public void noReferenceIsLeftOut() throws Exception {
        final Holder holder = new Holder();
        assertFalse(holder.toJson().contains("target"), holder.toJson());
    }

    @Test
    public void aShortCodeIsReadAsAReferenceByKey() throws Exception {
        final Holder holder = Holder.DISPATCH.readFromJson(Holder::new, "{\"target\": \"K2\"}");
        assertInstanceOf(ForeignKeyString.class, holder.getTarget());
        assertEquals("K2", ForeignKeys.keyOf(holder.getTarget()));
    }

    @Test
    public void nullIsReadAsNoReference() throws Exception {
        final Holder holder = Holder.DISPATCH.readFromJson(Holder::new, "{\"target\": null}");
        assertTrue(holder.getTarget().isNull());
    }

    @Test
    public void aReferenceThatIsNotAShortCodeIsRejected() {
        assertThrows(Exception.class, () -> Holder.DISPATCH.readFromJson(Holder::new, "{\"target\": {\"a\": 1}}"));
    }

    @Test
    public void readingWhatWasWrittenGivesTheSameReference() throws Exception {
        final Holder holder = new Holder();
        holder.setTarget(new ForeignKeyString<>("K3", Holder.UPLINK.dao));
        final Holder copy = Holder.DISPATCH.readFromJson(Holder::new, holder.toJson());
        assertEquals("K3", ForeignKeys.keyOf(copy.getTarget()));
    }

    @Test
    public void aTargetWithoutShortCodeCannotBeWritten() {
        final Holder holder = new Holder();
        holder.setTarget(new ForeignKeyInteger<>(7, new StubDao(null)));
        assertThrows(Exception.class, holder::toJson);
    }


    @Test
    public void theListHandleKnowsWhatItReferences() {
        assertEquals("Target", Holder.TARGETS.getMetaData(Janitor.MetaData.REF));
    }

    @Test
    public void aListOfReferencesIsWrittenAsAnArrayOfKeys() throws Exception {
        final Holder holder = new Holder();
        holder.getTargets().add(new ForeignKeyString<>("A", Holder.UPLINK.dao));
        holder.getTargets().add(new ForeignKeyInteger<>(7, Holder.UPLINK.dao));
        final String json = holder.toJson().replaceAll("\\s", "");
        assertTrue(json.contains("\"targets\":[\"A\",\"FROM-ID\"]"), json);
    }

    @Test
    public void anEmptyListIsLeftOut() throws Exception {
        assertFalse(new Holder().toJson().contains("targets"));
    }

    @Test
    public void anArrayOfShortCodesIsReadAsReferencesByKey() throws Exception {
        final Holder holder = Holder.DISPATCH.readFromJson(Holder::new, "{\"targets\": [\"X\", null, \"\", \"Y\"]}");
        assertEquals(2, holder.getTargets().size(), "null and blank entries are dropped");
        assertEquals("X", ForeignKeys.keyOf(holder.getTargets().get(0)));
        assertEquals("Y", ForeignKeys.keyOf(holder.getTargets().get(1)));
        assertInstanceOf(ForeignKeyString.class, holder.getTargets().get(0));
    }

    @Test
    public void nullIsReadAsAnEmptyList() throws Exception {
        final Holder holder = Holder.DISPATCH.readFromJson(Holder::new, "{\"targets\": null}");
        assertTrue(holder.getTargets().isEmpty());
    }

    @Test
    public void aListThatIsNotAnArrayOfShortCodesIsRejected() {
        assertThrows(Exception.class, () -> Holder.DISPATCH.readFromJson(Holder::new, "{\"targets\": [1, 2]}"));
    }

    @Test
    public void readingWhatWasWrittenGivesTheSameList() throws Exception {
        final Holder holder = new Holder();
        holder.getTargets().add(new ForeignKeyString<>("K4", Holder.UPLINK.dao));
        holder.getTargets().add(new ForeignKeyString<>("K5", Holder.UPLINK.dao));
        final Holder copy = Holder.DISPATCH.readFromJson(Holder::new, holder.toJson());
        assertEquals(List.of("K4", "K5"), copy.getTargets().stream().map(ForeignKeys::keyOf).toList());
    }

}
