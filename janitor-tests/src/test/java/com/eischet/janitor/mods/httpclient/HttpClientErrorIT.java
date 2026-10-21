// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.mods.httpclient;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.modules.httpclient.HttpClientModule;
import com.sun.net.httpserver.HttpServer;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Checks that every request method of the httpclient module reports failed requests the same way: scripts can read the
 * status code, the response body, the method and the response headers from the caught exception.
 * <p>
 * An integration test, run by failsafe from {@code mvn verify} on: the server needs a loopback connection, which sandboxes
 * do not always allow.
 * </p>
 */
public class HttpClientErrorIT extends JanitorTest {

    private HttpServer server;
    private String url;

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        // /status/<code> answers with that status, a body naming the request method and a header
        server.createContext("/status/", exchange -> {
            exchange.getRequestBody().readAllBytes();
            final int code = Integer.parseInt(exchange.getRequestURI().getPath().substring("/status/".length()));
            final byte[] body = ("answer to " + exchange.getRequestMethod()).getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("X-test", "yes");
            if (code == 204) {
                exchange.sendResponseHeaders(code, -1);
                exchange.close();
                return;
            }
            exchange.sendResponseHeaders(code, body.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(body);
            }
        });
        server.start();
        url = "http://127.0.0.1:" + server.getAddress().getPort() + "/status/";
    }

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    private String run(final @Language("Janitor") String calls, final String baseUrl) throws JanitorCompilerException, JanitorRuntimeException {
        final JanitorObject result = evaluate("""
                import httpclient;
                client = httpclient.build();
                function describe(call) {
                    try {
                        call();
                        return "ok";
                    } catch (e) {
                        return "" + e.code + "|" + e.body + "|" + e.method + "|" + e.headers.get("x-test");
                    }
                }
                """ + calls, env -> env.addModule(HttpClientModule.REGISTRATION), globals -> globals.bind("url", baseUrl));
        return result.janitorToString();
    }

    @Test
    void failedRequestsExposeStatusAndBody() throws Exception {
        final String result = run("""
                return [
                    describe(() -> { client.getString(url + "422"); }),
                    describe(() -> { client.getBinary(url + "422"); }),
                    describe(() -> { client.getJson(url + "422"); }),
                    describe(() -> { client.postForm(url + "422", {"a": "b"}); }),
                    describe(() -> { client.postJson(url + "422", {"a": "b"}); }),
                    describe(() -> { client.putJson(url + "422", {"a": "b"}); }),
                    describe(() -> { client.patchJson(url + "422", {"a": "b"}); }),
                    describe(() -> { client.delete(url + "422"); }),
                    describe(() -> { httpclient.fetch(url + "500"); })
                ].join("\\n");
                """, url);
        assertEquals("""
                422|answer to GET|GET|yes
                422|answer to GET|GET|yes
                422|answer to GET|GET|yes
                422|answer to POST|POST|yes
                422|answer to POST|POST|yes
                422|answer to PUT|PUT|yes
                422|answer to PATCH|PATCH|yes
                422|answer to DELETE|DELETE|yes
                500|answer to GET|GET|yes""", result);
    }

    @Test
    void allMethodsAcceptTheSameStatusCodes() throws Exception {
        // redirects are followed by the client; a 3xx without a Location header arrives as it is and counts as success
        for (final String status : new String[]{"200", "204", "300"}) {
            final String result = run("""
                    return [
                        describe(() -> { client.getString(url); }),
                        describe(() -> { client.getBinary(url); }),
                        describe(() -> { client.getJson(url); }),
                        describe(() -> { client.postForm(url, {"a": "b"}); }),
                        describe(() -> { client.postJson(url, {"a": "b"}); }),
                        describe(() -> { client.putJson(url, {"a": "b"}); }),
                        describe(() -> { client.patchJson(url, {"a": "b"}); }),
                        describe(() -> { client.delete(url); })
                    ].join(",");
                    """, url + status);
            assertEquals("ok,ok,ok,ok,ok,ok,ok,ok", result, "status " + status);
        }
    }

    @Test
    void networkErrorsHaveNoStatus() throws Exception {
        final int deadPort;
        try (ServerSocket socket = new ServerSocket(0)) {
            deadPort = socket.getLocalPort();
        }
        final String result = run("""
                return [
                    describe(() -> { client.getString(url); }),
                    describe(() -> { client.postJson(url, {"a": "b"}); }),
                    describe(() -> { client.delete(url); })
                ].join(",");
                """, "http://127.0.0.1:" + deadPort + "/");
        assertEquals("0|null|GET|null,0|null|POST|null,0|null|DELETE|null", result);
    }
}
