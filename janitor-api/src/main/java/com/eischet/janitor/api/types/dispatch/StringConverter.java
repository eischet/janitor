package com.eischet.janitor.api.types.dispatch;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JString;

public class StringConverter implements TwoWayConverter<String> {

    public static final StringConverter INSTANCE = new StringConverter();

    @Override
    public String convertFromJanitor(final JanitorScriptProcess process, final JanitorObject janitorObject) throws JanitorRuntimeException {
        if (janitorObject instanceof JString str) {
            return str.janitorGetHostValue();
        }
        throw new JanitorArgumentException(process, "Expected a string, but got: " + janitorObject);
    }

    @Override
    public JanitorObject convertToJanitor(final JanitorScriptProcess process, final String value) {
        return process.getBuiltins().nullableString(value);
    }
}
