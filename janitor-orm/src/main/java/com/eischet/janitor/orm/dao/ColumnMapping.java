// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.dao;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.dispatch.AttributeLookupHandler;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.orm.JanitorOrm;
import com.eischet.janitor.orm.sql.ColumnTypeHint;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Everything a DAO needs to know to read or write one column of an entity, looked up once when the DAO is constructed instead of from the
 * meta-data of the dispatch table for every column of every row.
 *
 * @param column        the name of the column
 * @param field         the name of the entity's field that is mapped to the column
 * @param type          the type of the column
 * @param referenceType the class name of the referenced entity if the column holds a foreign key, otherwise null
 * @param hostNullable  whether the field accepts null values, if specified
 * @param handler       looks up the field's property on an entity
 * @param <T>           the type of the entity
 */
public record ColumnMapping<T extends JanitorObject>(
        @NotNull String column,
        @NotNull String field,
        @NotNull ColumnTypeHint type,
        @Nullable String referenceType,
        @Nullable Boolean hostNullable,
        @NotNull AttributeLookupHandler<T> handler) {

    /**
     * Collects the mapping of a column from the meta-data of the dispatch table.
     * @param table  the dispatch table of the entity
     * @param field  the name of the field
     * @param column the name of the column that the field is mapped to
     * @return the mapping
     * @throws NullPointerException if the field has no property, or no column type, which is a configuration error that is better reported early
     */
    public static <T extends JanitorObject> @NotNull ColumnMapping<T> of(final @NotNull DispatchTable<T> table, final @NotNull String field, final @NotNull String column) {
        return new ColumnMapping<>(
                column,
                field,
                Objects.requireNonNull(table.getMetaData(field, JanitorOrm.MetaData.COLUMN_TYPE), "no column type hint for field '" + field + "' / column '" + column + "'"),
                table.getMetaData(field, Janitor.MetaData.REF),
                table.getMetaData(field, Janitor.MetaData.HOST_NULLABLE),
                Objects.requireNonNull(table.get(field), "no property for field '" + field + "' / column '" + column + "'"));
    }
}
