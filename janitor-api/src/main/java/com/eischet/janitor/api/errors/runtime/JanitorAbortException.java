// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.errors.runtime;

import com.eischet.janitor.api.JanitorScriptProcess;
import org.jetbrains.annotations.NotNull;

/**
 * Base class for errors with which the host aborts a script, e.g. because it exceeded a limit.
 * A script cannot catch these: {@code catch} lets them pass, only {@code finally} blocks still run.
 * Hosts can catch this class to handle all kinds of aborts at once.
 */
public abstract class JanitorAbortException extends JanitorRuntimeException {

    /**
     * Constructs a new JanitorAbortException.
     * @param process the running script
     * @param message the detail message
     * @param cls the class of the exception
     */
    protected JanitorAbortException(final @NotNull JanitorScriptProcess process, final String message, final Class<? extends JanitorRuntimeException> cls) {
        super(process, message, cls);
    }
}
