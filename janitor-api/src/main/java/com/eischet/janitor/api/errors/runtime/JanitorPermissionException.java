// SPDX-FileCopyrightText: 2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.errors.runtime;

import com.eischet.janitor.api.JanitorScriptProcess;
import org.jetbrains.annotations.NotNull;

/**
 * An exception thrown when a script calls something that its process has no right to,
 * see {@link JanitorScriptProcess#require(com.eischet.janitor.api.metadata.MetaDataKey)}.
 */
public class JanitorPermissionException extends JanitorRuntimeException {

    /**
     * Constructs a new JanitorPermissionException.
     * @param process the running script
     * @param message the detail message
     */
    public JanitorPermissionException(final @NotNull JanitorScriptProcess process, final String message) {
        super(process, message, JanitorPermissionException.class);
    }

}
