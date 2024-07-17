package com.eischet.janitor.api.errors.runtime;

import com.eischet.janitor.api.JanitorScriptProcess;

/**
 * An exception thrown when a type error occurs in the Janitor interpreter.
 */
public class JanitorTypeException extends JanitorRuntimeException {
    /**
     * Constructs a new JanitorTypeException.
     * @param process the running script process
     * @param message the detail message
     */
    public JanitorTypeException(final JanitorScriptProcess process, final String message) {
        super(process, message, JanitorTypeException.class);
    }
}
