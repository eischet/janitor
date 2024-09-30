package com.eischet.janitor.toolbox.json.api;

/**
 * Interface for classes that support reading their own object properties from a JSON stream.
 *
 * This works with the dispatch tables to support reading object properties.
 */
public interface JsonReader {
    void readJson(final JsonInputStream stream) throws JsonException;
}
