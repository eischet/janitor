package com.eischet.janitor.runtime;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.chrono.Chronology;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.FormatStyle;
import java.util.Locale;

public class JanitorFormattingGerman implements JanitorFormatting {
    private final String fullDateFormatWithoutSeconds = DateTimeFormatterBuilder.getLocalizedDateTimePattern(FormatStyle.MEDIUM, FormatStyle.SHORT, Chronology.ofLocale(Locale.GERMANY), Locale.GERMANY);
    private final DateTimeFormatter noSeconds = DateTimeFormatter.ofPattern(fullDateFormatWithoutSeconds);
    private final DateTimeFormatter hoursAndMinutes = DateTimeFormatter.ofPattern("HH:mm");
    private final String fullDateFormat = DateTimeFormatterBuilder.getLocalizedDateTimePattern(FormatStyle.MEDIUM, FormatStyle.MEDIUM, Chronology.ofLocale(Locale.GERMANY), Locale.GERMANY);
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(fullDateFormat);

    @Override
    public String asTimeString(final LocalDateTime hostValue) {
        return hoursAndMinutes.format(hostValue);
    }

    @Override
    public String formatDateTime(final LocalDateTime hostValue) {
        return dateTimeFormatter.format(hostValue);
    }

    @Override
    public String formatDateTime(final ZonedDateTime hostValue) {
        return dateTimeFormatter.format(hostValue);
    }

    @Override
    public String formatDateTimeNoSeconds(final LocalDateTime hostValue) {
        return noSeconds.format(hostValue);
    }

}
