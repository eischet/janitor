package com.eischet.janitor.api.types.builtin;

import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonExportablePrimitive;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class JNull implements JanitorObject, JsonExportablePrimitive {
    public static final JNull NULL = new JNull();

    @Override
    public String toString() {
        return "{null}";
    }

    private JNull() {
    }

    @Override
    public @Nullable Object janitorGetHostValue() {
        return null;
    }

    @Override
    public @NotNull String janitorToString() {
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
