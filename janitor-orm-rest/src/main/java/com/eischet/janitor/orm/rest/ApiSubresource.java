// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.rest;

import org.jetbrains.annotations.NotNull;

/**
 * The entities of one type that belong to an entity of another type, through a reference property (see {@link RestMetaData#SUB_PATH}):
 * {@code /folders/{id}/docs} lists the docs whose {@code folder} is the folder with that id. Individual docs are not addressed through the
 * parent; they stay at {@code /docs/{id}}.
 *
 * @param path   the path below the parent, e.g. "docs"
 * @param parent the entity that the path is below
 * @param child  the entities that are listed (and created) there
 * @param field  the property of the child that points to the parent: a reference, or the id of the parent as a number
 */
public record ApiSubresource(@NotNull String path, @NotNull ApiEntity parent, @NotNull ApiEntity child, @NotNull ApiField field) {
}
