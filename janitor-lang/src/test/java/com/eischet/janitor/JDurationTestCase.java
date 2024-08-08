package com.eischet.janitor;

import com.eischet.janitor.api.RunnableScript;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.errors.runtime.JanitorTypeException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JDuration;
import com.eischet.janitor.runtime.OutputCatchingTestRuntime;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class JDurationTestCase {

    final OutputCatchingTestRuntime rt = OutputCatchingTestRuntime.fresh();

    private JDuration make(final long value, final JDuration.JDurationKind kind) {
        return rt.getEnvironment().getBuiltins().duration(value, kind);
    }

    private void testDurationMethod(final String script, final Object expectedResult) throws JanitorCompilerException, JanitorRuntimeException {
        final RunnableScript runnableScript = rt.compile("test", script);
        final @NotNull JanitorObject result = runnableScript.run();
        final Object actualResult = result.janitorGetHostValue();
        assertEquals(expectedResult, actualResult, script);
    }

    @Test
    public void testDurations() throws JanitorRuntimeException, JanitorCompilerException {
        // Simple instances
        testDurationMethod("@1y", make(1, JDuration.JDurationKind.YEARS));
        testDurationMethod("@1mo", make(1, JDuration.JDurationKind.MONTHS));
        testDurationMethod("@1w", make(1, JDuration.JDurationKind.WEEKS));
        testDurationMethod("@1d", make(1, JDuration.JDurationKind.DAYS));
        testDurationMethod("@1h", make(1, JDuration.JDurationKind.HOURS));
        testDurationMethod("@1mi", make(1, JDuration.JDurationKind.MINUTES));
        testDurationMethod("@1s", make(1, JDuration.JDurationKind.SECONDS));
        // More simple instances
        testDurationMethod("@2y", make(2, JDuration.JDurationKind.YEARS));
        testDurationMethod("@2mo", make(2, JDuration.JDurationKind.MONTHS));
        testDurationMethod("@2w", make(2, JDuration.JDurationKind.WEEKS));
        testDurationMethod("@2d", make(2, JDuration.JDurationKind.DAYS));
        testDurationMethod("@2h", make(2, JDuration.JDurationKind.HOURS));
        testDurationMethod("@2mi", make(2, JDuration.JDurationKind.MINUTES));
        testDurationMethod("@2s", make(2, JDuration.JDurationKind.SECONDS));
        // Basic equality
        testDurationMethod("@1y == @1y", Boolean.TRUE);
        testDurationMethod("@1mo == @1mo", Boolean.TRUE);
        testDurationMethod("@1w == @1w", Boolean.TRUE);
        testDurationMethod("@1d == @1d", Boolean.TRUE);
        testDurationMethod("@1h == @1h", Boolean.TRUE);
        testDurationMethod("@1mi == @1mi", Boolean.TRUE);
        testDurationMethod("@1s == @1s", Boolean.TRUE);
        testDurationMethod("@1w == @7d", Boolean.TRUE);
        // Simple comparisons
        testDurationMethod("@2y > @1y", Boolean.TRUE);
        testDurationMethod("@2mo > @1mo", Boolean.TRUE);
        testDurationMethod("@2w > @1w", Boolean.TRUE);
        testDurationMethod("@2d > @1d", Boolean.TRUE);
        testDurationMethod("@2h > @1h", Boolean.TRUE);
        testDurationMethod("@2mi > @1mi", Boolean.TRUE);
        testDurationMethod("@2s > @1s", Boolean.TRUE);
        testDurationMethod("@1w < @8d", Boolean.TRUE);
        testDurationMethod("@1w > @6d", Boolean.TRUE);
        // Mixed comparisons
        testDurationMethod("@60s == @1mi", Boolean.TRUE);
        testDurationMethod("@61s > @1mi", Boolean.TRUE);
        testDurationMethod("@1mi <= @61s", Boolean.TRUE);
        testDurationMethod("@61s <= @2mi", Boolean.TRUE);
        testDurationMethod("@13mo > @1y", Boolean.TRUE);
        testDurationMethod("@13mo < @1y", Boolean.FALSE);
        // Duration calculations
        testDurationMethod("@1y + @2y", make(3, JDuration.JDurationKind.YEARS));
        testDurationMethod("@1d + @8d - @2d", make(1, JDuration.JDurationKind.WEEKS));
        testDurationMethod("@24h + @24h == @2d", Boolean.TRUE);

        // TODO: I'm not sure if the signs are correct or not here: 10:00 to 12:00 is 2 hours, but yields "-2" here.
        testDurationMethod("@2024-08-07-12:00 - @2024-08-07-11:00", make(1, JDuration.JDurationKind.HOURS));
        testDurationMethod("@2024-08-07-12:00 - @2024-08-07-10:00", make(2, JDuration.JDurationKind.HOURS));
        testDurationMethod("@2024-08-07-10:00 - @2024-08-07-12:00", make(-2, JDuration.JDurationKind.HOURS));

        // Durations can be multiplied and divided by numbers
        testDurationMethod("2 * @1h", make(120, JDuration.JDurationKind.MINUTES));
        testDurationMethod("@1h * 2", make(2, JDuration.JDurationKind.HOURS));
        testDurationMethod("@1d / 2", make(12, JDuration.JDurationKind.HOURS));
        // Division of durations by numbers is fine, but I think we cannot divide a number by a duration, so:
        assertThrows(JanitorTypeException.class, () -> testDurationMethod("2 / @1d", make(12, JDuration.JDurationKind.HOURS)));

    }

    @Test
    public void testDurationMethods() throws JanitorRuntimeException, JanitorCompilerException {
        testDurationMethod("(@1mi).seconds", 60L);
        testDurationMethod("(@1h).minutes", 60L);
        testDurationMethod("(@1d).hours", 24L);
        testDurationMethod("(@2d).hours", 48L);
        testDurationMethod("(@7d).weeks", 1L);
        testDurationMethod("(@14d).weeks", 2L);
        testDurationMethod("(@3w).weeks", 3L);
        testDurationMethod("(@3w).days", 21L);
    }

    @Test
    public void testInvalidOperations() throws JanitorRuntimeException, JanitorCompilerException {
        assertThrows(JanitorTypeException.class, () -> testDurationMethod("@12h * @12h", null));
        assertThrows(JanitorTypeException.class, () -> testDurationMethod("@12h / @12h", null));
        assertThrows(JanitorTypeException.class, () -> testDurationMethod("@today / @12h", null));
        assertThrows(JanitorTypeException.class, () -> testDurationMethod("@today * @12h", null));
        assertThrows(JanitorTypeException.class, () -> testDurationMethod("@12h * @today", null));
        assertThrows(JanitorTypeException.class, () -> testDurationMethod("@12h / @today", null));

        assertThrows(JanitorTypeException.class, () -> testDurationMethod("@1d + 1", null));
        assertThrows(JanitorTypeException.class, () -> testDurationMethod("1 + @1d", null));
        assertThrows(JanitorTypeException.class, () -> testDurationMethod("@1d - 1", null));
        assertThrows(JanitorTypeException.class, () -> testDurationMethod("1 - @1d", null));
    }

}
