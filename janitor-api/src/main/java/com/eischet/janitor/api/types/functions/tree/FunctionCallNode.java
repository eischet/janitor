// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.types.functions.tree;

import com.eischet.janitor.api.scopes.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A call of a function by name, e.g. {@code max(a, b)}.
 * @param location where
 * @param name the function's name
 * @param arguments the arguments
 */
public record FunctionCallNode(@Nullable Location location, @NotNull String name, @NotNull List<CallArgument> arguments) implements ExpressionNode {
    public FunctionCallNode {
        arguments = List.copyOf(arguments);
    }
}
