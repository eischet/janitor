// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.features;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.JanitorEnvironment;
import com.eischet.janitor.api.credentials.ClientSecretCredential;
import com.eischet.janitor.api.credentials.JanitorCredential;
import com.eischet.janitor.api.credentials.PrivateKeyCredential;
import com.eischet.janitor.api.credentials.TokenCredential;
import com.eischet.janitor.api.credentials.UsernamePasswordCredential;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.docusign.DocusignModule;
import com.eischet.janitor.env.JanitorDefaultEnvironment;
import com.eischet.janitor.modules.httpclient.HttpClientModule;
import com.eischet.janitor.modules.mcp.McpModule;
import com.eischet.janitor.modules.msgraph.MsGraphModule;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Modules that log in somewhere take the host's credentials by name: they accept the kinds they can use, reject the others
 * with a message that names the credentials, and never show the secret to the script.
 */
public class CredentialsTestCase extends JanitorTest {

    record Password(String name, String username, String password) implements UsernamePasswordCredential {
    }

    record Token(String name, String token) implements TokenCredential {
    }

    record Client(String name, String clientId, String clientSecret) implements ClientSecretCredential {
    }

    record Key(String name, String clientId, String subject, String privateKey) implements PrivateKeyCredential {
    }

    private static final Map<String, JanitorCredential> STORE = Map.of(
            "PW", new Password("PW", "user", "pw-secret"),
            "TOKEN", new Token("TOKEN", "token-secret"),
            "CLIENT", new Client("CLIENT", "client-id", "client-secret"),
            "KEY", new Key("KEY", "integration-key", "user-id", "key-secret"));

    private static Consumer<JanitorEnvironment> withCredentials(final boolean provider) {
        return env -> {
            env.addModule(HttpClientModule.REGISTRATION);
            env.addModule(McpModule.REGISTRATION);
            env.addModule(MsGraphModule.REGISTRATION);
            env.addModule(DocusignModule.REGISTRATION);
            if (provider) {
                ((JanitorDefaultEnvironment) env).setCredentialProvider((process, name) -> STORE.get(name));
            }
        };
    }

    private @NotNull String error(final @Language("Janitor") String script, final boolean provider) {
        final JanitorRuntimeException e = assertThrows(JanitorRuntimeException.class, () -> evaluate(script, withCredentials(provider), null));
        return e.getMessage();
    }

    private static void assertNoSecret(final String text) {
        for (final String secret : new String[]{"pw-secret", "token-secret", "client-secret", "key-secret"}) {
            assertFalse(text.contains(secret), text);
        }
    }

    @Test
    void httpClientAcceptsPasswordsAndTokens() throws Exception {
        final String result = evaluate("""
                import httpclient;
                a = httpclient.build().credential("PW");
                b = httpclient.build().credential("TOKEN");
                "" + a + b;
                """, withCredentials(true), null).janitorToString();
        assertNoSecret(result);
    }

    @Test
    void wrongKindIsRejectedByName() {
        final String http = error("""
                import httpclient;
                httpclient.build().credential("CLIENT");
                """, true);
        assertTrue(http.contains("httpclient.credential: credentials CLIENT hold a client id and secret, which cannot be used here"), http);
        final String mcp = error("""
                import mcp;
                mcp.connect("http://127.0.0.1:1/", {"credential": "KEY"});
                """, true);
        assertTrue(mcp.contains("mcp.connect: credentials KEY hold a private key"), mcp);
        final String graph = error("""
                import msgraph;
                msgraph.connectWithCredential("PW", "tenant");
                """, true);
        assertTrue(graph.contains("msgraph.connectWithCredential: credentials PW hold a user name and password"), graph);
        final String docusign = error("""
                import docusign;
                docusign.Client().loginWithCredential("TOKEN", ["signature"]);
                """, true);
        assertTrue(docusign.contains("docusign.loginWithCredential: credentials TOKEN hold a token"), docusign);
        assertNoSecret(http + mcp + graph + docusign);
    }

    @Test
    void unknownNamesAndHostsWithoutCredentialsAreErrors() {
        final String unknown = error("""
                import httpclient;
                httpclient.build().credential("NOPE");
                """, true);
        assertTrue(unknown.contains("credentials NOPE do not exist"), unknown);
        final String none = error("""
                import httpclient;
                httpclient.build().credential("PW");
                """, false);
        assertTrue(none.contains("no credentials are available here"), none);
    }

    @Test
    void hostCanRefuseCredentials() {
        final JanitorRuntimeException e = assertThrows(JanitorRuntimeException.class, () -> evaluate("""
                import httpclient;
                httpclient.build().credential("PW");
                """, env -> {
            env.addModule(HttpClientModule.REGISTRATION);
            ((JanitorDefaultEnvironment) env).setCredentialProvider((process, name) -> {
                throw new com.eischet.janitor.api.errors.runtime.JanitorArgumentException(process, "not in this script");
            });
        }, null));
        assertEquals(true, e.getMessage().contains("not in this script"), e.getMessage());
    }
}
