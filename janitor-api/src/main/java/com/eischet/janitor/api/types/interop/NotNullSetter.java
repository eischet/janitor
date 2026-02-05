package com.eischet.janitor.api.types.interop;

import org.jetbrains.annotations.NotNull;

public interface NotNullSetter<INSTANCE, PROPERTY> {
    void set(@NotNull INSTANCE instance, @NotNull PROPERTY value) throws Exception;
}
