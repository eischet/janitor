package com.eischet.janitor.api.types.interop;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface PrimitiveBooleanSetter<INSTANCE> {
    void set(@NotNull INSTANCE instance, boolean value);
}

