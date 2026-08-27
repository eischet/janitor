package com.eischet.janitor.internals;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.RunnableScript;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.runtime.OutputCatchingTestRuntime;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Regression tests for JCallArgs.getRequiredIntValue(): it used to only check the upper overflow
 * bound ({@code toLong() > Integer.MAX_VALUE}), not the lower one, so a strongly negative JInt (e.g.
 * -5_000_000_000, well below Integer.MIN_VALUE) was silently truncated by the narrowing {@code (int)}
 * cast instead of raising a JanitorArgumentException like the symmetric positive-overflow case
 * already did.
 */
public class JCallArgsTestCase extends JanitorTest {

    private static final class Probe extends JanitorComposed<Probe> {
        private static final DispatchTable<Probe> DISPATCH = new DispatchTable<>(null);

        static {
            DISPATCH.addMethod("intArg", (self, process, arguments) -> Janitor.integer(arguments.getRequiredIntValue(0)));
        }

        Probe() {
            super(DISPATCH);
        }
    }

    private int intArg(final String literal) throws JanitorRuntimeException, JanitorCompilerException {
        final OutputCatchingTestRuntime rt = OutputCatchingTestRuntime.fresh();
        final RunnableScript script = rt.compile("test", "return probe.intArg(" + literal + ");");
        return ((Number) script.run(g -> g.bind("probe", new Probe())).janitorGetHostValue()).intValue();
    }

    @Test
    public void inRangeValuesWork() throws Exception {
        assertEquals(17, intArg("17"));
        assertEquals(-17, intArg("-17"));
        assertEquals(Integer.MAX_VALUE, intArg(String.valueOf(Integer.MAX_VALUE)));
        assertEquals(Integer.MIN_VALUE, intArg(String.valueOf(Integer.MIN_VALUE)));
    }

    @Test
    public void tooLargePositiveValueRaisesAnError() {
        assertThrows(JanitorArgumentException.class, () -> intArg("5000000000"));
    }

    @Test
    public void tooLargeNegativeValueRaisesAnError() {
        // This is the case that used to be silently truncated instead of raising an error.
        assertThrows(JanitorArgumentException.class, () -> intArg("-5000000000"));
    }

}
