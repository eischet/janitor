package com.eischet.janitor.api.errors.runtime;

import com.eischet.janitor.api.JanitorScriptProcess;

/**
 * An exception thrown when an argument is invalid or missing.
 */
public class JanitorArgumentException extends JanitorRuntimeException {
    /**
     * Constructs a new JanitorArgumentException.
     * @param process the running script
     * @param message the detail message
     */
    public JanitorArgumentException(final JanitorScriptProcess process, final String message) {
        super(process, message, JanitorArgumentException.class);
    }

    /**
     * Constructs a new JanitorArgumentException with the specified detail message and cause.
     * @param process the running script
     * @param message the detail message
     * @param cause the cause
     */
    public JanitorArgumentException(final JanitorScriptProcess process, final String message, final Throwable cause) {
        super(process, message, cause, JanitorArgumentException.class);
    }
}
