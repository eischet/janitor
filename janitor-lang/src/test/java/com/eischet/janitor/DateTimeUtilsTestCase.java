package com.eischet.janitor;

import com.eischet.janitor.json.impl.DateTimeUtils;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class DateTimeUtilsTestCase {


    @Test
    public void toZulu() {

        DateTimeUtils.getZoneId();

        assertNull(DateTimeUtils.localToZulu(null));

        final LocalDateTime local = LocalDateTime.of(2022, 1, 24, 10, 0, 0, 0);

        System.out.printf("local time:   %s\n", local);
        final ZonedDateTime atLocalZone = local.atZone(TimeZone.getDefault().toZoneId());
        System.out.printf("to zoned:     %s\n", atLocalZone);
        final ZonedDateTime atServerZone = local.atZone(DateTimeUtils.getZoneId());
        System.out.printf("to server:    %s\n", atServerZone);

        final ZonedDateTime utcTime = atServerZone.withZoneSameInstant(ZoneOffset.UTC);
        System.out.printf("at UTC:       %s\n", utcTime);

        final LocalDateTime utcLocal = utcTime.toLocalDateTime();
        System.out.printf("to local:     %s\n", utcLocal);


        final LocalDateTime converted = DateTimeUtils.localToZulu(
                LocalDateTime.of(2022, 1, 24, 10, 0, 0,0 )
        );

        assertEquals(utcTime.toString(), converted.toString() + "Z");
        assertEquals(utcLocal, converted);

        // this can't work when time rolls over:
        // final String expected = ZonedDateTime.now(ZoneOffset.UTC).toString().substring(0, 15);
        // final String received = DateTimeUtils.localToZulu(LocalDateTime.now()).toString().substring(0, 15);
        // We're cutting off the date strings because time passes between both ".now()" calls.
        // Expected: 2022-01-24T07:41:53.398186Z
        // Actual:   2022-01-24T07:41:53.409035
        //           ^0             ^15
        // *but that's not enough, because when the nanosecond is "full", the second rolls up, etc., so we might even
        // get a totally different hour of day or even a different day. or year, if someone runs this test on new year.
        // assertEquals(expected, received);
    }

    /* TODO: make this work; failed on github runners (of course)...
    @BeforeAll
    public static void setup() {
        DateTimeUtils.setTimeZoneSource(new DateTimeUtils.TimeZoneSource() {
            @Override
            public @Nullable ZoneId getLocalTimeZone() {
                return ZoneId.of("Europe/Berlin");
            }
        });
    }

    @Test public void datesTest() {
        final LocalDateTime desiredDate = LocalDateTime.of(2016, 2, 4, 0, 0, 0);
        final LocalDateTime zuluDate = DateTimeUtils.localToZulu(desiredDate);
        assertEquals(LocalDateTime.of(2016, 2, 3, 23, 0, 0), zuluDate);
    }

     */

}
