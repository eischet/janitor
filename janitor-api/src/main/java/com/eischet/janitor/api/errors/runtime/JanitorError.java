package com.eischet.janitor.api.errors.runtime;

public class JanitorError extends RuntimeException {
    public JanitorError() {
    }

    public JanitorError(final String message) {
        super(message);
    }

    public JanitorError(final String message, final Throwable cause) {
        super(message, cause);
    }

    public JanitorError(final Throwable cause) {
        super(cause);
    }

    public JanitorError(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
