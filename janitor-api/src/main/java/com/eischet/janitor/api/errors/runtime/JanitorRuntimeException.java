package com.eischet.janitor.api.errors.runtime;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.JanitorException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.scopes.ScriptModule;
import com.eischet.janitor.api.types.JanitorObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;

/**
 * A runtime exception that is thrown by the Janitor interpreter.
 */
public abstract class JanitorRuntimeException extends JanitorException implements JanitorObject {

    /**
     * Constructs a new JanitorRuntimeException.
     * @param process the running script
     * @param cls the class of the exception
     */
    public JanitorRuntimeException(final JanitorScriptProcess process, final Class<? extends JanitorRuntimeException> cls) {
        super(formatScriptStackTrace(process, null, cls, null));
    }

    /**
     * Constructs a new JanitorRuntimeException.
     * @param process the running script
     * @param message the detail message
     * @param cls the class of the exception
     */
    public JanitorRuntimeException(final JanitorScriptProcess process, final String message, final Class<? extends JanitorRuntimeException> cls) {
        super(formatScriptStackTrace(process, message, cls, null));
    }

    /**
     * Constructs a new JanitorRuntimeException.
     * @param process the running script
     * @param message the detail message
     * @param cause the cause
     * @param cls the class of the exception
     */
    public JanitorRuntimeException(final JanitorScriptProcess process, final String message, final Throwable cause, final Class<? extends JanitorRuntimeException> cls) {
        super(formatScriptStackTrace(process, message, cls, cause), cause);
    }

    /**
     * Constructs a new JanitorRuntimeException.
     * @param process the running script
     * @param cause the cause
     * @param cls the class of the exception
     */
    public JanitorRuntimeException(final JanitorScriptProcess process, final Throwable cause, final Class<? extends JanitorRuntimeException> cls) {
        super(formatScriptStackTrace(process, null, cls, cause), cause);
    }


    @Override
    public @Nullable JanitorObject janitorGetAttribute(final @NotNull JanitorScriptProcess process, final @NotNull String name, final boolean required) throws JanitorRuntimeException {
        if ("message".equals(name)) {
            return process.getEnvironment().getBuiltins().nullableString(getMessage());
        }
        if ("type".equals(name)) {
            return process.getEnvironment().getBuiltins().string(getClass().getSimpleName());
        }
        return JanitorObject.super.janitorGetAttribute(process, name, required);
    }

    /**
     * Format the stack trace for a script.
     * @param process the running script
     * @param message the detail message
     * @param cls the class of the exception
     * @param cause the optional cause of the exception
     * @return the formatted stack trace
     */
    private static String formatScriptStackTrace(final JanitorScriptProcess process, final String message, final Class<? extends JanitorRuntimeException> cls, final Throwable cause) {
        final ArrayList<Location> stack = new ArrayList<>(process.getStackTrace());
        Collections.reverse(stack);

        // final ImmutableList<String> sourceLines = splitSource(process);
        final StringBuilder out = new StringBuilder();
        out.append("Traceback (most recent call last):");
        if (!stack.isEmpty()) {
            for (final Location location : stack) {
                if (ScriptModule.isBuiltin(location.getModule())) {
                    // skip over the builtin module, or else it would appear at the top of every script
                    continue;
                }
                out.append("\n  ").append(location);
                // final String line = getLine(sourceLines, location.getLine());
                final String line = location.getSourceLine();
                if (line != null) {
                    out.append("\n    ").append(line.trim());
                }
            }
        }
        final String errorClass = cls == null ? "unknown error" : cls.getSimpleName();
        out.append("\n");
        if (errorClass.startsWith("Janitor")) {
            out.append(errorClass.substring(7));
        } else {
            out.append(errorClass);
        }
        if (message != null && !message.isBlank()) {
            out.append(": ").append(message);
        }
        if (cause != null) {
            Throwable i = cause;
            while (i != null) {
                out.append("\n caused by ").append(i.getClass().getSimpleName());
                if (i.getMessage() != null) {
                    out.append(": ").append(i.getMessage());
                }
                i = i.getCause();
            }

        }
        return out.toString();
    }

    @Override
    public String janitorToString() {
        return getClass().getSimpleName() + ": " + getMessage();
    }

    @Override
    public @NotNull String janitorClassName() {
        return "exception";
    }

}
