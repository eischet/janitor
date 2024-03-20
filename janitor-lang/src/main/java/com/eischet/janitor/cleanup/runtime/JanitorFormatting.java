package com.eischet.janitor.cleanup.runtime;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

public interface JanitorFormatting {
    String asTimeString(LocalDateTime hostValue);
    String formatDateTime(LocalDateTime hostValue);
    String formatDateTime(ZonedDateTime hostValue);
    String formatDateTimeNoSeconds(final LocalDateTime hostValue);
}
