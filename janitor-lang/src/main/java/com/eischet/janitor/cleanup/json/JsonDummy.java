package com.eischet.janitor.cleanup.json;

import com.eischet.janitor.api.json.JsonException;

public class JsonDummy implements JsonWriter {

    @Override
    public void writeJson(final JsonOutputStream producer) throws JsonException {
        producer.nullValue();
    }

}
