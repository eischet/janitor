package com.eischet.janitor.api.types.interop;

import org.jetbrains.annotations.NotNull;

public interface NotNullGetter<INSTANCE, PROPERTY> {
    @NotNull PROPERTY get(@NotNull INSTANCE instance) throws Exception;
}
