package com.eischet.janitor.types;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.RunnableScript;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.runtime.OutputCatchingTestRuntime;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Target behavior for Python-style stepped slices: {@code li[start:end:step]}, most importantly
 * {@code li[::-1]} to cleanly read a list in reverse.
 * <p>
 * This is the trade-off for aligning {@code JList.getRange()} with real Python on descending ranges
 * (see the now-disabled {@code readingADescendingRangeReturnsEmptyAndDoesNotReorderTheSourceList} in
 * JListTestCase): once a plain {@code li[4:1]} (no step) reads as {@code []} instead of a reversed
 * slice, {@code li[::-1]} becomes the one, explicit, Python-idiomatic way to get a list reversed.
 * <p>
 * As of this writing, none of the tests below pass -- not because of wrong semantics somewhere, but
 * because the feature doesn't exist at all yet:
 * <ul>
 *     <li>The grammar already has a rule for this -- {@code indexExpressionSteppedRange} in
 *     Janitor.g4 ({@code expression LBRACK expression? COLON expression? COLON expression RBRACK}) --
 *     so {@code li[::-1]} parses at the grammar level.</li>
 *     <li>But {@code JanitorAntlrCompiler} has no {@code visitIndexExpressionSteppedRange} override,
 *     so compiling any stepped-slice expression currently fails outright with a generic
 *     {@code JanitorCompilerException} ("Compiler Error").</li>
 *     <li>{@code JList.getRange()} and {@code JListClass.__get}/{@code __getSliced} only ever take a
 *     start and end index; there's no step parameter anywhere yet (matches the existing
 *     "LATER: stepping" comment in {@code JList.getRange()}).</li>
 * </ul>
 * <p>
 * Semantics to implement, matching Python: {@code step} defaults to 1 when omitted. A {@code step}
 * of exactly 0 is an error (Python: "slice step cannot be zero"). For a positive step, the effective
 * start/end default to 0/len(li) when omitted (like the existing non-stepped range) and iteration
 * runs forward; for a negative step, the effective start/end default to len(li)-1/-1 (i.e. from the
 * last element down to, but not including, the first) when omitted, and iteration runs backward.
 * This is what makes {@code li[::-1]} work: omitted start/end plus a negative step means "the whole
 * list, back to front".
 * <p>
 * Explicit bounds combined with a step follow the same "must resolve into [0, len]" rule already
 * established for the non-stepped case (see JListPythonIndexingTestCase) -- out-of-range bounds are
 * an error, not silently clamped.
 * <p>
 * Out of scope for now (not requested): assigning to a stepped slice (e.g. {@code li[::2] = [...]});
 * Python requires the replacement to have exactly as many elements as the selection for a non-unit
 * step, which is a meaningfully different rule from plain slice assignment. Read-only for now.
 */
public class JListSteppedSliceTestCase extends JanitorTest {

    private String run(@Language("Janitor") final String script) throws Exception {
        final OutputCatchingTestRuntime rt = OutputCatchingTestRuntime.fresh();
        final RunnableScript runnableScript = rt.compile("test", script);
        runnableScript.run();
        return rt.getAllOutput();
    }

    @Test
    public void fullReverseWithNegativeStep() throws Exception {
        assertEquals("[5, 4, 3, 2, 1]\n", run("""
                li = [1, 2, 3, 4, 5];
                print(li[::-1]);
                """));
    }

    @Test
    public void everySecondElementFromTheStart() throws Exception {
        assertEquals("[1, 3, 5]\n", run("""
                li = [1, 2, 3, 4, 5];
                print(li[::2]);
                """));
    }

    @Test
    public void everySecondElementReversed() throws Exception {
        // starting from the last element (5), stepping backward by 2: 5, 3, 1
        assertEquals("[5, 3, 1]\n", run("""
                li = [1, 2, 3, 4, 5];
                print(li[::-2]);
                """));
    }

    @Test
    public void boundedRangeWithPositiveStep() throws Exception {
        // indices 1, 3, 5 (elements 2, 4, 6) out of a 7-element list
        assertEquals("[2, 4, 6]\n", run("""
                li = [1, 2, 3, 4, 5, 6, 7];
                print(li[1:6:2]);
                """));
    }

    @Test
    public void boundedRangeWithNegativeStep() throws Exception {
        // start at index 5 (element 6), step backward down to (but excluding) index 1 (element 2):
        // indices 5, 3 -> elements 6, 4
        assertEquals("[6, 4]\n", run("""
                li = [1, 2, 3, 4, 5, 6, 7];
                print(li[5:1:-2]);
                """));
    }

    @Test
    public void stepOfZeroRaisesAnError() {
        assertThrows(JanitorRuntimeException.class, () -> run("""
                li = [1, 2, 3];
                print(li[::0]);
                """));
    }

}
