package com.eischet.janitor.api.errors;

/**
 * This is the base class for all Janitor exceptions.
 */
public abstract class JanitorException extends Exception {
    // Yes, this is a checked exception. No, I'm not going to change this.

    /**
     * Constructs a new JanitorException with {@code null} as its detail message.
     */
    public JanitorException() {
    }

    /**
     * Constructs a new JanitorException with the specified detail message.
     * @param message the detail message.
     */
    public JanitorException(final String message) {
        super(message);
    }

    /**
     * Constructs a new JanitorException with the specified detail message and cause.
     * @param message the detail message.
     * @param cause the cause.
     */
    public JanitorException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new JanitorException with the specified cause.
     * @param cause the cause.
     */
    public JanitorException(final Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new JanitorException with the specified detail message, cause, suppression enabled or disabled, and writable stack trace enabled or disabled.
     * @param message the detail message.
     * @param cause the cause.
     * @param enableSuppression whether or not suppression is enabled or disabled.
     * @param writableStackTrace whether or not the stack trace should be writable.
     */
    public JanitorException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
