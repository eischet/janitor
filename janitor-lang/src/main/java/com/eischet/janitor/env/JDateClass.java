package com.eischet.janitor.env;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.builtin.JDate;
import com.eischet.janitor.api.types.builtin.JString;
import com.eischet.janitor.api.types.functions.JCallArgs;

import java.time.format.DateTimeFormatter;

public class JDateClass {

    public static JString __format(final JDate date, final JanitorScriptProcess janitorScriptProcess, final JCallArgs jCallArgs) throws JanitorRuntimeException {
        final String fmt = jCallArgs.getOptionalStringValue(0, null);
        if (fmt == null) {
            return Janitor.string(janitorScriptProcess.getFormatting().formatDate(date.janitorGetHostValue()));
        } else {
            return Janitor.string(DateTimeFormatter.ofPattern(fmt).format(date.janitorGetHostValue()));
        }
    }

}
