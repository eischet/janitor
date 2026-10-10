package com.eischet.janitor.api.types.dispatch;

import com.eischet.janitor.api.errors.glue.JanitorGlueException;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.builtin.JFloat;
import com.eischet.janitor.api.types.builtin.JInt;

/** Converts between Janitor integers and Java {@link Long} values. */
public class LongConverter implements TwoWayConverter<Long> {

    public static final LongConverter INSTANCE = new LongConverter();

    @Override
    public Long convertFromJanitor(final JanitorObject janitorObject) throws JanitorGlueException {
        if (janitorObject instanceof JInt integer) {
            return integer.janitorGetHostValue();
        }
        if (janitorObject instanceof JFloat floating) {
            return floating.janitorGetHostValue().longValue();
        }
        throw new JanitorGlueException(JanitorArgumentException::fromGlue, "Expected an integer, but got: " + janitorObject);
    }

    @Override
    public JanitorObject convertToJanitor(final Long value) throws JanitorGlueException {
        return Janitor.nullableInteger(value);
    }

}
