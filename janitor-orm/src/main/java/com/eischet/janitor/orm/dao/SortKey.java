// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.dao;

import org.jetbrains.annotations.NotNull;

/**
 * One criterion of a sort order for {@link FilterQuery#orderBy(String)}: the name of an entity <b>field</b> (not a column; the DAO maps it and
 * rejects unknown names), and the direction.
 *
 * @param field      the name of the field
 * @param descending true to sort from high to low
 */
public record SortKey(@NotNull String field, boolean descending) {
}
