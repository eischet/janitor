// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.errors.glue;

/**
 * Control Flow Exceptions are used internally by the Janitor interpreter.
 * These exceptions should never be thrown to outside users, unless there is a bug in the interpreter.
 * <p>
 * Every {@code return}, {@code break} and {@code continue} throws one, so they carry no stack trace: filling it in walks
 * the deep stack of the interpreter and used to cost a large share of the run time of scripts that call functions.
 * </p>
 */
public abstract class JanitorControlFlowException extends Exception {

    /**
     * Creates the exception without a message, cause, suppressed exceptions or stack trace.
     */
    protected JanitorControlFlowException() {
        super(null, null, false, false);
    }
}
