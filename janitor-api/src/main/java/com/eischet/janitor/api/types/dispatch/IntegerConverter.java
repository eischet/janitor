package com.eischet.janitor.api.types.dispatch;

import com.eischet.janitor.api.errors.glue.JanitorGlueException;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.builtin.JFloat;
import com.eischet.janitor.api.types.builtin.JInt;

public class IntegerConverter implements TwoWayConverter<Integer> {

    public static final IntegerConverter INSTANCE = new IntegerConverter();

    @Override
    public Integer convertFromJanitor(final JanitorObject janitorObject) throws JanitorGlueException {
        if (janitorObject instanceof JInt integer) {
            return integer.janitorGetHostValue().intValue(); // LATER: might need a range check here; Java should throw in case of value > Integer.MAX_VALUE, though, so we already have that
        }
        if (janitorObject instanceof JFloat floating) {
            return floating.janitorGetHostValue().intValue();
        }
        throw new JanitorGlueException(JanitorArgumentException::fromGlue, "Expected an integer, but got: " + janitorObject);
    }

    @Override
    public JanitorObject convertToJanitor(final Integer value) throws JanitorGlueException {
        return Janitor.nullableInteger(value);
    }

}
