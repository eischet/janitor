// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.rest;

import com.eischet.dbxs.DatabaseConnection;
import com.eischet.dbxs.exceptions.DatabaseError;
import com.eischet.janitor.orm.entity.OrmEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * How the host creates entities of a kind itself, instead of the API reading the body into a new entity and inserting it (see
 * {@link RestPolicy#creation}). For entities whose creation is more than an insert, e.g. a booking that changes other records as well, or that
 * takes other input than the entity's properties. The policy still decides whether a request may create the entity at all ({@link RestPolicy#permits}),
 * and the parent of a sub-resource has to be visible.
 *
 * @param <U> the type of the user
 */
public interface RestCreation<U> {

    /**
     * @return the JSON schema of the request body, for the API description, as a tree of maps and lists (see {@link JsonTree})
     */
    @NotNull Map<String, Object> bodySchema();

    /**
     * Creates the entity, in the host's own way. The new entity is returned as the API shows it, after the policy had a look at it
     * ({@link RestPolicy#reveal}).
     *
     * @param user   the user of the request, or null if there is none
     * @param parent the entity that the path names for a request below it (a sub-resource), as the caller may see it; null for a request to
     *               the entity's own path
     * @param body   the body of the request as text, or null if there is none
     * @param conn   the connection of the transaction of the request
     * @return the entity that was created, with its id
     * @throws RestRejection to refuse the request, e.g. with 422 for a body that cannot be used
     * @throws DatabaseError on database errors
     */
    @NotNull OrmEntity create(@Nullable U user, @Nullable OrmEntity parent, @Nullable String body, @NotNull DatabaseConnection conn) throws DatabaseError;
}
