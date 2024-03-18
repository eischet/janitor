package com.eischet.janitor.experimental;

import com.eischet.janitor.api.types.JanitorObject;

@FunctionalInterface
public interface JSetter<T> {
    void setPropertyValue(T instance, JanitorObject value);
}
