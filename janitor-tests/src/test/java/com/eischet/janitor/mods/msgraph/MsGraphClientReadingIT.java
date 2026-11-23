// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.mods.msgraph;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.modules.msgraph.MailMessage;
import com.eischet.janitor.modules.msgraph.MessageQuery;
import com.eischet.janitor.modules.msgraph.MsGraphClient;
import com.eischet.janitor.modules.msgraph.MsGraphException;
import com.eischet.janitor.modules.msgraph.MsGraphOptions;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Reading a mailbox page by page: a page further down the folder is fetched with $skip and carries date, author and attachment
 * flag, and a message that is gone meanwhile shows as status 404.
 */
class MsGraphClientReadingIT extends JanitorTest {

    private HttpServer server;
    private final List<String> requests = new ArrayList<>();

    @BeforeEach
    void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1.0/users/desk@example.com/mailFolders/inbox/messages", exchange -> {
            requests.add(URLDecoder.decode(exchange.getRequestURI().getRawQuery(), StandardCharsets.UTF_8));
            answer(exchange, 200, """
                    {"value": [{"id": "m51", "subject": "Drucker", "receivedDateTime": "2026-11-23T09:15:00Z", "hasAttachments": true,
                                "from": {"emailAddress": {"name": "Pat", "address": "pat@example.com"}},
                                "sender": {"emailAddress": {"name": "Robot", "address": "robot@example.com"}}}]}
                    """);
        });
        server.createContext("/v1.0/users/desk@example.com/messages/", exchange -> {
            if (exchange.getRequestURI().getPath().endsWith("/gone")) {
                answer(exchange, 404, "{\"error\": {\"code\": \"ErrorItemNotFound\", \"message\": \"The specified object was not found in the store.\"}}");
            } else {
                answer(exchange, 200, "{\"id\": \"m51\", \"subject\": \"Drucker\", \"hasAttachments\": false}");
            }
        });
        server.start();
    }

    @AfterEach
    void stop() {
        server.stop(0);
    }

    @Test
    void readsOnePageFurtherDown() {
        final List<MailMessage> page = new ArrayList<>();
        client().forEachMessage("desk@example.com", "inbox", new MessageQuery()
                .setSelect(List.of("id", "subject", "from", "sender", "receivedDateTime", "hasAttachments"))
                .setTop(50).setSkip(50).setOrderBy("receivedDateTime desc"), message -> page.add(message) && page.size() < 50);

        assertEquals(1, requests.size());
        assertTrue(requests.getFirst().contains("$skip=50"), requests.getFirst());
        assertTrue(requests.getFirst().contains("$top=50"), requests.getFirst());
        final MailMessage message = page.getFirst();
        assertEquals("2026-11-23T09:15:00Z", message.getReceivedDateTime());
        assertEquals(Boolean.TRUE, message.getHasAttachments());
        assertEquals("pat@example.com", message.getFrom().getEmailAddress().getAddress());
        assertEquals("robot@example.com", message.getSender().getEmailAddress().getAddress());
    }

    @Test
    void theFirstPageSendsNoSkip() {
        client().forEachMessage("desk@example.com", "inbox", new MessageQuery().setSkip(-5), message -> false);
        assertTrue(!requests.getFirst().contains("$skip"), requests.getFirst());
    }

    @Test
    void aSingleMessageAndOneThatIsGone() {
        final MsGraphClient client = client();
        assertEquals(Boolean.FALSE, client.getMessage("desk@example.com", "m51", List.of("id", "subject", "hasAttachments")).getHasAttachments());
        final MsGraphException gone = assertThrows(MsGraphException.class, () -> client.getMessage("desk@example.com", "gone", null));
        assertEquals(404, gone.getStatus());
    }

    private MsGraphClient client() {
        final MsGraphOptions options = new MsGraphOptions();
        options.setBaseUrl("http://127.0.0.1:" + server.getAddress().getPort() + "/v1.0");
        return new MsGraphClient(new FixedToken(), options);
    }

    private static void answer(final com.sun.net.httpserver.HttpExchange exchange, final int status, final String json) throws IOException {
        final byte[] body = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, body.length);
        exchange.getResponseBody().write(body);
        exchange.close();
    }

    private static final class FixedToken implements TokenCredential {
        @Override
        public Mono<AccessToken> getToken(final TokenRequestContext request) {
            return Mono.just(getTokenSync(request));
        }

        @Override
        public AccessToken getTokenSync(final TokenRequestContext request) {
            return new AccessToken("test-token", OffsetDateTime.now().plusHours(1));
        }
    }
}
