// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.types.functions.tree;

import com.eischet.janitor.api.scopes.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A list literal, e.g. {@code ["A", "B"]}.
 * @param location where
 * @param elements the elements
 */
public record ListNode(@Nullable Location location, @NotNull List<ExpressionNode> elements) implements ExpressionNode {
    public ListNode {
        elements = List.copyOf(elements);
    }
}
