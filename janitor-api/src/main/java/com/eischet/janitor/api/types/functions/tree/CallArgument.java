// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.types.functions.tree;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An argument of a call.
 * @param name the name of a named argument, or null for a positional one
 * @param value the value
 */
public record CallArgument(@Nullable String name, @NotNull ExpressionNode value) {
}
