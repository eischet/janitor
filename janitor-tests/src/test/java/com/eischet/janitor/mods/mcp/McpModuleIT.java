// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.mods.mcp;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.modules.mcp.McpConnection;
import com.eischet.janitor.modules.mcp.McpModule;
import com.eischet.janitor.modules.mcp.McpOptions;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.jackson2.JacksonMcpJsonMapper;
import io.modelcontextprotocol.json.schema.jackson2.DefaultJsonSchemaValidator;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.transport.HttpServletStatelessServerTransport;
import io.modelcontextprotocol.spec.McpSchema;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.ee10.servlet.FilterHolder;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Exercises the mcp module against a real MCP server: the stateless server of the MCP Java SDK in an embedded Jetty, with
 * a few tools. Covers connecting, listing and calling tools from scripts, tool errors, headers, timeouts and the
 * automatic close at the end of a script.
 * <p>
 * An integration test, run by failsafe from {@code mvn verify} on, not by {@code mvn test} or {@code mvn package}: the server
 * needs a loopback connection, which sandboxes do not always allow.
 * </p>
 */
public class McpModuleIT extends JanitorTest {

    private static final String TOKEN = "Bearer test-token";
    private static final McpJsonMapper JSON = new JacksonMcpJsonMapper(new ObjectMapper());

    private Server server;
    private String url;
    private final List<String> authorizations = Collections.synchronizedList(new ArrayList<>());

    @BeforeEach
    void startServer() throws Exception {
        final HttpServletStatelessServerTransport transport = HttpServletStatelessServerTransport.builder()
                .jsonMapper(JSON)
                .messageEndpoint("/mcp")
                .build();
        // building the server connects it to the transport, which is the servlet
        McpServer.sync(transport)
                .serverInfo("test-server", "1.2.3")
                .instructions("Use the tools.")
                .capabilities(McpSchema.ServerCapabilities.builder().tools(false).build())
                .jsonMapper(JSON)
                .jsonSchemaValidator(new DefaultJsonSchemaValidator())
                .toolCall(McpSchema.Tool.builder()
                                .name("echo")
                                .title("Echo")
                                .description("Echoes the arguments.")
                                .inputSchema(JSON, """
                                        {"type": "object", "properties": {"text": {"type": "string"}, "n": {"type": "integer"}}, "required": ["text"]}""")
                                .build(),
                        (context, request) -> McpSchema.CallToolResult.builder()
                                .addTextContent("echo: " + request.arguments().get("text"))
                                .addTextContent("second part")
                                .structuredContent(request.arguments())
                                .build())
                .toolCall(McpSchema.Tool.builder()
                                .name("fail")
                                .description("Always fails.")
                                .inputSchema(JSON, "{\"type\": \"object\"}")
                                .build(),
                        (context, request) -> McpSchema.CallToolResult.builder()
                                .addTextContent("boom")
                                .isError(true)
                                .build())
                .toolCall(McpSchema.Tool.builder()
                                .name("slow")
                                .description("Takes its time.")
                                .inputSchema(JSON, "{\"type\": \"object\"}")
                                .build(),
                        (context, request) -> {
                            try {
                                Thread.sleep(3000);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                            return McpSchema.CallToolResult.builder().addTextContent("done").build();
                        })
                .build();

        // records the Authorization header and rejects requests without the expected token, like a real server would
        final Filter auth = (request, response, chain) -> {
            final String authorization = ((HttpServletRequest) request).getHeader("Authorization");
            authorizations.add(String.valueOf(authorization));
            if (!TOKEN.equals(authorization)) {
                ((HttpServletResponse) response).sendError(401, "Unauthorized");
                return;
            }
            chain.doFilter(request, response);
        };

        final ServletContextHandler context = new ServletContextHandler();
        context.addServlet(new ServletHolder(transport), "/mcp");
        context.addFilter(new FilterHolder(auth), "/*", EnumSet.of(DispatcherType.REQUEST));
        server = new Server(new InetSocketAddress("127.0.0.1", 0));
        server.setHandler(context);
        server.start();
        url = "http://127.0.0.1:" + ((ServerConnector) server.getConnectors()[0]).getLocalPort() + "/mcp";
    }

    @AfterEach
    void stopServer() throws Exception {
        if (server != null) {
            server.stop();
        }
    }

    private JanitorObject run(final @Language("Janitor") String script) throws JanitorCompilerException, JanitorRuntimeException {
        return evaluate(script, env -> env.addModule(McpModule.REGISTRATION), globals -> {
            globals.bind("url", url);
            globals.bind("token", TOKEN);
        });
    }

    @Test
    void connectsAndListsTools() throws Exception {
        final JanitorObject result = run("""
                import mcp;
                srv = mcp.connect(url, {"headers": {"Authorization": token}});
                names = [];
                for (tool in srv.tools()) {
                    names.add(tool.name);
                }
                echo = srv.tools()[0];
                r = srv.serverName + " " + srv.serverVersion + "|" + srv.instructions + "|" + names.join(",")
                    + "|" + echo.title + "|" + echo.description + "|" + echo.inputSchema.properties.text.type
                    + "|" + echo.inputSchema.required[0] + "|" + (srv.capabilities.tools != null);
                srv.close();
                r;
                """);
        assertEquals("test-server 1.2.3|Use the tools.|echo,fail,slow|Echo|Echoes the arguments.|string|text|true", result.janitorToString());
        assertTrue(authorizations.stream().allMatch(TOKEN::equals), authorizations.toString());
    }

    @Test
    void callsToolWithArguments() throws Exception {
        final JanitorObject result = run("""
                import mcp;
                srv = mcp.connect(url, {"headers": {"Authorization": token}});
                res = srv.call("echo", {"text": "Drucker", "n": 3, "open": true, "tags": ["a", "b"]});
                res.isError + "|" + res.text + "|" + res.content.size() + "|" + res.content[0].type + "|" + res.content[0].text
                    + "|" + res.structured.text + "|" + (res.structured.n == 3) + "|" + res.structured.open + "|" + res.structured.tags.join(",");
                """);
        assertEquals("false|echo: Drucker\nsecond part|2|text|echo: Drucker|Drucker|true|true|a,b", result.janitorToString());
    }

    @Test
    void toolErrorsAreFlaggedOrThrown() throws Exception {
        final JanitorObject result = run("""
                import mcp;
                srv = mcp.connect(url, {"headers": {"Authorization": token}});
                res = srv.call("fail");
                res.isError + "|" + res.text + "|" + (res.structured == null);
                """);
        assertEquals("true|boom|true", result.janitorToString());

        final JanitorRuntimeException e = assertThrows(JanitorRuntimeException.class, () -> run("""
                import mcp;
                srv = mcp.connect(url, {"headers": {"Authorization": token}, "throwOnError": true});
                srv.call("fail", {});
                """));
        assertTrue(e.getMessage().contains("boom"), e.getMessage());
    }

    @Test
    void unknownToolIsScriptError() {
        final JanitorRuntimeException e = assertThrows(JanitorRuntimeException.class, () -> run("""
                import mcp;
                srv = mcp.connect(url, {"headers": {"Authorization": token}});
                srv.call("nope", {});
                """));
        assertTrue(e.getMessage().contains("nope"), e.getMessage());
    }

    @Test
    void missingTokenIsRejectedWithoutLeakingHeaders() {
        // the token comes from a variable, because the error's traceback quotes the script line
        final JanitorRuntimeException e = assertThrows(JanitorRuntimeException.class, () -> evaluate("""
                import mcp;
                mcp.connect(url, {"headers": {"Authorization": badToken}});
                """, env -> env.addModule(McpModule.REGISTRATION), globals -> {
            globals.bind("url", url);
            globals.bind("badToken", "Bearer wrong-token");
        }));
        assertTrue(e.getMessage().contains("cannot connect to MCP server"), e.getMessage());
        assertTrue(e.getMessage().contains("401"), e.getMessage());
        assertFalse(e.getMessage().contains("wrong-token"), e.getMessage());
    }

    @Test
    void unreachableServerIsScriptError() throws Exception {
        final String deadUrl = url;
        server.stop();
        final JanitorRuntimeException e = assertThrows(JanitorRuntimeException.class, () -> evaluate("""
                import mcp;
                mcp.connect(deadUrl, {"timeoutSeconds": 5});
                """, env -> env.addModule(McpModule.REGISTRATION), globals -> globals.bind("deadUrl", deadUrl)));
        assertTrue(e.getMessage().contains("cannot connect to MCP server"), e.getMessage());
    }

    @Test
    void slowToolTimesOut() {
        final long start = System.nanoTime();
        final JanitorRuntimeException e = assertThrows(JanitorRuntimeException.class, () -> run("""
                import mcp;
                srv = mcp.connect(url, {"headers": {"Authorization": token}, "timeoutSeconds": 1});
                srv.call("slow", {});
                """));
        final long millis = (System.nanoTime() - start) / 1_000_000;
        assertTrue(e.getMessage().contains("slow"), e.getMessage());
        assertTrue(millis < 2900, "took " + millis + " ms");
    }

    @Test
    void connectionIsClosedAtScriptEnd() throws Exception {
        final JanitorObject result = run("""
                import mcp;
                mcp.connect(url, {"headers": {"Authorization": token}});
                """);
        final McpConnection connection = (McpConnection) result;
        assertTrue(connection.isClosed());
        assertThrows(RuntimeException.class, connection::listTools);
        connection.close(); // closing again does nothing
    }

    @Test
    void hostDefaultsApplyAndScriptsOverrideThem() throws Exception {
        final JanitorObject result = evaluate("""
                import mcp;
                srv = mcp.connect(url, {"clientName": "script"});
                srv.call("echo", {"text": "x"}).text;
                """, env -> env.addModule(McpModule.registration(() -> {
            final McpOptions options = new McpOptions();
            options.setHeader("Authorization", TOKEN);
            options.setClientName("host");
            return options;
        })), globals -> globals.bind("url", url));
        assertEquals("echo: x\nsecond part", result.janitorToString());
    }

    @Test
    void javaApi() {
        final McpOptions options = new McpOptions();
        options.setHeader("Authorization", TOKEN);
        final McpConnection connection = McpConnection.connect(url, options);
        try {
            assertEquals(List.of("echo", "fail", "slow"), connection.listTools().stream().map(McpSchema.Tool::name).toList());
            final McpSchema.CallToolResult result = connection.callTool("echo", "{\"text\": \"hi\"}");
            assertEquals(Map.of("text", "hi"), result.structuredContent());
        } finally {
            connection.close();
        }
    }
}
