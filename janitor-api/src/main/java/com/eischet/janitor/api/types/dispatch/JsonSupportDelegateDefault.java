// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.types.dispatch;

import org.jetbrains.annotations.NotNull;

/**
 * Decides whether a value is the default for its type, in which case it may be omitted from JSON output.
 * @param <U> the type of the value
 */
@FunctionalInterface
public interface JsonSupportDelegateDefault<U> {
    /**
     * @param object the value
     * @return true if the value is the default for its type
     */
    boolean isDefault(final @NotNull U object);
}
