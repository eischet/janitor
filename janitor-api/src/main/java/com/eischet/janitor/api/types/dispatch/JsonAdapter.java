// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.types.dispatch;

import com.eischet.janitor.toolbox.json.api.JsonInputStream;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;

/**
 * Reads and writes a single property of an object from and to JSON.
 * @param <T> the type of the object that owns the property
 */
public interface JsonAdapter<T> {
    /**
     * Writes the property of an object.
     * @param stream the target
     * @param instance the object
     * @throws Exception on errors
     */
    void write(final JsonOutputStream stream, final T instance) throws Exception;

    /**
     * Reads the property of an object.
     * @param stream the source
     * @param instance the object
     * @throws Exception on errors
     */
    void read(final JsonInputStream stream, final T instance) throws Exception;

    /**
     * Checks whether the property has its default value, so that it may be left out.
     * @param instance the object
     * @return true if the property has the default value
     * @throws Exception on errors
     */
    boolean isDefault(final T instance) throws Exception;
}
