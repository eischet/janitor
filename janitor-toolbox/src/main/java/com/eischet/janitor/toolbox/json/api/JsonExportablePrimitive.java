package com.eischet.janitor.toolbox.json.api;

/** A {@link JsonExportable} that is exported as a single JSON value, e.g. a number or a string. */
public interface JsonExportablePrimitive extends JsonExportable {
    @Override
    default boolean isList() {
        return false;
    }

    @Override
    default boolean isObject() {
        return false;
    }

    @Override
    default boolean isValue() {
        return true;
    }

    @Override
    boolean isDefaultOrEmpty();

    @Override
    void writeJson(JsonOutputStream producer) throws JsonException;
}
