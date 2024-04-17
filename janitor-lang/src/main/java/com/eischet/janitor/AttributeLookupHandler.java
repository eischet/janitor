package com.eischet.janitor;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.types.JanitorObject;

@FunctionalInterface
public interface AttributeLookupHandler<T extends JanitorObject> {
    JanitorObject lookupAttribute(final JanitorScriptProcess runningScript, final T instance);
}
