package com.eischet.janitor.repl;

import com.eischet.janitor.api.errors.runtime.JanitorNativeException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;

import java.io.IOException;

/**
 * Interface for interacting with the user in a REPL.
 */
public interface ReplIO {

    /**
     * Show the prompt to the user and return the text input by the user.
     * <p>This method will usually block until input is received.</p>
     * @param prompt the prompt
     * @return the user input
     * @throws IOException on errors getting input
     */
    String readLine(String prompt) throws IOException;

    /**
     * Show a message, not followed by a line break.
     * @param text a message
     */
    void print(String text);

    /**
     * Show a message, followed by a line break.
     * @param text a message
     */
    void println(String text);

    /**
     * Show an error message.
     * @param text a message
     */
    void error(String text);

    /**
     * Show a verbose message.
     * <p>The default implementation does nothing.</p>
     * @param text a message
     */
    default void verbose(String text) { }

    /**
     * Report an exception.
     * <p>
     * When implementing this, not that there's no need to print the stack trace if {@link #shouldPrintStackTrace}
     * returns false.
     * </p>
     * @param e
     */
    void exception(Exception e);

    /**
     * Decide whether to show a full Java stack trace for an exception.
     * <p>
     * JanitorRuntimeException instances already contain a full script stack trace, so showing their message should
     * usually be enough. JanitorNativeException instances are an exception, because there the Java stack trace will
     * be helpful in debugging.
     * </p>
     * @param e the exception
     * @return true if the stack trace should be printed
     */
    default boolean shouldPrintStackTrace(Exception e) {
        if (e instanceof JanitorRuntimeException) {
            return e instanceof JanitorNativeException; // only these
        } else {
            return true;
        }
    }

}
