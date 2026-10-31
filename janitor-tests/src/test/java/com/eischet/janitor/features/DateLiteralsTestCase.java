// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.features;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.runtime.OutputCatchingTestRuntime;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Date and datetime literals are read strictly: dates and times that do not exist are compile errors, instead of being
 * moved silently to the end of the month or the next day. parseDate stays lenient, because scripts give their own patterns.
 */
public class DateLiteralsTestCase extends JanitorTest {

    private static JanitorObject run(final String source) throws JanitorCompilerException, JanitorRuntimeException {
        return OutputCatchingTestRuntime.fresh().compile("test", source).run();
    }

    private static void assertInvalid(final String source, final String expectedText) {
        final JanitorCompilerException e = assertThrows(JanitorCompilerException.class, () -> run(source), source);
        assertTrue(e.getMessage().contains(expectedText), e.getMessage());
    }

    @Test
    void validLiterals() throws Exception {
        assertEquals("@2024-02-29", run("@2024-02-29").janitorToString());
        assertEquals("@2024-03-05-14:30:00", run("@2024-03-05-14:30").janitorToString());
        assertEquals("@2024-03-05-23:59:59", run("@2024-03-05-23:59:59").janitorToString());
        assertEquals("@0001-01-01", run("@0001-01-01").janitorToString());
        // month and day with one digit
        assertEquals("@2026-01-05", run("@2026-1-5").janitorToString());
        assertEquals("@2026-11-05", run("@2026-11-5").janitorToString());
        assertEquals("@2026-03-05-09:30:00", run("@2026-3-05-09:30").janitorToString());
        assertEquals("@2026-03-05-09:30:07", run("@2026-03-5-09:30:07").janitorToString());
        assertEquals("date", run("@today.class").janitorToString());
        assertEquals("datetime", run("@now.class").janitorToString());
    }

    @Test
    void datesThatDoNotExist() {
        assertInvalid("@2024-02-30", "invalid date literal: @2024-02-30");
        assertInvalid("@2023-02-29", "invalid date literal");
        assertInvalid("@2024-04-31", "invalid date literal");
        assertInvalid("@2024-13-01", "invalid date literal");
        assertInvalid("@0000-01-01", "invalid date literal");
        assertInvalid("@2024-2-30", "invalid date literal: @2024-2-30");
        assertInvalid("x = 1;\nd = @2024-02-30;", "line 2");
    }

    @Test
    void datetimesThatDoNotExist() {
        assertInvalid("@2024-02-30-10:00", "invalid datetime literal");
        assertInvalid("@2024-01-01-24:00", "invalid datetime literal");
        assertInvalid("@2024-01-01-23:60", "invalid datetime literal");
        assertInvalid("@2024-01-01-9:30", "invalid datetime literal"); // the hour needs two digits
    }

    @Test
    void negativeYearsAreNotPartOfTheGrammar() {
        assertThrows(JanitorCompilerException.class, () -> run("@-2024-01-01"));
        assertThrows(JanitorCompilerException.class, () -> run("@-2024-01-01-10:00"));
    }

    @Test
    void parseDateStaysLenient() throws Exception {
        assertEquals("@2024-02-29", run("\"2024-02-30\".parseDate(\"yyyy-MM-dd\")").janitorToString());
    }
}
