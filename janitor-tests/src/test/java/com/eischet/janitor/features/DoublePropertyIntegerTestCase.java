// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.features;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A double property takes any number a script assigns: script authors write whole numbers without a decimal point.
 */
public class DoublePropertyIntegerTestCase extends JanitorTest {

    private static class Booking extends JanitorComposed<Booking> {
        public static final DispatchTable<Booking> DISPATCH = new DispatchTable<>(Booking::new);

        static {
            DISPATCH.addDoubleProperty("minutes", self -> self.minutes, (self, value) -> self.minutes = value);
        }

        private double minutes;

        Booking() {
            super(DISPATCH);
        }
    }

    @Test
    void wholeNumberIsAssigned() throws Exception {
        final Booking booking = new Booking();
        evaluate("booking.minutes = 5;", g -> g.bind("booking", booking));
        assertEquals(5.0, booking.minutes);
    }

    @Test
    void wholeNumberIsApplied() throws Exception {
        final Booking booking = new Booking();
        evaluate("booking.apply({minutes: 5});", g -> g.bind("booking", booking));
        assertEquals(5.0, booking.minutes);
    }

    @Test
    void floatingPointNumberIsStillAssigned() throws Exception {
        final Booking booking = new Booking();
        evaluate("booking.minutes = 2.5;", g -> g.bind("booking", booking));
        assertEquals(2.5, booking.minutes);
    }

}
