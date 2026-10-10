// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

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
