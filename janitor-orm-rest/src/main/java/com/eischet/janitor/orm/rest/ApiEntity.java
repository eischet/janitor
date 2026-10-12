// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.rest;

import com.eischet.janitor.orm.entity.OrmEntity;
import com.eischet.janitor.orm.meta.EntityDispatchTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

/**
 * An entity as it appears in the API.
 *
 * @param className     the simple name of the entity class, which names the schema in the API description
 * @param path          the path of the entity, e.g. "articles"
 * @param table         the dispatch table of the entity
 * @param fields        the properties that the API exposes
 * @param idField       the name of the property that holds the id
 * @param keyField      the name of the property that holds the key, or null if the entity has no key
 * @param versionField  the name of the property that holds the version, or null if the entity is not versioned
 * @param modifiedField the name of the property that holds the modification date, or null if there is none
 * @param maxPageSize   the largest page size that clients may ask for
 * @param defaultSort   the default sort order, as in {@link RestMetaData#DEFAULT_SORT}, or null for none
 * @param operations    the operations that the API offers for this entity
 */
public record ApiEntity(@NotNull String className,
                        @NotNull String path,
                        @NotNull EntityDispatchTable<? extends OrmEntity, ?> table,
                        @NotNull List<ApiField> fields,
                        @NotNull String idField,
                        @Nullable String keyField,
                        @Nullable String versionField,
                        @Nullable String modifiedField,
                        int maxPageSize,
                        @Nullable String defaultSort,
                        @NotNull Set<RestOperation> operations) implements ApiFields {

    /**
     * @param operation an operation
     * @return true if the API offers it for this entity
     */
    public boolean allows(final @NotNull RestOperation operation) {
        return operations.contains(operation);
    }

    /** @return true if changing and deleting needs an {@code If-Match} header, see {@link RestMetaData#REQUIRE_IF_MATCH}; only versioned entities can */
    public boolean requiresIfMatch() {
        return versionField != null && Boolean.TRUE.equals(table.getMetaData(RestMetaData.REQUIRE_IF_MATCH));
    }

    /** @return true if entities can be looked up by key */
    public boolean hasKey() {
        return keyField != null;
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
