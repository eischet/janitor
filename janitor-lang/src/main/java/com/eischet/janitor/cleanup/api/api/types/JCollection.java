package com.eischet.janitor.cleanup.api.api.types;

import com.eischet.janitor.cleanup.runtime.JanitorScript;
import com.eischet.janitor.api.json.JsonInputStream;
import com.eischet.janitor.api.json.JsonException;

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
            case NULL -> JanitorScript.returnNullAndIgnore(reader.nextNullObject());
            case END_DOCUMENT -> throw new JsonException("Unexpected end of document at " + reader.getPath());
        };

    }
}
