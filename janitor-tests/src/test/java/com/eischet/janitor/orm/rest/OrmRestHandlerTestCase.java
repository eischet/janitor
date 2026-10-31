// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.rest;

import com.eischet.dbxs.SimpleDataManager;
import com.eischet.dbxs.TestDb;
import com.eischet.dbxs.exceptions.DatabaseError;
import com.eischet.dbxs.statements.UpdateStatement;
import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.builtin.JList;
import com.eischet.janitor.api.types.builtin.JMap;
import com.eischet.janitor.orm.filter.FilterExpression;
import com.eischet.janitor.orm.filter.FilterOperator;
import com.eischet.janitor.orm.ref.ForeignKeyInteger;
import com.eischet.janitor.orm.rest.OrmApiModelTestCase.Category;
import com.eischet.janitor.orm.rest.OrmApiModelTestCase.Item;
import com.eischet.janitor.orm.rest.OrmApiModelTestCase.Tag;
import com.eischet.janitor.orm.rest.OrmApiModelTestCase.TestCollection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** The request handling of the read-only API, against a real (H2) database. */
public class OrmRestHandlerTestCase extends JanitorTest {

    private TestCollection collection;
    private OrmRestHandler<Object> handler;
    private long gadgetId;

    @BeforeEach
    void setUpDatabase() throws DatabaseError {
        final SimpleDataManager manager = TestDb.newManager();
        collection = new TestCollection(manager);
        manager.executeTransaction(conn -> {
            conn.update(UpdateStatement.of("create sequence seq_category start with 1"));
            conn.update(UpdateStatement.of("create sequence seq_tag start with 1"));
            conn.update(UpdateStatement.of("create sequence seq_item start with 1"));
            conn.update(UpdateStatement.of("create table test_category (category_id bigint primary key, category_key varchar(20), category_name varchar(50))"));
            conn.update(UpdateStatement.of("create table test_tag (tag_id bigint primary key, tag_name varchar(50))"));
            conn.update(UpdateStatement.of("""
                    create table test_item (
                      item_id bigint primary key, item_key varchar(30), item_name varchar(80), item_code varchar(5), item_secret varchar(40),
                      item_rank integer, modified_at timestamp, version_no bigint, category_id bigint, tag_id bigint)"""));
            final Category tools = category("tools", "Tools");
            collection.getCategoryDao().insert(conn, tools);
            final Category toys = category("toys", "Toys");
            collection.getCategoryDao().insert(conn, toys);
            final Tag red = new Tag();
            red.setName("red");
            collection.getTagDao().insert(conn, red);
            insertItem(conn, "hammer", "Hammer", 3, tools, red, 7);
            insertItem(conn, "saw", "Saw", 1, tools, null, 1);
            insertItem(conn, "robot", "Toy Robot", 2, toys, null, 1);
            insertItem(conn, "gadget", "Gadget", 5, null, null, 1);
            insertItem(conn, "wrench", "Wrench", 4, tools, null, 1);
            gadgetId = collection.getItemDao().findByKey(conn, "gadget").getId();
        });
        handler = OrmRestHandler.of(collection, RestPolicy.allowAll());
    }

    private static Category category(final String key, final String name) {
        final Category category = new Category();
        category.setKey(key);
        category.setName(name);
        return category;
    }

    private void insertItem(final com.eischet.dbxs.DatabaseConnection conn, final String key, final String name, final int rank, final @Nullable Category category, final @Nullable Tag tag, final long version) throws DatabaseError {
        final Item item = new Item(collection);
        item.setKey(key);
        item.setName(name);
        item.setCode(key.substring(0, 3).toUpperCase());
        item.setSecret("s3cret-" + key);
        item.setRank(rank);
        item.setVersion(version);
        if (category != null) {
            item.setCategory(new ForeignKeyInteger<>(category.getId(), collection.getCategoryDao()));
        }
        if (tag != null) {
            item.setTag(new ForeignKeyInteger<>(tag.getId(), collection.getTagDao()));
        }
        collection.getItemDao().insert(conn, item);
    }

    // --- helpers ---

    private RestResponse get(final String path, final String... params) {
        return handler.handle(new RestRequest<>("GET", path, query(params), Map.of(), null, null));
    }

    private static Map<String, List<String>> query(final String... keysAndValues) {
        final java.util.HashMap<String, List<String>> result = new java.util.HashMap<>();
        for (int i = 0; i < keysAndValues.length; i += 2) {
            result.put(keysAndValues[i], List.of(keysAndValues[i + 1]));
        }
        return result;
    }

    private static JMap json(final RestResponse response) throws Exception {
        assertNotNull(response.body(), "no body, status " + response.status());
        return Janitor.current().parseJsonToMap(response.body());
    }

    private static List<String> names(final RestResponse response, final String field) throws Exception {
        final JList items = (JList) json(response).get(Janitor.string("items"));
        return items.stream().map(item -> ((JMap) item).get(Janitor.string(field)).janitorGetHostValue()).map(String::valueOf).toList();
    }

    private static String compact(final RestResponse response) {
        return response.body().replaceAll("\\s", "");
    }

    // --- lists ---

    @Test
    public void listsAPageWithTotalAndDefaultOrder() throws Exception {
        final RestResponse response = get("/items");
        assertEquals(200, response.status());
        assertEquals("application/json", response.contentType());
        assertEquals(List.of("gadget", "hammer", "robot", "saw", "wrench"), names(response, "key"));
        final JMap body = json(response);
        assertEquals(5L, ((Number) body.get(Janitor.string("total")).janitorGetHostValue()).longValue());
        assertEquals(0L, ((Number) body.get(Janitor.string("page")).janitorGetHostValue()).longValue());
        assertEquals(50L, ((Number) body.get(Janitor.string("size")).janitorGetHostValue()).longValue());
    }

    @Test
    public void pagesAreSlicesOfTheSameOrder() throws Exception {
        final RestResponse second = get("/items", "page", "1", "size", "2");
        assertEquals(List.of("robot", "saw"), names(second, "key"));
        assertEquals(5L, ((Number) json(second).get(Janitor.string("total")).janitorGetHostValue()).longValue(), "total counts all pages");
        assertEquals(List.of("wrench"), names(get("/items", "page", "2", "size", "2"), "key"));
    }

    @Test
    public void tooLargePageSizesAreReducedToTheMaximumOfTheEntity() throws Exception {
        final RestResponse response = get("/categories", "size", "100");
        assertEquals(200, response.status());
        assertEquals(2L, ((Number) json(response).get(Janitor.string("size")).janitorGetHostValue()).longValue());
        assertEquals(2, ((JList) json(response).get(Janitor.string("items"))).size());
    }

    @Test
    public void sortsByAllowedPropertiesInBothDirections() throws Exception {
        assertEquals(List.of("saw", "robot", "hammer", "wrench", "gadget"), names(get("/items", "sort", "rank"), "key"));
        assertEquals(List.of("gadget", "wrench", "hammer", "robot", "saw"), names(get("/items", "sort", "-rank"), "key"));
    }

    @Test
    public void filtersByAllowedProperties() throws Exception {
        final String filter = "{\"field\":\"name\",\"operator\":\"contains\",\"valueString\":\"toy\"}";
        final RestResponse response = get("/items", "filter", filter);
        assertEquals(List.of("robot"), names(response, "key"));
        assertEquals(1L, ((Number) json(response).get(Janitor.string("total")).janitorGetHostValue()).longValue());
    }

    @Test
    public void filtersCanBeGroups() throws Exception {
        final String filter = "{\"logic\":\"or\",\"filters\":[{\"field\":\"key\",\"operator\":\"eq\",\"valueString\":\"saw\"},{\"field\":\"rank\",\"operator\":\"gt\",\"valueLong\":4}]}";
        // rank is not filterable, so the whole filter is refused
        assertEquals(400, get("/items", "filter", filter).status());
        final String allowed = "{\"logic\":\"or\",\"filters\":[{\"field\":\"key\",\"operator\":\"eq\",\"valueString\":\"saw\"},{\"field\":\"key\",\"operator\":\"eq\",\"valueString\":\"robot\"}]}";
        assertEquals(List.of("saw", "robot"), names(get("/items", "filter", allowed, "sort", "-id"), "key").reversed());
    }

    @Test
    public void rejectsFiltersAndSortOrdersThatTheApiDoesNotAllow() {
        assertEquals(400, get("/items", "filter", "{\"field\":\"rank\",\"operator\":\"eq\",\"valueLong\":1}").status(), "not filterable");
        assertEquals(400, get("/items", "filter", "{\"field\":\"secret\",\"operator\":\"eq\",\"valueString\":\"x\"}").status(), "hidden");
        assertEquals(400, get("/items", "filter", "{\"field\":\"nope\",\"operator\":\"eq\",\"valueString\":\"x\"}").status(), "unknown");
        assertEquals(400, get("/items", "filter", "{\"field\":\"name\",\"operator\":\"eq\"}").status(), "incomplete");
        assertEquals(400, get("/items", "filter", "{\"field\":\"owner.name\",\"operator\":\"eq\",\"valueString\":\"x\"}").status(), "path through a reference");
        assertEquals(400, get("/items", "filter", "not json").status(), "malformed");
        assertEquals(400, get("/items", "sort", "secret").status(), "hidden sort");
        assertEquals(400, get("/items", "sort", "nope").status(), "unknown sort");
        assertEquals(400, get("/items", "page", "-1").status());
        assertEquals(400, get("/items", "size", "0").status());
        assertEquals(400, get("/items", "size", "many").status());
    }

    @Test
    public void searchTakesItsParametersFromTheBody() throws Exception {
        final String body = "{\"filter\":{\"field\":\"key\",\"operator\":\"startswith\",\"valueString\":\"r\"},\"sort\":[\"-name\"],\"page\":0,\"size\":10}";
        final RestResponse response = handler.handle(new RestRequest<>("POST", "/items/search", Map.of(), Map.of(), body, null));
        assertEquals(200, response.status());
        assertEquals(List.of("robot"), names(response, "key"));
        final RestResponse all = handler.handle(new RestRequest<>("POST", "/items/search", Map.of(), Map.of(), null, null));
        assertEquals(5, ((JList) json(all).get(Janitor.string("items"))).size(), "no body: no filter");
        assertEquals(400, handler.handle(new RestRequest<>("POST", "/items/search", Map.of(), Map.of(), "{\"sort\":\"name\"}", null)).status());
    }

    // --- single entities ---

    @Test
    public void getsAnEntityWithAllExposedPropertiesAndNoHiddenOnes() throws Exception {
        final RestResponse list = get("/items", "filter", "{\"field\":\"key\",\"operator\":\"eq\",\"valueString\":\"hammer\"}");
        final long id = ((Number) ((JMap) ((JList) json(list).get(Janitor.string("items"))).get(0)).get(Janitor.string("id")).janitorGetHostValue()).longValue();
        final RestResponse response = get("/items/" + id);
        assertEquals(200, response.status());
        final String body = compact(response);
        assertTrue(body.contains("\"key\":\"hammer\""), body);
        assertTrue(body.contains("\"name\":\"Hammer\""), body);
        assertTrue(body.contains("\"rank\":3"), body);
        assertTrue(body.contains("\"version\":7"), body);
        assertTrue(body.contains("\"modifiedDate\":\"2026-01-02T03:04:05Z\""), body);
        assertTrue(body.contains("\"category\":\"tools\""), "a reference to a target with a key is the key: " + body);
        assertTrue(body.contains("\"tag\":1"), "a reference to a target without key is the id: " + body);
        assertFalse(body.contains("secret"), body);
    }

    @Test
    public void nullsAreWrittenExplicitly() throws Exception {
        final String body = compact(get("/items/" + gadgetId));
        assertTrue(body.contains("\"category\":null"), body);
        assertTrue(body.contains("\"tag\":null"), body);
    }

    @Test
    public void getsByKeyAndAnswersNotFoundForStrangers() throws Exception {
        assertEquals(200, get("/items/by-key/saw").status());
        assertTrue(compact(get("/items/by-key/saw")).contains("\"key\":\"saw\""));
        assertEquals(404, get("/items/by-key/nobody").status());
        assertEquals(404, get("/items/999999").status());
        assertEquals(400, get("/items/abc").status());
        assertEquals(404, get("/tags").status(), "tags have no path: not exposed");
        assertEquals(404, get("/tags/1").status());
        assertEquals(404, get("/nothing").status());
        assertEquals(404, get("/items/1/2/3").status());
        assertEquals(404, get("/").status());
    }

    @Test
    public void versionedEntitiesComeWithETagAndLastModifiedAndSupportConditionalRequests() {
        final RestResponse response = get("/items/by-key/hammer");
        assertEquals("\"7\"", response.headers().get("ETag"));
        assertEquals("Fri, 2 Jan 2026 03:04:05 GMT", response.headers().get("Last-Modified"));

        final RestResponse byTag = handler.handle(new RestRequest<>("GET", "/items/by-key/hammer", Map.of(), Map.of("If-None-Match", "\"7\""), null, null));
        assertEquals(304, byTag.status());
        assertNull(byTag.body());
        assertEquals("\"7\"", byTag.headers().get("ETag"));
        assertEquals(200, handler.handle(new RestRequest<>("GET", "/items/by-key/hammer", Map.of(), Map.of("If-None-Match", "\"6\""), null, null)).status());
        assertEquals(304, handler.handle(new RestRequest<>("GET", "/items/by-key/hammer", Map.of(), Map.of("if-none-match", "W/\"7\", \"9\""), null, null)).status());

        assertEquals(304, handler.handle(new RestRequest<>("GET", "/items/by-key/hammer", Map.of(), Map.of("If-Modified-Since", "Fri, 2 Jan 2026 03:04:05 GMT"), null, null)).status());
        assertEquals(200, handler.handle(new RestRequest<>("GET", "/items/by-key/hammer", Map.of(), Map.of("If-Modified-Since", "Fri, 2 Jan 2026 03:04:04 GMT"), null, null)).status());
        assertEquals(200, handler.handle(new RestRequest<>("GET", "/items/by-key/hammer", Map.of(), Map.of("If-Modified-Since", "garbage"), null, null)).status());
    }

    @Test
    public void entitiesWithoutVersionHaveNoCachingHeaders() {
        final RestResponse response = get("/categories/by-key/tools");
        assertEquals(200, response.status());
        assertNull(response.headers().get("ETag"));
        assertNull(response.headers().get("Last-Modified"));
    }

    // --- routing ---

    @Test
    public void otherMethodsAreNotAllowed() {
        final RestResponse response = handler.handle(RestRequest.of("DELETE", "/items/1", null));
        assertEquals(405, response.status());
        assertEquals("GET", response.headers().get("Allow"));
        assertEquals("application/problem+json", response.contentType());
        assertEquals(405, handler.handle(RestRequest.of("POST", "/items", null)).status());
        assertEquals(405, handler.handle(RestRequest.of("GET", "/items/search", null)).status());
        assertEquals("POST", handler.handle(RestRequest.of("GET", "/items/search", null)).headers().get("Allow"));
    }

    @Test
    public void servesTheApiDescription() {
        final RestResponse response = get("/openapi.json");
        assertEquals(200, response.status());
        assertTrue(compact(response).contains("\"/items/{id}\""));
        assertEquals(405, handler.handle(RestRequest.of("POST", "/openapi.json", null)).status());
    }

    // --- policy ---

    @Test
    public void thePolicyDecidesPerEntityAndOperation() {
        final RestPolicy<Object> onlyCategoriesAndNoSearch = (principal, entity, operation) -> entity.path().equals("categories") && operation != RestOperation.SEARCH;
        handler = OrmRestHandler.of(collection, onlyCategoriesAndNoSearch);
        assertEquals(200, get("/categories").status());
        assertEquals(401, get("/items").status());
        assertEquals(401, get("/items/by-key/saw").status());
        assertEquals(401, handler.handle(RestRequest.of("POST", "/categories/search", null)).status());
        assertEquals(401, OrmRestHandler.of(collection, RestPolicy.denyAll()).handle(RestRequest.of("GET", "/items", null)).status());
    }

    @Test
    public void thePolicyGetsThePrincipal() {
        final Object[] seen = new Object[1];
        handler = OrmRestHandler.of(collection, (principal, entity, operation) -> {
            seen[0] = principal;
            return true;
        });
        handler.handle(new RestRequest<>("GET", "/items", Map.of(), Map.of(), null, "alice"));
        assertEquals("alice", seen[0]);
    }

    @Test
    public void aRestrictionLimitsListsAndLooksLikeNotFoundForSingleEntities() throws Exception {
        handler = OrmRestHandler.of(collection, new RestPolicy<Object>() {
            @Override
            public boolean permits(final @Nullable Object principal, final @NotNull ApiEntity entity, final @NotNull RestOperation operation) {
                return true;
            }

            @Override
            public @Nullable FilterExpression restriction(final @Nullable Object principal, final @NotNull ApiEntity entity) {
                return entity.path().equals("items") ? FilterExpression.from("name", FilterOperator.STARTSWITH, "S") : null;
            }
        });
        assertEquals(List.of("saw"), names(get("/items"), "key"));
        assertEquals(1L, ((Number) json(get("/items")).get(Janitor.string("total")).janitorGetHostValue()).longValue());
        assertEquals(200, get("/items/by-key/saw").status());
        assertEquals(404, get("/items/by-key/hammer").status());
        assertEquals(404, get("/items/" + gadgetId).status());
        assertEquals(List.of("saw"), names(get("/items", "filter", "{\"field\":\"key\",\"operator\":\"neq\",\"valueString\":\"x\"}"), "key"), "combined with the filter of the request");
    }

    @Test
    public void deniedFieldsAreLeftOutAndCannotBeFilteredOrSortedBy() throws Exception {
        handler = OrmRestHandler.of(collection, new RestPolicy<Object>() {
            @Override
            public boolean permits(final @Nullable Object principal, final @NotNull ApiEntity entity, final @NotNull RestOperation operation) {
                return true;
            }

            @Override
            public @NotNull Set<String> deniedFields(final @Nullable Object principal, final @NotNull ApiEntity entity) {
                return Set.of("name");
            }
        });
        assertFalse(compact(get("/items")).contains("\"name\""));
        assertFalse(compact(get("/items/by-key/saw")).contains("\"name\""));
        assertEquals(400, get("/items", "filter", "{\"field\":\"name\",\"operator\":\"eq\",\"valueString\":\"Saw\"}").status());
        assertEquals(400, get("/items", "sort", "name").status());
        assertEquals(200, get("/items", "sort", "key").status());
    }
}
