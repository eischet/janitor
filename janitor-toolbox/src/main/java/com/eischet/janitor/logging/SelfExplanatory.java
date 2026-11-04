// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.logging;

/**
 * An exception whose message says all there is to say, so that a Java stack trace would not help the reader.
 * <p>
 * The typical case is an error in a script: its message contains the traceback of the script, and the Java stack trace below it only
 * shows the interpreter at work. When such an exception is passed to a {@link JanitorLogger} and all of its causes are self-explanatory
 * as well, the logger writes the messages without the stack trace. Any other exception in the chain, e.g. one thrown by Java code that a
 * script called, or a suppressed exception, keeps the stack trace of the whole chain.
 * </p>
 */
public interface SelfExplanatory {

    /**
     * @return whether the message of this exception is enough; an exception can answer false if, as an exception to the rule of its class,
     * it points to a fault in Java code
     */
    default boolean isSelfExplanatory() {
        return true;
    }

}
