package com.eischet.janitor.api.types.builtin;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorNameException;
import com.eischet.janitor.api.types.JConstant;
import com.eischet.janitor.api.types.JanitorObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

/**
 * A duration object, representing a time span.
 * This is one of the built-in types that Janitor provides automatically.
 */
public class JDuration implements JConstant, Comparable<JDuration> {

    private final long amount;
    private final JDurationKind unit;

    /**
     * Create a new JDuration.
     * @param amount the amount
     * @param unit the unit
     */
    public JDuration(final long amount, final JDurationKind unit) {
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
    public static JanitorObject add(final JDate date, final JDuration duration) {
        if (duration.getUnit() == null || duration.getAmount() == 0) {
            return date;
        }
        return switch (duration.getUnit()) {
            case YEARS -> new JDate(date.janitorGetHostValue().plusYears(duration.getAmount()));
            case MONTHS -> new JDate(date.janitorGetHostValue().plusMonths(duration.getAmount()));
            case WEEKS -> new JDate(date.janitorGetHostValue().plusWeeks(duration.getAmount()));
            case DAYS -> new JDate(date.janitorGetHostValue().plusDays(duration.getAmount()));
            case HOURS -> new JDateTime(date.janitorGetHostValue().atStartOfDay().plusHours(duration.getAmount()));
            case MINUTES -> new JDateTime(date.janitorGetHostValue().atStartOfDay().plusMinutes(duration.getAmount()));
            case SECONDS -> new JDateTime(date.janitorGetHostValue().atStartOfDay().plusSeconds(duration.getAmount()));
        };
    }

    /**
     * Add a duration to a datetime.
     * @param dateTime the datetime
     * @param duration the duration
     * @return the new datetime
     */
    public static JanitorObject add(final JDateTime dateTime, final JDuration duration) {
        if (duration.getUnit() == null || duration.getAmount() == 0) {
            return dateTime;
        }
        return switch (duration.getUnit()) {
            case YEARS -> new JDateTime(dateTime.janitorGetHostValue().plusYears(duration.getAmount()));
            case MONTHS -> new JDateTime(dateTime.janitorGetHostValue().plusMonths(duration.getAmount()));
            case WEEKS -> new JDateTime(dateTime.janitorGetHostValue().plusWeeks(duration.getAmount()));
            case DAYS -> new JDateTime(dateTime.janitorGetHostValue().plusDays(duration.getAmount()));
            case HOURS -> new JDateTime(dateTime.janitorGetHostValue().plusHours(duration.getAmount()));
            case MINUTES -> new JDateTime(dateTime.janitorGetHostValue().plusMinutes(duration.getAmount()));
            case SECONDS -> new JDateTime(dateTime.janitorGetHostValue().plusSeconds(duration.getAmount()));
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
    public static JanitorObject subtract(final JDate date, final JDuration duration) {
        if (duration.getUnit() == null || duration.getAmount() == 0) {
            return date;
        }
        return switch (duration.getUnit()) {
            case YEARS -> new JDate(date.janitorGetHostValue().minusYears(duration.getAmount()));
            case MONTHS -> new JDate(date.janitorGetHostValue().minusMonths(duration.getAmount()));
            case WEEKS -> new JDate(date.janitorGetHostValue().minusWeeks(duration.getAmount()));
            case DAYS -> new JDate(date.janitorGetHostValue().minusDays(duration.getAmount()));
            case HOURS -> new JDateTime(date.janitorGetHostValue().atStartOfDay().minusHours(duration.getAmount()));
            case MINUTES -> new JDateTime(date.janitorGetHostValue().atStartOfDay().minusMinutes(duration.getAmount()));
            case SECONDS -> new JDateTime(date.janitorGetHostValue().atStartOfDay().minusSeconds(duration.getAmount()));
        };
    }

    /**
     * Subtract a duration from a datetime.
     * @param dateTime the datetime
     * @param duration the duration
     * @return the new datetime
     * TODO wouldn't it be much simpler to add(datetime, -duration) here?
     */
    public static JanitorObject subtract(final JDateTime dateTime, final JDuration duration) {
        if (duration.getUnit() == null || duration.getAmount() == 0) {
            return dateTime;
        }
        return switch (duration.getUnit()) {
            case YEARS -> new JDateTime(dateTime.janitorGetHostValue().minusYears(duration.getAmount()));
            case MONTHS -> new JDateTime(dateTime.janitorGetHostValue().minusMonths(duration.getAmount()));
            case WEEKS -> new JDateTime(dateTime.janitorGetHostValue().minusWeeks(duration.getAmount()));
            case DAYS -> new JDateTime(dateTime.janitorGetHostValue().minusDays(duration.getAmount()));
            case HOURS -> new JDateTime(dateTime.janitorGetHostValue().minusHours(duration.getAmount()));
            case MINUTES -> new JDateTime(dateTime.janitorGetHostValue().minusMinutes(duration.getAmount()));
            case SECONDS -> new JDateTime(dateTime.janitorGetHostValue().minusSeconds(duration.getAmount()));
        };
    }

    /**
     * Calculate the difference between two dates.
     * Time flies from the left to the right in this context, in case you're wondering about the parameter names.
     * @param left the left date
     * @param right the right date
     * @return the duration between the two dates
     */
    public static JDuration between(final JDate left, final JDate right) {
        return new JDuration(Duration.between(right.janitorGetHostValue().atStartOfDay(), left.janitorGetHostValue().atStartOfDay()).toDays(), JDurationKind.DAYS);
    }

    /**
     * Calculate the difference between two datetimes.
     * Time flies from the left to the right in this context, in case you're wondering about the parameter names.
     * @param left the left date
     * @param right the right date
     * @return the duration between the two dates
     */
    public static JDuration between(final JDateTime left, final JDateTime right) {
        return new JDuration(Duration.between(right.janitorGetHostValue(), left.janitorGetHostValue()).toSeconds(), JDurationKind.SECONDS);
    }


    /**
     * Create a duration from a string.
     * @param text a string
     * @return a duraction, of null when not valid
     */
    public static @Nullable JDuration of(final String text) {
        for (final JDurationKind value : JDurationKind.values()) {
            if (text.endsWith(value.tag)) {
                return new JDuration(Long.parseLong(text.substring(0, text.length() - value.tag.length())), value);
            }
        }
        return null;
    }



    @Override
    public @Nullable JanitorObject janitorGetAttribute(final JanitorScriptProcess runningScript, final String name, final boolean required) throws JanitorNameException {
        // TODO: move this to a proper dispatch table
        if ("seconds".equals(name)) {
            return runningScript.getEnvironment().getBuiltins().integer(toSeconds());
        }
        if ("minutes".equals(name)) {
            return runningScript.getEnvironment().getBuiltins().integer(toSeconds() / 60);
        }
        if ("hours".equals(name)) {
            return runningScript.getEnvironment().getBuiltins().integer(toSeconds() / 3600);
        }
        if ("days".equals(name)) {
            return runningScript.getEnvironment().getBuiltins().integer(toSeconds() / 86400);
        }
        if ("weeks".equals(name)) {
            return runningScript.getEnvironment().getBuiltins().integer(toSeconds() / 604800);
        }
        return JConstant.super.janitorGetAttribute(runningScript, name, required);
    }

    /**
     * Kinds of durations, from second to year.
     */
    public enum JDurationKind {
        // I'm really not a big fan of these Doc comments, BUT I've decided to provide good Javadocs, and without them
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
