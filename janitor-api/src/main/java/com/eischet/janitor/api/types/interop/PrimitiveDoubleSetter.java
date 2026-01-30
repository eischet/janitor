package com.eischet.janitor.api.types.interop;

import com.eischet.janitor.api.errors.glue.JanitorGlueException;

@FunctionalInterface
public interface PrimitiveDoubleSetter<INSTANCE> {
    void set(INSTANCE instance, double value) throws JanitorGlueException;
}
