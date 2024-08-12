package com.eischet.janitor.api.i18n;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

/**
 * Interface for formatting date and time values.
 * Being an embedded scripting language, Janitor will rely on a host to supply this functionality.
 * Which means you, as a host, have full control over these aspects, which is good.
 */
public interface JanitorFormatting {
    /**
     * Formats a date time value as a string.
     * @param hostValue the date time value
     * @return the formatted string
     */
    String asTimeString(final LocalDateTime hostValue);

    /**
     * Formats a date time value as a string.
     * @param hostValue the date time value
     * @return the formatted string
     */
    String formatDateTime(final LocalDateTime hostValue);

    /**
     * Formats a date time value as a string.
     * @param hostValue the date time value
     * @return the formatted string
     */
    String formatDateTime(final ZonedDateTime hostValue);

    /**
     * Formates a date time value as a string, without seconds.
     * @param hostValue the date time value
     * @return the formatted string
     */
    String formatDateTimeNoSeconds(final LocalDateTime hostValue);
}
