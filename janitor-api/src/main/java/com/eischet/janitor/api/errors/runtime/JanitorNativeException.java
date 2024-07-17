package com.eischet.janitor.api.errors.runtime;

import com.eischet.janitor.api.JanitorScriptProcess;

/**
 * An exception thrown when a native method throws an exception.
 */
public class JanitorNativeException extends JanitorRuntimeException {
    /**
     * Constructs a new JanitorNativeException.
     * @param rs the running script
     * @param s the detail message
     * @param e the cause
     */
    public JanitorNativeException(final JanitorScriptProcess rs, final String s, final Throwable e) {
        super(rs, s, e, JanitorNativeException.class);
    }
}
