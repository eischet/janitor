package com.eischet.janitor.api.errors.runtime;

import com.eischet.janitor.api.JanitorScriptProcess;

/**
 * An exception thrown when an assignment operation fails.
 * For example, when you assign a string value to a number property, host code may throw this exception.
 */
public class JanitorAssignmentException extends JanitorRuntimeException {

    /**
     * Constructs a new JanitorAssignmentException.
     * @param process the running script
     */
    public JanitorAssignmentException(final JanitorScriptProcess process) {
        super(process, JanitorAssignmentException.class);
    }

    /**
     * Constructs a new JanitorAssignmentException.
     * @param process the running script
     * @param message the detail message
     */
    public JanitorAssignmentException(final JanitorScriptProcess process, final String message) {
        super(process, message, JanitorAssignmentException.class);
    }

    /**
     * Constructs a new JanitorAssignmentException.
     * @param process the running script
     * @param message the detail message
     * @param cause the cause
     */
    public JanitorAssignmentException(final JanitorScriptProcess process, final String message, final Throwable cause) {
        super(process, message, cause, JanitorAssignmentException.class);
    }

    /**
     * Constructs a new JanitorAssignmentException.
     * @param process the running script
     * @param cause the cause
     */
    public JanitorAssignmentException(final JanitorScriptProcess process, final Throwable cause) {
        super(process, cause, JanitorAssignmentException.class);
    }
}
