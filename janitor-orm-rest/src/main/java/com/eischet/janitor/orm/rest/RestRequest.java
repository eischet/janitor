// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.rest;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * A request as the {@link OrmRestHandler} sees it, independent of any HTTP framework.
 *
 * @param method  the HTTP method, e.g. "GET"
 * @param path    the path below the place where the API is mounted, already decoded, e.g. "/items/5"
 * @param query   the decoded query parameters; a parameter can occur several times
 * @param headers the request headers; the names are compared case-insensitively
 * @param body    the body as text, or null if there is none
 * @param user    who is calling, as the HTTP binding found out; handed to the {@link RestPolicy}; null for an anonymous caller
 * @param <U>     the type of the user
 */
public record RestRequest<U>(@NotNull String method,
                             @NotNull String path,
                             @NotNull Map<String, List<String>> query,
                             @NotNull Map<String, String> headers,
                             @Nullable String body,
                             @Nullable U user) {

    /** Normalizes the method to upper case and the header names to lower case. */
    public RestRequest {
        method = method.toUpperCase(Locale.ROOT);
        final Map<String, String> lowered = new HashMap<>();
        headers.forEach((name, value) -> lowered.put(name.toLowerCase(Locale.ROOT), value));
        headers = Map.copyOf(lowered);
        query = Map.copyOf(query);
    }

    /**
     * @param method the HTTP method
     * @param path   the path
     * @param user   the user, or null
     * @param <U>    the type of the user
     * @return a request without parameters, headers and body
     */
    public static <U> @NotNull RestRequest<U> of(final @NotNull String method, final @NotNull String path, final @Nullable U user) {
        return new RestRequest<>(method, path, Map.of(), Map.of(), null, user);
    }

    /**
     * @param name the name of a query parameter
     * @return its first value, or null if it is absent
     */
    public @Nullable String param(final @NotNull String name) {
        final List<String> values = query.get(name);
        return values == null || values.isEmpty() ? null : values.get(0);
    }

    /**
     * @param name the name of a header, in any case
     * @return its value, or null if it is absent
     */
    public @Nullable String header(final @NotNull String name) {
        return headers.get(name.toLowerCase(Locale.ROOT));
    }
}
