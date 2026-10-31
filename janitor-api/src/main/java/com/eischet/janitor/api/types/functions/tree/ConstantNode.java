// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.types.functions.tree;

import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.types.JanitorObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A literal with a fixed value, e.g. a string, number, boolean, date or {@code null}.
 * @param location where
 * @param value the value
 */
public record ConstantNode(@Nullable Location location, @NotNull JanitorObject value) implements ExpressionNode {
}
