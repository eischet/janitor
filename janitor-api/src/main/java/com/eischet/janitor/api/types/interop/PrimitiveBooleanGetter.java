// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.types.interop;

import org.jetbrains.annotations.NotNull;

/**
 * Reads a primitive {@code boolean} property from an instance.
 * @param <INSTANCE> the type of the object that owns the property
 */
@FunctionalInterface
public interface PrimitiveBooleanGetter<INSTANCE> {
    /**
     * Reads the property.
     * @param instance the object
     * @return the value
     */
    boolean get(@NotNull INSTANCE instance);
}
