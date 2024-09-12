package com.eischet.janitor.api.types.dispatch;

import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface JsonSupportDelegateWrite<U> {
    void write(final @NotNull JsonOutputStream stream, final @NotNull U object) throws JsonException;
}
