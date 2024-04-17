package com.eischet.janitor.api.calls;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;

@FunctionalInterface
public interface JUnboundMethod<T> {
    JanitorObject call(final T self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException;
}
