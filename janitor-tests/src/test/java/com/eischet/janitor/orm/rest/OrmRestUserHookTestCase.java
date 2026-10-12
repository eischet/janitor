// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.rest;

import com.eischet.dbxs.DatabaseConnection;
import com.eischet.dbxs.SimpleDataManager;
import com.eischet.dbxs.TestDb;
import com.eischet.dbxs.exceptions.DatabaseError;
import com.eischet.dbxs.statements.UpdateStatement;
import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.orm.entity.OrmEntity;
import com.eischet.janitor.orm.entity.OrmJoined;
import com.eischet.janitor.orm.rest.OrmRestWriteTestCase.Doc;
import com.eischet.janitor.orm.rest.OrmRestWriteTestCase.Folder;
import com.eischet.janitor.orm.rest.OrmRestWriteTestCase.WriteCollection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The user of a request has the type that the host chooses (here: a person), and the hooks of the policy that run before something is written
 * can use it, e.g. to record who made the change.
 */
public class OrmRestUserHookTestCase extends JanitorTest {

    /** The kind of user that this host has. */
    record Person(long id, String name) {
    }

    private static final Person ALICE = new Person(7, "alice");

    /** Records the last modifying person on documents, and what the hooks saw. */
    static class Auditing implements RestPolicy<Person> {
        final List<String> seen = new ArrayList<>();
        @Nullable RestRejection rejectUpdates;

        @Override
        public boolean permits(final @Nullable Person user, final @NotNull ApiEntity entity, final @NotNull RestOperation operation) {
            return true;
        }

        @Override
        public void beforeWrite(final @Nullable Person user, final @NotNull ApiEntity entity, final @NotNull RestOperation operation, final @NotNull OrmEntity instance, final @NotNull DatabaseConnection conn) {
            seen.add(operation + " " + entity.className() + " by " + (user == null ? "nobody" : user.name()) + " rank=" + (instance instanceof Doc doc ? doc.getRank() : "-"));
            if (operation == RestOperation.UPDATE && rejectUpdates != null) {
                throw rejectUpdates;
            }
            if (instance instanceof Doc doc && operation != RestOperation.DELETE && user != null) {
                doc.setModifiedBy(user.id());
            }
        }

        @Override
        public void beforeLinkWrite(final @Nullable Person user, final @NotNull ApiLink link, final @NotNull RestOperation operation, final @NotNull OrmJoined row, final @NotNull DatabaseConnection conn) {
            seen.add("link " + operation + " " + link.className() + " by " + (user == null ? "nobody" : user.name()));
            if (user == null) {
                throw new RestRejection(403, "links need a user");
            }
        }
    }

    private SimpleDataManager manager;
    private WriteCollection collection;
    private Auditing policy;
    private OrmRestHandler<Person> handler;

    @BeforeEach
    void setUp() throws DatabaseError {
        manager = TestDb.newManager();
        collection = new WriteCollection(manager);
        manager.executeTransaction(conn -> {
            conn.update(UpdateStatement.of("create sequence seq_w_folder start with 1"));
            conn.update(UpdateStatement.of("create sequence seq_w_doc start with 1"));
            conn.update(UpdateStatement.of("create table w_folder (folder_id bigint primary key, folder_key varchar(20) unique, folder_name varchar(50))"));
            conn.update(UpdateStatement.of("""
                    create table w_doc (
                      doc_id bigint primary key, doc_key varchar(30), doc_title varchar(10), doc_rank integer, doc_due timestamp, doc_modified_by bigint,
                      doc_secret varchar(40), modified_at timestamp, version_no bigint, folder_id bigint references w_folder(folder_id))"""));
        });
        policy = new Auditing();
        handler = OrmRestHandler.of(collection, policy);
    }

    private RestResponse send(final @Nullable Person user, final String method, final String path, final @Nullable String body) {
        return handler.handle(new RestRequest<>(method, path, Map.of(), new HashMap<>(), body, user));
    }

    private Doc stored(final long id) throws DatabaseError {
        return manager.callTransaction(conn -> collection.getDocDao().findByIdIgnoringCache(conn, id));
    }

    private long create(final @Nullable Person user, final String body) throws Exception {
        final RestResponse response = send(user, "POST", "/docs", body);
        assertEquals(201, response.status(), response.body());
        return ((Number) Janitor.current().parseJsonToMap(response.body()).get(Janitor.string("id")).janitorGetHostValue()).longValue();
    }

    @Test
    public void thePolicySeesTheUserWithItsOwnType() {
        final List<Person> users = new ArrayList<>();
        final OrmRestHandler<Person> checking = OrmRestHandler.of(collection, (Person user, ApiEntity entity, RestOperation operation) -> {
            users.add(user);
            return user != null && user.name().equals("alice");
        });
        assertEquals(200, checking.handle(RestRequest.of("GET", "/docs", ALICE)).status());
        assertEquals(403, checking.handle(RestRequest.of("GET", "/docs", new Person(8, "mallory"))).status());
        assertEquals(401, checking.handle(RestRequest.<Person>of("GET", "/docs", null)).status(), "nobody is authenticated");
        assertEquals(java.util.Arrays.asList(ALICE, new Person(8, "mallory"), null), users);
    }

    @Test
    public void aPolicyForAnyKindOfUserServesEveryKindOfUser() {
        final OrmRestHandler<Person> handlerForPeople = OrmRestHandler.of(collection, RestPolicy.<Object>allowAll());
        assertEquals(200, handlerForPeople.handle(RestRequest.of("GET", "/docs", ALICE)).status());
    }

    // --- recording who changed something ---

    @Test
    public void theHookCanRecordTheUserWhoCreatedAnEntity() throws Exception {
        final long id = create(ALICE, "{\"rank\":3}");
        assertEquals(7, stored(id).getModifiedBy());
        assertEquals(List.of("CREATE Doc by alice rank=3"), policy.seen, "the body was applied before the hook ran");
    }

    @Test
    public void theHookRecordsTheUserWhoChangedAnEntity() throws Exception {
        final long id = create(ALICE, "{\"rank\":1}");
        policy.seen.clear();
        assertEquals(200, send(new Person(9, "bob"), "PATCH", "/docs/" + id, "{\"rank\":2}").status());
        assertEquals(9, stored(id).getModifiedBy());
        assertEquals(200, send(new Person(10, "carol"), "PUT", "/docs/" + id, "{\"rank\":5}").status());
        assertEquals(10, stored(id).getModifiedBy());
        assertEquals(List.of("UPDATE Doc by bob rank=2", "UPDATE Doc by carol rank=5"), policy.seen);
    }

    @Test
    public void theHookAlsoRunsForDeletes() throws Exception {
        final long id = create(ALICE, "{\"rank\":1}");
        policy.seen.clear();
        assertEquals(204, send(ALICE, "DELETE", "/docs/" + id, null).status());
        assertEquals(List.of("DELETE Doc by alice rank=1"), policy.seen);
    }

    @Test
    public void anAnonymousCallerIsNullInTheHook() throws Exception {
        final long id = create(null, "{\"rank\":1}");
        assertEquals(0, stored(id).getModifiedBy(), "this hook records nobody");
        assertEquals(List.of("CREATE Doc by nobody rank=1"), policy.seen);
    }

    // --- refusing ---

    @Test
    public void aRejectionRollsTheRequestBack() throws Exception {
        final long id = create(ALICE, "{\"rank\":1}");
        policy.rejectUpdates = new RestRejection(422, "not this one");
        final RestResponse response = send(ALICE, "PATCH", "/docs/" + id, "{\"rank\":99}");
        assertEquals(422, response.status());
        assertEquals("application/problem+json", response.contentType());
        assertTrue(response.body().contains("not this one"), response.body());
        assertEquals(1, stored(id).getRank(), "nothing was written");
        assertEquals(0, stored(id).getVersion());
        policy.rejectUpdates = null;
        assertEquals(200, send(ALICE, "PATCH", "/docs/" + id, "{\"rank\":99}").status());
    }

    @Test
    public void onlyClientErrorsCanBeRejectedWith() {
        assertThrows(IllegalArgumentException.class, () -> new RestRejection(500, "no"));
        assertThrows(IllegalArgumentException.class, () -> new RestRejection(200, "no"));
        assertEquals(403, new RestRejection(403, "yes").status());
    }

    @Test
    public void theHookDoesNotRunForRequestsThatAreRefusedBeforeAnythingIsWritten() {
        assertEquals(400, send(ALICE, "POST", "/docs", "{\"nope\":1}").status());
        assertEquals(400, send(ALICE, "POST", "/docs", "{}").status(), "rank is missing");
        assertEquals(404, send(ALICE, "PATCH", "/docs/999", "{\"rank\":1}").status());
        assertEquals(403, OrmRestHandler.of(collection, new Auditing() {
            @Override public boolean permits(final @Nullable Person user, final @NotNull ApiEntity entity, final @NotNull RestOperation operation) { return false; }
        }).handle(RestRequest.of("POST", "/docs", ALICE)).status());
        assertEquals(List.of(), policy.seen);
    }

    // --- links ---

    @Test
    public void theLinkHookSeesLinksBeingWrittenAndCanRefuseThem() throws Exception {
        final OrmRestRelationsFixture.RelationsCollection relations = OrmRestRelationsFixture.newCollection();
        long team;
        long member;
        final com.eischet.dbxs.DataManager data = relations.getDataManager();
        final long[] ids = new long[2];
        data.executeTransaction(conn -> {
            final OrmRestRelationsFixture.Team t = new OrmRestRelationsFixture.Team();
            t.setKey("alpha");
            relations.getTeamDao().insert(conn, t);
            final OrmRestRelationsFixture.Member m = new OrmRestRelationsFixture.Member();
            m.setKey("ann");
            relations.getMemberDao().insert(conn, m);
            ids[0] = t.getId();
            ids[1] = m.getId();
        });
        team = ids[0];
        member = ids[1];
        final Auditing linking = new Auditing();
        final OrmRestHandler<Person> linkHandler = OrmRestHandler.of(relations, linking);
        final String path = "/teams/" + team + "/members/" + member;

        assertEquals(403, linkHandler.handle(RestRequest.of("PUT", path, null)).status(), "the hook refuses anonymous callers");
        assertEquals(404, linkHandler.handle(RestRequest.of("GET", path, ALICE)).status(), "so nothing was created");
        assertEquals(201, linkHandler.handle(RestRequest.of("PUT", path, ALICE)).status());
        assertEquals(204, linkHandler.handle(RestRequest.of("DELETE", path, ALICE)).status());
        assertEquals(List.of("link CREATE Membership by nobody", "link CREATE Membership by alice", "link DELETE Membership by alice"), linking.seen);
    }
}
