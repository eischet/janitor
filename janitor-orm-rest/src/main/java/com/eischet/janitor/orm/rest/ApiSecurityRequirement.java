// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.rest;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * What it takes to call an operation, in terms of the security schemes of the API description: all of the named schemes (with these scopes)
 * together. Several requirements in a list are alternatives, of which one is enough; a list that is empty means that the operation is public.
 * The {@linkplain #anonymous() empty requirement} among alternatives makes authentication optional.
 *
 * @param schemes the names of the schemes (see {@link OpenApiOptions#withSecurityScheme}), and the scopes that are needed of each; scopes only
 *                matter for OAuth 2 and OpenID Connect
 */
public record ApiSecurityRequirement(@NotNull Map<String, List<String>> schemes) {

    /** Makes a defensive copy. */
    public ApiSecurityRequirement {
        schemes = Map.copyOf(schemes);
    }

    /**
     * @param scheme the name of a scheme
     * @param scopes the scopes that it needs, if it has scopes
     * @return a requirement for that scheme
     */
    public static @NotNull ApiSecurityRequirement of(final @NotNull String scheme, final @NotNull String... scopes) {
        return new ApiSecurityRequirement(Map.of(scheme, List.of(scopes)));
    }

    /** @return the requirement that nothing is needed, which makes authentication optional when it is one of several alternatives */
    public static @NotNull ApiSecurityRequirement anonymous() {
        return new ApiSecurityRequirement(Map.of());
    }

    /** @return true if this requirement needs nothing */
    public boolean isAnonymous() {
        return schemes.isEmpty();
    }

    /**
     * @param alternatives requirements of which one is enough
     * @return true if a caller has to authenticate: there are requirements, and none of them is the anonymous one
     */
    public static boolean requiresAuthentication(final @NotNull List<ApiSecurityRequirement> alternatives) {
        return !alternatives.isEmpty() && alternatives.stream().noneMatch(ApiSecurityRequirement::isAnonymous);
    }

    /** @return the entry for a {@code security} list, as a tree of maps */
    public @NotNull Map<String, Object> toTree() {
        final Map<String, Object> tree = new LinkedHashMap<>();
        schemes.forEach(tree::put);
        return tree;
    }
}
