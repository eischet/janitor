// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.msgraph;

import org.jetbrains.annotations.Nullable;

/** A failed Graph request: HTTP status, the Graph error code and message, and the request id for Microsoft support. */
public class MsGraphException extends RuntimeException {

    private final int status;
    private final @Nullable String code;
    private final @Nullable String requestId;

    public MsGraphException(final String message, final int status, final @Nullable String code, final @Nullable String requestId) {
        super(message);
        this.status = status;
        this.code = code;
        this.requestId = requestId;
    }

    public MsGraphException(final String message, final Throwable cause) {
        super(message, cause);
        this.status = 0;
        this.code = null;
        this.requestId = null;
    }

    /**
     * @return the HTTP status, or 0 if the request did not get a response
     */
    public int getStatus() {
        return status;
    }

    /**
     * @return the Graph error code, e.g. "ErrorItemNotFound", if the response contained one
     */
    public @Nullable String getCode() {
        return code;
    }

    /**
     * @return the value of the "request-id" response header, if any
     */
    public @Nullable String getRequestId() {
        return requestId;
    }
}
