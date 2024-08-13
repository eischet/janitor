package com.eischet.janitor.env;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.types.functions.JCallArgs;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.builtin.JDate;
import com.eischet.janitor.api.types.builtin.JDateTime;
import com.eischet.janitor.api.types.builtin.JInt;
import com.eischet.janitor.api.types.builtin.JString;
import com.eischet.janitor.json.impl.DateTimeUtils;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.Locale;

/**
 * Operations for Datetime objects.
 * TODO: wrap these with a DispatchTable, to allow greater customisation options to hosts.
 */
public class JDateTimeClass {

    private static final WeekFields weekFields = WeekFields.of(Locale.GERMANY);

    public static JInt __epoch(final JDateTime csDateTime, final JanitorScriptProcess janitorScriptProcess, final JCallArgs jCallArgs) throws JanitorRuntimeException {
        jCallArgs.require(0);
        return janitorScriptProcess.getEnvironment().getBuiltinTypes().integer(csDateTime.janitorGetHostValue().toEpochSecond(DateTimeUtils.getZoneId().getRules().getOffset(csDateTime.janitorGetHostValue())));
    }

    public static long __epochAsAttribute(final JDateTime csDateTime) {
        return csDateTime.janitorGetHostValue().toEpochSecond(DateTimeUtils.getZoneId().getRules().getOffset(csDateTime.janitorGetHostValue()));
    }

    public static JDate __date(final JDateTime csDateTime, final JanitorScriptProcess janitorScriptProcess, final JCallArgs jCallArgs) throws JanitorRuntimeException {
        jCallArgs.require(0);
        return janitorScriptProcess.getBuiltins().date(csDateTime.janitorGetHostValue().toLocalDate());
    }

    public static JString __time(final JDateTime csDateTime, final JanitorScriptProcess janitorScriptProcess, final JCallArgs jCallArgs) throws JanitorRuntimeException {
        jCallArgs.require(0);
        return janitorScriptProcess.getRuntime().getEnvironment().getBuiltinTypes().string(janitorScriptProcess.getEnvironment().getFormatting().asTimeString(csDateTime.janitorGetHostValue()));
    }

    public static JString __string(final JDateTime csDateTime, final JanitorScriptProcess janitorScriptProcess, final JCallArgs jCallArgs) throws JanitorRuntimeException {
        final String fmt = jCallArgs.getOptionalStringValue(0, null);
        if (fmt == null) {
            return janitorScriptProcess.getEnvironment().getBuiltinTypes().string(janitorScriptProcess.getFormatting().formatDateTime(csDateTime.janitorGetHostValue()));
        } else {
            return janitorScriptProcess.getEnvironment().getBuiltinTypes().string(DateTimeFormatter.ofPattern(fmt).format(csDateTime.janitorGetHostValue()));
        }
    }

    public static JString __formatAtTimezone(final JDateTime csDateTime, final JanitorScriptProcess janitorScriptProcess, final JCallArgs jCallArgs) throws JanitorRuntimeException {
        final String tz = jCallArgs.getString(0).janitorGetHostValue();
        final String fmt = jCallArgs.getOptionalStringValue(1, null);

        final ZonedDateTime zoned = fmt != null ? csDateTime.janitorGetHostValue().atZone(ZoneId.of(tz)) : null;

        if (fmt == null) {
            return janitorScriptProcess.getEnvironment().getBuiltinTypes().string(janitorScriptProcess.getFormatting().formatDateTime(zoned));
        } else {
            return janitorScriptProcess.getEnvironment().getBuiltinTypes().string(DateTimeFormatter.ofPattern(fmt).format(zoned));
        }
    }

    public static JInt __year(final JDateTime jDateTime, final JanitorScriptProcess janitorScriptProcess, final JCallArgs jCallArgs) throws JanitorRuntimeException {
        return janitorScriptProcess.getEnvironment().getBuiltinTypes().integer(jDateTime.janitorGetHostValue().getYear());
    }

    public static JString __kw(final JDateTime jDateTime, final JanitorScriptProcess janitorScriptProcess, final JCallArgs jCallArgs) throws JanitorRuntimeException {
        final int kw = jDateTime.janitorGetHostValue().get(weekFields.weekOfWeekBasedYear());
        return janitorScriptProcess.getEnvironment().getBuiltinTypes().string(kw < 10 ? "0" + kw : String.valueOf(kw));
    }

}
