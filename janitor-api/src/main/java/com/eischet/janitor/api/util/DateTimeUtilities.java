package com.eischet.janitor.api.util;

import java.time.*;
import java.util.Date;

/**
 * Utility class for date and time conversions.
 * These are, for example, useful when working with JDBC Timestamps.
 * TODO: these should be moved into the implementation module instead.
 */
public class DateTimeUtilities {

    /**
     * Converts a {@link Date} to a {@link LocalDateTime}.
     * @param oldDate the date to convert
     * @return the converted date
     */
    public static LocalDateTime convert(final Date oldDate) {
        if (oldDate == null) {
            return null;
        }
        // TODO: merge this into DateTimeUtils, because the ZoneId.systemDefault() is not a good idea. Over there, we have a user-settable alternative.
        return oldDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * Converts a {@link LocalDateTime} to a {@link Date}.
     * @param date the date to convert
     * @return the converted date
     */
    public static Date convert(final LocalDateTime date) {
        if (date == null) {
            return null;
        }
        return java.sql.Timestamp.valueOf(date);
    }


    /**
     * Converts a {@link LocalDateTime} to a {@link Long} representing the number of seconds since the epoch.
     * @param ts the timestamp to convert
     * @return the converted timestamp
     */
    public static LocalDateTime utcFromEpochSeconds(final long ts) {
        return LocalDateTime.ofEpochSecond(ts, 0, ZoneOffset.UTC);
    }

    /**
     * Converts a {@link Long} representing the number of seconds since the epoch to a {@link LocalDateTime}.
     * @param ts the timestamp to convert
     * @return the converted timestamp
     */
    public static LocalDateTime localFromEpochSeconds(final long ts) {
        // TODO: merge this into DateTimeUtils, because the ZoneId.systemDefault() is not a good idea. Over there, we have a user-settable alternative.
        return ZonedDateTime.ofInstant(Instant.ofEpochSecond(ts), ZoneId.systemDefault()).toLocalDateTime();
    }

}
