// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.rest;

import org.jetbrains.annotations.NotNull;

/**
 * Thrown by the hooks of a {@link RestPolicy} that run while something is written, to refuse the request: the transaction is rolled back, and
 * the caller gets a problem response with the given status and the message as its detail.
 */
public class RestRejection extends RuntimeException {

    private final int status;

    /**
     * @param status the HTTP status, a client error between 400 and 499, e.g. 403 or 422
     * @param detail what the caller is told
     * @throws IllegalArgumentException if the status is not a client error
     */
    public RestRejection(final int status, final @NotNull String detail) {
        super(detail, null, false, false);
        if (status < 400 || status > 499) {
            throw new IllegalArgumentException("a rejection needs a client error status (4xx), not " + status);
        }
        this.status = status;
    }

    /** @return the HTTP status */
    public int status() {
        return status;
    }
}
