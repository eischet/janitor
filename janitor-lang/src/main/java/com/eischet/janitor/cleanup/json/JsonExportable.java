package com.eischet.janitor.cleanup.json;

import com.eischet.janitor.api.json.JsonException;

public interface JsonExportable {

    boolean isList();
    boolean isObject();
    boolean isValue();
    boolean isDefaultOrEmpty();

    void writeJson(JsonOutputStream producer) throws JsonException;


    default String exportToJson() throws JsonException {
        final GsonOutputStream.GsonStringOut out = new GsonOutputStream.GsonStringOut(JsonExportControls.standard());
        writeJson(out);
        return out.getString();

    }


}
