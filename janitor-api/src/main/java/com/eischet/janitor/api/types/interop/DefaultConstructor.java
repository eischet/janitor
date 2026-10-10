package com.eischet.janitor.api.types.interop;

import org.jetbrains.annotations.NotNull;

/**
 * Creates new instances of a type without any arguments.
 * @param <T> the type to create
 */
@FunctionalInterface
public interface DefaultConstructor<T> {
    @NotNull T create();
}
