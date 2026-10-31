// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.types.functions.tree;

import com.eischet.janitor.api.scopes.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An arithmetic operation, e.g. {@code limit + 1} or {@code today - 3d}.
 * @param location where
 * @param operator the operator
 * @param left the left operand
 * @param right the right operand
 */
public record ArithmeticNode(@Nullable Location location, @NotNull ArithmeticOperator operator,
                             @NotNull ExpressionNode left, @NotNull ExpressionNode right) implements ExpressionNode {
}
