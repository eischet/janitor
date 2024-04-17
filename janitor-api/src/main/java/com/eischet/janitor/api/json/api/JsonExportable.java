package com.eischet.janitor.api.json.api;

import com.eischet.janitor.api.JanitorEnvironment;

public interface JsonExportable {

    boolean isList();
    boolean isObject();
    boolean isValue();
    boolean isDefaultOrEmpty();

    void writeJson(JsonOutputStream producer) throws JsonException;


    default String exportToJson(final JanitorEnvironment environment) throws JsonException {
        return environment.writeJson(this::writeJson);
    }


}
