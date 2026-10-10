// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.types.dispatch;

import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;
import org.jetbrains.annotations.NotNull;

/**
 * Writes a value to a JSON stream.
 * @param <U> the type of the value
 */
@FunctionalInterface
public interface JsonSupportDelegateWrite<U> {
    /**
     * Writes a value.
     * @param stream the target
     * @param object the value
     * @throws JsonException if the value cannot be written
     */
    void write(final @NotNull JsonOutputStream stream, final @NotNull U object) throws JsonException;
}
