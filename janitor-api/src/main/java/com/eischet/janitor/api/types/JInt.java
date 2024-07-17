package com.eischet.janitor.api.types;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.errors.runtime.JanitorNameException;
import com.eischet.janitor.api.traits.JConstant;
import com.eischet.janitor.api.util.DateTimeUtilities;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonExportablePrimitive;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * An integer object, representing a 64-bit signed integer.
 * This is one of the built-in types that Janitor provides automatically.
 */
public class JInt implements JConstant, JsonExportablePrimitive {

    private final long integer;

    /**
     * Create a new JInt.
     *
     * @param integer the integer
     */
    public JInt(final long integer) {
        this.integer = integer;
    }

    /**
     * Create a new JInt.
     *
     * @param scriptProcess the script process
     * @param value         the value
     * @return the integer
     * @throws JanitorArgumentException if the value is not an integer
     */
    public static JInt requireInt(final JanitorScriptProcess scriptProcess, final JanitorObject value) throws JanitorArgumentException {
        if (value instanceof JInt ok) {
            return ok;
        }
        throw new JanitorArgumentException(scriptProcess, "Expected an integer value, but got " + value.janitorClassName() + " instead.");
    }

    /**
     * Create a new JInt.
     *
     * @param value the integer
     * @return the integer, or NULL if the input is null
     */
    public static JanitorObject ofNullable(final Long value) {
        return value == null ? JNull.NULL : JInt.of(value);
    }

    /**
     * Create a new JInt.
     *
     * @param value the integer
     * @return the integer, or NULL if the input is null
     */
    public static JanitorObject ofNullable(final Integer value) {
        return value == null ? JNull.NULL : JInt.of(value);
    }

    /**
     * Create a new JInt.
     *
     * @param value the integer
     * @return the integer
     */
    public static JInt of(final long value) {
        return new JInt(value);
    }

    /**
     * Create a new JInt, but return 0 when passed null.
     *
     * @param value the integer
     * @return the integer
     */
    public static JInt ofNullableOrZero(final Long value) {
        return value == null ? JInt.of(0) : JInt.of(value);
    }

    /**
     * Create a new JInt.
     *
     * @param value the integer
     * @return the integer
     */
    public static JInt of(final int value) {
        return new JInt(value);
    }

    /**
     * Defined truthiness: value is not zero.
     *
     * @return true if the value is not zero
     */
    @Override
    public boolean janitorIsTrue() {
        return integer != 0;
    }

    @Override
    public Long janitorGetHostValue() {
        return integer;
    }

    /**
     * Get the integer as a double.
     *
     * @return the integer as a double
     */
    public double getAsDouble() {
        return integer;
    }

    /**
     * Get the integer as an int.
     * Beware: long values overflowing int values will be crippled
     *
     * @return the integer as an int
     */
    public int getAsInt() {
        return (int) integer;
    }

    @Override
    public String toString() {
        return String.valueOf(integer);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final JInt that = (JInt) o;
        return integer == that.integer;
    }

    @Override
    public int hashCode() {
        return Objects.hash(integer);
    }

    /**
     * Get the value of the integer.
     *
     * @return the integer
     */
    public long getValue() {
        return integer;
    }

    @Override
    public @Nullable JanitorObject janitorGetAttribute(final JanitorScriptProcess runningScript, final String name, final boolean required) throws JanitorNameException {
        // TODO: convert this into a proper dispatch table
        if (Objects.equals(name, "int")) {
            return this;
        }
        if (Objects.equals(name, "epoch")) {
            return JDateTime.ofNullable(DateTimeUtilities.localFromEpochSeconds(integer));
        }
        return JConstant.super.janitorGetAttribute(runningScript, name, required);
    }


    @Override
    public @NotNull String janitorClassName() {
        return "int";
    }

    @Override
    public boolean isDefaultOrEmpty() {
        return integer == 0;
    }

    @Override
    public void writeJson(final JsonOutputStream producer) throws JsonException {
        producer.value(integer);
    }


}
