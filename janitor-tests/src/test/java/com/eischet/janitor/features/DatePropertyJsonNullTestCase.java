// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.features;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * An object with date properties reads the JSON it writes itself, also when a date is empty and written as null.
 */
public class DatePropertyJsonNullTestCase extends JanitorTest {

    private static class Appointment extends JanitorComposed<Appointment> {
        public static final DispatchTable<Appointment> DISPATCH = new DispatchTable<>(Appointment::new);

        static {
            DISPATCH.addDateProperty("day", self -> self.day, (self, value) -> self.day = value);
            DISPATCH.addDateTimeProperty("start", self -> self.start, (self, value) -> self.start = value);
            DISPATCH.addStringProperty("title", self -> self.title, (self, value) -> self.title = value);
        }

        private LocalDate day = LocalDate.of(2020, 1, 1);
        private LocalDateTime start = LocalDateTime.of(2020, 1, 1, 12, 0);
        private String title;

        Appointment() {
            super(DISPATCH);
        }
    }

    @Test
    void nullDateIsReadAndTheRestOfTheObjectToo() throws Exception {
        final Appointment appointment = Appointment.DISPATCH.readFromJson(Appointment::new, "{\"day\":null,\"title\":\"review\"}");
        assertNull(appointment.day);
        assertEquals("review", appointment.title);
    }

    @Test
    void nullDateTimeIsReadAndTheRestOfTheObjectToo() throws Exception {
        final Appointment appointment = Appointment.DISPATCH.readFromJson(Appointment::new, "{\"start\":null,\"title\":\"review\"}");
        assertNull(appointment.start);
        assertEquals("review", appointment.title);
    }

    @Test
    void datesAreStillRead() throws Exception {
        final Appointment original = new Appointment();
        original.day = LocalDate.of(2026, 11, 27);
        original.start = LocalDateTime.of(2026, 11, 27, 9, 30);
        original.title = "review";
        final Appointment copy = Appointment.DISPATCH.readFromJson(Appointment::new, Appointment.DISPATCH.writeToJson(original));
        assertEquals(original.day, copy.day);
        assertEquals(original.start, copy.start);
        assertEquals("review", copy.title);
    }

}
