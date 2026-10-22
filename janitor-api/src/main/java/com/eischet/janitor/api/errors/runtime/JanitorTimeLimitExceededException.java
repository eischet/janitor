// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.errors.runtime;

import com.eischet.janitor.api.JanitorScriptProcess;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

/** Thrown when a script runs longer than it is allowed to, which protects hosts from scripts that take too long. */
public class JanitorTimeLimitExceededException extends JanitorAbortException {
    public JanitorTimeLimitExceededException(final @NotNull JanitorScriptProcess process, final @NotNull Duration limit) {
        super(process, "Script execution exceeded maximum run time: " + limit.toMillis() + " ms", JanitorTimeLimitExceededException.class);
    }
}
