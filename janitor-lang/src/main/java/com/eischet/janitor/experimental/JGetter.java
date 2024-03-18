package com.eischet.janitor.experimental;

import com.eischet.janitor.api.types.JanitorObject;

@FunctionalInterface
public interface JGetter<T> {
    JanitorObject getPropertyValue(T instance);
}
