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
        // Every HELP text starts with the call form, "name(arguments): " or "name: " for properties, which the language reference relies on.
        dateDispatch.addLongProperty("year", JDate::getYear)
                .setMetaData(Janitor.MetaData.HELP, "year: The year, e.g. 2024.");
        dateDispatch.addLongProperty("month", JDate::getMonth)
                .setMetaData(Janitor.MetaData.HELP, "month: The month, 1 to 12.");
        dateDispatch.addLongProperty("day", JDate::getDayOfMonth)
                .setMetaData(Janitor.MetaData.HELP, "day: The day of the month, 1 to 31.");
        dateDispatch.addMethod("format", JDateClass::__format)
                .setMetaData(Janitor.MetaData.HELP, "format(pattern): Formats the date with a Java DateTimeFormatter pattern like \"dd.MM.yyyy\"; without a pattern, in the host's default format.");
        dateDispatch.addMethod("string", JDateClass::__format)
                .setMetaData(Janitor.MetaData.HELP, "string(pattern): Same as format(pattern).");
    }

}
