// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.toolbox.json.api;

/**
 * JSON types, as used in the context of JSON schema.
 */
public enum JsonType {
    ARRAY,
    OBJECT,
    STRING,
    NUMBER,
    BOOLEAN,
    NULL;

    /**
     * @return the name of this type in JSON Schema
     */
    public String getJsonSchemaType() {
        return switch (this) {
            case ARRAY -> "array";
            case OBJECT -> "object";
            case STRING -> "string";
            case NUMBER -> "number";
            case BOOLEAN -> "boolean";
            case NULL -> "null";
        };
    }
}
