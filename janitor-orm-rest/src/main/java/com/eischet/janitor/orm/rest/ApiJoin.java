// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.rest;

import com.eischet.janitor.api.types.dispatch.DispatchTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

/**
 * The records of a join table at a path of their own (see {@link RestMetaData#JOIN_PATH}), e.g. {@code /person-items}: listed by any part of
 * their key ({@code GET /person-items?person=ann}), created or changed by posting a whole record ({@code POST /person-items}, a merge), and
 * deleted by naming their whole key ({@code DELETE /person-items?person=ann&item=pc1&reason=owner}). Unlike links ({@link ApiLink}), this works
 * for join tables whose key has more than two columns, e.g. a reason.
 *
 * @param path       the path, e.g. "person-items"
 * @param className  the simple name of the join record class, which names the schema in the API description
 * @param table      the dispatch table of the join records
 * @param fields     the properties of the join records that the API exposes
 * @param keyFields  the names of the properties that make up the primary key, in its order
 * @param operations the operations that the API offers: {@link RestOperation#LIST}, {@link RestOperation#CREATE} (a merge) and
 *                   {@link RestOperation#DELETE}
 */
public record ApiJoin(@NotNull String path,
                      @NotNull String className,
                      @NotNull DispatchTable<?> table,
                      @NotNull List<ApiField> fields,
                      @NotNull List<String> keyFields,
                      @NotNull Set<RestOperation> operations) implements ApiFields {

    /**
     * @param operation an operation
     * @return true if the API offers it for these join records
     */
    public boolean allows(final @NotNull RestOperation operation) {
        return operations.contains(operation);
    }

    @Override
    public @Nullable ApiField field(final @NotNull String name) {
        for (final ApiField field : fields) {
            if (field.name().equals(name)) {
                return field;
            }
        }
        return null;
    }
}
