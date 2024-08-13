package com.eischet.janitor.api.types.functions;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;

/**
 * A method that is not bound to an object instance, but needs an instance of the object to be called.
 * @param <T> the type of object that this method calls "this".
 */
@FunctionalInterface
public interface JUnboundMethod<T> {
    JanitorObject call(final T self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException;
}
