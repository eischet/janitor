// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.rest;

import com.eischet.dbxs.DatabaseConnection;
import com.eischet.dbxs.exceptions.DatabaseError;
import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.builtin.JList;
import com.eischet.janitor.api.types.builtin.JMap;
import com.eischet.janitor.orm.filter.FilterExpression;
import com.eischet.janitor.orm.filter.FilterOperator;
import com.eischet.janitor.orm.ref.ForeignKeyInteger;
import com.eischet.janitor.orm.rest.OrmRestRelationsFixture.Member;
import com.eischet.janitor.orm.rest.OrmRestRelationsFixture.Membership;
import com.eischet.janitor.orm.rest.OrmRestRelationsFixture.RelationsCollection;
import com.eischet.janitor.orm.rest.OrmRestRelationsFixture.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** The records of a join table as links below the entities that they connect: {@code /teams/{id}/members/{memberId}}. */
public class OrmRestLinkTestCase extends JanitorTest {

    private RelationsCollection collection;
    private OrmRestHandler<Object> handler;
    private long alpha;
    private long beta;
    private long ann;
    private long bob;
    private long cid;

    @BeforeEach
    void setUp() throws DatabaseError {
        collection = OrmRestRelationsFixture.newCollection();
        collection.getDataManager().executeTransaction(conn -> {
            alpha = insertTeam(conn, "alpha");
            beta = insertTeam(conn, "beta");
            ann = insertMember(conn, "ann");
            bob = insertMember(conn, "bob");
            cid = insertMember(conn, "cid");
            insertMembership(conn, alpha, ann, "lead");
            insertMembership(conn, alpha, bob, "dev");
            insertMembership(conn, beta, bob, "dev");
        });
        handler = OrmRestHandler.of(collection, RestPolicy.allowAll());
    }

    private long insertTeam(final DatabaseConnection conn, final String key) throws DatabaseError {
        final Team team = new Team();
        team.setKey(key);
        team.setName(key.toUpperCase());
        collection.getTeamDao().insert(conn, team);
        return team.getId();
    }

    private long insertMember(final DatabaseConnection conn, final String key) throws DatabaseError {
        final Member member = new Member();
        member.setKey(key);
        member.setName(key.toUpperCase());
        collection.getMemberDao().insert(conn, member);
        return member.getId();
    }

    private void insertMembership(final DatabaseConnection conn, final long team, final long member, final String role) throws DatabaseError {
        final Membership membership = new Membership(collection);
        membership.setTeam(new ForeignKeyInteger<>(team, collection.getTeamDao()));
        membership.setMember(new ForeignKeyInteger<>(member, collection.getMemberDao()));
        membership.setRole(role);
        collection.getMembershipDao().insert(conn, membership);
    }

    private RestResponse send(final OrmRestHandler<Object> h, final String method, final String path, final @Nullable String body, final String... params) {
        final Map<String, List<String>> query = new HashMap<>();
        for (int i = 0; i < params.length; i += 2) {
            query.put(params[i], List.of(params[i + 1]));
        }
        return h.handle(new RestRequest<>(method, path, query, Map.of(), body, null));
    }

    private RestResponse get(final String path, final String... params) {
        return send(handler, "GET", path, null, params);
    }

    private static String compact(final RestResponse response) {
        assertNotNull(response.body(), "no body, status " + response.status());
        return response.body().replaceAll("\\s", "");
    }

    private static List<String> field(final RestResponse response, final String name) throws Exception {
        final JList items = (JList) Janitor.current().parseJsonToMap(response.body()).get(Janitor.string("items"));
        return items.stream().map(item -> String.valueOf(((JMap) item).get(Janitor.string(name)).janitorGetHostValue())).toList();
    }

    private static long total(final RestResponse response) throws Exception {
        return ((Number) Janitor.current().parseJsonToMap(response.body()).get(Janitor.string("total")).janitorGetHostValue()).longValue();
    }

    private int countOf(final String path) throws Exception {
        return (int) total(get(path));
    }

    // --- reading ---

    @Test
    public void listsTheLinksOfAnEntityInBothDirections() throws Exception {
        final RestResponse members = get("/teams/" + alpha + "/members");
        assertEquals(200, members.status(), members.body());
        assertEquals(List.of("ann", "bob"), field(members, "member"), "in the order of the keys at the other end");
        assertEquals(List.of("alpha", "alpha"), field(members, "team"));
        assertEquals(List.of("lead", "dev"), field(members, "role"));
        assertEquals(2, total(members));
        assertEquals(List.of("alpha", "beta"), field(get("/members/" + bob + "/teams"), "team"));
        assertEquals(List.of("alpha"), field(get("/members/" + ann + "/teams"), "team"));
        assertEquals(0, countOf("/members/" + cid + "/teams"));
    }

    @Test
    public void linksArePagedButNotFilteredOrSorted() throws Exception {
        assertEquals(List.of("bob"), field(get("/teams/" + alpha + "/members", "page", "1", "size", "1"), "member"));
        assertEquals(2, total(get("/teams/" + alpha + "/members", "page", "5", "size", "1")), "the total does not depend on the page");
        assertEquals(400, get("/teams/" + alpha + "/members", "filter", "{\"field\":\"role\",\"operator\":\"eq\",\"valueString\":\"dev\"}").status());
        assertEquals(400, get("/teams/" + alpha + "/members", "sort", "role").status());
        assertEquals(400, get("/teams/" + alpha + "/members", "page", "-1").status());
    }

    @Test
    public void getsOneLink() throws Exception {
        final RestResponse response = get("/teams/" + alpha + "/members/" + bob);
        assertEquals(200, response.status(), response.body());
        final String body = compact(response);
        assertTrue(body.contains("\"team\":\"alpha\"") && body.contains("\"member\":\"bob\"") && body.contains("\"role\":\"dev\""), body);
        assertEquals(404, get("/teams/" + alpha + "/members/" + cid).status(), "cid is not in the team");
        assertEquals(404, get("/teams/" + beta + "/members/" + ann).status());
        assertEquals(404, get("/teams/999999/members/" + ann).status());
        assertEquals(404, get("/teams/" + alpha + "/members/999999").status());
        assertEquals(400, get("/teams/" + alpha + "/members/bob").status(), "the other end is addressed by id, too");
        assertEquals(404, get("/teams/" + alpha + "/nothing/" + ann).status());
        assertEquals(404, get("/teams/" + alpha + "/members/" + ann + "/more").status());
    }

    // --- writing ---

    @Test
    public void putCreatesAndThenUpdatesALink() throws Exception {
        final String path = "/teams/" + alpha + "/members/" + cid;
        final RestResponse created = send(handler, "PUT", path, "{\"role\":\"qa\"}");
        assertEquals(201, created.status(), created.body());
        assertTrue(compact(created).contains("\"member\":\"cid\"") && compact(created).contains("\"role\":\"qa\""), created.body());
        assertEquals(3, countOf("/teams/" + alpha + "/members"));
        assertEquals(200, get(path).status());

        final RestResponse updated = send(handler, "PUT", path, "{\"role\":\"lead\"}");
        assertEquals(200, updated.status(), updated.body());
        assertTrue(compact(get(path)).contains("\"role\":\"lead\""));
        assertEquals(3, countOf("/teams/" + alpha + "/members"), "still one link");

        assertEquals(200, send(handler, "PUT", path, null).status(), "no body replaces the extra columns by null");
        assertTrue(compact(get(path)).contains("\"role\":null"));
        assertEquals(1, countOf("/members/" + cid + "/teams"), "the link can be seen from the other side");
    }

    @Test
    public void thePathSaysWhatTheReferencesAreWhateverTheBodySays() throws Exception {
        final RestResponse response = send(handler, "PUT", "/teams/" + alpha + "/members/" + cid, "{\"team\":" + beta + ",\"member\":\"ann\",\"role\":\"x\"}");
        assertEquals(201, response.status(), response.body());
        assertTrue(compact(response).contains("\"team\":\"alpha\"") && compact(response).contains("\"member\":\"cid\""), response.body());
        assertEquals(1, countOf("/teams/" + beta + "/members"), "beta is untouched: bob only");
        assertEquals(List.of("lead", "dev", "x"), field(get("/teams/" + alpha + "/members"), "role"));
    }

    @Test
    public void refusesWhatCannotBeStored() throws Exception {
        final int before = countOf("/teams/" + alpha + "/members");
        assertEquals(400, send(handler, "PUT", "/teams/" + alpha + "/members/" + cid, "{\"nope\":1}").status(), "unknown property");
        assertEquals(400, send(handler, "PUT", "/teams/" + alpha + "/members/" + cid, "{\"role\":\"far too long for twenty characters\"}").status());
        assertEquals(400, send(handler, "PUT", "/teams/" + alpha + "/members/" + cid, "{broken").status());
        assertEquals(404, send(handler, "PUT", "/teams/" + alpha + "/members/999999", "{}").status(), "no such member");
        assertEquals(404, send(handler, "PUT", "/teams/999999/members/" + cid, "{}").status(), "no such team");
        assertEquals(before, countOf("/teams/" + alpha + "/members"));
    }

    @Test
    public void deletesALink() throws Exception {
        final String path = "/teams/" + alpha + "/members/" + bob;
        assertEquals(204, send(handler, "DELETE", path, null).status());
        assertEquals(404, get(path).status());
        assertEquals(404, send(handler, "DELETE", path, null).status(), "it is gone");
        assertEquals(List.of("ann"), field(get("/teams/" + alpha + "/members"), "member"));
        assertEquals(List.of("beta"), field(get("/members/" + bob + "/teams"), "team"), "the other team's link is not touched");
        assertEquals(200, get("/members/" + bob).status(), "and neither is the member");
    }

    @Test
    public void onlyTheMethodsOfTheRouteAreAllowed() {
        final RestResponse onList = send(handler, "PUT", "/teams/" + alpha + "/members", "{}");
        assertEquals(405, onList.status());
        assertEquals("GET", onList.headers().get("Allow"));
        final RestResponse onItem = send(handler, "POST", "/teams/" + alpha + "/members/" + ann, "{}");
        assertEquals(405, onItem.status());
        assertEquals("GET, PUT, DELETE", onItem.headers().get("Allow"));
        assertEquals(405, send(handler, "PATCH", "/teams/" + alpha + "/members/" + ann, "{}").status());
    }

    // --- policy and restrictions ---

    @Test
    public void linksAreManagedLikeUpdatingTheEntityUnlessThePolicySaysOtherwise() {
        final RestPolicy<Object> readOnly = (principal, entity, operation) -> operation.isRead();
        final OrmRestHandler<Object> reading = OrmRestHandler.of(collection, readOnly);
        assertEquals(200, send(reading, "GET", "/teams/" + alpha + "/members", null).status());
        assertEquals(401, send(reading, "PUT", "/teams/" + alpha + "/members/" + cid, "{}").status(), "changing links needs UPDATE on the team");
        assertEquals(401, send(reading, "DELETE", "/teams/" + alpha + "/members/" + ann, null).status());

        final RestPolicy<Object> custom = new RestPolicy<Object>() {
            @Override public boolean permits(final @Nullable Object principal, final @NotNull ApiEntity entity, final @NotNull RestOperation operation) { return false; }
            @Override public boolean permitsLink(final @Nullable Object principal, final @NotNull ApiLink link, final @NotNull RestOperation operation) { return link.path().equals("members"); }
        };
        final OrmRestHandler<Object> linksOnly = OrmRestHandler.of(collection, custom);
        assertEquals(200, send(linksOnly, "GET", "/teams/" + alpha + "/members", null).status());
        assertEquals(201, send(linksOnly, "PUT", "/teams/" + alpha + "/members/" + cid, "{}").status());
        assertEquals(401, send(linksOnly, "GET", "/members/" + ann + "/teams", null).status(), "the other direction is not permitted");
    }

    private RestPolicy<Object> restrict(final String path, final FilterExpression restriction) {
        return new RestPolicy<Object>() {
            @Override public boolean permits(final @Nullable Object principal, final @NotNull ApiEntity entity, final @NotNull RestOperation operation) { return true; }
            @Override public @Nullable FilterExpression restriction(final @Nullable Object principal, final @NotNull ApiEntity entity) { return entity.path().equals(path) ? restriction : null; }
        };
    }

    @Test
    public void restrictionsOfBothEndsApply() throws Exception {
        final OrmRestHandler<Object> noBob = OrmRestHandler.of(collection, restrict("members", FilterExpression.from("key", FilterOperator.NEQ, "bob")));
        assertEquals(List.of("ann"), field(send(noBob, "GET", "/teams/" + alpha + "/members", null), "member"), "bob is not visible");
        assertEquals(404, send(noBob, "GET", "/teams/" + alpha + "/members/" + bob, null).status());
        assertEquals(404, send(noBob, "PUT", "/teams/" + alpha + "/members/" + bob, "{}").status());
        assertEquals(404, send(noBob, "DELETE", "/teams/" + alpha + "/members/" + bob, null).status());
        assertEquals(200, get("/teams/" + alpha + "/members/" + bob).status(), "and still there");

        final OrmRestHandler<Object> onlyAlpha = OrmRestHandler.of(collection, restrict("teams", FilterExpression.from("key", FilterOperator.EQ, "alpha")));
        assertEquals(200, send(onlyAlpha, "GET", "/teams/" + alpha + "/members", null).status());
        assertEquals(404, send(onlyAlpha, "GET", "/teams/" + beta + "/members", null).status());
        assertEquals(404, send(onlyAlpha, "PUT", "/teams/" + beta + "/members/" + cid, "{}").status());
        assertEquals(404, send(onlyAlpha, "DELETE", "/teams/" + beta + "/members/" + bob, null).status());
    }
}
