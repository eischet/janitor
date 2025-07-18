package com.eischet.janitor.api.types.dispatch;

import com.eischet.janitor.api.errors.glue.JanitorGlueException;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.builtin.JString;

public class StringConverter implements TwoWayConverter<String> {

    public static final StringConverter INSTANCE = new StringConverter();

    @Override
    public String convertFromJanitor(final JanitorObject janitorObject) throws JanitorGlueException {
        if (janitorObject instanceof JString str) {
            return str.janitorGetHostValue();
        }
        throw new JanitorGlueException(JanitorArgumentException::fromGlue, "Expected a string, but got: " + janitorObject);
    }

    @Override
    public JanitorObject convertToJanitor(final String value) {
        return Janitor.getBuiltins().nullableString(value);
    }
}
