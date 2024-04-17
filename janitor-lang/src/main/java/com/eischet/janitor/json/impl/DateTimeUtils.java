/*
 * © Eischet Software e.K., Köln
 */

package com.eischet.janitor.json.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.attribute.FileTime;
import java.time.*;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

public class DateTimeUtils {

    private static final Logger log = LoggerFactory.getLogger(DateTimeUtils.class);

    public interface TimeZoneSource {
        @Nullable ZoneId getLocalTimeZone();
    }

    private static TimeZoneSource timeZoneSource;

    public static void setTimeZoneSource(final TimeZoneSource timeZoneSource) {
        DateTimeUtils.timeZoneSource = timeZoneSource;
        if (stz != null) {
            stz = null;
            log.info("clearing time zone source");
            getZoneId();
        }
    }

    private static ZoneId stz = null;

    public static ZoneId getZoneId() {
        if (stz == null) {
            if (timeZoneSource != null) {
                stz = timeZoneSource.getLocalTimeZone();
            }
            if (stz == null) {
                stz = TimeZone.getDefault().toZoneId();
                log.error("cannot get server time zone from app config, using fallback: {}", stz);
            }
            log.info("initialized time stamp interpreter time zone to: {}", stz);
        }
        return stz;
    }

    @NotNull
    public static LocalDateTime fromSystemTimeMillis(final long ts) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(ts), getZoneId());
    }

    public static long asEpochMilliseconds(final LocalDateTime localDateTime) {
        return localDateTime.atZone(getZoneId()).toEpochSecond() * 1000;
    }

    public static LocalDateTime fromLong(final long longValue) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(longValue), getZoneId());
    }

    public static ZonedDateTime toZonedDateTime(final FileTime lastModifiedTime) {
        return lastModifiedTime.toInstant().atZone(getZoneId());
    }

    public static ZonedDateTime toZonedDateTime(final LocalDateTime localDateTime) {
        return localDateTime.atZone(getZoneId());
    }

    public static LocalDateTime convert(final Date oldDate) {
        if (oldDate == null) {
            return null;
        }
        return oldDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static Date convert(final LocalDateTime date) {
        if (date == null) {
            return null;
        }
        return java.sql.Timestamp.valueOf(date);
    }

    public static String toJsonZulu(final LocalDateTime value) {
        final LocalDateTime converted = localToZulu(value);
        if (converted == null) {
            return null;
        }
        return converted.toString() + "Z";
    }

    public static LocalDateTime localToZulu(final LocalDateTime value) {
        if (value == null) {
            return null;
        }
        return value.atZone(getZoneId()).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
    }

    public List<ZoneId> getAvailableTimezoneInstances() {
        return ZoneId.getAvailableZoneIds().stream().sorted().map(ZoneId::of).collect(Collectors.toList());
    }

}
