package com.eischet.janitor.api.types.interop;

import com.eischet.janitor.api.errors.glue.JanitorGlueException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface NullableGetter<INSTANCE, PROPERTY> {
    @Nullable PROPERTY get(@NotNull INSTANCE instance) throws JanitorGlueException;
}
