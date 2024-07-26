package com.eischet.janitor.api.types;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.scripting.Dispatcher;
import com.eischet.janitor.api.scripting.JanitorWrapper;
import com.eischet.janitor.api.traits.JConstant;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonExportablePrimitive;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * An integer object, representing a 64-bit signed integer.
 * This is one of the built-in types that Janitor provides automatically.
 */
public class JInt extends JanitorWrapper<Long> implements JConstant, JsonExportablePrimitive {

    public JInt(final Dispatcher<JanitorWrapper<Long>> dispatcher, final Long wrapped) {
        super(dispatcher, wrapped);
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
     * Defined truthiness: value is not zero.
     *
     * @return true if the value is not zero
     */
    @Override
    public boolean janitorIsTrue() {
        return wrapped != 0;
    }

    @Override
    public Long janitorGetHostValue() {
        return wrapped;
    }

    /**
     * Get the integer as a double.
     *
     * @return the integer as a double
     */
    public double getAsDouble() {
        return wrapped.doubleValue();
    }

    /**
     * Get the integer as an int.
     * Beware: long values overflowing int values will be crippled
     *
     * @return the integer as an int
     */
    public int getAsInt() {
        return wrapped.intValue();
    }

    @Override
    public String toString() {
        return String.valueOf(wrapped);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final JInt that = (JInt) o;
        return wrapped.longValue() == ((JInt) o).janitorGetHostValue().longValue();
    }

    @Override
    public int hashCode() {
        return Objects.hash(wrapped);
    }

    /**
     * Get the value of the integer.
     *
     * @return the integer
     */
    public long getValue() {
        return wrapped;
    }



    @Override
    public @NotNull String janitorClassName() {
        return "int";
    }

    @Override
    public boolean isDefaultOrEmpty() {
        return wrapped == 0;
    }

    @Override
    public void writeJson(final JsonOutputStream producer) throws JsonException {
        producer.value(wrapped);
    }

    public static JInt newInstance(final Dispatcher<JanitorWrapper<Long>> dispatcher, final long value) {
        return new JInt(dispatcher, value);
    }

}
