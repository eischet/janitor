package com.eischet.janitor.api.json;

/**
 * Types of tokens that can be found in a JSON stream.
 *
 * <p>These values have, more ore less, been copied from the Gson library, though they really come from the JSON
 * specification, logically.</p>
 */
public enum JsonTokenType {
    BEGIN_ARRAY,
    END_ARRAY,
    BEGIN_OBJECT,
    END_OBJECT,
    NAME,
    STRING,
    NUMBER,
    BOOLEAN,
    NULL,
    END_DOCUMENT
}
