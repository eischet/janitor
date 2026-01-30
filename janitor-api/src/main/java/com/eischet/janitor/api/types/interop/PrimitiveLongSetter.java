package com.eischet.janitor.api.types.interop;

import com.eischet.janitor.api.errors.glue.JanitorGlueException;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;

@FunctionalInterface
public interface PrimitiveLongSetter<INSTANCE> {
    void set(INSTANCE instance, long value);


}
