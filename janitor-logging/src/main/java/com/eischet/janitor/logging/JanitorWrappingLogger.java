package com.eischet.janitor.logging;

import com.eischet.janitor.logging.jul.LoggingContext;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.spi.LoggingEventBuilder;

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

    protected void withEntity(final Runnable runnable) {
        if (entity == null) {
            runnable.run();
        } else {
            LoggingContext.withEntity(entity, runnable);
        }
    }

    @Override
    public void trace(final String msg) {
        withEntity(() -> wrapped.trace(msg));
    }

    @Override
    public void trace(final String format, final Object arg) {
        withEntity(() -> wrapped.trace(format, arg));
    }

    @Override
    public void trace(final String format, final Object arg1, final Object arg2) {
        withEntity(() -> wrapped.trace(format, arg1, arg2));
    }

    @Override
    public void trace(final String format, final Object... arguments) {
        withEntity(() -> wrapped.trace(format, arguments));
    }

    @Override
    public void trace(final String msg, final Throwable t) {
        withEntity(() -> wrapped.trace(msg, t));
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
        withEntity(() -> wrapped.trace(marker, format, arg));
    }

    @Override
    public void trace(final Marker marker, final String format, final Object arg1, final Object arg2) {
        withEntity(() -> wrapped.trace(marker, format, arg1, arg2));
    }

    @Override
    public void trace(final Marker marker, final String format, final Object... argArray) {
        withEntity(() -> wrapped.trace(marker, format, argArray));
    }

    @Override
    public void trace(final Marker marker, final String msg, final Throwable t) {
        withEntity(() -> wrapped.trace(marker, msg, t));
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
        withEntity(() -> wrapped.debug(format, arg));
    }

    @Override
    public void debug(final String format, final Object arg1, final Object arg2) {
        withEntity(() -> wrapped.debug(format, arg1, arg2));
    }

    @Override
    public void debug(final String format, final Object... arguments) {
        withEntity(() -> wrapped.debug(format, arguments));
    }

    @Override
    public void debug(final String msg, final Throwable t) {
        withEntity(() -> wrapped.debug(msg, t));
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
        withEntity(() -> wrapped.debug(marker, format, arg));
    }

    @Override
    public void debug(final Marker marker, final String format, final Object arg1, final Object arg2) {
        withEntity(() -> wrapped.debug(marker, format, arg1, arg2));
    }

    @Override
    public void debug(final Marker marker, final String format, final Object... arguments) {
        withEntity(() -> wrapped.debug(marker, format, arguments));
    }

    @Override
    public void debug(final Marker marker, final String msg, final Throwable t) {
        withEntity(() -> wrapped.debug(marker, msg, t));
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
        withEntity(() -> wrapped.info(format, arg));
    }

    @Override
    public void info(final String format, final Object arg1, final Object arg2) {
        withEntity(() -> wrapped.info(format, arg1, arg2));
    }

    @Override
    public void info(final String format, final Object... arguments) {
        withEntity(() -> wrapped.info(format, arguments));
    }

    @Override
    public void info(final String msg, final Throwable t) {
        withEntity(() -> wrapped.info(msg, t));
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
        withEntity(() -> wrapped.info(marker, format, arg));
    }

    @Override
    public void info(final Marker marker, final String format, final Object arg1, final Object arg2) {
        withEntity(() -> wrapped.info(marker, format, arg1, arg2));
    }

    @Override
    public void info(final Marker marker, final String format, final Object... arguments) {
        withEntity(() -> wrapped.info(marker, format, arguments));
    }

    @Override
    public void info(final Marker marker, final String msg, final Throwable t) {
        withEntity(() -> wrapped.info(marker, msg, t));
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
        withEntity(() -> wrapped.warn(format, arg));
    }

    @Override
    public void warn(final String format, final Object... arguments) {
        withEntity(() -> wrapped.warn(format, arguments));
    }

    @Override
    public void warn(final String format, final Object arg1, final Object arg2) {
        withEntity(() -> wrapped.warn(format, arg1, arg2));
    }

    @Override
    public void warn(final String msg, final Throwable t) {
        withEntity(() -> wrapped.warn(msg, t));
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
        withEntity(() -> wrapped.warn(marker, format, arg));
    }

    @Override
    public void warn(final Marker marker, final String format, final Object arg1, final Object arg2) {
        withEntity(() -> wrapped.warn(marker, format, arg1, arg2));
    }

    @Override
    public void warn(final Marker marker, final String format, final Object... arguments) {
        withEntity(() -> wrapped.warn(marker, format, arguments));
    }

    @Override
    public void warn(final Marker marker, final String msg, final Throwable t) {
        withEntity(() -> wrapped.warn(marker, msg, t));
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
        withEntity(() -> wrapped.error(format, arg));
    }

    @Override
    public void error(final String format, final Object arg1, final Object arg2) {
        withEntity(() -> wrapped.error(format, arg1, arg2));
    }

    @Override
    public void error(final String format, final Object... arguments) {
        withEntity(() -> wrapped.error(format, arguments));
    }

    @Override
    public void error(final String msg, final Throwable t) {
        withEntity(() -> wrapped.error(msg, t));
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
        withEntity(() -> wrapped.error(marker, format, arg));
    }

    @Override
    public void error(final Marker marker, final String format, final Object arg1, final Object arg2) {
        withEntity(() -> wrapped.error(marker, format, arg1, arg2));
    }

    @Override
    public void error(final Marker marker, final String format, final Object... arguments) {
        withEntity(() -> wrapped.error(marker, format, arguments));
    }

    @Override
    public void error(final Marker marker, final String msg, final Throwable t) {
        withEntity(() -> wrapped.error(marker, msg, t));
    }

    @CheckReturnValue
    @Override
    public LoggingEventBuilder atError() {
        return wrapped.atError();
    }
}
