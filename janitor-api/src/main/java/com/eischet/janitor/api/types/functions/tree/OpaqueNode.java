// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.types.functions.tree;

import com.eischet.janitor.api.scopes.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Any other kind of expression, e.g. {@code today}, a ternary, or an index expression. Its parts are not available.
 * @param location where
 * @param kind a description of the expression, for error messages, e.g. "TodayLiteral"
 * @param mayUseParameters false if it is known not to use the parameters, so that {@link JInspectableFunction#evaluate} can compute it
 */
public record OpaqueNode(@Nullable Location location, @NotNull String kind, boolean mayUseParameters) implements ExpressionNode {
}
