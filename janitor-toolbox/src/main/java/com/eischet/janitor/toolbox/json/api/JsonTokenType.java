package com.eischet.janitor.toolbox.json.api;

/** The types of tokens that a {@link JsonInputStream} can encounter. */
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
