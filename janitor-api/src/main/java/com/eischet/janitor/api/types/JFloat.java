package com.eischet.janitor.api.types;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorNameException;
import com.eischet.janitor.api.json.api.JsonException;
import com.eischet.janitor.api.json.api.JsonExportablePrimitive;
import com.eischet.janitor.api.json.api.JsonOutputStream;
import com.eischet.janitor.api.traits.JConstant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class JFloat implements JConstant, JsonExportablePrimitive {

    private final double number;

    public JFloat(final double number) {
        this.number = number;
    }

    public static JanitorObject ofNullable(final Double value) {
        if (value == null) {
            return JNull.NULL;
        } else {
            return JFloat.of(value);
        }
    }

    @Override
    public Double janitorGetHostValue() {
        return number;
    }

    @Override
    public String toString() {
        return String.valueOf(number);
    }

    @Override
    public boolean janitorIsTrue() {
        return number != 0.0;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final JFloat that = (JFloat) o;
        return number == that.number;
    }

    @Override
    public int hashCode() {
        return Objects.hash(number);
    }

    public static JFloat of(final double value) {
        return new JFloat(value);
    }

    public static JFloat of(final long value) {
        return new JFloat(value);
    }

    public static JFloat of(final int value) {
        return new JFloat(value);
    }

    public double getValue() {
        return number;
    }

    public double toDouble() {
        return number;
    }

    @Override
    public @Nullable JanitorObject janitorGetAttribute(final JanitorScriptProcess runningScript, final String name, final boolean required) throws JanitorNameException {
        if (Objects.equals(name, "int")) {
            return JInt.of((long) number);
        }
        return JConstant.super.janitorGetAttribute(runningScript, name, required);
    }

    @Override
    public @NotNull String janitorClassName() {
        return "float";
    }

    @Override
    public boolean isDefaultOrEmpty() {
        return number == 0.0d;
    }

    @Override
    public void writeJson(final JsonOutputStream producer) throws JsonException {
        producer.value(number);
    }
}
