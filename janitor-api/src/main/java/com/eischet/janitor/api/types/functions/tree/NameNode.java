// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.types.functions.tree;

import com.eischet.janitor.api.scopes.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A name that is not a parameter, e.g. a variable the function captured, or a global.
 * Its value comes from {@link JInspectableFunction#evaluate}.
 * @param location where
 * @param name the name
 */
public record NameNode(@Nullable Location location, @NotNull String name) implements ExpressionNode {
}
