package com.eischet.janitor.api.types.interop;

import org.jetbrains.annotations.NotNull;

/**
 * Writes a primitive {@code boolean} property to an instance.
 * @param <INSTANCE> the type of the object that owns the property
 */
@FunctionalInterface
public interface PrimitiveBooleanSetter<INSTANCE> {
    /**
     * Sets the property.
     * @param instance the object
     * @param value the new value
     */
    void set(@NotNull INSTANCE instance, boolean value);
}

