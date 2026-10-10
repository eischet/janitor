package com.eischet.janitor.json.impl;

import com.eischet.janitor.logging.JanitorLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.attribute.FileTime;
import java.time.*;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

/**
 * Utility functions for working with date and time.
 */
public class DateTimeUtils {

    private static final JanitorLogger log = JanitorLogger.getLogger(DateTimeUtils.class);

    /** Supplies the time zone that is used to interpret local date and time values, e.g. from an application's configuration. */
    public interface TimeZoneSource {
        @Nullable ZoneId getLocalTimeZone();
    }

    private static TimeZoneSource timeZoneSource;

    /**
     * Sets the source of the time zone to use, and resets the time zone that was determined so far.
     * @param timeZoneSource the new source
     */
    public static void setTimeZoneSource(final TimeZoneSource timeZoneSource) {
        DateTimeUtils.timeZoneSource = timeZoneSource;
        if (stz != null) {
            stz = null;
            log.info("clearing time zone source");
            getZoneId();
        }
    }

    private static ZoneId stz = null;

    private static boolean hasInitiaizledStz = false;

    /**
     * Returns the time zone for interpreting local date and time values: the one given by the {@link TimeZoneSource}, if any,
     * or else the default time zone of the JVM.
     * @return the time zone
     */
    public static ZoneId getZoneId() {
        if (stz == null) {
            if (timeZoneSource != null) {
                stz = timeZoneSource.getLocalTimeZone();
            }
            if (stz == null) {
                stz = TimeZone.getDefault().toZoneId();
                if (!hasInitiaizledStz) {
                    log.info("cannot get server time zone from app config, using fallback: {}", stz);
                }
            }
            if (!hasInitiaizledStz) {
                log.info("initialized time stamp interpreter time zone to: {}", stz);
            }
            hasInitiaizledStz = true;
        }
        return stz;
    }

    /**
     * Converts milliseconds since the epoch to a local datetime.
     * @param ts the milliseconds since the epoch
     * @return the datetime in the configured time zone
     */
    @NotNull
    public static LocalDateTime fromSystemTimeMillis(final long ts) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(ts), getZoneId());
    }

    /**
     * Converts a local datetime to milliseconds since the epoch, with second precision.
     * @param localDateTime the datetime in the configured time zone
     * @return the milliseconds since the epoch
     */
    public static long asEpochMilliseconds(final LocalDateTime localDateTime) {
        return localDateTime.atZone(getZoneId()).toEpochSecond() * 1000;
    }

    /**
     * Converts milliseconds since the epoch to a local datetime.
     * @param longValue the milliseconds since the epoch
     * @return the datetime in the configured time zone
     */
    public static LocalDateTime fromLong(final long longValue) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(longValue), getZoneId());
    }

    /**
     * Converts a file time to a zoned datetime.
     * @param lastModifiedTime the file time
     * @return the datetime in the configured time zone
     */
    public static ZonedDateTime toZonedDateTime(final FileTime lastModifiedTime) {
        return lastModifiedTime.toInstant().atZone(getZoneId());
    }

    /**
     * Attaches the configured time zone to a local datetime.
     * @param localDateTime the local datetime
     * @return the zoned datetime
     */
    public static ZonedDateTime toZonedDateTime(final LocalDateTime localDateTime) {
        return localDateTime.atZone(getZoneId());
    }

    /**
     * Converts a legacy {@link Date} to a local datetime.
     * @param oldDate the date, may be null
     * @return the datetime in the configured time zone, or null if the date is null
     */
    public static LocalDateTime convert(final Date oldDate) {
        if (oldDate == null) {
            return null;
        }
        return oldDate.toInstant().atZone(getZoneId()).toLocalDateTime();
    }

    /**
     * Converts a legacy {@link Date} to a local date.
     * @param oldDate the date, may be null
     * @return the date in the configured time zone, or null if the date is null
     */
    public static LocalDate convertDateToLocalDate(final Date oldDate) {
        if (oldDate == null) {
            return null;
        }
        return oldDate.toInstant().atZone(getZoneId()).toLocalDate();
    }


    /**
     * Converts a local datetime to a legacy {@link Date}, namely a {@link java.sql.Timestamp}.
     * @param date the datetime, may be null
     * @return the timestamp, or null if the datetime is null
     */
    public static Date convert(final LocalDateTime date) {
        if (date == null) {
            return null;
        }
        return java.sql.Timestamp.valueOf(date);
    }

    /**
     * Formats a local datetime as an ISO string in UTC (Zulu time), as used in JSON.
     * @param value the datetime in the configured time zone, may be null
     * @return the string, with a trailing "Z", or null if the value is null
     */
    public static String toJsonZulu(final LocalDateTime value) {
        final LocalDateTime converted = localToZulu(value);
        if (converted == null) {
            return null;
        }
        return converted.toString() + "Z";
    }

    /**
     * Converts a local datetime to UTC.
     * @param value the datetime in the configured time zone, may be null
     * @return the datetime in UTC, or null if the value is null
     */
    public static LocalDateTime localToZulu(final LocalDateTime value) {
        if (value == null) {
            return null;
        }
        return value.atZone(getZoneId()).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
    }

    /**
     * @return all time zones that the JVM knows, sorted by their IDs
     */
    public List<ZoneId> getAvailableTimezoneInstances() {
        return ZoneId.getAvailableZoneIds().stream().sorted().map(ZoneId::of).collect(Collectors.toList());
    }

}
