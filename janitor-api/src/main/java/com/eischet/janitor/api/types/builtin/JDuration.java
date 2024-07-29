package com.eischet.janitor.api.types.builtin;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.types.JConstant;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.Dispatcher;
import org.jetbrains.annotations.NotNull;

import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

/**
 * A duration object, representing a time span.
 * This is one of the built-in types that Janitor provides automatically.
 */
public class JDuration extends JanitorComposed<JDuration> implements JConstant, Comparable<JDuration> {

    private final long amount;
    private final JDurationKind unit;

    /**
     * Create a new JDuration.
     * @param amount the amount
     * @param unit the unit
     */
    public JDuration(final Dispatcher<JDuration> dispatcher, final long amount, final JDurationKind unit) {
        super(dispatcher);
        this.amount = amount;
        this.unit = unit;
    }

    /**
     * Get the duration in seconds.
     * @return the duration in seconds
     */
    public long toSeconds() {
        return unit.unit.getDuration().toSeconds() * amount;
    }

    @Override
    public int compareTo(@NotNull final JDuration o) {
        return Long.compare(this.toSeconds(), o.toSeconds());
    }

    @Override
    public @NotNull String janitorClassName() {
        return "duration";
    }

    @Override
    public JDuration janitorGetHostValue() {
        return this;
    }

    @Override
    public String janitorToString() {
        return toString();
    }

    /**
     * Define truthiness: the duration is not zero.
     * @return true if the duration is not zero
     */
    @Override
    public boolean janitorIsTrue() {
        return amount != 0;
    }

    @Override
    public String toString() {
        return "@" + amount + unit.tag;
    }

    /**
     * Get the amount of the duration.
     * @return the amount
     */
    public long getAmount() {
        return amount;
    }

    /**
     * Get the unit of the duration.
     * @return the unit
     */
    public JDurationKind getUnit() {
        return unit;
    }

    /**
     * Two durations are equal if they have the same amount of seconds, regardless of how those were specified.
     * E.g. @1d and @86400s are equal.
     * @param obj the other object
     * @return true if the objects are equal
     */
    @Override
    public boolean equals(final Object obj) {
        return obj instanceof JDuration dur && dur.toSeconds() == toSeconds();
    }

    @Override
    public int hashCode() {
        return Long.hashCode(toSeconds());
    }

    /**
     * Add a duration to a date.
     * Once you add a fraction of a day, the result will be a datetime.
     * @param date the date
     * @param duration the duration
     * @return the new date or datetime
     */
    public static JanitorObject add(final JanitorScriptProcess process, final JDate date, final JDuration duration) {
        if (duration.getUnit() == null || duration.getAmount() == 0) {
            return date;
        }
        return switch (duration.getUnit()) {
            case YEARS -> process.getBuiltins().date(date.janitorGetHostValue().plusYears(duration.getAmount()));
            case MONTHS -> process.getBuiltins().date(date.janitorGetHostValue().plusMonths(duration.getAmount()));
            case WEEKS -> process.getBuiltins().date(date.janitorGetHostValue().plusWeeks(duration.getAmount()));
            case DAYS -> process.getBuiltins().date(date.janitorGetHostValue().plusDays(duration.getAmount()));
            case HOURS -> process.getBuiltins().dateTime(date.janitorGetHostValue().atStartOfDay().plusHours(duration.getAmount()));
            case MINUTES -> process.getBuiltins().dateTime(date.janitorGetHostValue().atStartOfDay().plusMinutes(duration.getAmount()));
            case SECONDS -> process.getBuiltins().dateTime(date.janitorGetHostValue().atStartOfDay().plusSeconds(duration.getAmount()));
        };
    }

    /**
     * Add a duration to a datetime.
     * @param dateTime the datetime
     * @param duration the duration
     * @return the new datetime
     */
    public static JanitorObject add(final JanitorScriptProcess process, final JDateTime dateTime, final JDuration duration) {
        if (duration.getUnit() == null || duration.getAmount() == 0) {
            return dateTime;
        }
        return switch (duration.getUnit()) {
            case YEARS -> process.getBuiltins().dateTime(dateTime.janitorGetHostValue().plusYears(duration.getAmount()));
            case MONTHS -> process.getBuiltins().dateTime(dateTime.janitorGetHostValue().plusMonths(duration.getAmount()));
            case WEEKS -> process.getBuiltins().dateTime(dateTime.janitorGetHostValue().plusWeeks(duration.getAmount()));
            case DAYS -> process.getBuiltins().dateTime(dateTime.janitorGetHostValue().plusDays(duration.getAmount()));
            case HOURS -> process.getBuiltins().dateTime(dateTime.janitorGetHostValue().plusHours(duration.getAmount()));
            case MINUTES -> process.getBuiltins().dateTime(dateTime.janitorGetHostValue().plusMinutes(duration.getAmount()));
            case SECONDS -> process.getBuiltins().dateTime(dateTime.janitorGetHostValue().plusSeconds(duration.getAmount()));
        };
    }


    /**
     * Subtract a duration from a date.
     * Once you subtract a fraction of a day, the result will be a datetime.
     * @param date the date
     * @param duration the duration
     * @return the new date or datetime
     * TODO wouldn't it be much simpler to add(date, -duration) here?
     */
    public static JanitorObject subtract(final JanitorScriptProcess process, final JDate date, final JDuration duration) {
        if (duration.getUnit() == null || duration.getAmount() == 0) {
            return date;
        }
        return switch (duration.getUnit()) {
            case YEARS -> process.getBuiltins().date(date.janitorGetHostValue().minusYears(duration.getAmount()));
            case MONTHS -> process.getBuiltins().date(date.janitorGetHostValue().minusMonths(duration.getAmount()));
            case WEEKS -> process.getBuiltins().date(date.janitorGetHostValue().minusWeeks(duration.getAmount()));
            case DAYS -> process.getBuiltins().date(date.janitorGetHostValue().minusDays(duration.getAmount()));
            case HOURS -> process.getBuiltins().dateTime(date.janitorGetHostValue().atStartOfDay().minusHours(duration.getAmount()));
            case MINUTES -> process.getBuiltins().dateTime(date.janitorGetHostValue().atStartOfDay().minusMinutes(duration.getAmount()));
            case SECONDS -> process.getBuiltins().dateTime(date.janitorGetHostValue().atStartOfDay().minusSeconds(duration.getAmount()));
        };
    }

    /**
     * Subtract a duration from a datetime.
     * @param dateTime the datetime
     * @param duration the duration
     * @return the new datetime
     * TODO wouldn't it be much simpler to add(datetime, -duration) here?
     */
    public static JanitorObject subtract(final JanitorScriptProcess process, final JDateTime dateTime, final JDuration duration) {
        if (duration.getUnit() == null || duration.getAmount() == 0) {
            return dateTime;
        }
        return switch (duration.getUnit()) {
            case YEARS -> process.getBuiltins().dateTime(dateTime.janitorGetHostValue().minusYears(duration.getAmount()));
            case MONTHS -> process.getBuiltins().dateTime(dateTime.janitorGetHostValue().minusMonths(duration.getAmount()));
            case WEEKS -> process.getBuiltins().dateTime(dateTime.janitorGetHostValue().minusWeeks(duration.getAmount()));
            case DAYS -> process.getBuiltins().dateTime(dateTime.janitorGetHostValue().minusDays(duration.getAmount()));
            case HOURS -> process.getBuiltins().dateTime(dateTime.janitorGetHostValue().minusHours(duration.getAmount()));
            case MINUTES -> process.getBuiltins().dateTime(dateTime.janitorGetHostValue().minusMinutes(duration.getAmount()));
            case SECONDS -> process.getBuiltins().dateTime(dateTime.janitorGetHostValue().minusSeconds(duration.getAmount()));
        };
    }


    public static JDuration newInstance(final Dispatcher<JDuration> dispatcher, final long amount, final JDurationKind unit) {
        return new JDuration(dispatcher, amount, unit);
    }

    /**
     * Kinds of durations, from second to year.
     */
    public enum JDurationKind {
        // I'm really not a big fan of these Doc comments, BUT I've decided to provide good Javadocs, and without them,
        // we get errors building the Javadoc. So here we are.
        /** Years  */  YEARS("y", ChronoUnit.YEARS),
        /** Months */  MONTHS("mo", ChronoUnit.MONTHS),
        /** Weeks */   WEEKS("w", ChronoUnit.WEEKS),
        /** Days */    DAYS("d", ChronoUnit.DAYS),
        /** Hours */   HOURS("h", ChronoUnit.HOURS),
        /** Minutes */ MINUTES("mi", ChronoUnit.MINUTES),
        /** Seconds */ SECONDS("s", ChronoUnit.SECONDS);

        public final String tag;
        public final TemporalUnit unit;

        /**
         * Create a new JDurationKind.
         * @param tag the tag, script side
         * @param unit the unit, java side
         */
        JDurationKind(final String tag, final TemporalUnit unit) {
            this.tag = tag;
            this.unit = unit;
        }

    }
}
