package com.eischet.janitor.api.scripting;

import com.eischet.janitor.api.JanitorScriptProcess;
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
     * @param runningScript the running script.
     * @return the attribute
     */
    JanitorObject lookupAttribute(final T instance, final JanitorScriptProcess runningScript);
}
