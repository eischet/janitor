package com.eischet.janitor.json;

public class JsonDummy implements JsonWriter {

    @Override
    public void writeJson(final JsonProducer producer) throws JsonException {
        producer.nullValue();
    }

}
