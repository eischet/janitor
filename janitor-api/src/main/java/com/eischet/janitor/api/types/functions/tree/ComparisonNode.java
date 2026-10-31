// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.types.functions.tree;

import com.eischet.janitor.api.scopes.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A comparison, e.g. {@code item.code == "FOO"} or {@code item.state in ["A", "B"]}.
 * @param location where
 * @param operator the operator
 * @param left the left operand
 * @param right the right operand
 */
public record ComparisonNode(@Nullable Location location, @NotNull ComparisonOperator operator,
                             @NotNull ExpressionNode left, @NotNull ExpressionNode right) implements ExpressionNode {
}
