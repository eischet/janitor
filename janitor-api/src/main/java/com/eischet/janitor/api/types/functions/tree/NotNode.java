// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.types.functions.tree;

import com.eischet.janitor.api.scopes.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A logical negation, e.g. {@code not a} or {@code !a}.
 * @param location where
 * @param operand the negated expression
 */
public record NotNode(@Nullable Location location, @NotNull ExpressionNode operand) implements ExpressionNode {
}
