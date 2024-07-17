package com.eischet.janitor.api.calls;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;

/**
 * A method that does not return a value.
 * @param <T> the type of object that this method calls "this".
 */
@FunctionalInterface
public interface JVoidMethod<T> {
    void call(final T self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException;
}

// TODO: there's already code for the same purpose in JBoundMethod/JUnboundMethod, so this is probably redundant. Consolidate!