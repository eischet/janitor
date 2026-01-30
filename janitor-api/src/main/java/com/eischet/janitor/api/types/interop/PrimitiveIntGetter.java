package com.eischet.janitor.api.types.interop;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface PrimitiveIntGetter<INSTANCE> {
    int get(@NotNull INSTANCE instance);
}
