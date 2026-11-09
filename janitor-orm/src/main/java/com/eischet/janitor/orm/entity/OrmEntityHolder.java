// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.entity;

import org.jetbrains.annotations.NotNull;

/**
 * A script value that stands for an entity without being one, e.g. a wrapper that combines an entity with additional
 * columns from a query. Where only the entity itself matters, like a value in a filter, which compares references by
 * the ID of the entity, the holder is replaced by the entity it holds.
 */
public interface OrmEntityHolder {
    /**
     * Returns the entity this value stands for.
     * @return the entity
     */
    @NotNull OrmEntity getOrmEntity();
}
