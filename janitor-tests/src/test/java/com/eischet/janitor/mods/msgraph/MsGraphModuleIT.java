// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.mods.msgraph;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.modules.msgraph.MailFolder;
import com.eischet.janitor.modules.msgraph.MailMessage;
import com.eischet.janitor.modules.msgraph.MessageQuery;
import com.eischet.janitor.modules.msgraph.MessageUpdate;
import com.eischet.janitor.modules.msgraph.MsGraphClient;
import com.eischet.janitor.modules.msgraph.MsGraphException;
import com.eischet.janitor.modules.msgraph.MsGraphOptions;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Exercises the msgraph module against a local fake Graph server: paging, retries, error handling, JSON mapping of the
 * Janitor objects and the script interface.
 * <p>
 * An integration test, run by failsafe from {@code mvn verify} on, not by {@code mvn test} or {@code mvn package}: the fake server
 * needs a loopback connection, which sandboxes do not always allow.
 * </p>
 */
public class MsGraphModuleIT extends JanitorTest {

    private static final String USER = "someone@example.com";
    private static final String USER_PATH = "/v1.0/users/someone%40example.com";

    private HttpServer server;
    private String baseUrl;
    private final Map<String, Responder> routes = new LinkedHashMap<>();
    private final List<Recorded> requests = Collections.synchronizedList(new ArrayList<>());

    @FunctionalInterface
    private interface Responder {
        void respond(HttpExchange exchange, String body) throws IOException;
    }

    private record Recorded(String method, String path, @Nullable String query, String body, @Nullable String authorization, @Nullable String prefer) {
    }

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            final String body;
            try (InputStream in = exchange.getRequestBody()) {
                body = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }
            final String path = exchange.getRequestURI().getRawPath();
            requests.add(new Recorded(exchange.getRequestMethod(), path, exchange.getRequestURI().getRawQuery(), body,
                    exchange.getRequestHeaders().getFirst("Authorization"), exchange.getRequestHeaders().getFirst("Prefer")));
            final Responder responder = routes.get(exchange.getRequestMethod() + " " + path);
            if (responder == null) {
                send(exchange, 404, "{\"error\": {\"code\": \"NoRoute\", \"message\": \"no route for " + path + "\"}}");
            } else {
                responder.respond(exchange, body);
            }
        });
        server.start();
        baseUrl = "http://127.0.0.1:" + server.getAddress().getPort() + "/v1.0";
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    private MsGraphClient client() {
        final MsGraphOptions options = new MsGraphOptions();
        options.setBaseUrl(baseUrl);
        options.setTimeoutSeconds(10);
        return new MsGraphClient(new FixedToken(), options);
    }

    private void route(final String methodAndPath, final int status, final @Language("JSON") String json) {
        routes.put(methodAndPath, (exchange, body) -> send(exchange, status, json));
    }

    private static void send(final HttpExchange exchange, final int status, final String body) throws IOException {
        final byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.getResponseHeaders().add("request-id", "req-" + status);
        if (status == 204) {
            exchange.sendResponseHeaders(204, -1);
        } else {
            exchange.sendResponseHeaders(status, bytes.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(bytes);
            }
        }
        exchange.close();
    }

    private static Map<String, String> queryOf(final Recorded recorded) {
        if (recorded.query() == null) {
            return Map.of();
        }
        final Map<String, String> result = new LinkedHashMap<>();
        for (final String pair : recorded.query().split("&")) {
            final int eq = pair.indexOf('=');
            result.put(URLDecoder.decode(pair.substring(0, eq), StandardCharsets.UTF_8), URLDecoder.decode(pair.substring(eq + 1), StandardCharsets.UTF_8));
        }
        return result;
    }

    @Test
    void foldersAreReadAcrossPagesAndRecursively() {
        routes.put("GET " + USER_PATH + "/mailFolders", (exchange, body) -> {
            if (exchange.getRequestURI().getRawQuery().contains("page=2")) {
                send(exchange, 200, """
                        {"value": [{"id": "B", "displayName": "Second", "childFolderCount": 0, "totalItemCount": 3}]}""");
            } else {
                send(exchange, 200, """
                        {"@odata.context": "ignored",
                         "value": [{"id": "A", "displayName": "First", "childFolderCount": 1, "totalItemCount": 7, "wellKnownName": "unknown field"}],
                         "@odata.nextLink": "%s/users/someone%%40example.com/mailFolders?page=2"}""".formatted(baseUrl));
            }
        });
        route("GET " + USER_PATH + "/mailFolders/A/childFolders", 200, """
                {"value": [{"id": "A1", "displayName": "Child", "childFolderCount": 0}]}""");

        final List<MailFolder> top = client().listFolders(USER, null);
        assertEquals(List.of("A", "B"), top.stream().map(MailFolder::getId).toList());
        assertEquals(7, top.getFirst().getTotalItemCount());

        final List<MailFolder> all = client().listAllFolders(USER);
        assertEquals(List.of("First", "Child", "Second"), all.stream().map(MailFolder::getDisplayName).toList());
        assertTrue(requests.stream().allMatch(r -> "Bearer test-token".equals(r.authorization())));
    }

    @Test
    void messagesWithUniqueBodyAndNestedSender() {
        route("GET " + USER_PATH + "/mailFolders/inbox/messages", 200, """
                {"value": [{
                   "@odata.etag": "W/\\"x\\"",
                   "id": "m1",
                   "subject": "Hello",
                   "sender": {"emailAddress": {"name": "Alice", "address": "alice@example.com"}, "extra": 1},
                   "categories": ["Red", "Blue"],
                   "uniqueBody": {"contentType": "html", "content": "<p>only the new part</p>"},
                   "flag": {"flagStatus": "notFlagged"}
                }]}""");

        final MessageQuery query = new MessageQuery().setSelect(List.of("id", "subject", "sender", "categories", "uniqueBody")).setBodyType("text");
        final List<MailMessage> messages = client().listMessages(USER, "inbox", query);
        assertEquals(1, messages.size());
        final MailMessage message = messages.getFirst();
        assertEquals("Hello", message.getSubject());
        assertNotNull(message.getSender());
        assertEquals("alice@example.com", message.getSender().getAddress());
        assertEquals(List.of("Red", "Blue"), message.getCategories());
        assertNotNull(message.getUniqueBody());
        assertTrue(message.getUniqueBody().isHtml());
        assertEquals("<p>only the new part</p>", message.getUniqueBody().getContent());

        final Recorded request = requests.getFirst();
        final Map<String, String> params = queryOf(request);
        assertEquals("id,subject,sender,categories,uniqueBody", params.get("$select"));
        assertEquals("receivedDateTime asc", params.get("$orderby"));
        assertEquals("outlook.body-content-type=\"text\"", request.prefer());
    }

    @Test
    void throttledRequestsAreRetried() {
        final AtomicInteger calls = new AtomicInteger();
        routes.put("GET " + USER_PATH + "/mailFolders/inbox", (exchange, body) -> {
            if (calls.incrementAndGet() == 1) {
                exchange.getResponseHeaders().add("Retry-After", "0");
                send(exchange, 429, "{\"error\": {\"code\": \"TooManyRequests\", \"message\": \"slow down\"}}");
            } else {
                send(exchange, 200, "{\"id\": \"inbox-id\", \"displayName\": \"Inbox\", \"totalItemCount\": 42}");
            }
        });
        final MailFolder inbox = client().getFolder(USER, "inbox");
        assertEquals(42, inbox.getTotalItemCount());
        assertEquals(2, calls.get());
    }

    @Test
    void errorsCarryGraphCodeAndRequestId() {
        route("GET " + USER_PATH + "/mailFolders/missing", 404, """
                {"error": {"code": "ErrorItemNotFound", "message": "The specified object was not found in the store."}}""");
        final MsGraphException e = assertThrows(MsGraphException.class, () -> client().getFolder(USER, "missing"));
        assertEquals(404, e.getStatus());
        assertEquals("ErrorItemNotFound", e.getCode());
        assertEquals("req-404", e.getRequestId());
        assertTrue(e.getMessage().contains("The specified object was not found"));
    }

    @Test
    void updateSendsOnlyTheSetProperties() {
        route("PATCH " + USER_PATH + "/messages/m1", 200, "{\"id\": \"m1\"}");
        client().updateMessage(USER, "m1", new MessageUpdate().setSubject("[42] Hello").setCategories(List.of()));
        final Recorded request = requests.getFirst();
        assertEquals("PATCH", request.method());
        assertEquals("{\"subject\":\"[42] Hello\",\"categories\":[]}", request.body());

        requests.clear();
        client().updateMessage(USER, "m1", new MessageUpdate());
        assertTrue(requests.isEmpty(), "an empty update must not be sent");
    }

    @Test
    void moveMimeAndFolderMaintenance() {
        route("POST " + USER_PATH + "/messages/m1/move", 201, "{\"id\": \"m1-moved\"}");
        routes.put("GET " + USER_PATH + "/messages/m1/$value", (exchange, body) -> {
            final byte[] mime = "Subject: Hello\r\n\r\nBody\r\n".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, mime.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(mime);
            }
            exchange.close();
        });
        route("GET " + USER_PATH + "/mailFolders", 200, "{\"value\": [{\"id\": \"F1\", \"displayName\": \"O'Brien\"}]}");
        route("POST " + USER_PATH + "/mailFolders/F1/childFolders", 201, "{\"id\": \"F2\", \"displayName\": \"Archive\"}");
        route("DELETE " + USER_PATH + "/mailFolders/F2", 204, "");

        final MsGraphClient client = client();
        assertEquals("m1-moved", client.moveMessage(USER, "m1", "deleteditems"));
        assertEquals("{\"destinationId\":\"deleteditems\"}", requests.getFirst().body());
        assertTrue(new String(client.getMimeContent(USER, "m1"), StandardCharsets.UTF_8).startsWith("Subject: Hello"));

        final MailFolder found = client.findFolder(USER, "O'Brien", null);
        assertNotNull(found);
        assertEquals("displayName eq 'O''Brien'", queryOf(requests.get(2)).get("$filter"));

        final MailFolder created = client.createFolder(USER, "Archive", "F1");
        assertEquals("F2", created.getId());
        assertEquals("{\"displayName\":\"Archive\",\"isHidden\":false}", requests.get(3).body());
        client.deleteFolder(USER, "F2");
        assertEquals("DELETE", requests.get(4).method());
    }

    @Test
    void findFolderReturnsNullWhenThereIsNoMatch() {
        route("GET " + USER_PATH + "/mailFolders", 200, "{\"value\": []}");
        assertNull(client().findFolder(USER, "nope", null));
    }

    @Test
    void scriptInterface() throws JanitorCompilerException, JanitorRuntimeException {
        route("GET " + USER_PATH + "/mailFolders", 200, "{\"value\": [{\"id\": \"F1\", \"displayName\": \"Inbox\", \"childFolderCount\": 0}]}");
        route("GET " + USER_PATH + "/mailFolders/inbox/messages", 200, """
                {"value": [{"id": "m1", "subject": "Hello", "sender": {"emailAddress": {"address": "alice@example.com"}},
                            "uniqueBody": {"contentType": "text", "content": "new text"}}]}""");
        route("PATCH " + USER_PATH + "/messages/m1", 200, "{\"id\": \"m1\"}");

        final @Language("Janitor") String script = """
                names = [];
                for (folder in graph.allFolders(user)) {
                    names.add(folder.displayName);
                }
                msgs = graph.messages(user, "inbox", {select: ["id", "subject", "sender", "uniqueBody"], top: 5});
                m = msgs[0];
                graph.update(user, m.id, {subject: "[1] " + m.subject, categories: ["Done"]});
                names.join(",") + "|" + m.sender.emailAddress.address + "|" + m.uniqueBody.content + "|" + m.uniqueBody.html;
                """;
        final JanitorObject result = evaluate(script, null, globals -> {
            globals.bind("graph", client());
            globals.bind("user", USER);
        });
        assertEquals("Inbox|alice@example.com|new text|false", result.janitorToString());
        final Recorded patch = requests.stream().filter(r -> r.method().equals("PATCH")).findFirst().orElseThrow();
        assertEquals("{\"subject\":\"[1] Hello\",\"categories\":[\"Done\"]}", patch.body());
        assertEquals("5", queryOf(requests.get(1)).get("$top"));
    }

    @Test
    void scriptErrorsBecomeScriptExceptions() {
        route("GET " + USER_PATH + "/mailFolders/missing", 404, "{\"error\": {\"code\": \"ErrorItemNotFound\", \"message\": \"gone\"}}");
        final JanitorRuntimeException e = assertThrows(JanitorRuntimeException.class, () -> evaluate("graph.folder(user, \"missing\");", null, globals -> {
            globals.bind("graph", client());
            globals.bind("user", USER);
        }));
        assertTrue(e.getMessage().contains("ErrorItemNotFound"), e.getMessage());
        assertFalse(e.getMessage().contains("test-token"));
    }

    @Test
    void optionsTreatBlankProxyAsNone() {
        final MsGraphOptions options = new MsGraphOptions();
        options.setProxyHost("  ");
        assertFalse(options.hasProxy());
        options.setBaseUrl("https://example.com/v1.0/");
        assertEquals("https://example.com/v1.0", options.getBaseUrl());
        assertEquals(MsGraphOptions.DEFAULT_BASE_URL, new MsGraphOptions().getBaseUrl());
        assertEquals("id,subject,sender,categories", String.join(",", new MessageQuery().getSelect()));
    }

    /**
     * Hands out a fixed token, so the tests do not need Entra ID.
     */
    private static final class FixedToken implements TokenCredential {
        @Override
        public Mono<AccessToken> getToken(final TokenRequestContext request) {
            return Mono.just(new AccessToken("test-token", OffsetDateTime.now().plusHours(1)));
        }

        @Override
        public AccessToken getTokenSync(final TokenRequestContext request) {
            return new AccessToken("test-token", OffsetDateTime.now().plusHours(1));
        }
    }
}
