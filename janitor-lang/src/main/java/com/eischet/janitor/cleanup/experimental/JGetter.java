package com.eischet.janitor.cleanup.experimental;

import com.eischet.janitor.cleanup.api.api.types.JanitorObject;

@FunctionalInterface
public interface JGetter<T> {
    JanitorObject getPropertyValue(T instance);
}
