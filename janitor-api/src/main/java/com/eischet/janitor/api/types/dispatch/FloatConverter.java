package com.eischet.janitor.api.types.dispatch;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JFloat;
import com.eischet.janitor.api.types.builtin.JInt;

public class FloatConverter implements TwoWayConverter<Double> {
    public static final FloatConverter INSTANCE = new FloatConverter();

    @Override
    public Double convertFromJanitor(final JanitorScriptProcess process, final JanitorObject janitorObject) throws JanitorRuntimeException {
        if (janitorObject instanceof JInt integer) {
            return integer.janitorGetHostValue().doubleValue();
        }
        if (janitorObject instanceof JFloat floating) {
            return floating.janitorGetHostValue();
        }
        throw new JanitorArgumentException(process, "Expected an integer, but got: " + janitorObject);
    }

    @Override
    public JanitorObject convertToJanitor(final JanitorScriptProcess process, final Double value) throws JanitorRuntimeException {
        return process.getBuiltins().nullableFloatingPoint(value);
    }
}
