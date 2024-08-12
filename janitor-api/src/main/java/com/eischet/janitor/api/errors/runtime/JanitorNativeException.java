package com.eischet.janitor.api.errors.runtime;

import com.eischet.janitor.api.JanitorScriptProcess;

/**
 * An exception thrown when a native method throws an exception.
 */
public class JanitorNativeException extends JanitorRuntimeException {
    /**
     * Constructs a new JanitorNativeException.
     * @param process the running script
     * @param message the detail message
     * @param cause the cause
     */
    public JanitorNativeException(final JanitorScriptProcess process, final String message, final Throwable cause) {
        super(process, message, cause, JanitorNativeException.class);
    }
}
