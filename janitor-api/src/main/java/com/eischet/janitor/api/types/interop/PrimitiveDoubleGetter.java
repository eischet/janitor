package com.eischet.janitor.api.types.interop;

import com.eischet.janitor.api.errors.glue.JanitorGlueException;

/**
 * Reads a primitive {@code double} property from an instance.
 * @param <INSTANCE> the type of the object that owns the property
 */
@FunctionalInterface
public interface PrimitiveDoubleGetter<INSTANCE> {
    /**
     * Reads the property.
     * @param instance the object
     * @return the value
     * @throws JanitorGlueException if the value cannot be read
     */
    double get(INSTANCE instance) throws JanitorGlueException;
}
