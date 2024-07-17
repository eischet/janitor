package com.eischet.janitor.runtime;

import com.eischet.janitor.api.i18n.JanitorFormatting;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.chrono.Chronology;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.FormatStyle;
import java.util.Locale;

public class JanitorFormattingLocale implements JanitorFormatting {

    private final DateTimeFormatter noSeconds;
    private final DateTimeFormatter hoursAndMinutes;
    private final DateTimeFormatter dateTimeFormatter;

    public JanitorFormattingLocale(final Locale locale) {
        String fullDateFormat = DateTimeFormatterBuilder.getLocalizedDateTimePattern(FormatStyle.MEDIUM,
                FormatStyle.MEDIUM,
                Chronology.ofLocale(locale),
                locale);
        String fullDateFormatWithoutSeconds = DateTimeFormatterBuilder.getLocalizedDateTimePattern(FormatStyle.MEDIUM,
                FormatStyle.SHORT,
                Chronology.ofLocale(locale),
                locale);
        noSeconds = DateTimeFormatter.ofPattern(fullDateFormatWithoutSeconds);
        hoursAndMinutes = DateTimeFormatter.ofPattern("HH:mm");
        dateTimeFormatter = DateTimeFormatter.ofPattern(fullDateFormat);
    }


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
