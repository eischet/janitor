package com.eischet.janitor.logging;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper for SLF4J's Logger interface.
 * <p>
 * This wrapper adds a "verbose" mode, which boils down to a variant of "info" that is controlled not by the developer but by the user.
 * </p>
 */
public interface JanitorLogger extends Logger {

    /**
     * @return whether this logger is in verbose mode
     */
    boolean isVerbose();

    /**
     * Set whether this logger is in verbose mode.
     *
     * <p>Verbose mode should be triggered by some user-controlled configuration setting.
     * If it were under your, the developer's, control, you would not need this.</p>
     *
     * @param verbose true for verbose mode, false otherwise
     */
    void setVerbose(boolean verbose);

    /**
     * Log an INFO message if this logger is in verbose mode.
     * @param msg the message
     */
    default void verbose(String msg) {
        if (isVerbose()) {
            info(msg);
        }
    }

    /**
     * Log a formatted INFO message if this logger is in verbose mode.
     * @param format the message format
     * @param arg argument
     */
    default void verbose(String format, Object arg) {
        if (isVerbose()) {
            info(format, arg);
        }
    }

    /**
     * Log a formatted INFO message if this logger is in verbose mode.
     * @param format the message format
     * @param arg1 the first argument
     * @param arg2 the second argument
     */
    default void verbose(String format, Object arg1, Object arg2) {
        if (isVerbose()) {
            info(format, arg1, arg2);
        }
    }

    /**
     * Log a formatted INFO message if this logger is in verbose mode.
     * @param format the message format
     * @param arguments the arguments
     */
    default void verbose(String format, Object... arguments) {
        if (isVerbose()) {
            info(format, arguments);
        }
    }

    /**
     * Get a logger for the given class.
     * @param clazz the class
     * @return the logger
     */
    static @NotNull JanitorLogger getLogger(final @NotNull Class<?> clazz) {
        return new JanitorWrappingLogger(LoggerFactory.getLogger(clazz), null);
    }

    /**
     * Get a logger for the given class and entity.
     * @param clazz the class
     * @param entity the entity
     * @return the logger
     */
    static @NotNull JanitorLogger  getLogger(final @NotNull Class<?> clazz, final @Nullable String entity) {
        return new JanitorWrappingLogger(LoggerFactory.getLogger(clazz), entity);
    }

    /**
     * Get a logger for the given class and debuggable entity.
     * @param clazz the class
     * @param debuggable the debuggable entity
     * @return the logger
     */
    static @NotNull JanitorLogger  getLogger(final @NotNull Class<?> clazz, final Debuggable debuggable) {
        @NotNull final JanitorLogger log = getLogger(clazz, debuggable.getDebugEntityName());
        log.setVerbose(debuggable.isDebugModeEnabled());
        return log;
    }

}
