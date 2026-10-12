// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.rest;

import org.jetbrains.annotations.NotNull;

/**
 * One direction in which the records of a join table are exposed: below an entity (the one that {@code fromField} refers to) at {@code path},
 * as the records that connect it to other entities (the ones that {@code toField} refers to).
 *
 * @param fromField the name of the property of the join records that refers to the entity that the links belong to
 * @param path      the path below that entity, e.g. {@code "groups"} for {@code /users/{id}/groups}
 * @param toField   the name of the property of the join records that refers to the entity at the other end
 */
public record RestLink(@NotNull String fromField, @NotNull String path, @NotNull String toField) {
}
