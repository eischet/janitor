// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.types.functions.tree;

import com.eischet.janitor.api.scopes.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Access to a member, e.g. {@code item.code}, or {@code item?.code} when guarded.
 * @param location where
 * @param target the object
 * @param name the member's name
 * @param guarded true for {@code ?.}
 */
public record MemberNode(@Nullable Location location, @NotNull ExpressionNode target, @NotNull String name, boolean guarded) implements ExpressionNode {
}
