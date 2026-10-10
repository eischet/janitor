package com.eischet.janitor.toolbox.i18n;

import org.jetbrains.annotations.Nullable;

/**
 * Helper for debugging translations: receives information about translation lookups and their results.
 * Initially added for the overrides machanism.
 */
public interface LookupInfoReceiver {
    /**
     * Called when a translation override was looked up.
     * @param key the key
     * @param foundValue the value that was found, or null
     */
    void searchOverride(final @Nullable String key, final @Nullable String foundValue);
}
