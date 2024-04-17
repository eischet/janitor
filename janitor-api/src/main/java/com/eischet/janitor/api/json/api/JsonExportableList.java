package com.eischet.janitor.api.json.api;

public interface JsonExportableList extends JsonExportable {
    @Override
    default boolean isList() {
        return true;
    }

    @Override
    default boolean isObject() {
        return false;
    }

    @Override
    default boolean isValue() {
        return false;
    }

}
