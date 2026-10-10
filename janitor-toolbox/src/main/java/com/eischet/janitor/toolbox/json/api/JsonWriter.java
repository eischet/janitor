package com.eischet.janitor.toolbox.json.api;

/** Implemented by objects that can write themselves to a {@link JsonOutputStream}. */
public interface JsonWriter {
    /**
     * Writes this object to a JSON stream.
     * @param producer the target
     * @throws JsonException on errors
     */
    void writeJson(JsonOutputStream producer) throws JsonException;
}
