package com.eischet.janitor.api.errors.runtime;

import com.eischet.janitor.api.JanitorScriptProcess;
import org.jetbrains.annotations.NotNull;

/**
 * An exception thrown when an arithmetic operation fails.
 * For example, division by zero.
 */
public class JanitorArithmeticException extends JanitorRuntimeException {

    /**
     * Constructs a new JanitorArithmeticException.
     * @param process the running script
     * @param message the detail message
     * @param e the cause
     */
    public JanitorArithmeticException(final @NotNull JanitorScriptProcess process, final String message, final ArithmeticException e) {
        super(process, message, e, JanitorArithmeticException.class);
    }
}
