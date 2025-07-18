package com.eischet.janitor.api.types.builtin;

import com.eischet.janitor.api.types.JConstant;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.Dispatcher;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * A date object, representing a date in the Gregorian calendar.
 * This is one of the built-in types that Janitor provides automatically.
 */
public class JDate extends JanitorComposed<JDate> implements JConstant {

    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final long date;

    public JDate(final Dispatcher<JDate> dispatcher, final long date) {
        super(dispatcher);
        this.date = date;
    }

    public static JDate newInstance(final Dispatcher<JDate> dispatcher, final long date) {
        return new JDate(dispatcher,  date);
    }


    /**
     * Unpack a date from a compact representation.
     * Turns out that LocalDate and LocalDateTime are real memory hogs when you have millions of them lying around.
     * @param packed the packed date
     * @return the date
     */
    public static LocalDate unpackLocalDate(final long packed) {
        if (packed == 0) {
            return null;
        }
        return LocalDate.of(
            (int) (packed / 100L / 100L / 100L / 100L / 100L),
            (int) (packed / 100L / 100L / 100L / 100L % 100L),
            (int) (packed / 100L / 100L / 100L % 100L)
        );
    }

    /**
     * Pack a date into a compact representation.
     * @param year the year
     * @param month the month
     * @param day the day
     * @return the packed date
     * @see JDate#unpackLocalDate
     */
    public static long packLocalDate(final long year, final long month, final long day) {
        return day * 100L * 100L * 100L +
               month * 100L * 100L * 100L * 100L +
               year * 100L * 100L * 100L * 100L * 100L;
    }

    /**
     * Pack a date into a compact representation.
     * @param ld the date
     * @return the packed date
     * @see JDate#unpackLocalDate
     */
    public static long packLocalDate(final LocalDate ld) {
        if (ld == null) {
            return 0;
        }
        return
            ld.getDayOfMonth() * 100L * 100L * 100L +
            ld.getMonthValue() * 100L * 100L * 100L * 100L +
            ld.getYear() * 100L * 100L * 100L * 100L * 100L;
    }

    @Override
    public LocalDate janitorGetHostValue() {
        return unpackLocalDate(date);
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

    /**
     * Get the internal representation of the date.
     * @return the internal representation (packed)
     */
    public long getInternalRepresentation() {
        return date;
    }

    @Override
    public @NotNull String janitorClassName() {
        return "date";
    }


    /**
     * Get the year of the date.
     * @return the year
     */
    public long getYear() {
        final LocalDate unpacked = unpackLocalDate(date);
        return unpacked == null ? 0 : unpacked.getYear();
    }

    /**
     * Get the month of the date.
     * @return the month
     */
    public long getMonth() {
        final LocalDate unpacked = unpackLocalDate(date);
        return unpacked == null ? 0 : unpacked.getMonthValue();
    }

    /**
     * Get the day of the month of the date.
     * @return the day of the month
     */
    public long getDayOfMonth() {
        final LocalDate unpacked = unpackLocalDate(date);
        return unpacked == null ? 0 : unpacked.getDayOfMonth();
    }

}
