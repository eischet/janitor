package com.eischet.janitor.api.errors.runtime;

import com.eischet.janitor.api.JanitorScriptProcess;
import org.jetbrains.annotations.NotNull;

/**
 * An exception thrown when an assertion fails.
 * Janitor has a built-in assert() function, which will produce this exception when the assertion fails.
 */
public class JanitorAssertionException extends JanitorRuntimeException {

    /**
     * Constructs a new JanitorAssertionException.
     * @param process the running script
     */
    public JanitorAssertionException(final @NotNull JanitorScriptProcess process) {
        super(process, JanitorAssertionException.class);
    }

    /**
     * Constructs a new JanitorAssertionException.
     * @param process the running script
     * @param message the detail message
     */
    public JanitorAssertionException(final @NotNull JanitorScriptProcess process, final String message) {
        super(process, message, JanitorAssertionException.class);
    }

    /**
     * Constructs a new JanitorAssertionException.
     * @param process the running script
     * @param message the detail message
     * @param cause the cause
     */
    public JanitorAssertionException(final @NotNull JanitorScriptProcess process, final String message, final Throwable cause) {
        super(process, message, cause, JanitorAssertionException.class);
    }

    /**
     * Constructs a new JanitorAssertionException.
     * @param process the running script
     * @param cause the cause
     */
    public JanitorAssertionException(final @NotNull JanitorScriptProcess process, final Throwable cause) {
        super(process, cause, JanitorAssertionException.class);
    }

}
