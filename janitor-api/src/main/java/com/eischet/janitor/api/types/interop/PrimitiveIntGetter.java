package com.eischet.janitor.api.types.interop;

import org.jetbrains.annotations.NotNull;

/**
 * Reads a primitive {@code int} property from an instance.
 * @param <INSTANCE> the type of the object that owns the property
 */
@FunctionalInterface
public interface PrimitiveIntGetter<INSTANCE> {
    /**
     * Reads the property.
     * @param instance the object
     * @return the value
     */
    int get(@NotNull INSTANCE instance);
}
