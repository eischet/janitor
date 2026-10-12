// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.rest;

import com.eischet.janitor.orm.sql.ColumnTypeHint;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * One property of an entity as it appears in the API.
 *
 * @param name             the name of the property, which is also its name in JSON and in filters
 * @param column           the database column
 * @param columnType       the type of the column
 * @param nullable         whether the value can be null
 * @param required         whether the value is always present in JSON written by the API
 * @param readOnly         whether clients cannot set the value: ids, versions, modification dates and properties without a setter
 * @param filterable       whether lists can be filtered by this property
 * @param sortable         whether lists can be sorted by this property
 * @param maxLength        the maximum length of a text, if known
 * @param precision        the number of digits of a decimal, if known
 * @param scale            the number of digits after the decimal point of a decimal, if known
 * @param referencedEntity the class name of the referenced entity if this is a reference, otherwise null
 * @param referenceByKey   for references: true if the reference is the target's key, false if it is the target's id (targets without key)
 * @param description      a description of the property, if known
 * @param example          an example value as JSON text, if known
 */
public record ApiField(@NotNull String name,
                       @NotNull String column,
                       @NotNull ColumnTypeHint columnType,
                       boolean nullable,
                       boolean required,
                       boolean readOnly,
                       boolean filterable,
                       boolean sortable,
                       @Nullable Integer maxLength,
                       @Nullable Integer precision,
                       @Nullable Integer scale,
                       @Nullable String referencedEntity,
                       boolean referenceByKey,
                       @Nullable String description,
                       @Nullable String example) {

    /** @return true if this property refers to another entity */
    public boolean isReference() {
        return referencedEntity != null;
    }

    /** @return the JSON schema type of the value: string, integer, number or boolean */
    public @NotNull String jsonType() {
        if (isReference()) {
            return referenceByKey ? "string" : "integer";
        }
        return switch (columnType) {
            case INT -> "integer";
            case DECIMAL -> "number";
            case BIT, BOOL_CHAR -> "boolean";
            case VARCHAR, NVARCHAR, CLOB, NCLOB, DATE, DATETIME -> "string";
        };
    }

    /** @return the JSON schema format of the value, or null if there is none */
    public @Nullable String jsonFormat() {
        if (isReference()) {
            return referenceByKey ? null : "int64";
        }
        return switch (columnType) {
            case INT -> "int64";
            case DATE -> "date";
            case DATETIME -> "date-time";
            default -> null;
        };
    }
}
