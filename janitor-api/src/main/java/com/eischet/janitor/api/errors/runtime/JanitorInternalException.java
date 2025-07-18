package com.eischet.janitor.api.errors.runtime;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.glue.JanitorControlFlowException;

/**
 * An exception thrown when an internal error occurs in the Janitor interpreter.
 */
public class JanitorInternalException extends JanitorRuntimeException {
    /**
     * Constructs a new JanitorInternalException.
     * @param process the running script
     * @param message the detail message
     * @param cause the cause
     */
    public JanitorInternalException(final JanitorScriptProcess process, final String message, final JanitorControlFlowException cause) {
        super(process, message, cause, JanitorInternalException.class);
    }
}
