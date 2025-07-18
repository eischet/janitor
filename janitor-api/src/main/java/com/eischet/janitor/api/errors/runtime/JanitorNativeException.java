package com.eischet.janitor.api.errors.runtime;

import com.eischet.janitor.api.JanitorScriptProcess;
import org.jetbrains.annotations.NotNull;

/**
 * An exception thrown when a native method throws an exception.
 * This is preferred over glue exceptions because it supplies the process as context from the start, whereas glue exceptions need to be converted
 * to get a stack trace for the script.
 */
public class JanitorNativeException extends JanitorRuntimeException {
    /**
     * Constructs a new JanitorNativeException.
     * @param process the running script
     * @param message the detail message
     * @param cause the cause
     */
    public JanitorNativeException(final @NotNull JanitorScriptProcess process, final String message, final Throwable cause) {
        super(process, message, cause, JanitorNativeException.class);
    }
}
