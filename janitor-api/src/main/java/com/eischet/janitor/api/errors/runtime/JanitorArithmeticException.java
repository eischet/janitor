// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.errors.runtime;

import com.eischet.janitor.api.JanitorScriptProcess;
import org.jetbrains.annotations.NotNull;

/**
 * An exception thrown when an arithmetic operation fails.
 * For example, division by zero.
 * <p>
 * It carries the message of the Java {@link ArithmeticException}, but not the exception itself: the error lies in the script, and
 * a Java cause would only repeat the message and make loggers print the stack trace of the interpreter.
 * </p>
 */
public class JanitorArithmeticException extends JanitorRuntimeException {

    /**
     * Constructs a new JanitorArithmeticException.
     * @param process the running script
     * @param message the detail message
     */
    public JanitorArithmeticException(final @NotNull JanitorScriptProcess process, final String message) {
        super(process, message, JanitorArithmeticException.class);
    }
}
