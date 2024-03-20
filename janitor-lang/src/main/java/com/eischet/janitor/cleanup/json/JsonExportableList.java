package com.eischet.janitor.cleanup.json;

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
