package com.eischet.janitor.api.types;

import com.eischet.janitor.api.json.api.JsonException;
import com.eischet.janitor.api.json.api.JsonInputStream;

public abstract class JCollection {

    public static JanitorObject parseJsonValue(JsonInputStream reader) throws JsonException {
        return switch (reader.peek()) {
            case BEGIN_ARRAY -> new JList().parseJson(reader);
            case END_ARRAY -> throw new JsonException("Unexpected end of array at " + reader.getPath());
            case BEGIN_OBJECT -> new JMap().parseJson(reader);
            case END_OBJECT -> throw new JsonException("Unexpected end of object at " + reader.getPath());
            case NAME -> throw new JsonException("Unexpected name array at " + reader.getPath());
            case STRING -> JString.ofNullable(reader.nextString());
            case NUMBER -> JFloat.of(reader.nextDouble());
            case BOOLEAN -> JBool.of(reader.nextBoolean());
            case NULL -> returnNullAndIgnore(reader.nextNullObject());
            case END_DOCUMENT -> throw new JsonException("Unexpected end of document at " + reader.getPath());
        };
    }

    private static JNull returnNullAndIgnore(final Object ignored) {
        return JNull.NULL;
    }

}
