package com.eischet.janitor.cleanup.tools;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.*;
import java.util.Date;

public class DateTimeUtilities {

    private static final Logger log = LoggerFactory.getLogger(DateTimeUtilities.class);

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
            log.warn("error unpacking date-time value from long: {}", packed, e);
            return null;
        }
    }

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
            log.warn("error packing date-time value: {}", ldt, e);
            return 0;
        }
    }

    public static LocalDate unpackLocalDate(final long packed) {
        if (packed == 0) {
            return null;
        }
        return LocalDate.of(
            (int) ( packed / 100L / 100L / 100L / 100L / 100L ),
            (int) ( packed / 100L / 100L / 100L / 100L % 100L ),
            (int) ( packed / 100L / 100L / 100L % 100L )
        );
    }

    public static long packLocalDate(final long year, final long month, final long day) {
        return day * 100L * 100L * 100L +
            month * 100L * 100L * 100L * 100L +
            year * 100L * 100L * 100L * 100L * 100L;
    }

    public static long packLocalDate(final LocalDate ld) {
        if (ld == null) {
            return 0;
        }
        return
            ld.getDayOfMonth() * 100L * 100L * 100L +
            ld.getMonthValue() * 100L * 100L * 100L * 100L +
            ld.getYear() * 100L * 100L * 100L * 100L * 100L;
    }

    public static LocalDateTime utcFromEpochSeconds(final long ts) {
        return LocalDateTime.ofEpochSecond(ts, 0, ZoneOffset.UTC);
    }

    public static LocalDateTime localFromEpochSeconds(final long ts) {
        return ZonedDateTime.ofInstant(Instant.ofEpochSecond(ts), ZoneId.systemDefault()).toLocalDateTime();
    }

}
