package com.eischet.janitor.api.errors.runtime;

import com.eischet.janitor.api.JanitorScriptProcess;
import org.jetbrains.annotations.NotNull;

/**
 * An exception thrown when a method is not implemented.
 */
public class JanitorNotImplementedException extends JanitorRuntimeException {
    /**
     * Constructs a new JanitorNotImplementedException.
     * @param process the running script
     * @param message the detail message
     */
    public JanitorNotImplementedException(final @NotNull JanitorScriptProcess process, final String message) {
        super(process, message, JanitorNotImplementedException.class);
    }
}
