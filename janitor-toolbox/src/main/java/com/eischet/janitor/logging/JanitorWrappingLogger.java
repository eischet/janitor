// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.logging;

import com.eischet.janitor.logging.LoggingContext;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.helpers.MessageFormatter;
import org.slf4j.spi.LoggingEventBuilder;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Like a logger, but with an optional configurable "entity" prefix output with every log message when JanitorLogging is used.
 * A "verbose mode" is inherited from the {@link JanitorLogger} interface, which extends the original SLF4J Logger interface.
 */
public class JanitorWrappingLogger implements JanitorLogger {

    private final @NotNull Logger wrapped;
    private final @Nullable String entity;
    private boolean verbose;

    public JanitorWrappingLogger(final @NotNull Logger logger, final @Nullable String entity) {
        this.wrapped = logger;
        this.entity = entity;
    }

    @Override
    public boolean isVerbose() {
        return verbose;
    }

    @Override
    public void setVerbose(final boolean verbose) {
        this.verbose = verbose;
    }

    @Override
    public String getName() {
        return wrapped.getName();
    }

    @Override
    public LoggingEventBuilder makeLoggingEventBuilder(final Level level) {
        return wrapped.makeLoggingEventBuilder(level);
    }

    @CheckReturnValue
    @Override
    public LoggingEventBuilder atLevel(final Level level) {
        return wrapped.atLevel(level);
    }

    @Override
    public boolean isEnabledForLevel(final Level level) {
        return wrapped.isEnabledForLevel(level);
    }

    @Override
    public boolean isTraceEnabled() {
        return wrapped.isTraceEnabled();
    }

    /**
     * Runs the code with the entity of this logger set as the current entity of the logging context, if it has one.
     * @param runnable the code to run
     */
    protected void withEntity(final Runnable runnable) {
        if (entity == null) {
            runnable.run();
        } else {
            LoggingContext.withEntity(entity, runnable);
        }
    }

    /**
     * Logs a message that may carry an exception, which SLF4J expects as the last argument. A {@link SelfExplanatory} exception is written
     * as part of the message, without its stack trace.
     * @param regular logs as usual
     * @param condensed logs the given text, which is not a format
     * @param format the message format
     * @param arguments the arguments of the format, perhaps with the exception at the end
     */
    private void logExplained(final Runnable regular, final Consumer<String> condensed, final String format, final Object... arguments) {
        if (arguments != null && arguments.length > 0 && arguments[arguments.length - 1] instanceof Throwable thrown && explainsItself(thrown)) {
            final String text = withMessages(MessageFormatter.arrayFormat(format, arguments).getMessage(), thrown);
            withEntity(() -> condensed.accept(text));
        } else {
            withEntity(regular);
        }
    }

    private static boolean explainsItself(final Throwable thrown) {
        final Set<Throwable> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Throwable t = thrown; t != null && seen.add(t); t = t.getCause()) {
            if (!(t instanceof SelfExplanatory explanatory) || !explanatory.isSelfExplanatory() || t.getSuppressed().length > 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Wrapping exceptions and script errors usually repeat the messages of their causes, so each message is written only if it is not
     * part of the text yet.
     */
    private static String withMessages(final String where, final Throwable thrown) {
        final StringBuilder out = new StringBuilder(where == null ? "" : where);
        final int start = out.length();
        boolean hasMessage = false;
        final Set<Throwable> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Throwable t = thrown; t != null && seen.add(t); t = t.getCause()) {
            final String message = t.getMessage();
            if (message != null && !message.isBlank()) {
                hasMessage = true;
                if (out.indexOf(message.strip()) < 0) {
                    appendPart(out, start, message.strip());
                }
            }
        }
        if (!hasMessage) {
            appendPart(out, start, thrown.getClass().getName());
        }
        return out.toString();
    }

    private static void appendPart(final StringBuilder out, final int start, final String part) {
        if (out.length() > start) {
            out.append('\n');
        } else if (start > 0) {
            out.append(":\n");
        }
        out.append(part);
    }

    @Override
    public void trace(final String msg) {
        withEntity(() -> wrapped.trace(msg));
    }

    @Override
    public void trace(final String format, final Object arg) {
        logExplained(() -> wrapped.trace(format, arg), text -> wrapped.trace(text), format, arg);
    }

    @Override
    public void trace(final String format, final Object arg1, final Object arg2) {
        logExplained(() -> wrapped.trace(format, arg1, arg2), text -> wrapped.trace(text), format, arg1, arg2);
    }

    @Override
    public void trace(final String format, final Object... arguments) {
        logExplained(() -> wrapped.trace(format, arguments), text -> wrapped.trace(text), format, arguments);
    }

    @Override
    public void trace(final String msg, final Throwable t) {
        logExplained(() -> wrapped.trace(msg, t), text -> wrapped.trace(text), msg, t);
    }

    @Override
    public boolean isTraceEnabled(final Marker marker) {
        return wrapped.isTraceEnabled(marker);
    }

    @CheckReturnValue
    @Override
    public LoggingEventBuilder atTrace() {
        return wrapped.atTrace();
    }

    @Override
    public void trace(final Marker marker, final String msg) {
        withEntity(() -> wrapped.trace(marker, msg));
    }

    @Override
    public void trace(final Marker marker, final String format, final Object arg) {
        logExplained(() -> wrapped.trace(marker, format, arg), text -> wrapped.trace(marker, text), format, arg);
    }

    @Override
    public void trace(final Marker marker, final String format, final Object arg1, final Object arg2) {
        logExplained(() -> wrapped.trace(marker, format, arg1, arg2), text -> wrapped.trace(marker, text), format, arg1, arg2);
    }

    @Override
    public void trace(final Marker marker, final String format, final Object... argArray) {
        logExplained(() -> wrapped.trace(marker, format, argArray), text -> wrapped.trace(marker, text), format, argArray);
    }

    @Override
    public void trace(final Marker marker, final String msg, final Throwable t) {
        logExplained(() -> wrapped.trace(marker, msg, t), text -> wrapped.trace(marker, text), msg, t);
    }

    @Override
    public boolean isDebugEnabled() {
        return wrapped.isDebugEnabled();
    }

    @Override
    public void debug(final String msg) {
        withEntity(() -> wrapped.debug(msg));
    }

    @Override
    public void debug(final String format, final Object arg) {
        logExplained(() -> wrapped.debug(format, arg), text -> wrapped.debug(text), format, arg);
    }

    @Override
    public void debug(final String format, final Object arg1, final Object arg2) {
        logExplained(() -> wrapped.debug(format, arg1, arg2), text -> wrapped.debug(text), format, arg1, arg2);
    }

    @Override
    public void debug(final String format, final Object... arguments) {
        logExplained(() -> wrapped.debug(format, arguments), text -> wrapped.debug(text), format, arguments);
    }

    @Override
    public void debug(final String msg, final Throwable t) {
        logExplained(() -> wrapped.debug(msg, t), text -> wrapped.debug(text), msg, t);
    }

    @Override
    public boolean isDebugEnabled(final Marker marker) {
        return wrapped.isDebugEnabled(marker);
    }

    @Override
    public void debug(final Marker marker, final String msg) {
        withEntity(() -> wrapped.debug(marker, msg));
    }

    @Override
    public void debug(final Marker marker, final String format, final Object arg) {
        logExplained(() -> wrapped.debug(marker, format, arg), text -> wrapped.debug(marker, text), format, arg);
    }

    @Override
    public void debug(final Marker marker, final String format, final Object arg1, final Object arg2) {
        logExplained(() -> wrapped.debug(marker, format, arg1, arg2), text -> wrapped.debug(marker, text), format, arg1, arg2);
    }

    @Override
    public void debug(final Marker marker, final String format, final Object... arguments) {
        logExplained(() -> wrapped.debug(marker, format, arguments), text -> wrapped.debug(marker, text), format, arguments);
    }

    @Override
    public void debug(final Marker marker, final String msg, final Throwable t) {
        logExplained(() -> wrapped.debug(marker, msg, t), text -> wrapped.debug(marker, text), msg, t);
    }

    @CheckReturnValue
    @Override
    public LoggingEventBuilder atDebug() {
        return wrapped.atDebug();
    }

    @Override
    public boolean isInfoEnabled() {
        return wrapped.isInfoEnabled();
    }

    @Override
    public void info(final String msg) {
        withEntity(() -> wrapped.info(msg));
    }

    @Override
    public void info(final String format, final Object arg) {
        logExplained(() -> wrapped.info(format, arg), text -> wrapped.info(text), format, arg);
    }

    @Override
    public void info(final String format, final Object arg1, final Object arg2) {
        logExplained(() -> wrapped.info(format, arg1, arg2), text -> wrapped.info(text), format, arg1, arg2);
    }

    @Override
    public void info(final String format, final Object... arguments) {
        logExplained(() -> wrapped.info(format, arguments), text -> wrapped.info(text), format, arguments);
    }

    @Override
    public void info(final String msg, final Throwable t) {
        logExplained(() -> wrapped.info(msg, t), text -> wrapped.info(text), msg, t);
    }

    @Override
    public boolean isInfoEnabled(final Marker marker) {
        return wrapped.isInfoEnabled(marker);
    }

    @Override
    public void info(final Marker marker, final String msg) {
        withEntity(() -> wrapped.info(marker, msg));
    }

    @Override
    public void info(final Marker marker, final String format, final Object arg) {
        logExplained(() -> wrapped.info(marker, format, arg), text -> wrapped.info(marker, text), format, arg);
    }

    @Override
    public void info(final Marker marker, final String format, final Object arg1, final Object arg2) {
        logExplained(() -> wrapped.info(marker, format, arg1, arg2), text -> wrapped.info(marker, text), format, arg1, arg2);
    }

    @Override
    public void info(final Marker marker, final String format, final Object... arguments) {
        logExplained(() -> wrapped.info(marker, format, arguments), text -> wrapped.info(marker, text), format, arguments);
    }

    @Override
    public void info(final Marker marker, final String msg, final Throwable t) {
        logExplained(() -> wrapped.info(marker, msg, t), text -> wrapped.info(marker, text), msg, t);
    }

    @CheckReturnValue
    @Override
    public LoggingEventBuilder atInfo() {
        return wrapped.atInfo();
    }

    @Override
    public boolean isWarnEnabled() {
        return wrapped.isWarnEnabled();
    }

    @Override
    public void warn(final String msg) {
        withEntity(() -> wrapped.warn(msg));
    }

    @Override
    public void warn(final String format, final Object arg) {
        logExplained(() -> wrapped.warn(format, arg), text -> wrapped.warn(text), format, arg);
    }

    @Override
    public void warn(final String format, final Object... arguments) {
        logExplained(() -> wrapped.warn(format, arguments), text -> wrapped.warn(text), format, arguments);
    }

    @Override
    public void warn(final String format, final Object arg1, final Object arg2) {
        logExplained(() -> wrapped.warn(format, arg1, arg2), text -> wrapped.warn(text), format, arg1, arg2);
    }

    @Override
    public void warn(final String msg, final Throwable t) {
        logExplained(() -> wrapped.warn(msg, t), text -> wrapped.warn(text), msg, t);
    }

    @Override
    public boolean isWarnEnabled(final Marker marker) {
        return wrapped.isWarnEnabled(marker);
    }

    @Override
    public void warn(final Marker marker, final String msg) {
        withEntity(() -> wrapped.warn(marker, msg));
    }

    @Override
    public void warn(final Marker marker, final String format, final Object arg) {
        logExplained(() -> wrapped.warn(marker, format, arg), text -> wrapped.warn(marker, text), format, arg);
    }

    @Override
    public void warn(final Marker marker, final String format, final Object arg1, final Object arg2) {
        logExplained(() -> wrapped.warn(marker, format, arg1, arg2), text -> wrapped.warn(marker, text), format, arg1, arg2);
    }

    @Override
    public void warn(final Marker marker, final String format, final Object... arguments) {
        logExplained(() -> wrapped.warn(marker, format, arguments), text -> wrapped.warn(marker, text), format, arguments);
    }

    @Override
    public void warn(final Marker marker, final String msg, final Throwable t) {
        logExplained(() -> wrapped.warn(marker, msg, t), text -> wrapped.warn(marker, text), msg, t);
    }

    @CheckReturnValue
    @Override
    public LoggingEventBuilder atWarn() {
        return wrapped.atWarn();
    }

    @Override
    public boolean isErrorEnabled() {
        return wrapped.isErrorEnabled();
    }

    @Override
    public void error(final String msg) {
        withEntity(() -> wrapped.error(msg));
    }

    @Override
    public void error(final String format, final Object arg) {
        logExplained(() -> wrapped.error(format, arg), text -> wrapped.error(text), format, arg);
    }

    @Override
    public void error(final String format, final Object arg1, final Object arg2) {
        logExplained(() -> wrapped.error(format, arg1, arg2), text -> wrapped.error(text), format, arg1, arg2);
    }

    @Override
    public void error(final String format, final Object... arguments) {
        logExplained(() -> wrapped.error(format, arguments), text -> wrapped.error(text), format, arguments);
    }

    @Override
    public void error(final String msg, final Throwable t) {
        logExplained(() -> wrapped.error(msg, t), text -> wrapped.error(text), msg, t);
    }

    @Override
    public boolean isErrorEnabled(final Marker marker) {
        return wrapped.isErrorEnabled(marker);
    }

    @Override
    public void error(final Marker marker, final String msg) {
        withEntity(() -> wrapped.error(marker, msg));
    }

    @Override
    public void error(final Marker marker, final String format, final Object arg) {
        logExplained(() -> wrapped.error(marker, format, arg), text -> wrapped.error(marker, text), format, arg);
    }

    @Override
    public void error(final Marker marker, final String format, final Object arg1, final Object arg2) {
        logExplained(() -> wrapped.error(marker, format, arg1, arg2), text -> wrapped.error(marker, text), format, arg1, arg2);
    }

    @Override
    public void error(final Marker marker, final String format, final Object... arguments) {
        logExplained(() -> wrapped.error(marker, format, arguments), text -> wrapped.error(marker, text), format, arguments);
    }

    @Override
    public void error(final Marker marker, final String msg, final Throwable t) {
        logExplained(() -> wrapped.error(marker, msg, t), text -> wrapped.error(marker, text), msg, t);
    }

    @CheckReturnValue
    @Override
    public LoggingEventBuilder atError() {
        return wrapped.atError();
    }
}
