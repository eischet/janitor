package com.eischet.janitor.cleanup.api.api.types;

import com.eischet.janitor.cleanup.api.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.cleanup.runtime.types.JCallArgs;
import com.eischet.janitor.cleanup.runtime.types.JUnboundMethod;
import com.eischet.janitor.cleanup.runtime.types.JanitorClass;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.api.map.MutableMap;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.Locale;

public class JDateTimeClass extends JanitorClass<JDateTime> {
    private static final ImmutableMap<String, JUnboundMethod<JDateTime>> methods;

    static {
        final MutableMap<String, JUnboundMethod<JDateTime>> m = Maps.mutable.empty();
        m.put("toEpoch", JDateTimeClass::__epoch);
        m.put("date", JDateTimeClass::__date);
        m.put("time", JDateTimeClass::__time);
        m.put("string", JDateTimeClass::__string);
        m.put("format", JDateTimeClass::__string);
        m.put("formatAtTimezone", JDateTimeClass::__formatAtTimezone);
        m.put("kw", JDateTimeClass::__kw);
        m.put("year", JDateTimeClass::__year);
        // LATER: Zeitzonen JZonedDateTime m.put("atZone", JDateTimeClass::__atZone);
        methods = m.toImmutable();

    }

    public JDateTimeClass() {
        super(null, methods);
    }

    private static JInt __epoch(final JDateTime csDateTime, final JanitorScriptProcess janitorScriptProcess, final JCallArgs jCallArgs) throws JanitorRuntimeException {
        jCallArgs.require(0);
        return JInt.of(csDateTime.janitorGetHostValue().toEpochSecond(ZoneId.systemDefault().getRules().getOffset(csDateTime.janitorGetHostValue())));
    }

    private static JDate __date(final JDateTime csDateTime, final JanitorScriptProcess janitorScriptProcess, final JCallArgs jCallArgs) throws JanitorRuntimeException {
        jCallArgs.require(0);
        return JDate.of(csDateTime.janitorGetHostValue().toLocalDate());
    }

    private static JString __time(final JDateTime csDateTime, final JanitorScriptProcess janitorScriptProcess, final JCallArgs jCallArgs) throws JanitorRuntimeException {
        jCallArgs.require(0);
        return JString.of(janitorScriptProcess.getRuntime().getFormatting().asTimeString(csDateTime.janitorGetHostValue()));
    }

    private static JString __string(final JDateTime csDateTime, final JanitorScriptProcess janitorScriptProcess, final JCallArgs jCallArgs) throws JanitorRuntimeException {
        final String fmt = jCallArgs.getOptionalStringValue(0, null);
        if (fmt == null) {
            return JString.of(janitorScriptProcess.getRuntime().getFormatting().formatDateTime(csDateTime.janitorGetHostValue()));
        } else {
            return JString.of(DateTimeFormatter.ofPattern(fmt).format(csDateTime.janitorGetHostValue()));
        }
    }

    private static JString __formatAtTimezone(final JDateTime csDateTime, final JanitorScriptProcess janitorScriptProcess, final JCallArgs jCallArgs) throws JanitorRuntimeException {
        final String tz = jCallArgs.getString(0).janitorGetHostValue();
        final String fmt = jCallArgs.getOptionalStringValue(1, null);

        final ZonedDateTime zoned = fmt != null ? csDateTime.janitorGetHostValue().atZone(ZoneId.of(tz)) : null;

        if (fmt == null) {
            return JString.of(janitorScriptProcess.getRuntime().getFormatting().formatDateTime(zoned));
        } else {
            return JString.of(DateTimeFormatter.ofPattern(fmt).format(zoned));
        }
    }

    private static JInt __year(final JDateTime jDateTime, final JanitorScriptProcess janitorScriptProcess, final JCallArgs jCallArgs) throws JanitorRuntimeException {
        return JInt.of(jDateTime.janitorGetHostValue().getYear());
    }

    private static JString __kw(final JDateTime jDateTime, final JanitorScriptProcess janitorScriptProcess, final JCallArgs jCallArgs) throws JanitorRuntimeException {
        final int kw = jDateTime.janitorGetHostValue().get(weekFields.weekOfWeekBasedYear());
        return JString.of(kw < 10 ? "0" + kw : String.valueOf(kw));
    }

    private static final WeekFields weekFields = WeekFields.of(Locale.GERMANY);

}
