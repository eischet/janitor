package com.eischet.janitor.api.types.functions;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;

/**
 * Specialized form of JCallable for writing class constructors.
 * @param <T> any object type
 */
public interface JConstructor<T extends JanitorObject> extends JCallable {
    @Override
    T call(final JanitorScriptProcess process, JCallArgs arguments) throws JanitorRuntimeException;
}
