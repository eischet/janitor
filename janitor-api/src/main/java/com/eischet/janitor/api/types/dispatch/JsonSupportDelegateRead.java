package com.eischet.janitor.api.types.dispatch;

import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonInputStream;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface JsonSupportDelegateRead<U> {
    @NotNull U read(final @NotNull JsonInputStream stream) throws JsonException;
}
