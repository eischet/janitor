// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.rest;

import com.eischet.dbxs.DatabaseConnection;
import com.eischet.dbxs.exceptions.DatabaseError;
import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.builtin.JList;
import com.eischet.janitor.api.types.builtin.JMap;
import com.eischet.janitor.orm.entity.OrmEntity;
import com.eischet.janitor.orm.ref.ForeignKeyInteger;
import com.eischet.janitor.orm.rest.OrmRestRelationsFixture.Member;
import com.eischet.janitor.orm.rest.OrmRestRelationsFixture.Membership;
import com.eischet.janitor.orm.rest.OrmRestRelationsFixture.RelationsCollection;
import com.eischet.janitor.orm.rest.OrmRestRelationsFixture.Task;
import com.eischet.janitor.orm.rest.OrmRestRelationsFixture.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Policies that check entities one by one ({@link RestPolicy#checksEachEntity}, {@link RestPolicy#reveal}): hidden entities look like "not found"
 * everywhere, changed copies are shown but not written, and lists check a limited number of candidates.
 */
public class OrmRestRevealTestCase extends JanitorTest {

    private RelationsCollection collection;
    private long alpha;
    private long beta;
    private long ann;
    private long cid;
    private long open;
    private long secret;

    /**
     * Hides the team "beta", the member "cid" and tasks named "secret"; shows tasks named "masked" as "***".
     */
    class Revealing implements RestPolicy<String> {
        int scanLimit = 1000;

        @Override public boolean permits(final @Nullable String user, final @NotNull ApiEntity entity, final @NotNull RestOperation operation) { return true; }
        @Override public boolean checksEachEntity(final @Nullable String user, final @NotNull ApiEntity entity) { return true; }
        @Override public int scanLimit(final @Nullable String user, final @NotNull ApiEntity entity) { return scanLimit; }

        @Override
        public @Nullable OrmEntity reveal(final @Nullable String user, final @NotNull ApiEntity entity, final @NotNull OrmEntity instance, final @NotNull DatabaseConnection conn) {
            if (instance instanceof Team team) {
                return "beta".equals(team.getKey()) ? null : team;
            }
            if (instance instanceof Member member) {
                return "cid".equals(member.getKey()) ? null : member;
            }
            if (instance instanceof Task task) {
                if ("secret".equals(task.getName())) {
                    return null;
                }
                if ("masked".equals(task.getName())) {
                    final Task copy = new Task(collection);
                    copy.setId(task.getId());
                    copy.setKey(task.getKey());
                    copy.setName("***");
                    copy.setTeam(task.getTeam());
                    return copy;
                }
            }
            return instance;
        }
    }

    @BeforeEach
    void setUp() throws DatabaseError {
        collection = OrmRestRelationsFixture.newCollection();
        collection.getDataManager().executeTransaction(conn -> {
            alpha = insertTeam(conn, "alpha");
            beta = insertTeam(conn, "beta");
            ann = insertMember(conn, "ann");
            final long bob = insertMember(conn, "bob");
            cid = insertMember(conn, "cid");
            insertMembership(conn, alpha, ann);
            insertMembership(conn, alpha, bob);
            insertMembership(conn, alpha, cid);
            insertMembership(conn, beta, bob);
            open = insertTask(conn, alpha, "t1", "open");
            secret = insertTask(conn, alpha, "t2", "secret");
            insertTask(conn, alpha, "t3", "masked");
            insertTask(conn, beta, "t4", "open");
        });
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

    private void insertMembership(final DatabaseConnection conn, final long team, final long member) throws DatabaseError {
        final Membership membership = new Membership(collection);
        membership.setTeam(new ForeignKeyInteger<>(team, collection.getTeamDao()));
        membership.setMember(new ForeignKeyInteger<>(member, collection.getMemberDao()));
        membership.setRole("dev");
        collection.getMembershipDao().insert(conn, membership);
    }

    private long insertTask(final DatabaseConnection conn, final long team, final String key, final String name) throws DatabaseError {
        final Task task = new Task(collection);
        task.setKey(key);
        task.setName(name);
        task.setTeam(new ForeignKeyInteger<>(team, collection.getTeamDao()));
        collection.getTaskDao().insert(conn, task);
        return task.getId();
    }

    private OrmRestHandler<String> handler(final Revealing policy) {
        return OrmRestHandler.of(collection, policy);
    }

    private static RestResponse send(final OrmRestHandler<String> handler, final String method, final String path, final @Nullable String body) {
        return handler.handle(new RestRequest<>(method, path, Map.of(), Map.of(), body, "user"));
    }

    private static JMap json(final RestResponse response) {
        return Janitor.current().parseJsonToMap(response.body());
    }

    private static List<String> values(final RestResponse response, final String name) {
        final JList items = (JList) json(response).get(Janitor.string("items"));
        return items.stream().map(item -> String.valueOf(((JMap) item).get(Janitor.string(name)).janitorGetHostValue())).toList();
    }

    private static Object value(final RestResponse response, final String name) {
        return json(response).get(Janitor.string(name)).janitorGetHostValue();
    }

    @Test
    public void hiddenEntitiesAreNotFoundAndChangedCopiesAreShown() {
        final OrmRestHandler<String> handler = handler(new Revealing());
        assertEquals(404, send(handler, "GET", "/tasks/" + secret, null).status());
        assertEquals(404, send(handler, "GET", "/teams/by-key/beta", null).status());
        final RestResponse masked = send(handler, "GET", "/tasks/by-key/t3", null);
        assertEquals(200, masked.status(), masked.body());
        assertEquals("***", value(masked, "name"));
        assertEquals(200, send(handler, "GET", "/tasks/" + open, null).status());
    }

    @Test
    public void listsShowOnlyWhatIsRevealed() {
        final RestResponse tasks = send(handler(new Revealing()), "GET", "/tasks", null);
        assertEquals(200, tasks.status(), tasks.body());
        assertEquals(List.of("t1", "t3", "t4"), values(tasks, "key"));
        assertEquals(List.of("open", "***", "open"), values(tasks, "name"));
        assertEquals(3L, ((Number) value(tasks, "total")).longValue());
        assertEquals(Boolean.FALSE, value(tasks, "truncated"));
    }

    @Test
    public void listsCheckNoMoreThanTheScanLimit() {
        final Revealing policy = new Revealing();
        policy.scanLimit = 2;
        final RestResponse tasks = send(handler(policy), "GET", "/tasks", null);
        assertEquals(200, tasks.status(), tasks.body());
        assertEquals(List.of("t1"), values(tasks, "key"), "of the first two candidates, the second is hidden");
        assertEquals(Boolean.TRUE, value(tasks, "truncated"));
        final RestResponse page = handler(new Revealing()).handle(new RestRequest<>("GET", "/tasks", Map.of("size", List.of("2"), "page", List.of("1")), Map.of(), null, "user"));
        assertEquals(List.of("t4"), values(page, "key"), "pages count the visible entities");
    }

    @Test
    public void belowAHiddenParentThereIsNothing() {
        final OrmRestHandler<String> handler = handler(new Revealing());
        assertEquals(404, send(handler, "GET", "/teams/" + beta + "/tasks", null).status());
        assertEquals(404, send(handler, "GET", "/teams/" + beta + "/members", null).status());
        final RestResponse tasks = send(handler, "GET", "/teams/" + alpha + "/tasks", null);
        assertEquals(List.of("t1", "t3"), values(tasks, "key"));
    }

    @Test
    public void linksToHiddenEntitiesAreLeftOut() {
        final OrmRestHandler<String> handler = handler(new Revealing());
        final RestResponse members = send(handler, "GET", "/teams/" + alpha + "/members", null);
        assertEquals(200, members.status(), members.body());
        assertEquals(2L, ((Number) value(members, "total")).longValue());
        assertEquals(404, send(handler, "GET", "/teams/" + alpha + "/members/" + cid, null).status());
        assertEquals(200, send(handler, "GET", "/teams/" + alpha + "/members/" + ann, null).status());
        assertEquals(404, send(handler, "PUT", "/teams/" + beta + "/members/" + ann, "{}").status());
    }

    @Test
    public void hiddenEntitiesCannotBeChangedAndChangedCopiesAreNotWritten() throws DatabaseError {
        final OrmRestHandler<String> handler = handler(new Revealing());
        assertEquals(404, send(handler, "PATCH", "/tasks/" + secret, "{\"name\": \"x\"}").status());
        assertEquals(404, send(handler, "DELETE", "/tasks/" + secret, null).status());
        final RestResponse hiding = send(handler, "PATCH", "/tasks/" + open, "{\"name\": \"secret\"}");
        assertEquals(403, hiding.status(), "a change that hides the entity is refused: " + hiding.body());
        final RestResponse renamed = send(handler, "PATCH", "/tasks/" + open, "{\"key\": \"t1b\"}");
        assertEquals(200, renamed.status(), renamed.body());
        final String stored = collection.getDataManager().callTransaction(conn -> collection.getTaskDao().findById(conn, open).getName());
        assertEquals("open", stored);
    }
}
