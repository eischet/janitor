package com.eischet.janitor.api.types.interop;

/**
 * Writes a primitive {@code long} property to an instance.
 * @param <INSTANCE> the type of the object that owns the property
 */
@FunctionalInterface
public interface PrimitiveLongSetter<INSTANCE> {
    /**
     * Sets the property.
     * @param instance the object
     * @param value the new value
     */
    void set(INSTANCE instance, long value);


}
