package com.eischet.janitor.api.errors.runtime;

import com.eischet.janitor.api.JanitorScriptProcess;

/**
 * An exception thrown when a method is not implemented.
 */
public class JanitorNotImplementedException extends JanitorRuntimeException {
    /**
     * Constructs a new JanitorNotImplementedException.
     * @param runningScript the running script
     * @param s the detail message
     */
    public JanitorNotImplementedException(final JanitorScriptProcess runningScript, final String s) {
        super(runningScript, s, JanitorNotImplementedException.class);
    }
}
