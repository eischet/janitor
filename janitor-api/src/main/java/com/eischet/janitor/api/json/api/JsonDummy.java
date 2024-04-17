package com.eischet.janitor.api.json.api;

public class JsonDummy implements JsonWriter {

    @Override
    public void writeJson(final JsonOutputStream producer) throws JsonException {
        producer.nullValue();
    }

}
