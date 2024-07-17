package com.eischet.janitor;

import com.eischet.janitor.api.types.JDate;
import com.eischet.janitor.api.types.JDateTime;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class DatePackingTestCase {

    private static final LocalDateTime BORN_TIME = LocalDateTime.of(1976, 1, 10, 13, 30, 0);
    private static final long BORN_PACKED = 1976_01_10_13_30_00L;
    private static final LocalDate BORN_DATE = BORN_TIME.toLocalDate();
    private static final long BORN_DATE_PACKED = 1976_01_10_000000L;

    private static final LocalDateTime NOW = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    private static final LocalDate NOW_DATE = NOW.toLocalDate();

    @Test
    public void packLocalDateTimeTest() {
        assertEquals(0, JDateTime.packLocalDateTime(null));
        assertNull(JDateTime.unpackLocalDateTime(0));

        assertEquals(BORN_PACKED, JDateTime.packLocalDateTime(BORN_TIME));
        assertEquals(BORN_TIME, JDateTime.unpackLocalDateTime(BORN_PACKED));

        final long packedNow = JDateTime.packLocalDateTime(NOW);
        assertEquals(packedNow, JDateTime.packLocalDateTime(NOW));
        assertEquals(NOW, JDateTime.unpackLocalDateTime(packedNow));
    }

    @Test
    public void packLocalDateTest() {
        assertEquals(0, JDate.packLocalDate(null));
        assertNull(JDate.unpackLocalDate(0));
        assertEquals(BORN_DATE_PACKED, JDate.packLocalDate(BORN_DATE));
        assertEquals(BORN_DATE, JDate.unpackLocalDate(BORN_DATE_PACKED));
        assertEquals(BORN_DATE, JDate.unpackLocalDate(BORN_PACKED)); // but still, never cross these!
        assertEquals(NOW_DATE, JDate.unpackLocalDate(JDate.packLocalDate(NOW_DATE)));
        assertEquals(NOW_DATE, JDate.unpackLocalDate(JDate.packLocalDate(NOW.toLocalDate())));
    }

}
