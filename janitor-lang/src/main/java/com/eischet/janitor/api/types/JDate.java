package com.eischet.janitor.api.types;

import com.eischet.janitor.api.errors.runtime.JanitorNameException;
import com.eischet.janitor.tools.DateTimeUtilities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;

public class JDate implements JConstant {

    private static final Logger log = LoggerFactory.getLogger(JDate.class);

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final long date;

    public JDate(final LocalDate date) {
        this.date = DateTimeUtilities.packLocalDate(date);
    }

    private JDate(final long year, final long month, final long day) {
        this.date = DateTimeUtilities.packLocalDate(year, month, day);
    }

    public JDate(final String text) {
        if ("today".equals(text)) {
            date = DateTimeUtilities.packLocalDate(LocalDate.now());
        } else {
            date = DateTimeUtilities.packLocalDate(LocalDate.parse(text, DATE_FORMAT));
        }
    }

    public static JDate today() {
        return new JDate("today");
    }

    public static JDate of(final LocalDate localDate) {
        return new JDate(localDate);
    }

    public static JDate of(final int year, final int month, final int day) {
        return new JDate(year, month, day);
    }

    public static JDate of(final long year, final long month, final long day) {
        return new JDate(year, month, day);
    }

    public static JanitorObject parse(final String string, final String format) {
        try {
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
            final LocalDate d = LocalDate.parse(string, formatter);
            return of(d);
        } catch (DateTimeParseException e) {
            log.warn("error parsing date '{}' with format '{}'", string, format, e);
            return JNull.NULL;
        }
    }

    @Override
    public LocalDate janitorGetHostValue() {
        return DateTimeUtilities.unpackLocalDate(date);
    }

    @Override
    public String janitorToString() {
        return "@" + DATE_FORMAT.format(janitorGetHostValue());
    }

    @Override
    public boolean janitorIsTrue() {
        return date != 0;
    }

    @Override
    public String toString() {
        return janitorToString();
    }

    public static JanitorObject ofNullable(final LocalDate localDate) {
        return localDate == null ? JNull.NULL : new JDate(localDate);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final JDate csDate = (JDate) o;
        return date == csDate.date;
    }

    @Override
    public int hashCode() {
        return Objects.hash(date);
    }

    public long getInternalRepresentation() {
        return date;
    }

    @Override
    public @NotNull String janitorClassName() {
        return "date";
    }

    @Override
    public @Nullable JanitorObject janitorGetAttribute(final JanitorScriptProcess runningScript, final String name, final boolean required) throws JanitorNameException {
        if ("year".equals(name)) {
            return JInt.of(getYear());
        }
        if ("month".equals(name)) {
            return JInt.of(getMonth());
        }
        if ("day".equals(name)) {
            return JInt.of(getDayOfMonth());
        }
        return JConstant.super.janitorGetAttribute(runningScript, name, required);
    }


    public long getYear() {
        final LocalDate unpacked = DateTimeUtilities.unpackLocalDate(date);
        return unpacked == null ? 0 : unpacked.getYear();
    }

    public long getMonth() {
        final LocalDate unpacked = DateTimeUtilities.unpackLocalDate(date);
        return unpacked == null ? 0 : unpacked.getMonthValue();
    }

    public long getDayOfMonth() {
        final LocalDate unpacked = DateTimeUtilities.unpackLocalDate(date);
        return unpacked == null ? 0 : unpacked.getDayOfMonth();
    }

}
