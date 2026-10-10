package com.eischet.janitor.api.types.interop;

import com.eischet.janitor.api.errors.glue.JanitorGlueException;

/**
 * Writes a primitive {@code double} property to an instance.
 * @param <INSTANCE> the type of the object that owns the property
 */
@FunctionalInterface
public interface PrimitiveDoubleSetter<INSTANCE> {
    /**
     * Sets the property.
     * @param instance the object
     * @param value the new value
     * @throws JanitorGlueException if the value cannot be set
     */
    void set(INSTANCE instance, double value) throws JanitorGlueException;
}
