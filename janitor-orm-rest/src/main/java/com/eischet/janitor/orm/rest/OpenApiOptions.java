// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.rest;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The parts of an API description that the entities cannot tell.
 *
 * @param title           the title of the API
 * @param version         the version of the API (not of OpenAPI)
 * @param description     a description of the API, or null for none
 * @param serverUrl       the URL under which the API is mounted, e.g. {@code "/api"}, or null to leave it out
 * @param securitySchemes how callers authenticate, by name; empty if the description should not say
 * @param security        what it takes to call an operation unless {@link RestSecurityDescription} says otherwise: alternatives, of which one is
 *                        enough; empty if nothing is said
 */
public record OpenApiOptions(@NotNull String title,
                             @NotNull String version,
                             @Nullable String description,
                             @Nullable String serverUrl,
                             @NotNull Map<String, ApiSecurityScheme> securitySchemes,
                             @NotNull List<ApiSecurityRequirement> security) {

    /** Makes defensive copies. */
    public OpenApiOptions {
        securitySchemes = java.util.Collections.unmodifiableMap(new LinkedHashMap<>(securitySchemes)); // keeps the order of definition
        security = List.copyOf(security);
    }

    /** @return options with a generic title and version 1.0, and nothing about authentication */
    public static @NotNull OpenApiOptions defaults() {
        return new OpenApiOptions("Janitor ORM API", "1.0", null, null, Map.of(), List.of());
    }

    /**
     * @param serverUrl the URL under which the API is mounted
     * @return a copy of these options with that URL
     */
    public @NotNull OpenApiOptions withServerUrl(final @Nullable String serverUrl) {
        return new OpenApiOptions(title, version, description, serverUrl, securitySchemes, security);
    }

    /**
     * @param description a description of the API
     * @return a copy of these options with that description
     */
    public @NotNull OpenApiOptions withDescription(final @Nullable String description) {
        return new OpenApiOptions(title, version, description, serverUrl, securitySchemes, security);
    }

    /**
     * Declares a way to authenticate. Declaring it does not make the API require it: say that with {@link #withSecurity}.
     * @param name   the name of the scheme, which requirements refer to
     * @param scheme the scheme
     * @return a copy of these options with that scheme
     */
    public @NotNull OpenApiOptions withSecurityScheme(final @NotNull String name, final @NotNull ApiSecurityScheme scheme) {
        final Map<String, ApiSecurityScheme> schemes = new LinkedHashMap<>(securitySchemes);
        schemes.put(name, scheme);
        return new OpenApiOptions(title, version, description, serverUrl, schemes, security);
    }

    /**
     * Says what it takes to call the operations of the API, unless a {@link RestSecurityDescription} says otherwise for one of them. The
     * requirements are alternatives, of which one is enough, e.g. {@code withSecurity(ApiSecurityRequirement.of("bearer"))}.
     * @param requirements the alternatives
     * @return a copy of these options with them, replacing earlier ones
     */
    public @NotNull OpenApiOptions withSecurity(final @NotNull ApiSecurityRequirement... requirements) {
        return new OpenApiOptions(title, version, description, serverUrl, securitySchemes, List.of(requirements));
    }

    /**
     * Makes authentication optional: callers may authenticate, but do not have to; whether anonymous callers get anywhere is up to the policy.
     * Adds to the requirements that were given before.
     * @return a copy of these options
     */
    public @NotNull OpenApiOptions withAnonymousAllowed() {
        final List<ApiSecurityRequirement> requirements = new ArrayList<>(security);
        requirements.add(ApiSecurityRequirement.anonymous());
        return new OpenApiOptions(title, version, description, serverUrl, securitySchemes, requirements);
    }
}
