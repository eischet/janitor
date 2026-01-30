package com.eischet.janitor.api.types.interop;

import com.eischet.janitor.api.errors.glue.JanitorGlueException;

@FunctionalInterface
public interface PrimitiveDoubleGetter<INSTANCE> {
    double get(INSTANCE instance) throws JanitorGlueException;
}
