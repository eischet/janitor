package com.eischet.janitor.internals;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.types.builtin.JDate;
import com.eischet.janitor.api.types.builtin.JDateTime;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Janitor-lang uses longs to pack dates and date-times, which needs testing.
 *
 * I'm pretty sure that later versions will remove the "date packing" feature, but that's how things work right now.
 * One huge advantage of this is that it's very easy to compare dates and date-times, and that they take very little RAM space.
 *
 * LocalDate and LocalDateTime are very convenient and have a neat API, but they gobble up <b>a lot</b> of RAM when used in great numbers.
 */
public class DatePackingTestCase extends JanitorTest {

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
