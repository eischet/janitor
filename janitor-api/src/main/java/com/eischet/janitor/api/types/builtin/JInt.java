package com.eischet.janitor.api.types.builtin;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.errors.runtime.JanitorNativeException;
import com.eischet.janitor.api.types.dispatch.Dispatcher;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * An integer object, representing a 64-bit signed integer.
 * This is one of the built-in types that Janitor provides automatically.
 */
public class JInt extends JanitorWrapper<Long> implements JNumber {

    public JInt(final Dispatcher<JanitorWrapper<Long>> dispatcher, final Long wrapped) {
        super(dispatcher, wrapped);
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

    @Override
    public double toDouble() {
        return (double) wrapped;
    }

    @Override
    public long toLong() {
        return wrapped;
    }
}
