package com.eischet.janitor.env;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JNull;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonInputStream;

import javax.xml.stream.XMLStreamReader;

/**
 * Static helpers for parsing collections from JSON.
 */
public abstract class JCollection {

    public static JanitorObject parseJsonValue(JsonInputStream reader) throws JsonException {
        return switch (reader.peek()) {
            case BEGIN_ARRAY ->  JListClass.parseJson(Janitor.list(), reader);
            case END_ARRAY -> throw new JsonException("Unexpected end of array at " + reader.getPath());
            case BEGIN_OBJECT -> JMapClass.parseJson(Janitor.map(), reader);
            case END_OBJECT -> throw new JsonException("Unexpected end of object at " + reader.getPath());
            case NAME -> throw new JsonException("Unexpected name array at " + reader.getPath());
            case STRING -> Janitor.nullableString(reader.nextString());
            case NUMBER -> Janitor.numeric(reader.nextDouble());

            case BOOLEAN -> Janitor.toBool(reader.nextBoolean());
            case NULL -> {
                reader.nextNullObject();
                yield JNull.NULL;
            }
            case END_DOCUMENT -> throw new JsonException("Unexpected end of document at " + reader.getPath());
        };
    }


}
