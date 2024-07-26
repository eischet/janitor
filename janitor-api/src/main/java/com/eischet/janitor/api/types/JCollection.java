package com.eischet.janitor.api.types;

import com.eischet.janitor.api.JanitorEnvironment;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonInputStream;

/**
 * Static helpers for parsing collections from JSON.
 * TODO: this should go into the lang module, as it's not really part of the API.
 */
public abstract class JCollection {

    public static JanitorObject parseJsonValue(JsonInputStream reader, final JanitorEnvironment env) throws JsonException {
        return switch (reader.peek()) {
            case BEGIN_ARRAY -> env.getBuiltins().list().parseJson(reader, env);
            case END_ARRAY -> throw new JsonException("Unexpected end of array at " + reader.getPath());
            case BEGIN_OBJECT -> env.getBuiltins().map().parseJson(reader, env);
            case END_OBJECT -> throw new JsonException("Unexpected end of object at " + reader.getPath());
            case NAME -> throw new JsonException("Unexpected name array at " + reader.getPath());
            case STRING -> env.getBuiltins().nullableString(reader.nextString());
            case NUMBER -> JFloat.of(reader.nextDouble());
            case BOOLEAN -> JBool.of(reader.nextBoolean());
            case NULL -> {
                reader.nextNullObject();
                yield JNull.NULL;
            }
            case END_DOCUMENT -> throw new JsonException("Unexpected end of document at " + reader.getPath());
        };
    }

}
