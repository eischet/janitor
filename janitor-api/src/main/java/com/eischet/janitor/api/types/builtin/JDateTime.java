package com.eischet.janitor.api.types.builtin;


import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.types.JConstant;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.Dispatcher;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonInputStream;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;

/**
 * A datetime object, representing a date and time in the Gregorian calendar.
 * This is one of the built-in types that Janitor provides automatically.
 */
public class JDateTime extends JanitorComposed<JDateTime> implements JConstant {

    public static final DateTimeFormatter DATE_FORMAT_LONG = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm:ss");
    public static final DateTimeFormatter DATE_FORMAT_SHORT = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm");

    public static final DateTimeFormatter JSON_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME; // yyyy-MM-dd'T'HH:mm:ss

    protected final long dateTime;



    /**
     * Create a new JDateTime.
     * @param dateTime the date and time
     */
    private JDateTime(final Dispatcher<JDateTime> dispatch, final LocalDateTime dateTime) {
        super(dispatch);
        this.dateTime = packLocalDateTime(dateTime);
    }

    public static JDateTime newInstance(final Dispatcher<JDateTime> dispatcher, final LocalDateTime dateTime) {
        return new JDateTime(dispatcher, dateTime);
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

    @Override
    public @NotNull LocalDateTime janitorGetHostValue() {
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
    public @NotNull String janitorToString() {
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
     * @param process the running script
     * @param string the string to parse
     * @param format the format to parse the string with
     * @return the date and time, or NULL if the input is invalid
     */
    public static JanitorObject parse(final JanitorScriptProcess process, final String string, final String format) {
        try {
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
            final LocalDateTime d = LocalDateTime.parse(string, formatter);
            return process.getBuiltins().dateTime(d);
        } catch (DateTimeParseException e) {
            process.warn(String.format("error parsing date '%s' with format '%s': %s", string, format, e.getMessage()));
            return JNull.NULL;
        }
    }

    /**
     * Get the date part of the date and time.
     * @return the date
     */
    public JDate toDate() {
        return Janitor.date(janitorGetHostValue().toLocalDate());
    }

    @Override
    public @NotNull String janitorClassName() {
        return "datetime";
    }


    @Override
    public void writeJson(final JsonOutputStream producer) throws JsonException {
        producer.value(JSON_FORMAT.format(janitorGetHostValue()));
    }

    @Override
    public void readJson(final JsonInputStream stream) throws JsonException {
        throw new JsonException("You cannot read a datetime from JSON like this because datetimes are immutable!");
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof final JDateTime jDateTime)) return false;
        return dateTime == jDateTime.dateTime;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(dateTime);
    }
}