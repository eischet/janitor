// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.errors.runtime;

import com.eischet.janitor.api.JanitorScriptProcess;
import org.jetbrains.annotations.NotNull;

/** Thrown when the host cancels a running script, see {@link JanitorScriptProcess#cancel()}. */
public class JanitorCancelledException extends JanitorAbortException {
    public JanitorCancelledException(final @NotNull JanitorScriptProcess process) {
        super(process, "Script execution was cancelled", JanitorCancelledException.class);
    }
}
