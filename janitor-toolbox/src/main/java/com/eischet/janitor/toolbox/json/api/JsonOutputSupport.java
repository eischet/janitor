package com.eischet.janitor.toolbox.json.api;

/** Creates JSON text from objects that can write themselves. */
public interface JsonOutputSupport {
    /**
     * Writes an object as JSON text.
     * @param writer writes the object
     * @return the JSON text
     * @throws JsonException on errors
     */
    String writeJson(JsonWriter writer) throws JsonException;
}
