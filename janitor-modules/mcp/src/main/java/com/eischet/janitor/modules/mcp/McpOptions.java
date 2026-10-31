// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.mcp;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Connection options for {@link McpConnection}. Scripts pass them as a map, e.g.
 * {@code mcp.connect(url, {headers: {"Authorization": "Bearer ..."}, timeoutSeconds: 30})}.
 * <p>
 * The headers are not a dispatch property, because they are a map of strings; {@link McpModule} copies them from the
 * script's options map with {@link #setHeader(String, String)}.
 * </p>
 */
public class McpOptions extends JanitorComposed<McpOptions> {

    /** The Streamable HTTP transport, which current MCP servers speak. */
    public static final String TRANSPORT_STREAMABLE = "streamable";
    /** The older HTTP+SSE transport, for servers that still require it. */
    public static final String TRANSPORT_SSE = "sse";

    public static final DispatchTable<McpOptions> DISPATCH = new DispatchTable<>(McpOptions::new);

    static {
        DISPATCH.addIntegerProperty("timeoutSeconds", McpOptions::getTimeoutSeconds, McpOptions::setTimeoutSeconds)
                .setMetaData(Janitor.MetaData.HELP, "timeoutSeconds: Time limit in seconds for connecting and for each request (default 60; 0 or less means 60); writable.");
        DISPATCH.addStringProperty("clientName", McpOptions::getClientName, McpOptions::setClientName)
                .setMetaData(Janitor.MetaData.HELP, "clientName: The name this client reports to the server (default \"Janitor\"; blank restores it); writable.");
        DISPATCH.addStringProperty("clientVersion", McpOptions::getClientVersion, McpOptions::setClientVersion)
                .setMetaData(Janitor.MetaData.HELP, "clientVersion: The version this client reports to the server (blank restores the default); writable.");
        DISPATCH.addStringProperty("transport", McpOptions::getTransport, McpOptions::setTransport)
                .setMetaData(Janitor.MetaData.HELP, "transport: \"streamable\" (the default) or \"sse\" for older servers; writable.");
        DISPATCH.addBooleanProperty("throwOnError", McpOptions::isThrowOnError, McpOptions::setThrowOnError)
                .setMetaData(Janitor.MetaData.HELP, "throwOnError: If true, a tool result flagged as an error becomes a script error instead of a result with isError = true; writable.");
    }

    private final Map<String, String> headers = new LinkedHashMap<>();
    private int timeoutSeconds = 60;
    private @NotNull String clientName = "Janitor";
    private @NotNull String clientVersion = defaultClientVersion();
    private @NotNull String transport = TRANSPORT_STREAMABLE;
    private boolean throwOnError;

    public McpOptions() {
        super(DISPATCH);
    }

    /**
     * @return the headers sent with every request, e.g. an API key or a bearer token; never logged
     */
    public @NotNull Map<String, String> getHeaders() {
        return Collections.unmodifiableMap(headers);
    }

    /**
     * Sets a header sent with every request, replacing an earlier value of the same name.
     * @param name the header name
     * @param value the value, or null to remove the header
     */
    public void setHeader(final @NotNull String name, final @Nullable String value) {
        if (value == null) {
            headers.remove(name);
        } else {
            headers.put(name, value);
        }
    }

    /**
     * @return the time limit for initializing the connection and for each request
     */
    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(final int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    /**
     * @return the name this client reports to the server
     */
    public @NotNull String getClientName() {
        return clientName;
    }

    public void setClientName(final @Nullable String clientName) {
        this.clientName = clientName == null || clientName.isBlank() ? "Janitor" : clientName;
    }

    /**
     * @return the version this client reports to the server
     */
    public @NotNull String getClientVersion() {
        return clientVersion;
    }

    public void setClientVersion(final @Nullable String clientVersion) {
        this.clientVersion = clientVersion == null || clientVersion.isBlank() ? defaultClientVersion() : clientVersion;
    }

    /**
     * @return the transport, {@link #TRANSPORT_STREAMABLE} (the default) or {@link #TRANSPORT_SSE}
     */
    public @NotNull String getTransport() {
        return transport;
    }

    public void setTransport(final @Nullable String transport) {
        this.transport = transport == null || transport.isBlank() ? TRANSPORT_STREAMABLE : transport.trim().toLowerCase();
    }

    /**
     * @return true if a tool result flagged as an error becomes a script error, instead of a result with isError = true
     */
    public boolean isThrowOnError() {
        return throwOnError;
    }

    public void setThrowOnError(final boolean throwOnError) {
        this.throwOnError = throwOnError;
    }

    private static String defaultClientVersion() {
        final String version = McpOptions.class.getPackage().getImplementationVersion();
        return version == null ? "dev" : version;
    }
}
