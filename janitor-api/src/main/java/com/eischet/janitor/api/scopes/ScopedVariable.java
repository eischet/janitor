// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.scopes;

import com.eischet.janitor.api.types.JanitorObject;
import org.jetbrains.annotations.NotNull;

/**
 * A variable that a lookup found, with the scope that defines it, e.g. to assign to it there.
 *
 * @param scope    the scope that defines the variable
 * @param variable its value
 */
public record ScopedVariable(@NotNull Scope scope, @NotNull JanitorObject variable) {
}
