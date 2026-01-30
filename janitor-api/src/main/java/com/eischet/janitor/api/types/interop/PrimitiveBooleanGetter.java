package com.eischet.janitor.api.types.interop;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface PrimitiveBooleanGetter<INSTANCE> {
    boolean get(@NotNull INSTANCE instance);
}
