// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.env;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.builtin.JDate;
import com.eischet.janitor.api.types.builtin.JString;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.api.types.functions.JCallArgs;

import java.time.format.DateTimeFormatter;

/** Operations for date objects. */
public class JDateClass {

    /**
     * Script method {@code date.format(pattern)}: formats the date, using the default format if no pattern is given.
     * @param date the object that the method is called on
     * @param janitorScriptProcess the running script process
     * @param jCallArgs the call arguments
     * @return the formatted date
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static JString __format(final JDate date, final JanitorScriptProcess janitorScriptProcess, final JCallArgs jCallArgs) throws JanitorRuntimeException {
        final String fmt = jCallArgs.getOptionalStringValue(0, null);
        if (fmt == null) {
            return Janitor.string(janitorScriptProcess.getFormatting().formatDate(date.janitorGetHostValue()));
        } else {
            return Janitor.string(DateTimeFormatter.ofPattern(fmt).format(date.janitorGetHostValue()));
        }
    }

    /**
     * Registers the standard methods and properties of dates.
     * @param dateDispatch the dispatch table to add them to
     */
    public static void applyDefaults(DispatchTable<JDate> dateDispatch) {
        dateDispatch.addLongProperty("year", JDate::getYear);
        dateDispatch.addLongProperty("month", JDate::getMonth);
        dateDispatch.addLongProperty("day", JDate::getDayOfMonth);
        dateDispatch.addMethod("format", JDateClass::__format);
        dateDispatch.addMethod("string", JDateClass::__format);
    }

}
