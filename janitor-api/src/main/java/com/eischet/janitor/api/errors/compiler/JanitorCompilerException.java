package com.eischet.janitor.api.errors.compiler;

import com.eischet.janitor.api.errors.JanitorException;

/**
 * Compiler Exceptions are thrown by the compilation stage of the Janitor interpreter.
 */
public class JanitorCompilerException extends JanitorException {

    /**
     * Constructs a new JanitorCompilerException with {@code null} as its detail message.
     */
    public JanitorCompilerException() {
    }

    /**
     * Constructs a new JanitorCompilerException with the specified detail message.
     * @param message the detail message.
     */
    public JanitorCompilerException(final String message) {
        super(message);
    }

    /**
     * Constructs a new JanitorCompilerException with the specified detail message and cause.
     * @param message the detail message.
     * @param cause the cause.
     */
    public JanitorCompilerException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new JanitorCompilerException with the specified cause.
     * @param cause the cause.
     */
    public JanitorCompilerException(final Throwable cause) {
        super(cause);
    }

}
