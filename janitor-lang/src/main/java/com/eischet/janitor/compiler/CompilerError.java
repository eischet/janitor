package com.eischet.janitor.compiler;

public class CompilerError extends RuntimeException {

    public CompilerError() {
    }

    public CompilerError(final String message) {
        super(message);
    }

    public CompilerError(final String message, final Throwable cause) {
        super(message, cause);
    }

    public CompilerError(final Throwable cause) {
        super(cause);
    }

    public CompilerError(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
