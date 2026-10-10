package com.eischet.dbxs;

import org.jetbrains.annotations.Nullable;

/**
 * Supplies a value, which may be null.
 * @param <T> the type of the value
 */
@FunctionalInterface
public interface ValueSource<T> {
    @Nullable T getValue();
}
