package com.eischet.janitor.logging;

import org.slf4j.Logger;

/**
 * Wrapper for SLF4J's Logger interface.
 *
 * This wrapper adds a "verbose" mode, which boils down to a variant of "info" that is controlled not by the developer but by the user.
 *
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

}
