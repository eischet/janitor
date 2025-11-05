package com.eischet.dbxs;

import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface ValueSource<T> {
    @Nullable T getValue();
}
