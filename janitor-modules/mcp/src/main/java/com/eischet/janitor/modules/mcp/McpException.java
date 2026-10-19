// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.mcp;

/** A failed MCP operation: the server is unreachable, rejected the request or answered with a protocol error. */
public class McpException extends RuntimeException {

    public McpException(final String message) {
        super(message);
    }

    public McpException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
