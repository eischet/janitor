// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.rest;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Chooses the part of a collection that an API offers (see {@link OrmApiModel#of(com.eischet.janitor.orm.dao.OrmDaoCollection, ApiSelection)}):
 * which of the exposed entities, and which operations each of them offers. Several APIs can serve different parts of the same database
 * this way, while {@link RestMetaData#OPERATIONS} is the same for all of them.
 */
@FunctionalInterface
public interface ApiSelection {

    /**
     * @param className the entity's class name, as the collection knows it
     * @return the operations the entity offers, or null to leave it out
     */
    @Nullable RestOperations operationsFor(@NotNull String className);

}
