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
