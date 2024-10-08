package com.eischet.janitor.api.types.dispatch;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;

/**
 * A handler for looking up attributes on JanitorObjects.
 * @param <T> the type of JanitorObject
 */
@FunctionalInterface
public interface AttributeLookupHandler<T extends JanitorObject> {
    /**
     * Looks up an attribute on the given instance.
     * @param instance the instance to look up the attribute on.
     * @param process the running script.
     * @return the attribute
     */
    JanitorObject lookupAttribute(final T instance, final JanitorScriptProcess process) throws JanitorRuntimeException;
}
