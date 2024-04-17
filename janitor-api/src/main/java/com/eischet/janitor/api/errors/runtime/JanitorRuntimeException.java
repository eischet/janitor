package com.eischet.janitor.api.errors.runtime;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.JanitorException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.scopes.ScriptModule;
import com.eischet.janitor.api.types.JString;
import com.eischet.janitor.api.types.JanitorObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class JanitorRuntimeException extends JanitorException implements JanitorObject {

    public JanitorRuntimeException(final JanitorScriptProcess process, final Class<? extends JanitorRuntimeException> cls) {
        super(formatScriptStackTrace(process, null, cls, null));
    }

    public JanitorRuntimeException(final JanitorScriptProcess process, final String message, final Class<? extends JanitorRuntimeException> cls) {
        super(formatScriptStackTrace(process, message, cls, null));
    }

    public JanitorRuntimeException(final JanitorScriptProcess process, final String message, final Throwable cause, final Class<? extends JanitorRuntimeException> cls) {
        super(formatScriptStackTrace(process, message, cls, cause), cause);
    }

    public JanitorRuntimeException(final JanitorScriptProcess process, final Throwable cause, final Class<? extends JanitorRuntimeException> cls) {
        super(formatScriptStackTrace(process, null, cls, cause), cause);
    }

    public static List<String> splitSource(final JanitorScriptProcess process) {
        if (process.getSource() == null) {
            return Collections.emptyList();
        } else {
            return List.of(process.getSource().split("\r?\n\r?"));
        }
    }

    public static String getLine(final List<String> lines, int line) {
        if (line > lines.size() || line < 1 || lines.isEmpty()) {
            return null;
        }
        return lines.get(line - 1);
    }

    @Override
    public @Nullable JanitorObject janitorGetAttribute(final JanitorScriptProcess runningScript, final String name, final boolean required) throws JanitorNameException {
        if ("message".equals(name)) {
            return JString.ofNullable(getMessage());
        }
        if ("type".equals(name)) {
            return JString.of(getClass().getSimpleName());
        }
        return JanitorObject.super.janitorGetAttribute(runningScript, name, required);
    }

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
        if (errorClass.startsWith("CS")) {
            out.append(errorClass.substring(2));
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
    public Object janitorGetHostValue() {
        return this;
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
