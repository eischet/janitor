// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.errors.runtime;

import com.eischet.janitor.api.JanitorScriptProcess;
import org.jetbrains.annotations.NotNull;

/** Thrown when a script exceeds the maximum number of instructions it is allowed to execute, which protects hosts from endless loops. */
public class JanitorInstructionLimitExceededException extends JanitorRuntimeException {
    public JanitorInstructionLimitExceededException(final @NotNull JanitorScriptProcess process, long limit) {
        super(process, "Script execution exceeded maximum instruction count: " + limit, JanitorInstructionLimitExceededException.class);
    }
}
