package com.eischet.janitor.cleanup.runtime.types;

import com.eischet.janitor.cleanup.api.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.cleanup.api.api.types.JanitorObject;
import com.eischet.janitor.cleanup.api.api.types.JanitorScriptProcess;

@FunctionalInterface
public interface JUnboundMethod<T> {
    JanitorObject call(final T self, final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException;
}
