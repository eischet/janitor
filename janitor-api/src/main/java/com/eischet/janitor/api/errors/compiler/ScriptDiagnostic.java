// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.errors.compiler;

import org.jetbrains.annotations.NotNull;

/**
 * A problem found while compiling a script, with its position, e.g. for marking it in an editor.
 * <p>
 * Lines start at 1, columns at 0, like ANTLR counts them; the end is exclusive. A line of 0 means that the position is
 * unknown, which happens for errors that the compiler cannot attribute to a part of the script.
 * </p>
 *
 * @param severity  how bad it is
 * @param line      the first line, starting at 1, or 0 if unknown
 * @param column    the first column, starting at 0
 * @param endLine   the last line
 * @param endColumn the column after the last character
 * @param message   what is wrong, without the position
 */
public record ScriptDiagnostic(@NotNull Severity severity, int line, int column, int endLine, int endColumn, @NotNull String message) {

    /** How bad a diagnostic is. */
    public enum Severity {
        /** The script cannot run. */
        ERROR,
        /** The script runs, but probably not as intended. */
        WARNING
    }

    /**
     * Create an error.
     * @param line      the first line, starting at 1, or 0 if unknown
     * @param column    the first column, starting at 0
     * @param endLine   the last line
     * @param endColumn the column after the last character
     * @param message   what is wrong
     * @return the diagnostic
     */
    public static ScriptDiagnostic error(final int line, final int column, final int endLine, final int endColumn, final @NotNull String message) {
        return new ScriptDiagnostic(Severity.ERROR, line, column, endLine, endColumn, message);
    }

    /**
     * Create an error without a known position.
     * @param message what is wrong
     * @return the diagnostic
     */
    public static ScriptDiagnostic error(final @NotNull String message) {
        return new ScriptDiagnostic(Severity.ERROR, 0, 0, 0, 0, message);
    }
}
