// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.mcp;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.errors.runtime.JanitorNativeException;
import com.eischet.janitor.api.modules.JanitorModule;
import com.eischet.janitor.api.modules.JanitorModuleRegistration;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JMap;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * The script module "mcp", a client for MCP (Model Context Protocol) servers:
 * <pre>
 * import mcp;
 * srv = mcp.connect("https://example.com/mcp", {"headers": {"Authorization": "Bearer ..."}, "timeoutSeconds": 30});
 * res = srv.call("search", {"text": "printer"});
 * </pre>
 * The options map may contain "headers" (a map of strings) and the properties of {@link McpOptions}.
 * See {@link McpConnection} for what a connection offers.
 */
public class McpModule extends JanitorComposed<McpModule> implements JanitorModule {

    public static final JanitorModuleRegistration REGISTRATION = new JanitorModuleRegistration("mcp", () -> new McpModule());

    private static final DispatchTable<McpModule> DISPATCH = new DispatchTable<>(() -> new McpModule(), false);

    static {
        DISPATCH.addMethod("connect", (self, process, args) -> {
            args.require(1, 2);
            final McpOptions options = self.defaults.get();
            final JMap map = args.size() > 1 ? args.getNullable(1, JMap.class) : null;
            if (map != null) {
                final JMap properties = process.getBuiltins().map();
                properties.putAll(map);
                final JanitorObject headers = properties.remove(Janitor.getBuiltins().string("headers"));
                if (headers instanceof JMap headerMap) {
                    for (final JanitorObject name : headerMap.keySet()) {
                        final JanitorObject value = headerMap.get(name);
                        options.setHeader(name.janitorToString(), value == Janitor.NULL ? null : value.janitorToString());
                    }
                } else if (headers != Janitor.NULL) {
                    throw new JanitorArgumentException(process, "mcp.connect: \"headers\" must be a map of strings");
                }
                properties.applyTo(process, options);
            }
            final String url = args.getRequiredStringValue(0);
            final McpConnection connection;
            try {
                connection = McpConnection.connect(url, options);
            } catch (McpException e) {
                throw new JanitorNativeException(process, e.getMessage(), e);
            }
            // close the connection when the script ends, even if it never lands in a variable
            process.registerCleanable(connection);
            return connection;
        });
    }

    private final @NotNull Supplier<McpOptions> defaults;

    /**
     * Creates the module with the built-in default options.
     */
    public McpModule() {
        this(McpOptions::new);
    }

    /**
     * Creates the module with host-specific default options, which scripts can still override per connect.
     * @param defaults creates the default options for each connect, e.g. with the host's client name and headers
     */
    public McpModule(final @NotNull Supplier<McpOptions> defaults) {
        super(DISPATCH);
        this.defaults = defaults;
    }

    /**
     * Creates a registration with host-specific default options.
     * @param defaults creates the default options for each connect
     * @return a registration for the module "mcp"
     */
    public static JanitorModuleRegistration registration(final @NotNull Supplier<McpOptions> defaults) {
        return new JanitorModuleRegistration("mcp", () -> new McpModule(defaults));
    }
}
