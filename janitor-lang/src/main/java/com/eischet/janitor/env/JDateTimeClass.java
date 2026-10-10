package com.eischet.janitor.env;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.builtin.JDate;
import com.eischet.janitor.api.types.builtin.JDateTime;
import com.eischet.janitor.api.types.builtin.JInt;
import com.eischet.janitor.api.types.builtin.JString;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.api.types.functions.JCallArgs;
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

    /**
     * Script method {@code datetime.toEpoch()}: the number of seconds since the epoch, in the environment's time zone.
     * @param csDateTime the object that the method is called on
     * @param janitorScriptProcess the running script process
     * @param jCallArgs the call arguments
     * @return the epoch seconds
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static JInt __epoch(final JDateTime csDateTime, final JanitorScriptProcess janitorScriptProcess, final JCallArgs jCallArgs) throws JanitorRuntimeException {
        jCallArgs.require(0);
        return Janitor.integer(csDateTime.janitorGetHostValue().toEpochSecond(DateTimeUtils.getZoneId().getRules().getOffset(csDateTime.janitorGetHostValue())));
    }

    /**
     * Script property {@code datetime.epoch}: the number of seconds since the epoch, in the environment's time zone.
     * @param csDateTime the datetime
     * @return the epoch seconds
     */
    public static long __epochAsAttribute(final JDateTime csDateTime) {
        return csDateTime.janitorGetHostValue().toEpochSecond(DateTimeUtils.getZoneId().getRules().getOffset(csDateTime.janitorGetHostValue()));
    }

    /**
     * Script method {@code datetime.date()}: the date part of the datetime.
     * @param csDateTime the object that the method is called on
     * @param janitorScriptProcess the running script process
     * @param jCallArgs the call arguments
     * @return the date
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static JDate __date(final JDateTime csDateTime, final JanitorScriptProcess janitorScriptProcess, final JCallArgs jCallArgs) throws JanitorRuntimeException {
        jCallArgs.require(0);
        return Janitor.date(csDateTime.janitorGetHostValue().toLocalDate());
    }

    /**
     * Script method {@code datetime.time()}: the time part of the datetime, formatted as a string.
     * @param csDateTime the object that the method is called on
     * @param janitorScriptProcess the running script process
     * @param jCallArgs the call arguments
     * @return the formatted time
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static JString __time(final JDateTime csDateTime, final JanitorScriptProcess janitorScriptProcess, final JCallArgs jCallArgs) throws JanitorRuntimeException {
        jCallArgs.require(0);
        return Janitor.string(janitorScriptProcess.getEnvironment().getFormatting().asTimeString(csDateTime.janitorGetHostValue()));
    }

    /**
     * Script method {@code datetime.string(pattern)}: formats the datetime, using the default format if no pattern is given.
     * @param csDateTime the object that the method is called on
     * @param janitorScriptProcess the running script process
     * @param jCallArgs the call arguments
     * @return the formatted datetime
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static JString __string(final JDateTime csDateTime, final JanitorScriptProcess janitorScriptProcess, final JCallArgs jCallArgs) throws JanitorRuntimeException {
        final String fmt = jCallArgs.getOptionalStringValue(0, null);
        if (fmt == null) {
            return Janitor.string(janitorScriptProcess.getFormatting().formatDateTime(csDateTime.janitorGetHostValue()));
        } else {
            return Janitor.string(DateTimeFormatter.ofPattern(fmt).format(csDateTime.janitorGetHostValue()));
        }
    }

    /**
     * Script method {@code datetime.formatAtTimezone(zone, pattern)}: formats the datetime at the given time zone.
     * @param csDateTime the object that the method is called on
     * @param janitorScriptProcess the running script process
     * @param jCallArgs the call arguments
     * @return the formatted datetime
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static JString __formatAtTimezone(final JDateTime csDateTime, final JanitorScriptProcess janitorScriptProcess, final JCallArgs jCallArgs) throws JanitorRuntimeException {
        final String tz = jCallArgs.getString(0).janitorGetHostValue();
        final String fmt = jCallArgs.getOptionalStringValue(1, null);

        final ZonedDateTime zoned = fmt != null ? csDateTime.janitorGetHostValue().atZone(ZoneId.of(tz)) : null;

        if (fmt == null) {
            return Janitor.string(janitorScriptProcess.getFormatting().formatDateTime(zoned));
        } else {
            return Janitor.string(DateTimeFormatter.ofPattern(fmt).format(zoned));
        }
    }

    /**
     * Script method {@code datetime.year()}: the year of the datetime.
     * @param jDateTime the object that the method is called on
     * @param janitorScriptProcess the running script process
     * @param jCallArgs the call arguments
     * @return the year
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static JInt __year(final JDateTime jDateTime, final JanitorScriptProcess janitorScriptProcess, final JCallArgs jCallArgs) throws JanitorRuntimeException {
        return Janitor.integer(jDateTime.janitorGetHostValue().getYear());
    }

    /**
     * Script method {@code datetime.kw()}: the calendar week ("Kalenderwoche") of the datetime, according to German week rules,
     * as a string with two digits.
     * @param jDateTime the object that the method is called on
     * @param janitorScriptProcess the running script process
     * @param jCallArgs the call arguments
     * @return the calendar week, e.g. "07"
     * @throws JanitorRuntimeException if the call arguments are invalid or the operation fails
     */
    public static JString __kw(final JDateTime jDateTime, final JanitorScriptProcess janitorScriptProcess, final JCallArgs jCallArgs) throws JanitorRuntimeException {
        final int kw = jDateTime.janitorGetHostValue().get(weekFields.weekOfWeekBasedYear());
        return Janitor.string(kw < 10 ? "0" + kw : String.valueOf(kw));
    }

    /**
     * Registers the standard methods and properties of datetimes.
     * @param dateTimeDispatch the dispatch table to add them to
     */
    public static void applyDefaults(DispatchTable<JDateTime> dateTimeDispatch) {
        dateTimeDispatch.addLongProperty("epoch", JDateTimeClass::__epochAsAttribute);
        dateTimeDispatch.addMethod("toEpoch", JDateTimeClass::__epoch);
        dateTimeDispatch.addMethod("date", JDateTimeClass::__date);
        dateTimeDispatch.addMethod("time", JDateTimeClass::__time);
        dateTimeDispatch.addMethod("string", JDateTimeClass::__string);
        dateTimeDispatch.addMethod("format", JDateTimeClass::__string);
        dateTimeDispatch.addMethod("formatAtTimezone", JDateTimeClass::__formatAtTimezone);
        dateTimeDispatch.addMethod("kw", JDateTimeClass::__kw);
        dateTimeDispatch.addMethod("year", JDateTimeClass::__year);
    }
}
