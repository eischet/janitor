package com.eischet.janitor.json;

import com.eischet.janitor.json.GsonProducer;
import com.eischet.janitor.json.JsonException;
import com.eischet.janitor.json.JsonExportControls;
import com.eischet.janitor.json.JsonProducer;

public interface JsonExportable {

    boolean isList();
    boolean isObject();
    boolean isValue();
    boolean isDefaultOrEmpty();

    void writeJson(JsonProducer producer) throws JsonException;


    default String exportToJson() throws JsonException {
        final GsonProducer.GsonStringOut out = new GsonProducer.GsonStringOut(JsonExportControls.standard());
        writeJson(out);
        return out.getString();

    }


}
