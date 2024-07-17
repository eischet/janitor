package com.eischet.janitor.toolbox.json.api;

public interface JsonOutputSupport {
    String writeJson(JsonWriter writer) throws JsonException;
}
