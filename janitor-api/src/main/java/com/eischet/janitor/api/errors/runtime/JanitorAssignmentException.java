package com.eischet.janitor.api.errors.runtime;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.glue.JanitorGlueException;
import org.jetbrains.annotations.NotNull;

/**
 * An exception thrown when an assignment operation fails.
 * For example, when you assign a string value to a number property, host code may throw this exception.
 * TODO: at the moment, JanitorArgumentException is actually thrown e.g. by the Dispatch Tables
 */
public class JanitorAssignmentException extends JanitorArgumentException {

    /**
     * Constructs a new JanitorAssignmentException.
     * @param process the running script
     */
    public JanitorAssignmentException(final @NotNull JanitorScriptProcess process) {
        super(process, JanitorAssignmentException.class);
    }

    /**
     * Constructs a new JanitorAssignmentException.
     * @param process the running script
     * @param message the detail message
     */
    public JanitorAssignmentException(final @NotNull JanitorScriptProcess process, final String message) {
        super(process, message, JanitorAssignmentException.class);
    }

    /**
     * Constructs a new JanitorAssignmentException.
     * @param process the running script
     * @param message the detail message
     * @param cause the cause
     */
    public JanitorAssignmentException(final @NotNull JanitorScriptProcess process, final String message, final Throwable cause) {
        super(process, message, cause, JanitorAssignmentException.class);
    }

    /**
     * Constructs a new JanitorAssignmentException.
     * @param process the running script
     * @param cause the cause
     */
    public JanitorAssignmentException(final @NotNull JanitorScriptProcess process, final Throwable cause) {
        super(process, cause, JanitorAssignmentException.class);
    }

    public static JanitorAssignmentException fromGlue(final JanitorScriptProcess process, final JanitorGlueException glueException) {
        return new JanitorAssignmentException(process, glueException.getMessage(), glueException.getCause());
    }

}
