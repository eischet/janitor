package com.eischet.janitor.api.util;

import java.time.*;
import java.util.Date;

public class DateTimeUtilities {


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


    public static LocalDateTime utcFromEpochSeconds(final long ts) {
        return LocalDateTime.ofEpochSecond(ts, 0, ZoneOffset.UTC);
    }

    public static LocalDateTime localFromEpochSeconds(final long ts) {
        return ZonedDateTime.ofInstant(Instant.ofEpochSecond(ts), ZoneId.systemDefault()).toLocalDateTime();
    }

}
