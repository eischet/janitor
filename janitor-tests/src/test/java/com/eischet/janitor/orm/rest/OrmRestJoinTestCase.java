// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.rest;

import com.eischet.dbxs.DatabaseConnection;
import com.eischet.dbxs.exceptions.DatabaseError;
import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.builtin.JList;
import com.eischet.janitor.api.types.builtin.JMap;
import com.eischet.janitor.orm.ref.ForeignKeyInteger;
import com.eischet.janitor.orm.rest.OrmRestRelationsFixture.Member;
import com.eischet.janitor.orm.rest.OrmRestRelationsFixture.Membership;
import com.eischet.janitor.orm.rest.OrmRestRelationsFixture.RelationsCollection;
import com.eischet.janitor.orm.rest.OrmRestRelationsFixture.Team;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** The records of a join table at a path of their own: {@code /memberships?team=alpha}, merged with POST, deleted by their whole key. */
public class OrmRestJoinTestCase extends JanitorTest {

    private RelationsCollection collection;
    private OrmRestHandler<Object> handler;

    @BeforeEach
    void setUp() throws DatabaseError {
        collection = OrmRestRelationsFixture.newCollection();
        collection.getDataManager().executeTransaction(conn -> {
            final long alpha = insertTeam(conn, "alpha");
            insertTeam(conn, "beta");
            final long ann = insertMember(conn, "ann");
            final long bob = insertMember(conn, "bob");
            insertMembership(conn, alpha, ann, "lead");
            insertMembership(conn, alpha, bob, "dev");
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

    private RestResponse send(final String method, final @Nullable String body, final String... params) {
        final Map<String, List<String>> query = new HashMap<>();
        for (int i = 0; i < params.length; i += 2) {
            query.put(params[i], List.of(params[i + 1]));
        }
        return handler.handle(new RestRequest<>(method, "/memberships", query, Map.of(), body, null));
    }

    private static List<String> values(final RestResponse response, final String name) {
        final JList items = (JList) Janitor.current().parseJsonToMap(response.body()).get(Janitor.string("items"));
        return items.stream().map(item -> String.valueOf(((JMap) item).get(Janitor.string(name)).janitorGetHostValue())).sorted().toList();
    }

    @Test
    public void recordsAreListedByPartOfTheirKey() {
        final RestResponse byTeam = send("GET", null, "team", "alpha");
        assertEquals(200, byTeam.status(), byTeam.body());
        assertEquals(List.of("ann", "bob"), values(byTeam, "member"));
        assertEquals(List.of("dev"), values(send("GET", null, "team", "alpha", "member", "bob"), "role"));
        assertEquals(List.of("lead"), values(send("GET", null, "team", "alpha", "role", "lead"), "role"), "other properties narrow it down");
        assertEquals(List.of(), values(send("GET", null, "team", "nowhere"), "role"));
    }

    @Test
    public void listingNeedsAReferenceOfTheKey() {
        assertEquals(400, send("GET", null).status());
        assertEquals(400, send("GET", null, "role", "dev").status());
        assertEquals(400, send("GET", null, "nope", "x").status());
    }

    @Test
    public void postMergesARecord() {
        final RestResponse created = send("POST", "{\"team\": \"beta\", \"member\": \"ann\", \"role\": \"guest\"}");
        assertEquals(200, created.status(), created.body());
        assertEquals(List.of("guest"), values(send("GET", null, "team", "beta"), "role"));
        assertEquals(200, send("POST", "{\"team\": \"beta\", \"member\": \"ann\", \"role\": \"lead\"}").status());
        assertEquals(List.of("lead"), values(send("GET", null, "team", "beta"), "role"), "the same key changes the record");
        assertEquals(400, send("POST", "{\"team\": \"beta\", \"role\": \"x\"}").status(), "the key is incomplete");
        assertEquals(400, send("POST", "{\"team\": \"beta\", \"member\": \"nobody\"}").status(), "a reference to something that does not exist");
    }

    @Test
    public void deleteNeedsTheWholeKey() {
        assertEquals(400, send("DELETE", null, "team", "alpha").status());
        assertEquals(204, send("DELETE", null, "team", "alpha", "member", "bob").status());
        assertEquals(List.of("ann"), values(send("GET", null, "team", "alpha"), "member"));
        assertEquals(404, send("DELETE", null, "team", "alpha", "member", "bob").status());
    }

    @Test
    public void theDescriptionHasThePath() {
        final String description = handler.handle(RestRequest.of("GET", "/openapi.json", null)).body();
        assertTrue(description.contains("\"/memberships\""), description);
        assertTrue(description.contains("mergeMembershipRecord") && description.contains("deleteMembershipRecord"), description);
    }
}
