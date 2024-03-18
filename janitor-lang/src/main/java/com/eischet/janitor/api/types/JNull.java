package com.eischet.janitor.api.types;

import com.eischet.janitor.json.JsonExportablePrimitive;
import com.eischet.janitor.json.JsonException;
import com.eischet.janitor.json.JsonProducer;
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
    public void writeJson(final JsonProducer producer) throws JsonException {
        producer.nullValue();
    }

}
