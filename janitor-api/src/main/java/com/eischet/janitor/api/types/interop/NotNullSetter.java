// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.types.interop;

import org.jetbrains.annotations.NotNull;

/**
 * Writes a property that must not be null to an instance.
 * @param <INSTANCE> the type of the object that owns the property
 * @param <PROPERTY> the type of the property
 */
public interface NotNullSetter<INSTANCE, PROPERTY> {
    /**
     * Sets the property.
     * @param instance the object
     * @param value the new value
     * @throws Exception on errors
     */
    void set(@NotNull INSTANCE instance, @NotNull PROPERTY value) throws Exception;
}
