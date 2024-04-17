package com.eischet.janitor.api.types;

import com.eischet.janitor.api.json.api.JsonException;
import com.eischet.janitor.api.json.api.JsonExportablePrimitive;
import com.eischet.janitor.api.json.api.JsonOutputStream;
import org.jetbrains.annotations.NotNull;

public class JNull implements JanitorObject, JsonExportablePrimitive {
    public static final JNull NULL = new JNull();

    @Override
    public String toString() {
        return "{null}";
    }

    public JNull() {
    }

    @Override
    public Object janitorGetHostValue() {
        return null;
    }

    @Override
    public String janitorToString() {
        return "null";
    }

    @Override
    public boolean janitorIsTrue() {
        return false;
    }

    @Override
    public @NotNull String janitorClassName() {
        return "null";
    }

    @Override
    public boolean isDefaultOrEmpty() {
        return true;
    }

    @Override
    public void writeJson(final JsonOutputStream producer) throws JsonException {
        producer.nullValue();
    }

}
