package com.eischet.janitor.json;

public interface JsonExportableObject extends JsonExportable {
    @Override
    default boolean isList() {
        return false;
    }

    @Override
    default boolean isObject() {
        return true;
    }

    @Override
    default boolean isValue() {
        return false;
    }
}
