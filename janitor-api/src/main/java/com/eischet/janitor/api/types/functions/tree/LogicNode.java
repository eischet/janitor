// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.types.functions.tree;

import com.eischet.janitor.api.scopes.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A logical combination, e.g. {@code a and b} or {@code a || b}.
 * @param location where
 * @param operator the operator
 * @param left the left operand
 * @param right the right operand
 */
public record LogicNode(@Nullable Location location, @NotNull LogicOperator operator,
                        @NotNull ExpressionNode left, @NotNull ExpressionNode right) implements ExpressionNode {
}
