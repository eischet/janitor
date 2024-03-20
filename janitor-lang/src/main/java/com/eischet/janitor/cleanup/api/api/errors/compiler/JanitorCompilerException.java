package com.eischet.janitor.cleanup.api.api.errors.compiler;

import com.eischet.janitor.cleanup.api.api.errors.JanitorException;

/**
 * Compiler Exceptions are thrown by the compilation stage of the Janitor interpreter.
 */
public class JanitorCompilerException extends JanitorException {

    public JanitorCompilerException() {
    }

    public JanitorCompilerException(final String message) {
        super(message);
    }

    public JanitorCompilerException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public JanitorCompilerException(final Throwable cause) {
        super(cause);
    }

}
