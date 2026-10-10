// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.types.interop;

import org.jetbrains.annotations.NotNull;

/**
 * Writes a primitive {@code int} property to an instance.
 * @param <INSTANCE> the type of the object that owns the property
 */
@FunctionalInterface
public interface PrimitiveIntSetter<INSTANCE> {
    /**
     * Sets the property.
     * @param instance the object
     * @param value the new value
     */
    void set(@NotNull INSTANCE instance, int value);
}
