package com.eischet.janitor.api.types.interop;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface PrimitiveLongGetter<INSTANCE> {
    long get(@NotNull INSTANCE instance);
}
