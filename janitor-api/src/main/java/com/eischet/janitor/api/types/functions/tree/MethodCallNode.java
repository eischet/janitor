// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.types.functions.tree;

import com.eischet.janitor.api.scopes.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A method call, e.g. {@code item.name.startsWith("A")}.
 * @param location where
 * @param target the object
 * @param name the method's name
 * @param arguments the arguments
 * @param guarded true for {@code ?.}
 */
public record MethodCallNode(@Nullable Location location, @NotNull ExpressionNode target, @NotNull String name,
                             @NotNull List<CallArgument> arguments, boolean guarded) implements ExpressionNode {
    public MethodCallNode {
        arguments = List.copyOf(arguments);
    }
}
