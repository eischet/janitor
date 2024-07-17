package com.eischet.janitor.api.errors.runtime;

import com.eischet.janitor.api.JanitorScriptProcess;

/**
 * An exception thrown when a name is invalid.
 * For example, when you try to assign a value to a name that is not a valid identifier.
 */
public class JanitorNameException extends JanitorRuntimeException {
    /**
     * Constructs a new JanitorNameException.
     * @param process the running script
     */
    public JanitorNameException(final JanitorScriptProcess process) {
        super(process, JanitorNameException.class);
    }

    /**
     * Constructs a new JanitorNameException.
     * @param process the running script
     * @param message the detail message
     */
    public JanitorNameException(final JanitorScriptProcess process, final String message) {
        super(process, message, JanitorNameException.class);
    }

    /**
     * Constructs a new JanitorNameException.
     * @param process the running script
     * @param message the detail message
     * @param cause the cause
     */
    public JanitorNameException(final JanitorScriptProcess process, final String message, final Throwable cause) {
        super(process, message, cause, JanitorNameException.class);
    }

    /**
     * Constructs a new JanitorNameException.
     * @param process the running script
     * @param cause the cause
     */
    public JanitorNameException(final JanitorScriptProcess process, final Throwable cause) {
        super(process, cause, JanitorNameException.class);
    }

}
