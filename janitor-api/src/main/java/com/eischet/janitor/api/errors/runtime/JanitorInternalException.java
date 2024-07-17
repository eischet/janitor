package com.eischet.janitor.api.errors.runtime;

import com.eischet.janitor.api.JanitorScriptProcess;

/**
 * An exception thrown when an internal error occurs in the Janitor interpreter.
 */
public class JanitorInternalException extends JanitorRuntimeException {
    /**
     * Constructs a new JanitorInternalException.
     * @param runningScript the running script
     * @param s the detail message
     * @param e the cause
     */
    public JanitorInternalException(final JanitorScriptProcess runningScript, final String s, final JanitorControlFlowException e) {
        super(runningScript, s, e, JanitorInternalException.class);
    }
}
