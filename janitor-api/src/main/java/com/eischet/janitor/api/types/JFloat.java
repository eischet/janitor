package com.eischet.janitor.api.types;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorNameException;
import com.eischet.janitor.api.traits.JConstant;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonExportablePrimitive;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * A float object, representing a floating-point number.
 */
public class JFloat implements JConstant, JsonExportablePrimitive {

    private final double number;

    /**
     * Create a new JFloat.
     * @param number the number
     */
    public JFloat(final double number) {
        this.number = number;
    }

    /**
     * Create a new JFloat.
     * @param value the number
     * @return the number, or NULL if the input is null
     */
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

    /**
     * Create a new JFloat.
     * @param value the number
     * @return the number
     */
    public static JFloat of(final double value) {
        return new JFloat(value);
    }

    /**
     * Create a new JFloat.
     * @param value the number
     * @return the number
     */
    public static JFloat of(final long value) {
        return new JFloat(value);
    }

    /**
     * Create a new JFloat.
     * @param value the number
     * @return the number
     */
    public static JFloat of(final int value) {
        return new JFloat(value);
    }

    /**
     * Get the value of the number.
     * It does not matter what you initially put into this, you'll always get back a double.
     * @return the value
     */
    public double getValue() {
        return number;
    }

    /**
     * Get the value of the number as a double.
     * @return the value
     * TODO: this is kind of redudant, and should be removed
     */
    public double toDouble() {
        return number;
    }

    @Override
    public @Nullable JanitorObject janitorGetAttribute(final JanitorScriptProcess runningScript, final String name, final boolean required) throws JanitorNameException {
        // TODO: convert this into a proper dispatch table
        // TODO: int is inconsistent with the int() method used elsewhere, which leads to notable confusion
        if (Objects.equals(name, "int")) {
            return runningScript.getEnvironment().getBuiltins().integer((long) number);
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
