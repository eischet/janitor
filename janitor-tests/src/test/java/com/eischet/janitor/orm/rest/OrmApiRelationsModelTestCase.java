// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.rest;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.orm.JanitorOrm;
import com.eischet.janitor.orm.dao.GenericDao;
import com.eischet.janitor.orm.dao.JoinDao;
import com.eischet.janitor.orm.dao.OrmDaoCollection;
import com.eischet.janitor.orm.dao.Uplink;
import com.eischet.janitor.orm.entity.OrmEntity;
import com.eischet.janitor.orm.entity.OrmJoined;
import com.eischet.janitor.orm.meta.EntityDispatchTable;
import com.eischet.janitor.orm.meta.OrmDispatchTable;
import com.eischet.janitor.orm.ref.ForeignKey;
import com.eischet.janitor.orm.ref.ForeignKeyNull;
import com.eischet.janitor.orm.rest.OrmRestRelationsFixture.RelationsCollection;
import com.eischet.dbxs.DataManager;
import com.eischet.dbxs.TestDb;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** Sub-resources (entities below an entity) and links (join records below an entity) in the model of the API. */
public class OrmApiRelationsModelTestCase extends JanitorTest {

    private OrmApiModel model() throws Exception {
        return OrmApiModel.of(OrmRestRelationsFixture.newCollection());
    }

    @Test
    public void aReferenceWithASubPathMakesTheChildrenASubresourceOfTheParent() throws Exception {
        final OrmApiModel model = model();
        assertEquals(1, model.getSubresources().size());
        final ApiSubresource sub = model.findSubresource(model.findByPath("teams"), "tasks");
        assertNotNull(sub);
        assertEquals("Task", sub.child().className());
        assertEquals("Team", sub.parent().className());
        assertEquals("team", sub.field().name());
        assertNull(model.findSubresource(model.findByPath("teams"), "nothing"));
        assertNull(model.findSubresource(model.findByPath("members"), "tasks"));
    }

    @Test
    public void joinTablesBecomeLinksInEachDirectionThatWasDeclared() throws Exception {
        final OrmApiModel model = model();
        assertEquals(2, model.getLinks().size());
        final ApiLink members = model.findLink(model.findByPath("teams"), "members");
        assertNotNull(members);
        assertEquals("Membership", members.className());
        assertEquals("Team", members.parent().className());
        assertEquals("Member", members.other().className());
        assertEquals("team", members.fromField());
        assertEquals("member", members.toField());
        assertEquals("team_id", members.fromColumn());
        assertEquals("member_id", members.toColumn());
        final ApiLink teams = model.findLink(model.findByPath("members"), "teams");
        assertNotNull(teams);
        assertEquals("member", teams.fromField());
        assertEquals("team", teams.toField());
    }

    @Test
    public void linksExposeTheReferencesAndTheExtraColumns() throws Exception {
        final ApiLink link = model().findLink(model().findByPath("teams"), "members");
        assertEquals(List.of("team", "member", "role"), link.fields().stream().map(ApiField::name).toList());
        assertTrue(link.field("team").readOnly() && link.field("member").readOnly(), "the path says what the references are");
        assertFalse(link.field("role").readOnly());
        assertTrue(link.field("member").referenceByKey(), "members have a key");
        assertEquals(Set.of(RestOperation.LIST, RestOperation.SEARCH, RestOperation.GET, RestOperation.CREATE, RestOperation.DELETE), link.operations());
        assertTrue(link.allows(RestOperation.DELETE));
        assertFalse(link.allows(RestOperation.UPDATE));
    }

    // --- things that are not allowed: the model refuses them when it is built ---

    /** A small collection in which the pieces of a link can be broken in turn. */
    static class Other implements OrmEntity {
        static final ForeignKeyNull<Other> NULL = new ForeignKeyNull<>(Other.class);
        static final EntityDispatchTable<Other, BrokenCollection> DISPATCH = new EntityDispatchTable<>(Other.class, up -> new Other(), NULL, BrokenCollection::getOtherDao);

        static {
            DISPATCH.dbTable("b_other", "other_id", "other_key", "other_name", "seq_b_other");
            DISPATCH.addLongColumn("id", "other_id", Other::getId, Other::setId);
            DISPATCH.addStringColumn("key", "other_key", Other::getKey, Other::setKey, 20);
            // not exposed: no path
        }

        private long id;
        private String key;

        @Override public long getId() { return id; }
        @Override public void setId(final long id) { this.id = id; }
        @Override public String getKey() { return key; }
        @Override public void setKey(final String key) { this.key = key; }
        @Override public String getName() { return null; }
        @Override public void setName(final String name) { }
        @Override public boolean isSoftDeleted() { return false; }
        @Override public void setSoftDeleted(final boolean softDeleted) { }
    }

    static class Join implements OrmJoined {
        static final OrmDispatchTable<Join, BrokenCollection> DISPATCH = new OrmDispatchTable<>(Join.class, up -> new Join(up));

        final BrokenCollection source;
        ForeignKey<Other> left = Other.NULL;
        ForeignKey<Other> right = Other.NULL;

        Join(final BrokenCollection source) { this.source = source; }

        static void declare(final String... primaryKey) {
            DISPATCH.setMetaData(JanitorOrm.MetaData.TABLE_NAME, "b_join");
            DISPATCH.setMetaData(JanitorOrm.MetaData.JOIN_TABLE_PK, JanitorOrm.MetaData.StringList.of(primaryKey));
            Other.DISPATCH.addReference(DISPATCH, "left", "left_id", j -> j.left, (j, v) -> j.left = v, j -> j.source);
            Other.DISPATCH.addReference(DISPATCH, "right", "right_id", j -> j.right, (j, v) -> j.right = v, j -> j.source);
        }
    }

    static class OtherDao extends GenericDao<Other, BrokenCollection> {
        OtherDao(final BrokenCollection c) { super(new DispatchTable<OtherDao>(false), c, Other.class, Other.DISPATCH, Other::new); }
        @Override public @NotNull Class<Other> getEntityClass() { return Other.class; }
        @Override public @NotNull String getEntityClassName() { return "Other"; }
    }

    static class JoinDaoImpl extends JoinDao<Join> {
        JoinDaoImpl(final BrokenCollection c) { super(Join.class, new DispatchTable<JoinDaoImpl>(false), c, Join.DISPATCH, () -> new Join(c)); }
        @Override public Join createFromEntityPair(final @NotNull JanitorScriptProcess process, final @NotNull ForeignKey<?> parentEntity, final @NotNull ForeignKey<?> addedEntity) throws JanitorRuntimeException {
            throw new UnsupportedOperationException();
        }
    }

    static class BrokenCollection extends OrmDaoCollection<BrokenCollection> implements Uplink {
        static final DispatchTable<BrokenCollection> DISPATCH = new DispatchTable<>();
        private final DataManager dataManager = TestDb.newManager();
        private final OtherDao otherDao;

        BrokenCollection() {
            super(DISPATCH, TestDb.newManager().getDialect(), null, null);
            this.otherDao = new OtherDao(this);
            new JoinDaoImpl(this);
        }

        OtherDao getOtherDao() { return otherDao; }
        @Override public DataManager getDataManager() { return dataManager; }
    }

    @Test
    public void aLinkBetweenEntitiesThatAreNotExposedIsRefused() {
        Join.declare("left_id", "right_id");
        RestOrm.link(Join.DISPATCH, "left", "others", "right");
        try {
            final IllegalStateException error = assertThrows(IllegalStateException.class, () -> OrmApiModel.of(new BrokenCollection()));
            assertTrue(error.getMessage().contains("both have to be exposed"), error.getMessage());
        } finally {
            Join.DISPATCH.setMetaData(RestMetaData.LINKS, null);
        }
    }

    @Test
    public void aLinkWhoseRecordsCannotBeAddressedIsRefused() {
        Join.declare("left_id", "right_id", "left_id");
        Join.DISPATCH.setMetaData(JanitorOrm.MetaData.JOIN_TABLE_PK, JanitorOrm.MetaData.StringList.of("left_id"));
        RestOrm.link(Join.DISPATCH, "left", "others", "right");
        // make both sides exposed so that only the primary key is wrong
        RestOrm.expose(Other.DISPATCH, "others");
        try {
            final IllegalStateException error = assertThrows(IllegalStateException.class, () -> OrmApiModel.of(new BrokenCollection()));
            assertTrue(error.getMessage().contains("cannot address its records"), error.getMessage());
        } finally {
            Join.DISPATCH.setMetaData(RestMetaData.LINKS, null);
            Other.DISPATCH.setMetaData(RestMetaData.PATH, null);
        }
    }

    @Test
    public void aLinkWithMoreColumnsInItsKeyCanOnlyBeListed() {
        Join.declare("left_id", "right_id", "reason_id");
        RestOrm.link(Join.DISPATCH, "left", "others", "right");
        RestOrm.allow(Join.DISPATCH, RestOperation.CREATE, RestOperation.DELETE);
        RestOrm.expose(Other.DISPATCH, "others");
        try {
            final ApiLink link = OrmApiModel.of(new BrokenCollection()).getLinks().get(0);
            assertTrue(link.allows(RestOperation.LIST));
            assertFalse(link.allows(RestOperation.GET), "one pair can be linked more than once, so a pair does not address a record");
            assertFalse(link.allows(RestOperation.CREATE));
            assertFalse(link.allows(RestOperation.DELETE));
        } finally {
            Join.DISPATCH.setMetaData(RestMetaData.LINKS, null);
            Join.DISPATCH.setMetaData(RestMetaData.OPERATIONS, null);
            Other.DISPATCH.setMetaData(RestMetaData.PATH, null);
        }
    }

    @Test
    public void aLinkThatNamesAPropertyThatIsNoReferenceIsRefused() {
        Join.declare("left_id", "right_id");
        RestOrm.expose(Other.DISPATCH, "others");
        RestOrm.link(Join.DISPATCH, "left", "others", "nope");
        try {
            final IllegalStateException error = assertThrows(IllegalStateException.class, () -> OrmApiModel.of(new BrokenCollection()));
            assertTrue(error.getMessage().contains("two different reference properties"), error.getMessage());
        } finally {
            Join.DISPATCH.setMetaData(RestMetaData.LINKS, null);
            Other.DISPATCH.setMetaData(RestMetaData.PATH, null);
        }
    }

    @Test
    public void pathsBelowAnEntityMustBeUnique() throws Exception {
        final DispatchTable<?> membership = OrmRestRelationsFixture.newCollection().getJoinDao("Membership").getEntityDispatchTable();
        final RestLinks before = membership.getMetaData(RestMetaData.LINKS);
        try {
            RestOrm.link((OrmDispatchTable<?, ?>) membership, "team", "members", "member"); // the same path again
            final IllegalStateException error = assertThrows(IllegalStateException.class, () -> OrmApiModel.of(OrmRestRelationsFixture.newCollection()));
            assertTrue(error.getMessage().contains("is used twice"), error.getMessage());
        } finally {
            membership.setMetaData(RestMetaData.LINKS, before);
        }
    }

    // --- the API description ---

    @SuppressWarnings("unchecked")
    private Map<String, Object> describe() throws Exception {
        return OpenApiEmitter.of(model()).build();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void theDescriptionCoversSubresourcesAndLinks() throws Exception {
        final Map<String, Object> document = describe();
        final Map<String, Object> paths = (Map<String, Object>) document.get("paths");
        assertEquals(Set.of("get", "post"), ((Map<String, Object>) paths.get("/teams/{id}/tasks")).keySet());
        assertEquals(Set.of("get"), ((Map<String, Object>) paths.get("/teams/{id}/members")).keySet());
        assertEquals(Set.of("get", "put", "delete"), ((Map<String, Object>) paths.get("/teams/{id}/members/{otherId}")).keySet());
        assertEquals(Set.of("get"), ((Map<String, Object>) paths.get("/members/{id}/teams")).keySet());
        assertEquals(Set.of("get", "put", "delete"), ((Map<String, Object>) paths.get("/members/{id}/teams/{otherId}")).keySet());
        assertNull(paths.get("/members/{id}/tasks"));
        final Map<String, Object> schemas = (Map<String, Object>) ((Map<String, Object>) document.get("components")).get("schemas");
        assertTrue(schemas.keySet().containsAll(List.of("Membership", "MembershipPage", "Task", "TaskPage")), schemas.keySet().toString());
        final Map<String, Object> properties = (Map<String, Object>) ((Map<String, Object>) schemas.get("Membership")).get("properties");
        assertEquals(Boolean.TRUE, ((Map<String, Object>) properties.get("team")).get("readOnly"));
        assertEquals("Member", ((Map<String, Object>) properties.get("member")).get("x-references"));
        assertNull(((Map<String, Object>) properties.get("role")).get("readOnly"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void theDescriptionIsConsistent() throws Exception {
        final Map<String, Object> document = describe();
        final List<String> refs = new java.util.ArrayList<>();
        collectRefs(document, refs);
        for (final String ref : refs) {
            Object node = document;
            for (final String part : ref.substring(2).split("/")) {
                node = ((Map<?, ?>) node).get(part);
                assertNotNull(node, "dangling reference " + ref);
            }
        }
        final List<String> operationIds = new java.util.ArrayList<>();
        for (final Object pathItem : ((Map<String, Object>) document.get("paths")).values()) {
            for (final Object operation : ((Map<String, Object>) pathItem).values()) {
                operationIds.add((String) ((Map<String, Object>) operation).get("operationId"));
            }
        }
        assertEquals(operationIds.size(), Set.copyOf(operationIds).size(), "operation ids are unique: " + operationIds);
        assertFalse(operationIds.contains(null));
    }

    private static void collectRefs(final Object node, final List<String> refs) {
        if (node instanceof Map<?, ?> map) {
            map.forEach((key, value) -> {
                if ("$ref".equals(key)) {
                    refs.add((String) value);
                } else {
                    collectRefs(value, refs);
                }
            });
        } else if (node instanceof List<?> list) {
            list.forEach(element -> collectRefs(element, refs));
        }
    }

    @Test
    public void subresourceAndLinkPathsAreChecked() {
        assertThrows(IllegalArgumentException.class, () -> RestOrm.link(OrmRestRelationsFixture.Membership.DISPATCH, "team", "a/b", "member"));
        assertThrows(IllegalArgumentException.class, () -> RestOrm.link(OrmRestRelationsFixture.Membership.DISPATCH, "team", "", "member"));
    }
    @Test
    public void aSelectionTakesOnlyTheNamedEntitiesWithTheirOperationsAndDropsWhatConnectsToOthers() throws Exception {
        final OrmApiModel model = OrmApiModel.of(OrmRestRelationsFixture.newCollection(),
                className -> className.equals("Team") ? RestOperations.of(RestOperation.LIST, RestOperation.GET, RestOperation.UPDATE)
                        : className.equals("Task") ? RestOperations.READ_ONLY : null);
        assertEquals(List.of("Task", "Team"), model.getEntities().stream().map(ApiEntity::className).sorted().toList());
        assertEquals(Set.of(RestOperation.LIST, RestOperation.GET, RestOperation.UPDATE), model.findByPath("teams").operations());
        assertNull(model.findByPath("members"), "not selected");
        assertEquals(1, model.getSubresources().size(), "tasks below teams: both are selected");
        assertTrue(model.getLinks().isEmpty(), "the links lead to members, which are not selected");
        assertEquals(2, model().getLinks().size(), "the full model is unchanged");
    }

}
