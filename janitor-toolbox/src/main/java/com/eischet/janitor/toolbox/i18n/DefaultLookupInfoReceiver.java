package com.eischet.janitor.toolbox.i18n;

import org.jetbrains.annotations.Nullable;

/**
 * Default implementation of {@link LookupInfoReceiver} that ignores all calls.
 */
public class DefaultLookupInfoReceiver implements LookupInfoReceiver {

    @Override
    public void searchOverride(final @Nullable String key, final @Nullable String foundValue) {
        // ignore
    }
}
