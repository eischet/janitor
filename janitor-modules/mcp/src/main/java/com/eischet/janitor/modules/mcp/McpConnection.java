// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.mcp;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.JanitorEnvironment;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorNativeException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorCleanupRequired;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JList;
import com.eischet.janitor.api.types.builtin.JMap;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.client.transport.McpHttpClientTransportAuthorizationException;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.client.transport.customizer.McpSyncHttpClientRequestCustomizer;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.jackson2.JacksonMcpJsonMapper;
import io.modelcontextprotocol.json.schema.jackson2.DefaultJsonSchemaValidator;
import io.modelcontextprotocol.spec.McpClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * A connection to an MCP server, built on the client of the official MCP Java SDK. Scripts get one from
 * {@code mcp.connect(url[, options])}:
 * <pre>
 * srv = mcp.connect("https://example.com/mcp", {"headers": {"Authorization": "Bearer ..."}});
 * for (tool in srv.tools()) { print(tool.name + ": " + tool.description); }
 * res = srv.call("search", {"text": "printer"});
 * print(res.text);
 * srv.close();
 * </pre>
 * <p>
 * Values cross between Janitor and the SDK as JSON: script arguments are written with Janitor's JSON writer and handed
 * to the SDK as text, and whatever the SDK returns (tools, schemas, content, capabilities) is written by the SDK's
 * mapper and read with Janitor's JSON reader. So Janitor values map to JSON exactly as everywhere else in Janitor.
 * </p>
 * <p>
 * The connection is closed when the script that opened it ends, if the script did not call {@code close()} itself.
 * </p>
 */
public class McpConnection extends JanitorComposed<McpConnection> implements JanitorCleanupRequired {

    private static final Logger log = LoggerFactory.getLogger(McpConnection.class);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final McpJsonMapper JSON_MAPPER = new JacksonMcpJsonMapper(OBJECT_MAPPER);

    public static final DispatchTable<McpConnection> DISPATCH = new DispatchTable<>();

    static {
        DISPATCH.addStringProperty("serverName", McpConnection::getServerName);
        DISPATCH.addStringProperty("serverVersion", McpConnection::getServerVersion);
        DISPATCH.addStringProperty("instructions", McpConnection::getInstructions);
        DISPATCH.addStringProperty("protocolVersion", McpConnection::getProtocolVersion);
        DISPATCH.addObjectProperty("capabilities", self -> toJanitor(Janitor.current(), self.initResult.capabilities()));
        DISPATCH.addMethod("tools", (self, process, args) -> {
            args.require(0);
            final List<McpSchema.Tool> tools = call(process, self::listTools);
            final JList list = process.getBuiltins().list(tools.size());
            for (final McpSchema.Tool tool : tools) {
                list.add(toJanitor(process.getEnvironment(), tool));
            }
            return list;
        });
        DISPATCH.addMethod("call", (self, process, args) -> {
            args.require(1, 2);
            final String name = args.getRequiredStringValue(0);
            final JMap arguments = args.size() > 1 ? args.getNullable(1, JMap.class) : null;
            final String json = arguments == null ? "{}" : writeJson(process, arguments);
            final McpSchema.CallToolResult result = call(process, () -> self.callTool(name, json));
            final McpToolResult toolResult = new McpToolResult(result,
                    contentToJanitor(process.getEnvironment(), result.content()),
                    toJanitor(process.getEnvironment(), result.structuredContent()));
            if (toolResult.isError() && self.options.isThrowOnError()) {
                final String text = toolResult.getText();
                throw new JanitorNativeException(process, "MCP tool '" + name + "' failed" + (text.isEmpty() ? "" : ": " + text),
                        new McpException("tool '" + name + "' returned an error"));
            }
            return toolResult;
        });
        DISPATCH.addVoidMethod("close", (self, process, args) -> {
            args.require(0);
            self.close();
        });
    }

    private final @NotNull McpSyncClient client;
    private final @NotNull McpSchema.InitializeResult initResult;
    private final @NotNull McpOptions options;
    private final @NotNull String endpoint;
    private final AtomicBoolean closed = new AtomicBoolean();

    private McpConnection(final @NotNull McpSyncClient client, final @NotNull McpSchema.InitializeResult initResult, final @NotNull McpOptions options, final @NotNull String endpoint) {
        super(DISPATCH);
        this.client = client;
        this.initResult = initResult;
        this.options = options;
        this.endpoint = endpoint;
    }

    /**
     * Connects to an MCP server and initializes the session.
     * @param url the server's endpoint, e.g. "https://example.com/mcp"
     * @param options connection options
     * @return the initialized connection
     * @throws McpException if the URL is invalid, the server cannot be reached or rejects the connection
     */
    public static @NotNull McpConnection connect(final @NotNull String url, final @NotNull McpOptions options) {
        final URI uri = parseUrl(url);
        final String base = uri.getScheme() + "://" + uri.getRawAuthority();
        final String path = (uri.getRawPath() == null || uri.getRawPath().isEmpty() ? "/" : uri.getRawPath())
                + (uri.getRawQuery() == null ? "" : "?" + uri.getRawQuery());
        // the endpoint without the query, which may carry a key, for log and error messages
        final String endpoint = base + (uri.getRawPath() == null ? "" : uri.getRawPath());
        final Duration timeout = Duration.ofSeconds(options.getTimeoutSeconds() > 0 ? options.getTimeoutSeconds() : 60);
        final Map<String, String> headers = Map.copyOf(options.getHeaders());
        final McpSyncHttpClientRequestCustomizer addHeaders = (request, method, requestUri, body, context) -> headers.forEach(request::setHeader);

        final McpClientTransport transport = switch (options.getTransport()) {
            case McpOptions.TRANSPORT_STREAMABLE -> HttpClientStreamableHttpTransport.builder(base)
                    .endpoint(path)
                    .jsonMapper(JSON_MAPPER)
                    .connectTimeout(timeout)
                    .httpRequestCustomizer(addHeaders)
                    .build();
            case McpOptions.TRANSPORT_SSE -> HttpClientSseClientTransport.builder(base)
                    .sseEndpoint(path)
                    .jsonMapper(JSON_MAPPER)
                    .connectTimeout(timeout)
                    .httpRequestCustomizer(addHeaders)
                    .build();
            default -> throw new McpException("unknown MCP transport '" + options.getTransport() + "', expected '"
                    + McpOptions.TRANSPORT_STREAMABLE + "' or '" + McpOptions.TRANSPORT_SSE + "'");
        };

        final McpSyncClient client = McpClient.sync(transport)
                .clientInfo(new McpSchema.Implementation(options.getClientName(), options.getClientVersion()))
                .requestTimeout(timeout)
                .initializationTimeout(timeout)
                .jsonSchemaValidator(new DefaultJsonSchemaValidator(OBJECT_MAPPER))
                .build();
        final McpSchema.InitializeResult initResult;
        try {
            initResult = client.initialize();
        } catch (RuntimeException e) {
            closeQuietly(client);
            throw new McpException("cannot connect to MCP server " + endpoint + ": " + describe(e), e);
        }
        final McpConnection connection = new McpConnection(client, initResult, options, endpoint);
        log.info("connected to MCP server {} {} at {}", connection.getServerName(), connection.getServerVersion(), endpoint);
        return connection;
    }

    /**
     * @return the server's name, as reported during initialization
     */
    public @Nullable String getServerName() {
        return initResult.serverInfo() == null ? null : initResult.serverInfo().name();
    }

    /**
     * @return the server's version, as reported during initialization
     */
    public @Nullable String getServerVersion() {
        return initResult.serverInfo() == null ? null : initResult.serverInfo().version();
    }

    /**
     * @return the server's instructions on how to use it, if any
     */
    public @Nullable String getInstructions() {
        return initResult.instructions();
    }

    /**
     * @return the protocol version both sides agreed on
     */
    public @Nullable String getProtocolVersion() {
        return initResult.protocolVersion();
    }

    /**
     * @return the result of the initialization, as received from the SDK
     */
    public @NotNull McpSchema.InitializeResult getInitializeResult() {
        return initResult;
    }

    /**
     * Lists all tools of the server, fetching all pages.
     * @return the tools
     * @throws McpException if the request fails
     */
    public @NotNull List<McpSchema.Tool> listTools() {
        ensureOpen();
        try {
            final List<McpSchema.Tool> tools = new ArrayList<>();
            final Set<String> seenCursors = new HashSet<>();
            String cursor = null;
            do {
                final McpSchema.ListToolsResult page = cursor == null ? client.listTools() : client.listTools(cursor);
                if (page.tools() != null) {
                    tools.addAll(page.tools());
                }
                cursor = page.nextCursor();
            } while (cursor != null && seenCursors.add(cursor)); // a server repeating a cursor must not keep us looping
            return tools;
        } catch (RuntimeException e) {
            throw new McpException("listing the tools of MCP server " + endpoint + " failed: " + describe(e), e);
        }
    }

    /**
     * Calls a tool.
     * @param name the tool's name
     * @param jsonArguments the arguments as a JSON object
     * @return the result; a failing tool usually returns a result with isError = true instead of failing the request
     * @throws McpException if the request fails, e.g. because the tool does not exist
     */
    public @NotNull McpSchema.CallToolResult callTool(final @NotNull String name, final @NotNull String jsonArguments) {
        ensureOpen();
        try {
            return client.callTool(new McpSchema.CallToolRequest(JSON_MAPPER, name, jsonArguments));
        } catch (RuntimeException e) {
            throw new McpException("calling tool '" + name + "' on MCP server " + endpoint + " failed: " + describe(e), e);
        }
    }

    /**
     * Closes the connection; further calls fail. Closing again does nothing.
     */
    public void close() {
        if (closed.compareAndSet(false, true)) {
            closeQuietly(client);
            log.info("closed connection to MCP server {}", endpoint);
        }
    }

    /**
     * @return true if the connection has been closed
     */
    public boolean isClosed() {
        return closed.get();
    }

    @Override
    public void janitorCleanup() {
        close();
    }

    private void ensureOpen() {
        if (closed.get()) {
            throw new McpException("the connection to MCP server " + endpoint + " has been closed");
        }
    }

    private static void closeQuietly(final McpSyncClient client) {
        try {
            // close() instead of closeGracefully(), which could keep the end of a script waiting for the server
            client.close();
        } catch (RuntimeException e) {
            log.warn("error closing MCP client", e);
        }
    }

    private static URI parseUrl(final String url) {
        final URI uri;
        try {
            uri = new URI(url.trim());
        } catch (URISyntaxException e) {
            throw new McpException("invalid MCP server URL: " + e.getMessage(), e);
        }
        if (!"http".equalsIgnoreCase(uri.getScheme()) && !"https".equalsIgnoreCase(uri.getScheme()) || uri.getRawAuthority() == null) {
            throw new McpException("invalid MCP server URL, expected http(s)://host/path: " + url);
        }
        return uri;
    }

    /**
     * Describes an SDK error by its root cause, which is usually the informative part (e.g. "Connection refused"),
     * while the outer exceptions mostly wrap it. A rejected authorization gets its HTTP status in front.
     */
    private static String describe(final Throwable e) {
        Throwable root = e;
        Integer authStatus = null;
        final Set<Throwable> seen = new HashSet<>();
        for (Throwable t = e; t != null && seen.add(t); t = t.getCause()) {
            root = t;
            if (authStatus == null && t instanceof McpHttpClientTransportAuthorizationException auth && auth.getResponseInfo() != null) {
                authStatus = auth.getResponseInfo().statusCode();
            }
        }
        final String message = root.getMessage();
        final String outer = e.getMessage();
        final String text;
        if (root == e) {
            text = message == null ? root.getClass().getSimpleName() : message;
        } else if (outer != null && message != null && !outer.contains(message)) {
            text = outer + " (" + root.getClass().getSimpleName() + ": " + message + ")";
        } else {
            text = outer != null ? outer : root.getClass().getSimpleName() + (message == null ? "" : ": " + message);
        }
        return authStatus == null ? text : "HTTP " + authStatus + ", authorization failed: " + text;
    }

    /**
     * Converts the parts of a tool result to Janitor maps. Each part is written as {@link McpSchema.Content}, so that
     * its "type" is included, which Jackson leaves out when it only sees the concrete record.
     */
    static @NotNull JList contentToJanitor(final @NotNull JanitorEnvironment environment, final @Nullable List<McpSchema.Content> content) {
        final JList list = environment.getBuiltinTypes().list();
        if (content != null) {
            for (final McpSchema.Content part : content) {
                try {
                    list.add(jsonToJanitor(environment, OBJECT_MAPPER.writerFor(McpSchema.Content.class).writeValueAsString(part)));
                } catch (IOException e) {
                    throw new McpException("cannot convert MCP content to Janitor: " + e.getMessage(), e);
                }
            }
        }
        return list;
    }

    /**
     * Converts an SDK value (record, map, list or scalar) to a Janitor value, by way of JSON.
     */
    static @NotNull JanitorObject toJanitor(final @NotNull JanitorEnvironment environment, final @Nullable Object value) {
        if (value == null) {
            return Janitor.NULL;
        }
        try {
            return jsonToJanitor(environment, JSON_MAPPER.writeValueAsString(value));
        } catch (IOException e) {
            throw new McpException("cannot convert MCP value to Janitor: " + e.getMessage(), e);
        }
    }

    private static @NotNull JanitorObject jsonToJanitor(final @NotNull JanitorEnvironment environment, final @NotNull String json) {
        try {
            final JanitorObject[] result = {Janitor.NULL};
            Janitor.readJson(json, stream -> result[0] = Janitor.readJsonValue(environment, stream));
            return result[0];
        } catch (JsonException e) {
            throw new McpException("cannot read MCP value as JSON: " + e.getMessage(), e);
        }
    }

    private static String writeJson(final JanitorScriptProcess process, final JMap arguments) throws JanitorRuntimeException {
        try {
            return process.writeJson(arguments::writeJson);
        } catch (JsonException e) {
            throw new JanitorNativeException(process, "cannot convert MCP tool arguments to JSON: " + e.getMessage(), e);
        }
    }

    /**
     * Runs an MCP operation from a script method, turning MCP errors into script errors.
     */
    private static <R> R call(final JanitorScriptProcess process, final Supplier<R> action) throws JanitorRuntimeException {
        try {
            return action.get();
        } catch (McpException e) {
            throw new JanitorNativeException(process, e.getMessage(), e);
        }
    }

    @Override
    public String toString() {
        return "McpConnection[" + endpoint + "]";
    }
}
