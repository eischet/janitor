package com.eischet.janitor.api.types.interop;

@FunctionalInterface
public interface PrimitiveLongSetter<INSTANCE> {
    void set(INSTANCE instance, long value);


}
