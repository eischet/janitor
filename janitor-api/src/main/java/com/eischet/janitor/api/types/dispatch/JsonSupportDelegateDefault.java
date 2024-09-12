package com.eischet.janitor.api.types.dispatch;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface JsonSupportDelegateDefault<U> {
    boolean isDefault(final @NotNull U object);
}
