package com.eischet.janitor.api.types.interop;

import org.jetbrains.annotations.NotNull;

/**
 * Reads a property that is never null from an instance.
 * @param <INSTANCE> the type of the object that owns the property
 * @param <PROPERTY> the type of the property
 */
public interface NotNullGetter<INSTANCE, PROPERTY> {
    @NotNull PROPERTY get(@NotNull INSTANCE instance) throws Exception;
}
