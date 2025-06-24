package com.eischet.janitor.api.errors.runtime;

import com.eischet.janitor.api.JanitorScriptProcess;

/**
 * An exception thrown when an argument is invalid or missing.
 */
public class JanitorArgumentException extends JanitorRuntimeException {
    /**
     * Constructs a new JanitorArgumentException.
     * @param process the running script
     * @param message the detail message
     */
    public JanitorArgumentException(final JanitorScriptProcess process, final String message) {
        super(process, message, JanitorArgumentException.class);
    }


    /**
     * Constructs a new JanitorArgumentException with the specified detail message and cause.
     * @param process the running script
     * @param message the detail message
     * @param cause the cause
     */
    public JanitorArgumentException(final JanitorScriptProcess process, final String message, final Throwable cause) {
        super(process, message, cause, JanitorArgumentException.class);
    }

    /**
     * Constructs a (child class of) JanitorArgumentException with the specified detail message.
     *
     * @param process the running script
     * @param message the detail message
     * @param cls the class of the child exception, a child class of this class
     */
    protected JanitorArgumentException(final JanitorScriptProcess process, final String message, final Class<? extends JanitorArgumentException> cls) {
        super(process, message, cls);
    }

    /**
     * Constructs a (child class of) JanitorArgumentException with the specified detail message.
     *
     * @param process the running script
     * @param cls the class of the child exception, a child class of this class
     */
    public JanitorArgumentException(final JanitorScriptProcess process, final Class<? extends JanitorRuntimeException> cls) {
        super(process, cls);
    }

    /**
     * Constructs a (child class of) JanitorArgumentException with the specified detail message.
     *
     * @param process the running script
     * @param message the detail message
     * @param cause the cause
     * @param cls the class of the child exception, a child class of this class
     */
    public JanitorArgumentException(final JanitorScriptProcess process, final String message, final Throwable cause, final Class<? extends JanitorRuntimeException> cls) {
        super(process, message, cause, cls);
    }

    /**
     * Constructs a (child class of) JanitorArgumentException with the specified detail message.
     *
     * @param process the running script
     * @param cause the cause
     * @param cls the class of the child exception, a child class of this class
     */
    public JanitorArgumentException(final JanitorScriptProcess process, final Throwable cause, final Class<? extends JanitorRuntimeException> cls) {
        super(process, cause, cls);
    }
}
