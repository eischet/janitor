// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.logging;

import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

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

    /**
     * @param thrown an exception
     * @return whether the messages of the exception and its causes say it all: every exception of the chain is self-explanatory, and none
     * has suppressed exceptions
     */
    static boolean coversChain(final Throwable thrown) {
        final Set<Throwable> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Throwable t = thrown; t != null && seen.add(t); t = t.getCause()) {
            if (!(t instanceof SelfExplanatory explanatory) || !explanatory.isSelfExplanatory() || t.getSuppressed().length > 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Describes an exception by its messages and those of its causes. Wrapping exceptions and script errors usually repeat the messages of
     * their causes, so each message is written only if it is not part of the text yet.
     *
     * @param where what the description starts with, followed by a colon, e.g. where the error occurred; null or empty for nothing
     * @param thrown the exception
     * @return the description, with the class name in place of the messages if no exception of the chain has a message
     */
    static String describe(final @Nullable String where, final Throwable thrown) {
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

}
