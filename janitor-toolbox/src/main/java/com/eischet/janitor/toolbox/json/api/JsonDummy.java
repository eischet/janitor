package com.eischet.janitor.toolbox.json.api;

/** A JSON writer that always writes {@code null}. */
public class JsonDummy implements JsonWriter {

    @Override
    public void writeJson(final JsonOutputStream producer) throws JsonException {
        producer.nullValue();
    }

}
