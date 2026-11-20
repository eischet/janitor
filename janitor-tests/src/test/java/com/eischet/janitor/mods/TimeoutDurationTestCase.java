// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.mods;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.docusign.DocusignModule;
import com.eischet.janitor.modules.httpclient.HttpClientModule;
import com.eischet.janitor.modules.httpclient.JanitorHttpClient;
import com.eischet.janitor.modules.mcp.McpOptions;
import com.eischet.janitor.modules.msgraph.MsGraphOptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Scripts may write timeouts either as a number of seconds or as a duration like {@code @60s}; both mean the same.
 */
public class TimeoutDurationTestCase extends JanitorTest {

    @Test
    void httpClientTimeoutsAcceptDurations() throws JanitorCompilerException, JanitorRuntimeException {
        final JanitorObject result = evaluate("""
                import httpclient;
                return httpclient.build().setTimeout(@2mi).setRequestTimeout(@1h);
                """, env -> env.addModule(HttpClientModule.REGISTRATION), NO_GLOBALS);
        final JanitorHttpClient client = assertInstanceOf(JanitorHttpClient.class, result);
        assertEquals(120, client.getConnectTimeoutSeconds());
        assertEquals(3600, client.getRequestTimeoutSeconds());
    }

    @Test
    void httpClientTimeoutsStillAcceptSeconds() throws JanitorCompilerException, JanitorRuntimeException {
        final JanitorObject result = evaluate("""
                import httpclient;
                return httpclient.build().setTimeout(15).setRequestTimeout(45);
                """, env -> env.addModule(HttpClientModule.REGISTRATION), NO_GLOBALS);
        final JanitorHttpClient client = assertInstanceOf(JanitorHttpClient.class, result);
        assertEquals(15, client.getConnectTimeoutSeconds());
        assertEquals(45, client.getRequestTimeoutSeconds());
    }

    @Test
    void mcpTimeoutAcceptsDurations() throws JanitorCompilerException, JanitorRuntimeException {
        final McpOptions options = new McpOptions();
        evaluate("opts.timeoutSeconds = @90s; return opts.timeoutSeconds;", g -> g.bind("opts", options));
        assertEquals(90, options.getTimeoutSeconds());
        evaluate("opts.timeoutSeconds = 30;", g -> g.bind("opts", options));
        assertEquals(30, options.getTimeoutSeconds());
    }

    @Test
    void msGraphTimeoutAcceptsDurations() throws JanitorCompilerException, JanitorRuntimeException {
        final MsGraphOptions options = new MsGraphOptions();
        evaluate("opts.timeoutSeconds = @5mi;", g -> g.bind("opts", options));
        assertEquals(300, options.getTimeoutSeconds());
        evaluate("opts.timeoutSeconds = 30;", g -> g.bind("opts", options));
        assertEquals(30, options.getTimeoutSeconds());
    }

    @Test
    void mcpTimeoutHasShortName() throws JanitorCompilerException, JanitorRuntimeException {
        final McpOptions options = new McpOptions();
        assertEquals(60L, evaluate("return opts.timeout;", g -> g.bind("opts", options)).janitorGetHostValue());
        evaluate("opts.timeout = @2mi;", g -> g.bind("opts", options));
        assertEquals(120, options.getTimeoutSeconds());
        evaluate("opts.timeout = 45;", g -> g.bind("opts", options));
        assertEquals(45, options.getTimeoutSeconds());
        assertEquals(45L, evaluate("return opts.timeoutSeconds;", g -> g.bind("opts", options)).janitorGetHostValue());
    }

    @Test
    void msGraphTimeoutHasShortName() throws JanitorCompilerException, JanitorRuntimeException {
        final MsGraphOptions options = new MsGraphOptions();
        evaluate("opts.timeout = @90s;", g -> g.bind("opts", options));
        assertEquals(90, options.getTimeoutSeconds());
        assertEquals(90L, evaluate("return opts.timeoutSeconds;", g -> g.bind("opts", options)).janitorGetHostValue());
    }

    @Test
    void timeoutRejectsOtherTypes() {
        final McpOptions options = new McpOptions();
        assertThrows(JanitorRuntimeException.class, () -> evaluate("opts.timeoutSeconds = 'soon';", g -> g.bind("opts", options)));
        assertEquals(60, options.getTimeoutSeconds());
    }

    /**
     * The DocuSign library reads the private key before it contacts the server, so an invalid key fails without any network
     * access. Before the lifetime of the token could be a duration, the call failed earlier, at the argument.
     */
    @Test
    void docusignTokenLifetimeAcceptsDurations() {
        for (final String expiresIn : new String[]{"@1h", "3600"}) {
            final JanitorRuntimeException e = assertThrows(JanitorRuntimeException.class, () -> evaluate(
                    "docusign.Client().requestJWTUserToken('client', 'user', ['signature'], 'not a key', " + expiresIn + ");",
                    g -> g.bind("docusign", new DocusignModule())));
            assertTrue(e.getMessage().contains("error getting oauth token"), expiresIn + ": " + e.getMessage());
        }
    }

}
