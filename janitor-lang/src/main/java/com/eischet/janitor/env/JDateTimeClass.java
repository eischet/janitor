package com.eischet.janitor.env;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.calls.JCallArgs;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JDate;
import com.eischet.janitor.api.types.JDateTime;
import com.eischet.janitor.api.types.JInt;
import com.eischet.janitor.api.types.JString;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.Locale;

public class JDateTimeClass {

    public static JInt __epoch(final JDateTime csDateTime, final JanitorScriptProcess janitorScriptProcess, final JCallArgs jCallArgs) throws JanitorRuntimeException {
        jCallArgs.require(0);
        return JInt.of(csDateTime.janitorGetHostValue().toEpochSecond(ZoneId.systemDefault().getRules().getOffset(csDateTime.janitorGetHostValue())));
    }

    public static JInt __epochAsAttribute(final JDateTime csDateTime) {
        return JInt.of(csDateTime.janitorGetHostValue().toEpochSecond(ZoneId.systemDefault().getRules().getOffset(csDateTime.janitorGetHostValue())));
    }



    public static JDate __date(final JDateTime csDateTime, final JanitorScriptProcess janitorScriptProcess, final JCallArgs jCallArgs) throws JanitorRuntimeException {
        jCallArgs.require(0);
        return JDate.of(csDateTime.janitorGetHostValue().toLocalDate());
    }

    public static JString __time(final JDateTime csDateTime, final JanitorScriptProcess janitorScriptProcess, final JCallArgs jCallArgs) throws JanitorRuntimeException {
        jCallArgs.require(0);
        return JString.of(janitorScriptProcess.getRuntime().getFormatting().asTimeString(csDateTime.janitorGetHostValue()));
    }

    public static JString __string(final JDateTime csDateTime, final JanitorScriptProcess janitorScriptProcess, final JCallArgs jCallArgs) throws JanitorRuntimeException {
        final String fmt = jCallArgs.getOptionalStringValue(0, null);
        if (fmt == null) {
            return JString.of(janitorScriptProcess.getRuntime().getFormatting().formatDateTime(csDateTime.janitorGetHostValue()));
        } else {
            return JString.of(DateTimeFormatter.ofPattern(fmt).format(csDateTime.janitorGetHostValue()));
        }
    }

    public static JString __formatAtTimezone(final JDateTime csDateTime, final JanitorScriptProcess janitorScriptProcess, final JCallArgs jCallArgs) throws JanitorRuntimeException {
        final String tz = jCallArgs.getString(0).janitorGetHostValue();
        final String fmt = jCallArgs.getOptionalStringValue(1, null);

        final ZonedDateTime zoned = fmt != null ? csDateTime.janitorGetHostValue().atZone(ZoneId.of(tz)) : null;

        if (fmt == null) {
            return JString.of(janitorScriptProcess.getRuntime().getFormatting().formatDateTime(zoned));
        } else {
            return JString.of(DateTimeFormatter.ofPattern(fmt).format(zoned));
        }
    }

    public static JInt __year(final JDateTime jDateTime, final JanitorScriptProcess janitorScriptProcess, final JCallArgs jCallArgs) throws JanitorRuntimeException {
        return JInt.of(jDateTime.janitorGetHostValue().getYear());
    }

    public static JString __kw(final JDateTime jDateTime, final JanitorScriptProcess janitorScriptProcess, final JCallArgs jCallArgs) throws JanitorRuntimeException {
        final int kw = jDateTime.janitorGetHostValue().get(weekFields.weekOfWeekBasedYear());
        return JString.of(kw < 10 ? "0" + kw : String.valueOf(kw));
    }

    private static final WeekFields weekFields = WeekFields.of(Locale.GERMANY);

}