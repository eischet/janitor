package com.eischet.janitor.logging;

import org.jetbrains.annotations.Nullable;

/** Implemented by objects that can switch on additional debug logging, optionally for a specific entity. */
public interface Debuggable {
    /**
     * @return true if debug mode is enabled
     */
    boolean isDebugModeEnabled();
    @Nullable String getDebugEntityName();
}
