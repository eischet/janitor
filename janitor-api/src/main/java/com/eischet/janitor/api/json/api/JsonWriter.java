package com.eischet.janitor.api.json.api;

public interface JsonWriter {
    void writeJson(JsonOutputStream producer) throws JsonException;
}
