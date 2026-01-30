package com.eischet.janitor.api.types.interop;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface NullableGetter<INSTANCE, PROPERTY> {
    @Nullable PROPERTY get(@NotNull INSTANCE instance) throws Exception;
}
