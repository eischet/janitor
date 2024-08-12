package com.eischet.janitor.api.errors.runtime;

import com.eischet.janitor.api.JanitorScriptProcess;

/**
 * An exception thrown when an arithmetic operation fails.
 * For example, division by zero.
 */
public class JanitorArithmeticException extends JanitorRuntimeException {

    /**
     * Constructs a new JanitorArithmeticException.
     * @param process the running script
     * @param s the detail message
     * @param e the cause
     */
    public JanitorArithmeticException(final JanitorScriptProcess process, final String s, final ArithmeticException e) {
        super(process, s, e, JanitorArithmeticException.class);
    }
}
