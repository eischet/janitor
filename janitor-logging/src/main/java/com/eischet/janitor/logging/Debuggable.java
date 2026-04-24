package com.eischet.janitor.logging;

import org.jetbrains.annotations.Nullable;

public interface Debuggable {
    boolean isDebugModeEnabled();
    @Nullable String getDebugEntityName();
}
