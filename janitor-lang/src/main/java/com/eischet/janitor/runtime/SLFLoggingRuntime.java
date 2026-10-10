// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.runtime;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.JanitorEnvironment;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.types.functions.JCallArgs;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.builtin.JNull;
import com.eischet.janitor.api.types.JanitorObject;
import org.slf4j.Logger;

/** A runtime that sends output and warnings to an SLF4J logger. */
public class SLFLoggingRuntime extends BaseRuntime {

    protected final Logger log;
    protected final ThreadLocal<String> context = new ThreadLocal<>();

    public SLFLoggingRuntime(final Logger log) {
        this(Janitor.current(), log);
    }

    public SLFLoggingRuntime(final JanitorEnvironment env, final Logger log) {
        super(env);
        this.log = log;
    }

    /**
     * @return the logger
     */
    public Logger getLog() {
        return log;
    }

    @Override
    public void warn(String warning) {
        log.warn(warning);
    }

    @Override
    public JanitorObject print(final JanitorScriptProcess process, final JCallArgs args) {
        final StringBuilder output = new StringBuilder();
        final int sz = args.size();
        final int last = sz -1;
        for (int i = 0; i < sz; i++) {
            final JanitorObject argument = args.get(i);
            output.append(argument.janitorToString());
            if (i != last) {
                output.append(" ");
            }
        }
        logAtInfoLevel(output.toString());
        return JNull.NULL;
    }

    /**
     * Logs a message at info level, prefixed with the current context if there is one.
     * @param message the message
     */
    protected void logAtInfoLevel(final String message) {
        final String c = getContext();
        if (c != null) {
            log.info("{} - {}", c, message);
        } else {
            log.info("{}", message);
        }
    }

    @Override
    protected void exception(final String s, final JanitorRuntimeException e) {
        log.error(s, e);
    }

    /**
     * Sets a context description for the current thread, which is added to log messages, e.g. the name of the script.
     * @param context the context
     */
    public void setContext(final String context) {
        this.context.set(context);
    }

    /**
     * @return the context of the current thread, or null if there is none
     */
    public String getContext() {
        return context.get();
    }

    /** Removes the context of the current thread. */
    public void clearContext() {
        context.remove();
    }
}
