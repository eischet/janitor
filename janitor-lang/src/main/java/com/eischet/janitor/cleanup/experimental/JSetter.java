package com.eischet.janitor.cleanup.experimental;

import com.eischet.janitor.cleanup.api.api.types.JanitorObject;

@FunctionalInterface
public interface JSetter<T> {
    void setPropertyValue(T instance, JanitorObject value);
}
