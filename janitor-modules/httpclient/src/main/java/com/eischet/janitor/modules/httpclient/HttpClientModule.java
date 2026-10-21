// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.httpclient;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.errors.runtime.JanitorNativeException;
import com.eischet.janitor.api.modules.JanitorModule;
import com.eischet.janitor.api.modules.JanitorModuleRegistration;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;

/** The script module "httpclient", with a simple {@code fetch(url)} function and a {@code build()} function that creates HTTP clients. */
public class HttpClientModule extends JanitorComposed<HttpClientModule> implements JanitorModule {

    public static final JanitorModuleRegistration REGISTRATION = new JanitorModuleRegistration("httpclient", HttpClientModule::new);

    private static final DispatchTable<HttpClientModule> DISPATCH = new DispatchTable<>(HttpClientModule::new, false);

    static {
        // Every HELP text starts with the call form, "name(arguments): ", which the language reference relies on.
        DISPATCH.addMethod("fetch", (self, process, arguments) -> {
            final String url = arguments.require(1).getRequiredStringValue(0);
            try {
                return Janitor.nullableString(self.client.getString(url));
            } catch (HttpException e) {
                // the same way as the client's own methods, so that e.code, e.body etc. can be read in a catch block
                throw new JanitorNativeException(process, e.getMessage(), e);
            }
        }).setMetaData(Janitor.MetaData.HELP, "fetch(url): Sends a GET request and returns the body of the response as a string; a failed request is an error with code, body, headers, method and url.");
        DISPATCH.addMethod("build", (self, process, arguments) -> {
            arguments.require(0);
            return new JanitorHttpClient();
        }).setMetaData(Janitor.MetaData.HELP, "build(): Returns a new HTTP client, for requests with headers, authentication, JSON and so on.");
    }

    private final JanitorHttpClient client = new JanitorHttpClient();

    public HttpClientModule() {
        super(DISPATCH);
    }
}
