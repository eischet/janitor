// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.msgraph;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.modules.JanitorModule;
import com.eischet.janitor.api.modules.JanitorModuleRegistration;
import com.eischet.janitor.api.types.builtin.JMap;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * The script module "msgraph", which connects to Microsoft Graph for mail folders and messages:
 * <pre>
 * import msgraph;
 * graph = msgraph.connect(clientId, clientSecret, tenantId);          // optional 4th argument: options map
 * for (folder in graph.allFolders("someone@example.com")) { print(folder.displayName); }
 * </pre>
 */
public class MsGraphModule extends JanitorComposed<MsGraphModule> implements JanitorModule {

    public static final JanitorModuleRegistration REGISTRATION = new JanitorModuleRegistration("msgraph", () -> new MsGraphModule());

    private static final DispatchTable<MsGraphModule> DISPATCH = new DispatchTable<>(() -> new MsGraphModule(), false);

    static {
        DISPATCH.addMethod("connect", (self, process, args) -> {
            args.require(3, 4);
            final MsGraphOptions options = self.defaults.get();
            if (args.size() > 3) {
                args.getRequired(3, JMap.class).applyTo(process, options);
            }
            return MsGraphClient.forClientSecret(args.getRequiredStringValue(0), args.getRequiredStringValue(1), args.getRequiredStringValue(2), options);
        }).setMetaData(Janitor.MetaData.HELP, "connect(clientId, clientSecret, tenantId, options): Returns a Microsoft Graph client that authenticates with the client secret; options is an optional map with baseUrl, proxyHost, proxyPort, tokenCache, timeoutSeconds and maxRetries.");
    }

    private final @NotNull Supplier<MsGraphOptions> defaults;

    /**
     * Creates the module with the built-in default options.
     */
    public MsGraphModule() {
        this(MsGraphOptions::new);
    }

    /**
     * Creates the module with host-specific default options, which scripts can still override per connect.
     * @param defaults creates the default options for each connect, e.g. with the host's token cache setting applied
     */
    public MsGraphModule(final @NotNull Supplier<MsGraphOptions> defaults) {
        super(DISPATCH);
        this.defaults = defaults;
    }

    /**
     * Creates a registration with host-specific default options.
     * @param defaults creates the default options for each connect
     * @return a registration for the module "msgraph"
     */
    public static JanitorModuleRegistration registration(final @NotNull Supplier<MsGraphOptions> defaults) {
        return new JanitorModuleRegistration("msgraph", () -> new MsGraphModule(defaults));
    }
}
