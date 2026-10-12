// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.rest;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * What the {@link OrmRestHandler} answers, independent of any HTTP framework.
 *
 * @param status      the HTTP status code
 * @param contentType the media type of the body, or null if there is no body
 * @param headers     further response headers
 * @param body        the body as text (JSON), or null if there is none
 */
public record RestResponse(int status, @Nullable String contentType, @NotNull Map<String, String> headers, @Nullable String body) {

    /** Makes the headers immutable. */
    public RestResponse {
        headers = Map.copyOf(headers);
    }

    /** @return a response without body */
    public static @NotNull RestResponse empty(final int status, final @NotNull Map<String, String> headers) {
        return new RestResponse(status, null, headers, null);
    }
}
