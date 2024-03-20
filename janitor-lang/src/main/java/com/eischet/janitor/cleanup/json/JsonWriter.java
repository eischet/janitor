package com.eischet.janitor.cleanup.json;

import com.eischet.janitor.api.json.JsonException;

public interface JsonWriter {
    void writeJson(JsonOutputStream producer) throws JsonException;
}
