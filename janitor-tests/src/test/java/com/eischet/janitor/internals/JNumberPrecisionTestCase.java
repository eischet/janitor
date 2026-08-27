package com.eischet.janitor.internals;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.RunnableScript;
import com.eischet.janitor.runtime.OutputCatchingTestRuntime;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Regression tests for JNumber.compareTo() and Janitor.Semantics.areEquals(): both used to compare
 * two JInt values exclusively via {@code Double.compare(toDouble(), ...)}, which loses precision for
 * long values beyond 2^53. E.g. 9007199254740992 (2^53) and 9007199254740993 (2^53+1) both round to
 * the same double, so they used to compare as equal and sort as indistinguishable despite being
 * genuinely different longs. Fixed by comparing as longs whenever both sides hold a Long host value,
 * falling back to double only for mixed int/float comparisons (which are inherently imprecise on the
 * float side regardless of implementation).
 */
public class JNumberPrecisionTestCase extends JanitorTest {

    private String run(@Language("Janitor") final String script) throws Exception {
        final OutputCatchingTestRuntime rt = OutputCatchingTestRuntime.fresh();
        final RunnableScript runnableScript = rt.compile("test", script);
        runnableScript.run();
        return rt.getAllOutput();
    }

    @Test
    public void distinctLargeLongsThatRoundToTheSameDoubleAreNotEqual() throws Exception {
        // 9007199254740992 (2^53) and 9007199254740993 (2^53+1) both round to 9.007199254740992E15
        // as doubles, so a double-based comparison used to treat them as equal.
        assertEquals("false\ntrue\n", run("""
                print(9007199254740992 == 9007199254740993);
                print(9007199254740992 != 9007199254740993);
                """));
    }

    @Test
    public void sortingLargeLongsUsesExactLongOrderNotRoundedDoubleOrder() throws Exception {
        assertEquals("[9007199254740992, 9007199254740993, 9007199254740994]\n", run("""
                li = [9007199254740993, 9007199254740992, 9007199254740994];
                li.sort();
                print(li);
                """));
    }

}
