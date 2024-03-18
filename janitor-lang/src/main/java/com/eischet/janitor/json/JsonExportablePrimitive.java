package com.eischet.janitor.json;

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
    void writeJson(JsonProducer producer) throws JsonException;
}
