package com.eischet.janitor.api.types;

import com.eischet.janitor.api.json.api.JsonException;
import com.eischet.janitor.api.json.api.JsonExportablePrimitive;
import com.eischet.janitor.api.json.api.JsonOutputStream;
import com.eischet.janitor.api.traits.JConstant;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class JBool implements JConstant, JsonExportablePrimitive {

    public static final JBool TRUE = new JBool(true);
    public static final JBool FALSE = new JBool(false);
    private final boolean value;

    @Override
    public boolean janitorIsTrue() {
        return value;
    }

    JBool(final boolean value) {
        this.value = value;
    }

    public static JBool of(final boolean value) {
        return value ? TRUE : FALSE;
    }

    public static JBool of(final Boolean value) {
        return value == Boolean.TRUE ? TRUE : FALSE;
    }


    public static JBool map(final boolean value) {
        return value ? TRUE : FALSE;
    }

    @Override
    public @NotNull
    Boolean janitorGetHostValue() {
        return value;
    }

    @Override
    public String janitorToString() {
        return value ? "true" : "false";
    }

    public JBool opposite() {
        return this == TRUE ? FALSE : TRUE;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final JBool that = (JBool) o;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value ? "true" : "false";
    }

    @Override
    public @NotNull String janitorClassName() {
        return "bool";
    }

    @Override
    public boolean isDefaultOrEmpty() {
        return !value;
    }

    @Override
    public void writeJson(final JsonOutputStream producer) throws JsonException {
        producer.value(value);
    }
}
