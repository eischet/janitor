// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.mods.httpclient;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.credentials.TokenCredential;
import com.eischet.janitor.api.credentials.UsernamePasswordCredential;
import com.eischet.janitor.env.JanitorDefaultEnvironment;
import com.eischet.janitor.modules.httpclient.HttpClientModule;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Requests of a client with {@code credential(name)} carry the host's credentials: a user name and password as HTTP basic
 * authentication, a token as bearer token.
 * <p>
 * An integration test, run by failsafe from {@code mvn verify} on: the server needs a loopback connection.
 * </p>
 */
public class HttpClientCredentialIT extends JanitorTest {

    record Password(String name, String username, String password) implements UsernamePasswordCredential {
    }

    record Token(String name, String token) implements TokenCredential {
    }

    private HttpServer server;
    private String url;

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        // answers with the Authorization header it received
        server.createContext("/", exchange -> {
            exchange.getRequestBody().readAllBytes();
            final String auth = exchange.getRequestHeaders().getFirst("Authorization");
            final byte[] body = String.valueOf(auth).getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(body);
            }
        });
        server.start();
        url = "http://127.0.0.1:" + server.getAddress().getPort() + "/";
    }

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void credentialsAreSentWithEveryRequest() throws Exception {
        final String result = evaluate("""
                import httpclient;
                pw = httpclient.build().credential("PW");
                token = httpclient.build().credential("TOKEN");
                [pw.getString(url), pw.postJson(url, {"a": 1}), token.getString(url)].join("|");
                """, env -> {
            env.addModule(HttpClientModule.REGISTRATION);
            ((JanitorDefaultEnvironment) env).setCredentialProvider((process, name) -> switch (name) {
                case "PW" -> new Password("PW", "user", "secret");
                case "TOKEN" -> new Token("TOKEN", "abc");
                default -> null;
            });
        }, globals -> globals.bind("url", url)).janitorToString();
        final String basic = "Basic " + Base64.getEncoder().encodeToString("user:secret".getBytes(StandardCharsets.UTF_8));
        assertEquals(basic + "|" + basic + "|Bearer abc", result);
    }
}
