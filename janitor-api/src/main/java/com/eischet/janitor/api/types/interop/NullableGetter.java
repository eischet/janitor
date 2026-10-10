package com.eischet.janitor.api.types.interop;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Reads a property that may be null from an instance.
 * @param <INSTANCE> the type of the object that owns the property
 * @param <PROPERTY> the type of the property
 */
@FunctionalInterface
public interface NullableGetter<INSTANCE, PROPERTY> {
    @Nullable PROPERTY get(@NotNull INSTANCE instance) throws Exception;

    /** Boxes a primitive getter into a {@link NullableGetter}, symmetric to {@link NullableSetter#guard}. */
    static <T> NullableGetter<T, Integer> of(final PrimitiveIntGetter<T> getter) {
        return getter::get;
    }

    /** Boxes a primitive getter into a {@link NullableGetter}, symmetric to {@link NullableSetter#guard}. */
    static <T> NullableGetter<T, Long> of(final PrimitiveLongGetter<T> getter) {
        return getter::get;
    }

    /** Boxes a primitive getter into a {@link NullableGetter}, symmetric to {@link NullableSetter#guard}. */
    static <T> NullableGetter<T, Double> of(final PrimitiveDoubleGetter<T> getter) {
        return getter::get;
    }

    /** Boxes a primitive getter into a {@link NullableGetter}, symmetric to {@link NullableSetter#guard}. */
    static <T> NullableGetter<T, Boolean> of(final PrimitiveBooleanGetter<T> getter) {
        return getter::get;
    }
}
