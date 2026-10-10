// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.filter;

/** Thrown when a filter expression is invalid, e.g. because of an unknown operator. */
public class MalformedExpression extends RuntimeException {
    public MalformedExpression(final String message) {
        super(message);
    }
    public MalformedExpression(final String message, final Throwable cause) {
        super(message, cause);
    }
}
