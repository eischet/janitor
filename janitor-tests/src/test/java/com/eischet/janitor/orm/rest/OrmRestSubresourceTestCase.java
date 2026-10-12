// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.rest;

import com.eischet.dbxs.exceptions.DatabaseError;
import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.builtin.JList;
import com.eischet.janitor.api.types.builtin.JMap;
import com.eischet.janitor.orm.filter.FilterExpression;
import com.eischet.janitor.orm.filter.FilterOperator;
import com.eischet.janitor.orm.ref.ForeignKeyInteger;
import com.eischet.janitor.orm.rest.OrmRestRelationsFixture.RelationsCollection;
import com.eischet.janitor.orm.rest.OrmRestRelationsFixture.Task;
import com.eischet.janitor.orm.rest.OrmRestRelationsFixture.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** The entities below another entity: {@code /teams/{id}/tasks}. */
public class OrmRestSubresourceTestCase extends JanitorTest {

    private RelationsCollection collection;
    private OrmRestHandler<Object> handler;
    private long alpha;
    private long beta;

    @BeforeEach
    void setUp() throws DatabaseError {
        collection = OrmRestRelationsFixture.newCollection();
        collection.getDataManager().executeTransaction(conn -> {
            final Team a = team("alpha", "Alpha");
            collection.getTeamDao().insert(conn, a);
            final Team b = team("beta", "Beta");
            collection.getTeamDao().insert(conn, b);
            alpha = a.getId();
            beta = b.getId();
            task(conn, "write", "Write", a);
            task(conn, "test", "Test", a);
            task(conn, "ship", "Ship", a);
            task(conn, "plan", "Plan", b);
        });
        handler = OrmRestHandler.of(collection, RestPolicy.allowAll());
    }

    private static Team team(final String key, final String name) {
        final Team team = new Team();
        team.setKey(key);
        team.setName(name);
        return team;
    }

    private void task(final com.eischet.dbxs.DatabaseConnection conn, final String key, final String name, final Team team) throws DatabaseError {
        final Task task = new Task(collection);
        task.setKey(key);
        task.setName(name);
        task.setTeam(new ForeignKeyInteger<>(team.getId(), collection.getTeamDao()));
        collection.getTaskDao().insert(conn, task);
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

    private static List<String> keys(final RestResponse response) throws Exception {
        final JList items = (JList) Janitor.current().parseJsonToMap(response.body()).get(Janitor.string("items"));
        return items.stream().map(item -> String.valueOf(((JMap) item).get(Janitor.string("key")).janitorGetHostValue())).toList();
    }

    private static long total(final RestResponse response) throws Exception {
        return ((Number) Janitor.current().parseJsonToMap(response.body()).get(Janitor.string("total")).janitorGetHostValue()).longValue();
    }

    private int taskCount() throws Exception {
        return (int) total(get("/tasks"));
    }

    // --- listing ---

    @Test
    public void listsOnlyTheEntitiesOfTheParent() throws Exception {
        final RestResponse response = get("/teams/" + alpha + "/tasks");
        assertEquals(200, response.status(), response.body());
        assertEquals(List.of("ship", "test", "write"), keys(response), "in the order of the key, as everywhere");
        assertEquals(3, total(response));
        assertEquals(List.of("plan"), keys(get("/teams/" + beta + "/tasks")));
        assertTrue(response.body().replaceAll("\\s", "").contains("\"team\":\"alpha\""), "a reference to a team is its key");
    }

    @Test
    public void filterSortAndPagingWorkWithinTheParent() throws Exception {
        assertEquals(List.of("write", "test", "ship"), keys(get("/teams/" + alpha + "/tasks", "sort", "-key")));
        assertEquals(List.of("test"), keys(get("/teams/" + alpha + "/tasks", "page", "1", "size", "1")));
        final RestResponse filtered = get("/teams/" + alpha + "/tasks", "filter", "{\"field\":\"name\",\"operator\":\"contains\",\"valueString\":\"s\"}");
        assertEquals(List.of("ship", "test"), keys(filtered));
        assertEquals(2, total(filtered), "the total is the one of the parent's entities that match");
        assertEquals(400, get("/teams/" + alpha + "/tasks", "sort", "nope").status());
    }

    @Test
    public void unknownParentsAndPathsAreNotFound() {
        assertEquals(404, get("/teams/999999/tasks").status());
        assertEquals(400, get("/teams/abc/tasks").status());
        assertEquals(404, get("/teams/" + alpha + "/nothing").status());
        assertEquals(404, get("/members/1/tasks").status(), "there is no such path below members");
        assertEquals(404, get("/teams/by-key/alpha/tasks").status(), "parents are addressed by id");
    }

    @Test
    public void onlyTheMethodsOfTheChildAreAllowed() {
        final RestResponse response = send(handler, "PUT", "/teams/" + alpha + "/tasks", "{}");
        assertEquals(405, response.status());
        assertEquals("GET, POST", response.headers().get("Allow"));
        assertEquals(405, send(handler, "DELETE", "/teams/" + alpha + "/tasks", null).status());
    }

    // --- policy ---

    @Test
    public void bothEntitiesAreAskedBeforeAnythingIsListed() {
        assertEquals(401, send(OrmRestHandler.of(collection, (principal, entity, operation) -> !entity.path().equals("teams")), "GET", "/teams/" + alpha + "/tasks", null).status(), "getting the team is not permitted");
        assertEquals(401, send(OrmRestHandler.of(collection, (principal, entity, operation) -> !entity.path().equals("tasks")), "GET", "/teams/" + alpha + "/tasks", null).status(), "listing the tasks is not permitted");
        assertEquals(401, send(OrmRestHandler.of(collection, (principal, entity, operation) -> !(entity.path().equals("tasks") && operation == RestOperation.CREATE)), "POST", "/teams/" + alpha + "/tasks", "{\"name\":\"x\"}").status());
    }

    private RestPolicy<Object> restrict(final String path, final FilterExpression restriction) {
        return new RestPolicy<Object>() {
            @Override public boolean permits(final @Nullable Object principal, final @NotNull ApiEntity entity, final @NotNull RestOperation operation) { return true; }
            @Override public @Nullable FilterExpression restriction(final @Nullable Object principal, final @NotNull ApiEntity entity) { return entity.path().equals(path) ? restriction : null; }
        };
    }

    @Test
    public void restrictionsOfBothEntitiesApply() throws Exception {
        final OrmRestHandler<Object> noBeta = OrmRestHandler.of(collection, restrict("teams", FilterExpression.from("key", FilterOperator.EQ, "alpha")));
        assertEquals(200, send(noBeta, "GET", "/teams/" + alpha + "/tasks", null).status());
        assertEquals(404, send(noBeta, "GET", "/teams/" + beta + "/tasks", null).status(), "a parent that is not visible is not found");
        assertEquals(404, send(noBeta, "POST", "/teams/" + beta + "/tasks", "{\"name\":\"x\"}").status());

        final OrmRestHandler<Object> onlyS = OrmRestHandler.of(collection, restrict("tasks", FilterExpression.from("name", FilterOperator.STARTSWITH, "S")));
        assertEquals(List.of("ship"), keys(send(onlyS, "GET", "/teams/" + alpha + "/tasks", null)));
    }

    // --- creating ---

    @Test
    public void createsAnEntityBelowTheParent() throws Exception {
        final RestResponse response = send(handler, "POST", "/teams/" + alpha + "/tasks", "{\"key\":\"deploy\",\"name\":\"Deploy\"}");
        assertEquals(201, response.status(), response.body());
        final long id = ((Number) Janitor.current().parseJsonToMap(response.body()).get(Janitor.string("id")).janitorGetHostValue()).longValue();
        assertEquals("../../tasks/" + id, response.headers().get("Location"), "the entity is at its own path, relative to the request");
        assertTrue(response.body().replaceAll("\\s", "").contains("\"team\":\"alpha\""), response.body());
        assertEquals(List.of("deploy", "ship", "test", "write"), keys(get("/teams/" + alpha + "/tasks")));
        assertEquals(200, get("/tasks/" + id).status());
    }

    @Test
    public void theBodyMayRepeatTheParentButNotNameAnother() throws Exception {
        assertEquals(201, send(handler, "POST", "/teams/" + alpha + "/tasks", "{\"name\":\"one\",\"team\":" + alpha + "}").status(), "the id");
        assertEquals(201, send(handler, "POST", "/teams/" + alpha + "/tasks", "{\"name\":\"two\",\"team\":\"alpha\"}").status(), "the key");
        assertEquals(201, send(handler, "POST", "/teams/" + alpha + "/tasks", "{\"name\":\"three\",\"team\":null}").status(), "nothing");
        final int before = taskCount();
        assertEquals(400, send(handler, "POST", "/teams/" + alpha + "/tasks", "{\"name\":\"x\",\"team\":" + beta + "}").status());
        assertEquals(400, send(handler, "POST", "/teams/" + alpha + "/tasks", "{\"name\":\"x\",\"team\":\"beta\"}").status());
        assertEquals(before, taskCount(), "nothing was created");
    }

    @Test
    public void theLocationOfPlainCreatesStaysTheSame() throws Exception {
        final RestResponse response = send(handler, "POST", "/tasks", "{\"name\":\"plain\"}");
        assertEquals(201, response.status(), response.body());
        assertTrue(response.headers().get("Location").matches("tasks/\\d+"), response.headers().get("Location"));
        assertTrue(response.body().replaceAll("\\s", "").contains("\"team\":null"));
    }
}
