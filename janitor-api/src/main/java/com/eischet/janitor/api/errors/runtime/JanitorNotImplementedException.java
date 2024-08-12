package com.eischet.janitor.api.errors.runtime;

import com.eischet.janitor.api.JanitorScriptProcess;

/**
 * An exception thrown when a method is not implemented.
 */
public class JanitorNotImplementedException extends JanitorRuntimeException {
    /**
     * Constructs a new JanitorNotImplementedException.
     * @param process the running script
     * @param message the detail message
     */
    public JanitorNotImplementedException(final JanitorScriptProcess process, final String message) {
        super(process, message, JanitorNotImplementedException.class);
    }
}
