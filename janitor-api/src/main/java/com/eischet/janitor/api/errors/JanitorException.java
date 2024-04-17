package com.eischet.janitor.api.errors;

/**
 * This is the base class for all Janitor exceptions.
 */
public abstract class JanitorException extends Exception {
    public JanitorException() {
    }

    public JanitorException(final String message) {
        super(message);
    }

    public JanitorException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public JanitorException(final Throwable cause) {
        super(cause);
    }

    public JanitorException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
