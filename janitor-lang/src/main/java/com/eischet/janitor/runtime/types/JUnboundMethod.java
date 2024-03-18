package com.eischet.janitor.runtime.types;

import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.JanitorScriptProcess;

@FunctionalInterface
public interface JUnboundMethod<T> {
    JanitorObject call(final T self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException;
}
