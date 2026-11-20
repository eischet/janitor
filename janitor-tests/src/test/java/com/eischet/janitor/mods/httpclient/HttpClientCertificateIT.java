// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.mods.httpclient;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.credentials.CertificateCredential;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.env.JanitorDefaultEnvironment;
import com.eischet.janitor.modules.httpclient.HttpClientModule;
import com.eischet.janitor.modules.httpclient.JanitorHttpClient;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsExchange;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * A client with {@code clientCertificate(name)} presents the certificate of the host's credentials to a server that demands one;
 * without it the server refuses the connection.
 * <p>
 * An integration test, run by failsafe from {@code mvn verify} on: the server needs a loopback connection. The server's certificate is
 * self-signed, so the client ignores security issues; the client certificate is presented the same way on the regular path.
 * </p>
 */
public class HttpClientCertificateIT extends JanitorTest {

    private static final char[] PASSWORD = "testtest".toCharArray();

    record Certificate(String name, PrivateKey privateKey, List<X509Certificate> certificateChain) implements CertificateCredential {
    }

    private HttpsServer server;
    private String url;
    private boolean allowedBefore;

    private static KeyStore load(final String name) throws Exception {
        final KeyStore store = KeyStore.getInstance("PKCS12");
        try (InputStream in = HttpClientCertificateIT.class.getResourceAsStream("/tls/" + name)) {
            store.load(in, PASSWORD);
        }
        return store;
    }

    @BeforeEach
    void startServer() throws Exception {
        allowedBefore = JanitorHttpClient.ALLOW_IGNORE_SECURITY_ISSUES;
        JanitorHttpClient.ALLOW_IGNORE_SECURITY_ISSUES = true;
        final KeyManagerFactory keys = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keys.init(load("server.p12"), PASSWORD);
        final TrustManagerFactory trust = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trust.init(load("server-trust.p12"));
        final SSLContext context = SSLContext.getInstance("TLS");
        context.init(keys.getKeyManagers(), trust.getTrustManagers(), null);
        server = HttpsServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.setHttpsConfigurator(new HttpsConfigurator(context) {
            @Override
            public void configure(final HttpsParameters params) {
                final SSLParameters parameters = context.getDefaultSSLParameters();
                parameters.setNeedClientAuth(true);
                params.setSSLParameters(parameters);
            }
        });
        // answers with the subject of the client certificate
        server.createContext("/", exchange -> {
            exchange.getRequestBody().readAllBytes();
            final String subject = ((X509Certificate) ((HttpsExchange) exchange).getSSLSession().getPeerCertificates()[0]).getSubjectX500Principal().getName();
            final byte[] body = subject.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(body);
            }
        });
        server.start();
        url = "https://127.0.0.1:" + server.getAddress().getPort() + "/";
    }

    @AfterEach
    void stopServer() {
        JanitorHttpClient.ALLOW_IGNORE_SECURITY_ISSUES = allowedBefore;
        if (server != null) {
            server.stop(0);
        }
    }

    private String run(final String script) throws Exception {
        final KeyStore client = load("client.p12");
        final Certificate certificate = new Certificate("CLIENT", (PrivateKey) client.getKey("client", PASSWORD),
                List.of((X509Certificate) client.getCertificate("client")));
        return evaluate(script, env -> {
            env.addModule(HttpClientModule.REGISTRATION);
            ((JanitorDefaultEnvironment) env).setCredentialProvider((process, name) -> "CLIENT".equals(name) ? certificate : null);
        }, globals -> globals.bind("url", url)).janitorToString();
    }

    @Test
    void theServerSeesTheClientCertificate() throws Exception {
        assertEquals("CN=janitor-test-client", run("""
                import httpclient;
                httpclient.build().ignoreSecurityIssues(true).clientCertificate("CLIENT").getString(url);
                """));
    }

    @Test
    void withoutItTheServerRefuses() {
        assertThrows(JanitorRuntimeException.class, () -> run("""
                import httpclient;
                httpclient.build().ignoreSecurityIssues(true).getString(url);
                """));
    }
}
