// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.types.functions.tree;

import com.eischet.janitor.api.scopes.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A reference to a parameter of the function, e.g. {@code item} in {@code item -> item.code == "FOO"}.
 * @param location where
 * @param name the parameter's name
 * @param index the parameter's position, starting at 0
 */
public record ParameterNode(@Nullable Location location, @NotNull String name, int index) implements ExpressionNode {
}
