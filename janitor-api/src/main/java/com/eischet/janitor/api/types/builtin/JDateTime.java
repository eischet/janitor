package com.eischet.janitor.api.types.builtin;


import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.types.JConstant;
import com.eischet.janitor.api.types.JanitorObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * A datetime object, representing a date and time in the Gregorian calendar.
 * This is one of the built-in types that Janitor provides automatically.
 *
 * TODO: convert this to a composite
 */
public class JDateTime implements JConstant {

    private static final DateTimeFormatter DATE_FORMAT_LONG = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMAT_SHORT = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm");

    protected final long dateTime;

    /**
     * Create a new JDateTime.
     * @param dateTime the date and time
     */
    public JDateTime(final LocalDateTime dateTime) {
        this.dateTime = packLocalDateTime(dateTime);
    }

    /**
     * Create a new JDateTime.
     * @param text the date and time as a string
     */
    public JDateTime(final String text) {
        if ("now".equals(text)) {
            dateTime = packLocalDateTime(LocalDateTime.now());
        } else if (text.lastIndexOf(':') != text.indexOf(':')) {
            dateTime = packLocalDateTime(LocalDateTime.parse(text, DATE_FORMAT_LONG));
        } else {
            dateTime = packLocalDateTime(LocalDateTime.parse(text, DATE_FORMAT_SHORT));
        }
    }

    /**
     * Create a new JDateTime.
     * @return the current date and time
     */
    public static JDateTime now() {
        return new JDateTime("now");
    }

    /**
     * Create a new JDateTime.
     * @param dateTime the date and time
     * @return the date and time, or NULL if the input is null
     */
    public static JanitorObject ofNullable(final LocalDateTime dateTime) {
        if (dateTime != null) {
            return new JDateTime(dateTime);
        } else {
            return JNull.NULL;
        }
    }

    /**
     * Create a new JDateTime from a packed representation.
     * @param packed the packed representation
     * @return the date and time, or NULL if the input is null
     */
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

    /**
     * Pack a date and time into a long.
     * @param ldt the date and time
     * @return the packed date and time
     */
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

    /**
     * Require a datetime value.
     * @param runningScript the running script
     * @param value the value to check
     * @return the value, if it's a datetime
     * @throws JanitorArgumentException if the value is not a datetime
     */
    public static JDateTime require(final JanitorScriptProcess runningScript, final JanitorObject value) throws JanitorArgumentException {
        if (value instanceof JDateTime ok) {
            return ok;
        }
        throw new JanitorArgumentException(runningScript, "Expected a datetime value, but got " + value.janitorClassName() + " instead.");
    }

    @Override
    public LocalDateTime janitorGetHostValue() {
        return unpackLocalDateTime(dateTime);
    }

    /**
     * Get the internal representation of the date and time.
     * @return the internal representation
     */
    public long getInternalRepresentation() {
        return dateTime;
    }

    /**
     * Convert to a string, in the format that can be parsed by the interpreter, e.g. @2021-01-01-12:00:00.
     * Like Python's repr().
     * @return the string representation
     */
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

    /**
     * Parse a date and time from a string.
     * @param runningScript the running script
     * @param string the string to parse
     * @param format the format to parse the string with
     * @return the date and time, or NULL if the input is invalid
     */
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

    /**
     * Get the date part of the date and time.
     * @return the date
     */
    public JDate toDate() {
        return JDate.of(janitorGetHostValue().toLocalDate());
    }

    @Override
    public @NotNull String janitorClassName() {
        return "datetime";
    }




}