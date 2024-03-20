package com.eischet.janitor.cleanup.experimental.fresh;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A JClass instance in the Janitor language runtime.
 */
public interface JObject {
    JClass jGetClass();
    default JObject jCall(@Nullable JObject self, @NotNull JCallArgs args) {
        return null;
    }
}
