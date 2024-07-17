package com.eischet.janitor.toolbox.json.api;

public interface JsonWriter {
    void writeJson(JsonOutputStream producer) throws JsonException;
}
