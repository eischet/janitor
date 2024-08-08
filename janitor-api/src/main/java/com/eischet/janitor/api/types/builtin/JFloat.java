package com.eischet.janitor.api.types.builtin;

import com.eischet.janitor.api.types.dispatch.Dispatcher;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;
import org.jetbrains.annotations.NotNull;

/**
 * A float object, representing a floating-point number.
 */
public class JFloat extends JanitorWrapper<Double> implements JNumber {

    private JFloat(final Dispatcher<JanitorWrapper<Double>> dispatcher, final Double wrapped) {
        super(dispatcher, wrapped);
    }

    @Override
    public Double janitorGetHostValue() {
        return wrapped;
    }

    @Override
    public String toString() {
        return String.valueOf(wrapped);
    }

    @Override
    public boolean janitorIsTrue() {
        return wrapped != 0.0;
    }

    /**
     * Get the value of the number.
     * It does not matter what you initially put into this, you'll always get back a double.
     * @return the value
     */
    public double getValue() {
        return wrapped;
    }

    /**
     * Get the value of the number as a double.
     * @return the value
     * TODO: this is kind of redundant, and should be removed
     */
    @Override
    public double toDouble() {
        return wrapped;
    }

    @Override
    public long toLong() {
        return wrapped.longValue();
    }


    @Override
    public @NotNull String janitorClassName() {
        return "float";
    }

    @Override
    public boolean isDefaultOrEmpty() {
        return wrapped == 0.0d;
    }

    @Override
    public void writeJson(final JsonOutputStream producer) throws JsonException {
        producer.value(wrapped);
    }

    public static JFloat newInstance(final Dispatcher<JanitorWrapper<Double>> dispatcher, final double value) {
        return new JFloat(dispatcher, value);
    }

}
