// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.errors.runtime;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.JanitorException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.scopes.ScriptSource;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.composed.JanitorAware;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;

/**
 * A runtime exception that is thrown by the Janitor interpreter.
 */
public abstract class JanitorRuntimeException extends JanitorException implements JanitorObject {

    /** The message without the traceback, as scripts see it in {@code e.message}. */
    private final @NotNull String text;

    /**
     * Constructs a new JanitorRuntimeException.
     * @param process the running script
     * @param cls the class of the exception
     */
    public JanitorRuntimeException(final @NotNull  JanitorScriptProcess process, final @NotNull Class<? extends JanitorRuntimeException> cls) {
        super(formatScriptStackTrace(process, null, cls, null));
        this.text = plainText(null, cls, null);
    }

    /**
     * Constructs a new JanitorRuntimeException.
     * @param process the running script
     * @param message the detail message
     * @param cls the class of the exception
     */
    public JanitorRuntimeException(final @NotNull  JanitorScriptProcess process, final String message, final Class<? extends JanitorRuntimeException> cls) {
        super(formatScriptStackTrace(process, message, cls, null));
        this.text = plainText(message, cls, null);
    }

    /**
     * Constructs a new JanitorRuntimeException.
     * @param process the running script
     * @param message the detail message
     * @param cause the cause
     * @param cls the class of the exception
     */
    public JanitorRuntimeException(final @NotNull  JanitorScriptProcess process, final String message, final Throwable cause, final Class<? extends JanitorRuntimeException> cls) {
        super(formatScriptStackTrace(process, message, cls, cause), cause);
        this.text = plainText(message, cls, cause);
    }

    /**
     * Constructs a new JanitorRuntimeException.
     * @param process the running script
     * @param cause the cause
     * @param cls the class of the exception
     */
    public JanitorRuntimeException(final @NotNull  JanitorScriptProcess process, final Throwable cause, final Class<? extends JanitorRuntimeException> cls) {
        super(formatScriptStackTrace(process, null, cls, cause), cause);
        this.text = plainText(null, cls, cause);
    }


    @Override
    public @Nullable JanitorObject janitorGetAttribute(final @NotNull JanitorScriptProcess process, final @NotNull String name, final boolean required) throws JanitorRuntimeException {
        if ("message".equals(name)) {
            return Janitor.getBuiltins().string(text);
        }
        if ("traceback".equals(name)) {
            return Janitor.getBuiltins().nullableString(getMessage());
        }
        if ("type".equals(name)) {
            return Janitor.getBuiltins().string(getClass().getSimpleName());
        }
        if ("value".equals(name)) {
            return Janitor.NULL; // only values thrown by scripts have one, see JanitorScriptThrownException
        }
        if (getCause() instanceof JanitorAware aware) {
            final JanitorObject jo = aware.asJanitorObject();
            if (jo != null) {
                return jo.janitorGetAttribute(process, name, required);
            }
        }
        if (getCause() instanceof JanitorObject jo) {
            return jo.janitorGetAttribute(process, name, required);
        }
        return JanitorObject.super.janitorGetAttribute(process, name, required);
    }

    /**
     * @return the message without the traceback, e.g. "name 'x' is not defined", which scripts see as {@code e.message};
     * {@link #getMessage()} contains the whole traceback
     */
    public @NotNull String getText() {
        return text;
    }

    /**
     * The message as scripts see it: the detail message, followed by the message of the cause unless it repeats it; without
     * either, the kind of error, e.g. "AssertionException".
     */
    private static @NotNull String plainText(final @Nullable String message, final @NotNull Class<? extends JanitorRuntimeException> cls, final @Nullable Throwable cause) {
        final String causeMessage = cause == null || cause.getMessage() == null || cause.getMessage().isBlank() ? null : cause.getMessage().strip();
        if (message != null && !message.isBlank()) {
            return causeMessage == null || isAlreadyTold(message, causeMessage) ? message : message + ": " + causeMessage;
        }
        if (causeMessage != null) {
            return causeMessage;
        }
        final String name = cls.getSimpleName();
        return name.startsWith("Janitor") ? name.substring(7) : name;
    }

    /**
     * Format the stack trace for a script.
     * @param process the running script
     * @param message the detail message
     * @param cls the class of the exception
     * @param cause the optional cause of the exception
     * @return the formatted stack trace
     */
    private static String formatScriptStackTrace(final @NotNull JanitorScriptProcess process, final String message, final Class<? extends JanitorRuntimeException> cls, final Throwable cause) {
        final StringBuilder out = new StringBuilder();
        final ArrayList<Location> stack = new ArrayList<>(process.getStackTrace());
        Collections.reverse(stack);

        // final ImmutableList<String> sourceLines = splitSource(process);
        out.append("Traceback (most recent call last):");
        if (!stack.isEmpty()) {
            for (final Location location : stack) {
                if (location != null) {
                    if (ScriptSource.isBuiltin(location.getModule())) {
                        // skip over the builtin module, or else it would appear at the top of every script
                        continue;
                    }
                    out.append("\n  ").append(location);
                    // final String line = getLine(sourceLines, location.getLine());
                    final String line = location.getSourceLine();
                    if (line != null) {
                        out.append("\n    ").append(line.trim());
                    }
                } else {
                    out.append("\n    <unknown location>");
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
                if (i.getMessage() != null && !isAlreadyTold(out, i.getMessage())) {
                    out.append(": ").append(i.getMessage());
                }
                i = i.getCause() == i ? null : i.getCause();
            }

        }
        return out.toString();
    }

    /**
     * Whether the first line of a cause's message is already part of the text, as it often is: wrappers tend to repeat the message of what
     * they wrap (e.g. a database error and the driver's exceptions below it). The class name of the cause is listed anyway.
     */
    private static boolean isAlreadyTold(final CharSequence text, final String message) {
        final String trimmed = message.strip();
        if (trimmed.isEmpty()) {
            return true;
        }
        final int newline = trimmed.indexOf('\n');
        final String firstLine = newline < 0 ? trimmed : trimmed.substring(0, newline).strip();
        // very short messages ("1", "null") would be found by chance
        return firstLine.length() >= 12 && text.toString().contains(firstLine);
    }

    @Override
    public @NotNull String janitorToString() {
        return getClass().getSimpleName() + ": " + getMessage();
    }

    @Override
    public @NotNull String janitorClassName() {
        return "exception";
    }

}
