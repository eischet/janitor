// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.compiler;

import com.eischet.janitor.api.scopes.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serial;

/** Unchecked exception for errors that are detected while the compiler builds the syntax tree, e.g. duplicate parameter names. */
public class CompilerError extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private transient @Nullable Location location;
    private @Nullable String detail;

    public CompilerError() {
    }

    public CompilerError(final String message) {
        super(message);
    }

    public CompilerError(final String message, final Throwable cause) {
        super(message, cause);
    }

    public CompilerError(final Throwable cause) {
        super(cause);
    }

    public CompilerError(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    /**
     * Create an error at a known place in the script. The message ends with the location; {@link #getDetail()} has it without.
     * @param detail   what is wrong
     * @param location where it is
     * @param cause    the cause, may be null
     */
    public CompilerError(final @NotNull String detail, final @NotNull Location location, final @Nullable Throwable cause) {
        super(detail + " at " + location, cause);
        this.detail = detail;
        this.location = location;
    }

    /**
     * @return where the error is, or null if unknown
     */
    public @Nullable Location getLocation() {
        return location;
    }

    /**
     * @return the message without the location
     */
    public @NotNull String getDetail() {
        return detail != null ? detail : String.valueOf(getMessage());
    }

    /**
     * Remember where the error is, unless that is already known; the message stays as it is.
     * @param where the location of the construct that was compiled when the error occurred
     * @return this
     */
    public CompilerError locatedAt(final @NotNull Location where) {
        if (location == null) {
            location = where;
        }
        return this;
    }
}
