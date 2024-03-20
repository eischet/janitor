package com.eischet.janitor.cleanup.api.api.types;

import com.eischet.janitor.cleanup.api.api.errors.runtime.JanitorNameException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;

public class JDuration implements JConstant, Comparable<JDuration> {

    private final long amount;
    private final JDurationKind unit;


    public JDuration(final long amount, final JDurationKind unit) {
        this.amount = amount;
        this.unit = unit;
    }

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

    @Override
    public boolean janitorIsTrue() {
        return amount != 0;
    }

    @Override
    public String toString() {
        return "@" + amount + unit.tag;
    }

    public long getAmount() {
        return amount;
    }

    public JDurationKind getUnit() {
        return unit;
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof JDuration dur && dur.toSeconds() == toSeconds();
    }

    @Override
    public int hashCode() {
        return Long.hashCode(toSeconds());
    }

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

    public static JDuration between(final JDate left, final JDate right) {
        return new JDuration(Duration.between(right.janitorGetHostValue().atStartOfDay(), left.janitorGetHostValue().atStartOfDay()).toDays(), JDurationKind.DAYS);
    }

    public static JDuration between(final JDateTime left, final JDateTime right) {
        return new JDuration(Duration.between(right.janitorGetHostValue(), left.janitorGetHostValue()).toSeconds(), JDurationKind.SECONDS);
    }

    // ACHTUNG: bei between wird gedreht, da wir hier SUBTRAKTION implementieren. Daten sind irgendwie immer verwirrend.


    public static JDuration of(final String text) {
        for (final JDurationKind value : JDurationKind.values()) {
            if (text.endsWith(value.tag)) {
                return new JDuration(Long.parseLong(text.substring(0, text.length() - value.tag.length())), value);
            }
        }
        return null;
    }

    @Override
    public @Nullable JanitorObject janitorGetAttribute(final JanitorScriptProcess runningScript, final String name, final boolean required) throws JanitorNameException {
        if ("seconds".equals(name)) {
            return JInt.of(toSeconds());
        }
        if ("minutes".equals(name)) {
            return JInt.of(toSeconds() / 60);
        }
        if ("hours".equals(name)) {
            return JInt.of(toSeconds() / 3600);
        }
        if ("days".equals(name)) {
            return JInt.of(toSeconds() / 86400);
        }
        if ("weeks".equals(name)) {
            return JInt.of(toSeconds() / 604800);
        }
        return JConstant.super.janitorGetAttribute(runningScript, name, required);
    }

}
