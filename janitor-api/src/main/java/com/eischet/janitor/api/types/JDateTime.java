package com.eischet.janitor.api.types;


import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.traits.JConstant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class JDateTime implements JConstant {


    private static final DateTimeFormatter DATE_FORMAT_LONG = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMAT_SHORT = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm");

    protected final long dateTime;

    public JDateTime(final LocalDateTime dateTime) {
        this.dateTime = packLocalDateTime(dateTime);
    }

    public JDateTime(final String text) {
        if ("now".equals(text)) {
            dateTime = packLocalDateTime(LocalDateTime.now());
        } else if (text.lastIndexOf(':') != text.indexOf(':')) {
            dateTime = packLocalDateTime(LocalDateTime.parse(text, DATE_FORMAT_LONG));
        } else {
            dateTime = packLocalDateTime(LocalDateTime.parse(text, DATE_FORMAT_SHORT));
        }
    }

    public static JDateTime now() {
        return new JDateTime("now");
    }

    public static JanitorObject ofNullable(final LocalDateTime dateTime) {
        if (dateTime != null) {
            return new JDateTime(dateTime);
        } else {
            return JNull.NULL;
        }
    }

    public static LocalDateTime unpackLocalDateTime(final long packed) {
        if (packed == 0) {
            return null;
        }
        try {
            return LocalDateTime.of(
                (int) (packed / 100L / 100L / 100L / 100L / 100L),
                (int) (packed / 100L / 100L / 100L / 100L % 100L),
                (int) (packed / 100L / 100L / 100L % 100L),
                (int) (packed / 100L / 100L % 100L),
                (int) (packed / 100L % 100L),
                (int) (packed % 100L)
            );
        } catch (DateTimeException e ) {
            // log.warn("error unpacking date-time value from long: {}", packed, e);
            return null;
        }
    }

    public static long packLocalDateTime(final @Nullable LocalDateTime ldt) {
        if (ldt == null) {
            return 0;
        }
        try {
            return ldt.getSecond() +
                ldt.getMinute() * 100L +
                ldt.getHour() * 100L * 100L +
                ldt.getDayOfMonth() * 100L * 100L * 100L +
                ldt.getMonthValue() * 100L * 100L * 100L * 100L +
                ldt.getYear() * 100L * 100L * 100L * 100L * 100L;
        } catch (DateTimeException e ) {
            // log.warn("error packing date-time value: {}", ldt, e);
            return 0;
        }
    }

    @Override
    public LocalDateTime janitorGetHostValue() {
        return unpackLocalDateTime(dateTime);
    }

    public long getInternalRepresentation() {
        return dateTime;
    }


    @Override
    public String janitorToString() {
        return "@" + DATE_FORMAT_LONG.format(janitorGetHostValue());
    }

    @Override
    public boolean janitorIsTrue() {
        return dateTime != 0;
    }

    @Override
    public String toString() {
        return janitorToString();
    }


    public static JanitorObject parse(final JanitorScriptProcess runningScript, final String string, final String format) {
        try {
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
            final LocalDateTime d = LocalDateTime.parse(string, formatter);
            return ofNullable(d);
        } catch (DateTimeParseException e) {
            runningScript.warn(String.format("error parsing date '%s' with format '%s': %s", string, format, e.getMessage()));
            return JNull.NULL;
        }
    }

    public JDate toDate() {
        return JDate.of(janitorGetHostValue().toLocalDate());
    }

    @Override
    public @NotNull String janitorClassName() {
        return "datetime";
    }




}