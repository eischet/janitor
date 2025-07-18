package com.eischet.janitor.api.types.dispatch;

import com.eischet.janitor.api.errors.glue.JanitorGlueException;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.builtin.JFloat;
import com.eischet.janitor.api.types.builtin.JInt;

import static com.eischet.janitor.api.util.ObjectUtilities.simpleClassNameOf;

public class FloatConverter implements TwoWayConverter<Double> {
    public static final FloatConverter INSTANCE = new FloatConverter();

    @Override
    public Double convertFromJanitor(final JanitorObject janitorObject) throws JanitorGlueException {
        if (janitorObject instanceof JInt integer) {
            return integer.janitorGetHostValue().doubleValue();
        }
        if (janitorObject instanceof JFloat floating) {
            return floating.janitorGetHostValue();
        }
        throw new JanitorGlueException(JanitorArgumentException::fromGlue, "Expected an integer, but got: " + janitorObject + " [" + simpleClassNameOf(janitorObject) + "]");
    }

    @Override
    public JanitorObject convertToJanitor(final Double value) throws JanitorGlueException {
        return Janitor.getBuiltins().nullableFloatingPoint(value);
    }
}
