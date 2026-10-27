// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.rest;

import com.eischet.janitor.api.types.dispatch.DispatchTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

/**
 * The records of a join table, seen from one of the two entities that they connect (see {@link RestLink}): {@code /users/{id}/groups} lists
 * the join records of a user, {@code /users/{id}/groups/{groupId}} is the one that connects it to a group.
 *
 * @param path       the path below the parent, e.g. "groups"
 * @param parent     the entity that the links belong to
 * @param other      the entity at the other end of the links
 * @param className  the simple name of the join record class, which names the schema in the API description
 * @param table      the dispatch table of the join records
 * @param fields     the properties of the join records that the API exposes; the two references are read-only, since the path says what they are
 * @param fromField  the name of the property that refers to the parent
 * @param toField    the name of the property that refers to the other entity
 * @param fromColumn the column of the reference to the parent
 * @param toColumn   the column of the reference to the other entity
 * @param operations the operations that the API offers: listing and getting always, {@link RestOperation#CREATE} (PUT) and {@link RestOperation#DELETE} if the join table allows them; only listing if its primary key has more than the two columns
 */
public record ApiLink(@NotNull String path,
                      @NotNull ApiEntity parent,
                      @NotNull ApiEntity other,
                      @NotNull String className,
                      @NotNull DispatchTable<?> table,
                      @NotNull List<ApiField> fields,
                      @NotNull String fromField,
                      @NotNull String toField,
                      @NotNull String fromColumn,
                      @NotNull String toColumn,
                      @NotNull Set<RestOperation> operations) implements ApiFields {

    /**
     * @param operation an operation
     * @return true if the API offers it for this link
     */
    public boolean allows(final @NotNull RestOperation operation) {
        return operations.contains(operation);
    }

    /**
     * @param name the name of a property
     * @return the property, or null if the API does not expose such a property
     */
    public @Nullable ApiField field(final @NotNull String name) {
        for (final ApiField field : fields) {
            if (field.name().equals(name)) {
                return field;
            }
        }
        return null;
    }
}
