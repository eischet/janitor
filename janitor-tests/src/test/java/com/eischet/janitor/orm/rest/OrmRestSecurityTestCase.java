// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.rest;

import com.eischet.dbxs.exceptions.DatabaseError;
import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.orm.filter.FilterExpression;
import com.eischet.janitor.orm.filter.FilterOperator;
import com.eischet.janitor.orm.rest.OrmRestRelationsFixture.RelationsCollection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.*;

/** Authentication in the API description, and what callers are told when they are not permitted: 401 if nobody is authenticated, else 403. */
public class OrmRestSecurityTestCase extends JanitorTest {

    private RelationsCollection collection;

    @BeforeEach
    void setUp() throws DatabaseError {
        collection = OrmRestRelationsFixture.newCollection();
    }

    /** A policy that permits nothing (unless told otherwise) and says what it takes to call operations. */
    static class Described implements RestPolicy<String> {
        BiFunction<ApiEntity, RestOperation, List<ApiSecurityRequirement>> securityFor = (entity, operation) -> null;
        boolean permitsEverything;
        boolean permitsDescription = true;
        @Nullable FilterExpression taskRestriction;

        @Override public boolean permits(final @Nullable String user, final @NotNull ApiEntity entity, final @NotNull RestOperation operation) { return permitsEverything; }
        @Override public boolean permitsApiDescription(final @Nullable String user) { return permitsDescription; }
        @Override public @Nullable List<ApiSecurityRequirement> securityFor(final @NotNull ApiEntity entity, final @NotNull RestOperation operation) { return securityFor.apply(entity, operation); }
        @Override public @Nullable FilterExpression restriction(final @Nullable String user, final @NotNull ApiEntity entity) { return entity.path().equals("tasks") ? taskRestriction : null; }
    }

    private OrmRestHandler<String> handler(final RestPolicy<String> policy, final OpenApiOptions options) {
        return new OrmRestHandler<>(collection, OrmApiModel.of(collection), policy, options);
    }

    private static RestResponse send(final OrmRestHandler<String> handler, final String method, final String path, final @Nullable String user) {
        return handler.handle(RestRequest.of(method, path, user));
    }

    private static final OpenApiOptions BEARER = OpenApiOptions.defaults()
            .withSecurityScheme("bearer", ApiSecurityScheme.bearer("JWT"))
            .withSecurity(ApiSecurityRequirement.of("bearer"));

    // --- 401 and 403 ---

    @Test
    public void nobodyIsAskedToAuthenticateWhileSomebodyWhoIsNotPermittedIsForbidden() {
        final OrmRestHandler<String> handler = handler(new Described(), BEARER);
        final RestResponse anonymous = send(handler, "GET", "/teams", null);
        assertEquals(401, anonymous.status());
        assertEquals("Bearer", anonymous.headers().get("WWW-Authenticate"));
        assertEquals("application/problem+json", anonymous.contentType());
        assertTrue(anonymous.body().contains("Unauthorized"), anonymous.body());
        final RestResponse forbidden = send(handler, "GET", "/teams", "mallory");
        assertEquals(403, forbidden.status());
        assertNull(forbidden.headers().get("WWW-Authenticate"));
    }

    @Test
    public void everyKindOfRequestAsksAnonymousCallersToAuthenticate() {
        final OrmRestHandler<String> handler = handler(new Described(), BEARER);
        for (final String[] request : new String[][]{
                {"GET", "/teams"}, {"POST", "/teams/search"}, {"GET", "/teams/1"}, {"GET", "/teams/by-key/x"}, {"POST", "/tasks"}, {"PATCH", "/tasks/1"}, {"PUT", "/tasks/1"},
                {"DELETE", "/tasks/1"}, {"GET", "/teams/1/tasks"}, {"POST", "/teams/1/tasks"}, {"GET", "/teams/1/members"}, {"GET", "/teams/1/members/2"},
                {"PUT", "/teams/1/members/2"}, {"DELETE", "/teams/1/members/2"}}) {
            final RestResponse response = send(handler, request[0], request[1], null);
            assertEquals(401, response.status(), request[0] + " " + request[1]);
            assertEquals("Bearer", response.headers().get("WWW-Authenticate"), request[0] + " " + request[1]);
        }
    }

    @Test
    public void theChallengeSaysWhatTheSchemesExpect() {
        final OpenApiOptions basic = OpenApiOptions.defaults().withSecurityScheme("basic", ApiSecurityScheme.basic("the \"api\"")).withSecurity(ApiSecurityRequirement.of("basic"));
        assertEquals("Basic realm=\"the \\\"api\\\"\"", send(handler(new Described(), basic), "GET", "/teams", null).headers().get("WWW-Authenticate"));

        final OpenApiOptions both = OpenApiOptions.defaults()
                .withSecurityScheme("bearer", ApiSecurityScheme.bearer(null))
                .withSecurityScheme("basic", ApiSecurityScheme.basic("api"))
                .withSecurity(ApiSecurityRequirement.of("bearer"), ApiSecurityRequirement.of("basic"));
        assertEquals("Bearer, Basic realm=\"api\"", send(handler(new Described(), both), "GET", "/teams", null).headers().get("WWW-Authenticate"), "alternatives are all offered");

        final OpenApiOptions oauth = OpenApiOptions.defaults()
                .withSecurityScheme("oauth", ApiSecurityScheme.oauth2ClientCredentials("https://auth.example/token", Map.of("read", "Read")))
                .withSecurity(ApiSecurityRequirement.of("oauth", "read"));
        assertEquals("Bearer", send(handler(new Described(), oauth), "GET", "/teams", null).headers().get("WWW-Authenticate"));

        final OpenApiOptions apiKey = OpenApiOptions.defaults().withSecurityScheme("key", ApiSecurityScheme.apiKey("header", "X-Api-Key")).withSecurity(ApiSecurityRequirement.of("key"));
        final RestResponse withoutChallenge = send(handler(new Described(), apiKey), "GET", "/teams", null);
        assertEquals(401, withoutChallenge.status());
        assertNull(withoutChallenge.headers().get("WWW-Authenticate"), "there is no HTTP challenge for an API key");

        final OpenApiOptions custom = OpenApiOptions.defaults().withSecurityScheme("key", ApiSecurityScheme.apiKey("header", "X-Api-Key").withChallenge("ApiKey header=\"X-Api-Key\"")).withSecurity(ApiSecurityRequirement.of("key"));
        assertEquals("ApiKey header=\"X-Api-Key\"", send(handler(new Described(), custom), "GET", "/teams", null).headers().get("WWW-Authenticate"));
    }

    @Test
    public void withoutDeclaredSchemesTheAnswerIsStill401ButHasNoChallenge() {
        final RestResponse response = send(handler(new Described(), OpenApiOptions.defaults()), "GET", "/teams", null);
        assertEquals(401, response.status());
        assertNull(response.headers().get("WWW-Authenticate"));
    }

    @Test
    public void theChallengeCanDependOnTheOperation() {
        final OpenApiOptions options = OpenApiOptions.defaults()
                .withSecurityScheme("bearer", ApiSecurityScheme.bearer(null))
                .withSecurityScheme("basic", ApiSecurityScheme.basic("admin"))
                .withSecurity(ApiSecurityRequirement.of("bearer"));
        final Described policy = new Described();
        policy.securityFor = (entity, operation) -> operation == RestOperation.CREATE ? List.of(ApiSecurityRequirement.of("basic")) : null;
        final OrmRestHandler<String> handler = handler(policy, options);
        assertEquals("Bearer", send(handler, "GET", "/tasks", null).headers().get("WWW-Authenticate"));
        assertEquals("Basic realm=\"admin\"", send(handler, "POST", "/tasks", null).headers().get("WWW-Authenticate"));
        assertEquals("Basic realm=\"admin\"", send(handler, "POST", "/teams/1/tasks", null).headers().get("WWW-Authenticate"), "below a parent, the child's operation counts");
    }

    @Test
    public void whatIsCreatedOutsideOfTheRestrictionIsUnauthorizedOrForbidden() {
        final Described policy = new Described();
        policy.permitsEverything = true;
        policy.taskRestriction = FilterExpression.from("name", FilterOperator.STARTSWITH, "S");
        final OrmRestHandler<String> handler = handler(policy, BEARER);
        final RestResponse anonymous = handler.handle(new RestRequest<>("POST", "/tasks", Map.of(), Map.of(), "{\"name\":\"Other\"}", null));
        assertEquals(401, anonymous.status());
        assertEquals("Bearer", anonymous.headers().get("WWW-Authenticate"));
        assertEquals(403, handler.handle(new RestRequest<>("POST", "/tasks", Map.of(), Map.of(), "{\"name\":\"Other\"}", "mallory")).status());
        assertEquals(201, handler.handle(new RestRequest<>("POST", "/tasks", Map.of(), Map.of(), "{\"name\":\"Some\"}", "mallory")).status());
    }

    @Test
    public void theApiDescriptionCanBeProtectedToo() {
        final Described policy = new Described();
        policy.permitsEverything = true;
        policy.permitsDescription = false;
        final OrmRestHandler<String> handler = handler(policy, BEARER);
        final RestResponse anonymous = send(handler, "GET", "/openapi.json", null);
        assertEquals(401, anonymous.status());
        assertEquals("Bearer", anonymous.headers().get("WWW-Authenticate"));
        assertEquals(403, send(handler, "GET", "/openapi.json", "mallory").status());
        policy.permitsDescription = true;
        assertEquals(200, send(handler(policy, BEARER), "GET", "/openapi.json", null).status(), "by default, it is public");
        assertEquals(200, send(handler(RestPolicy.allowAll(), OpenApiOptions.defaults()), "GET", "/openapi.json", null).status());
    }

    // --- the description ---

    @SuppressWarnings("unchecked")
    private static Map<String, Object> map(final Object tree, final String... path) {
        Object node = tree;
        for (final String part : path) {
            node = ((Map<String, Object>) node).get(part);
        }
        return (Map<String, Object>) node;
    }

    private Map<String, Object> describe(final OpenApiOptions options, final RestSecurityDescription security) {
        return new OpenApiEmitter(OrmApiModel.of(collection), options, security).build();
    }

    /** All operations of a document: "GET /path" to the operation. */
    @SuppressWarnings("unchecked")
    private static Map<String, Map<String, Object>> operations(final Map<String, Object> document) {
        final Map<String, Map<String, Object>> result = new java.util.LinkedHashMap<>();
        map(document, "paths").forEach((path, item) -> ((Map<String, Object>) item).forEach((method, operation) -> result.put(method.toUpperCase() + " " + path, (Map<String, Object>) operation)));
        return result;
    }

    @Test
    public void withoutOptionsTheDescriptionSaysNothingAboutAuthentication() {
        final Map<String, Object> document = describe(OpenApiOptions.defaults(), RestSecurityDescription.none());
        assertNull(document.get("security"));
        assertNull(map(document, "components").get("securitySchemes"));
        assertNull(map(document, "components", "responses").get("Unauthorized"));
        operations(document).forEach((name, operation) -> {
            assertNull(operation.get("security"), name);
            assertFalse(map(operation, "responses").containsKey("401"), name);
        });
        assertTrue(map(document, "info").get("description").toString().contains("restricted depending on who is calling"));
    }

    @Test
    public void declaredSchemesAndTheDefaultRequirementAppearInTheDocument() {
        final Map<String, Object> document = describe(BEARER, RestSecurityDescription.none());
        assertEquals(Map.of("type", "http", "scheme", "bearer", "bearerFormat", "JWT"), map(document, "components", "securitySchemes", "bearer"));
        assertEquals(List.of(Map.of("bearer", List.of())), document.get("security"));
        assertNotNull(map(document, "components", "responses", "Unauthorized").get("headers"), "the challenge is documented");
        operations(document).forEach((name, operation) -> {
            assertNull(operation.get("security"), name + " takes the default of the document");
            assertEquals(Map.of("$ref", "#/components/responses/Unauthorized"), map(operation, "responses").get("401"), name);
            assertEquals(Map.of("$ref", "#/components/responses/Forbidden"), map(operation, "responses").get("403"), name);
        });
        assertTrue(operations(document).size() > 10);
    }

    @Test
    public void allowingAnonymousCallersRemovesTheNeedToAuthenticate() {
        final Map<String, Object> document = describe(BEARER.withAnonymousAllowed(), RestSecurityDescription.none());
        assertEquals(List.of(Map.of("bearer", List.of()), Map.of()), document.get("security"), "bearer or nothing");
        operations(document).forEach((name, operation) -> assertFalse(map(operation, "responses").containsKey("401"), name));
        assertNull(map(document, "components", "responses").get("Unauthorized"), "not used, so not there");
    }

    @Test
    public void operationsCanDeviateFromTheDefault() {
        final OpenApiOptions options = BEARER.withSecurityScheme("oauth", ApiSecurityScheme.oauth2AuthorizationCode("https://auth.example/authorize", "https://auth.example/token", Map.of("write", "Change things")));
        final RestSecurityDescription security = new RestSecurityDescription() {
            @Override
            public List<ApiSecurityRequirement> securityFor(final @NotNull ApiEntity entity, final @NotNull RestOperation operation) {
                return switch (operation) {
                    case LIST -> List.of(); // public
                    case CREATE, UPDATE, DELETE -> List.of(ApiSecurityRequirement.of("oauth", "write"), ApiSecurityRequirement.of("bearer"));
                    default -> null;
                };
            }
        };
        final Map<String, Object> document = describe(options, security);
        final Map<String, Map<String, Object>> operations = operations(document);
        assertEquals(List.of(), operations.get("GET /teams").get("security"), "public: security is an empty list");
        assertFalse(map(operations.get("GET /teams"), "responses").containsKey("401"), "and needs no 401");
        assertEquals(List.of(Map.of("oauth", List.of("write")), Map.of("bearer", List.of())), operations.get("POST /tasks").get("security"));
        assertTrue(map(operations.get("POST /tasks"), "responses").containsKey("401"));
        assertNull(operations.get("GET /teams/{id}").get("security"), "not mentioned: the default applies");
        assertTrue(map(operations.get("GET /teams/{id}"), "responses").containsKey("401"));
        assertEquals(List.of(), operations.get("GET /teams/{id}/tasks").get("security"), "below a parent, the child's operation counts: listing is public");
        assertEquals(operations.get("POST /tasks").get("security"), operations.get("POST /teams/{id}/tasks").get("security"));
        assertEquals(operations.get("PATCH /tasks/{id}").get("security"), operations.get("DELETE /tasks/{id}").get("security"));
        assertEquals(Map.of("type", "oauth2", "flows", Map.of("authorizationCode", Map.of(
                        "authorizationUrl", "https://auth.example/authorize", "tokenUrl", "https://auth.example/token", "scopes", Map.of("write", "Change things")))),
                map(document, "components", "securitySchemes", "oauth"));
    }

    @Test
    public void linksAreAsProtectedAsTheEntityTheyAreBelowUnlessSaidOtherwise() {
        final RestSecurityDescription security = new RestSecurityDescription() {
            @Override
            public List<ApiSecurityRequirement> securityFor(final @NotNull ApiEntity entity, final @NotNull RestOperation operation) {
                return entity.path().equals("teams") && operation == RestOperation.UPDATE ? List.of(ApiSecurityRequirement.of("bearer", "manage")) : null;
            }
        };
        final Map<String, Map<String, Object>> operations = operations(describe(BEARER, security));
        assertEquals(List.of(Map.of("bearer", List.of("manage"))), operations.get("PUT /teams/{id}/members/{otherId}").get("security"), "changing a link is updating the team");
        assertEquals(List.of(Map.of("bearer", List.of("manage"))), operations.get("DELETE /teams/{id}/members/{otherId}").get("security"));
        assertNull(operations.get("GET /teams/{id}/members").get("security"), "reading a link is getting the team");
        assertNull(operations.get("PUT /members/{id}/teams/{otherId}").get("security"), "members have nothing special");
    }

    @Test
    public void everySchemeTypeIsDescribedInTheWayOpenApiExpects() {
        final OpenApiOptions options = OpenApiOptions.defaults()
                .withSecurityScheme("basic", ApiSecurityScheme.basic("api").withDescription("User name and password"))
                .withSecurityScheme("key", ApiSecurityScheme.apiKey("query", "api_key"))
                .withSecurityScheme("cc", ApiSecurityScheme.oauth2ClientCredentials("https://auth.example/token", Map.of("read", "Read")))
                .withSecurityScheme("oidc", ApiSecurityScheme.openIdConnect("https://auth.example/.well-known/openid-configuration"))
                .withSecurityScheme("mtls", ApiSecurityScheme.mutualTls());
        final Map<String, Object> schemes = map(describe(options, RestSecurityDescription.none()), "components", "securitySchemes");
        assertEquals(Map.of("type", "http", "scheme", "basic", "description", "User name and password"), schemes.get("basic"));
        assertEquals(Map.of("type", "apiKey", "in", "query", "name", "api_key"), schemes.get("key"));
        assertEquals(Map.of("type", "oauth2", "flows", Map.of("clientCredentials", Map.of("tokenUrl", "https://auth.example/token", "scopes", Map.of("read", "Read")))), schemes.get("cc"));
        assertEquals(Map.of("type", "openIdConnect", "openIdConnectUrl", "https://auth.example/.well-known/openid-configuration"), schemes.get("oidc"));
        assertEquals(Map.of("type", "mutualTLS"), schemes.get("mtls"));
        assertEquals(List.of("basic", "key", "cc", "oidc", "mtls"), new ArrayList<>(options.securitySchemes().keySet()), "the order of declaration is kept");
    }

    @Test
    public void challengesOfTheSchemes() {
        assertEquals("Bearer", ApiSecurityScheme.bearer(null).challenge());
        assertEquals("Basic", ApiSecurityScheme.basic(null).challenge());
        assertEquals("Basic realm=\"x\"", ApiSecurityScheme.basic("x").challenge());
        assertEquals("Bearer", ApiSecurityScheme.openIdConnect("https://x").challenge());
        assertNull(ApiSecurityScheme.apiKey("header", "X").challenge());
        assertNull(ApiSecurityScheme.mutualTls().challenge());
        assertEquals("Custom", ApiSecurityScheme.mutualTls().withChallenge("Custom").challenge());
        assertThrows(IllegalArgumentException.class, () -> ApiSecurityScheme.apiKey("body", "X"));
    }

    @Test
    public void theDescriptionThatTheHandlerServesKnowsThePolicy() throws Exception {
        final Described policy = new Described();
        policy.permitsEverything = true;
        policy.securityFor = (entity, operation) -> operation == RestOperation.LIST ? List.of() : null;
        final RestResponse response = send(handler(policy, BEARER), "GET", "/openapi.json", null);
        assertEquals(200, response.status());
        final String json = response.body().replaceAll("\\s", "");
        assertTrue(json.contains("\"securitySchemes\":{\"bearer\":{\"type\":\"http\",\"scheme\":\"bearer\",\"bearerFormat\":\"JWT\"}}"), json);
        assertTrue(json.contains("\"security\":[]"), "the public operations");
        assertTrue(json.contains("\"security\":[{\"bearer\":[]}]"), "the default of the document");
    }
}
