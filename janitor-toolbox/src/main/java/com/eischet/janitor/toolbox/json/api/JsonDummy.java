package com.eischet.janitor.toolbox.json.api;

public class JsonDummy implements JsonWriter {

    @Override
    public void writeJson(final JsonOutputStream producer) throws JsonException {
        producer.nullValue();
    }

}
