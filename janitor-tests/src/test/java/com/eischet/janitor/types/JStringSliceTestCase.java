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
 * Target behavior for bringing String indexing/slicing up to modern Python's semantics, mirroring
 * the work already done for JList (see JListPythonIndexingTestCase/JListTestCase/JListSteppedSliceTestCase),
 * minus assignment: strings are immutable in Janitor, exactly like in Python, so there is no
 * equivalent of list slice assignment to support here -- only reading.
 * <p>
 * Three rules, exactly matching the List precedent:
 * <ul>
 *     <li><b>Single-character indexing ({@code s[17]}) is strict</b>, exactly like Python: the index
 *     must reference an existing character (after resolving a negative index), or it's an error.
 *     This already works today (see the control tests below), just via a raw, generically-wrapped
 *     Java exception rather than a clean one -- {@code JStringClass.indexedGet()}'s 1-argument branch
 *     passes the resolved index straight into {@code String.substring()} with no bounds check of its
 *     own.</li>
 *     <li><b>Slicing ({@code s[a:b]}) is forgiving</b>, exactly like Python: out-of-range bounds are
 *     silently clamped into {@code [0, length()]}, and a descending range (the resolved end at or
 *     before the resolved start) reads as an empty string -- not implemented consistently today: the
 *     2-argument branch only clamps the upper bound in the "open start" case, leaves everything else
 *     unclamped (so e.g. {@code 'abcde'[100:200]} throws a raw {@code StringIndexOutOfBoundsException}
 *     instead of reading as {@code ''}), and has the same non-Python "reverse a descending range"
 *     quirk that {@code JList.getRange()} used to have (fixed there; see JListTestCase).</li>
 *     <li><b>Stepped slices ({@code s[::-1]}, {@code s[1:6:2]}, ...) are not implemented at all</b> --
 *     the grammar and compiler already support this syntax (shared with JList, both go through
 *     {@code JanitorAntlrCompiler.INDEXED_GET_METHOD}/{@code visitIndexExpressionSteppedRange}, which
 *     were added for JList's stepped slices), but {@code JStringClass.indexedGet()} has no 3-argument
 *     branch, so any stepped-slice expression on a string currently throws
 *     {@code JanitorArgumentException("invalid arguments: ...")}.</li>
 * </ul>
 */
public class JStringSliceTestCase extends JanitorTest {

    private String run(@Language("Janitor") final String script) throws Exception {
        final OutputCatchingTestRuntime rt = OutputCatchingTestRuntime.fresh();
        final RunnableScript runnableScript = rt.compile("test", script);
        runnableScript.run();
        return rt.getAllOutput();
    }

    // ---------------------------------------------------------------------
    // Already correct today -- control tests.
    // ---------------------------------------------------------------------

    @Test
    public void inRangePositiveIndexAlreadyWorks() throws Exception {
        assertEquals("b\n", run("print('abc'[1]);"));
    }

    @Test
    public void inRangeNegativeIndexAlreadyWorks() throws Exception {
        assertEquals("c\n", run("print('abc'[-1]);"));
    }

    @Test
    public void outOfRangePositiveIndexRaisesAnError() {
        assertThrows(JanitorRuntimeException.class, () -> run("print('abc'[10]);"));
    }

    @Test
    public void outOfRangeNegativeIndexRaisesAnError() {
        assertThrows(JanitorRuntimeException.class, () -> run("print('abc'[-10]);"));
    }

    @Test
    public void basicInRangeSliceAlreadyWorks() throws Exception {
        assertEquals("bc\n", run("print('abcde'[1:3]);"));
    }

    // ---------------------------------------------------------------------
    // Target behavior: slicing clamps out-of-range bounds into [0, length()], like Python.
    // ---------------------------------------------------------------------

    @Test
    public void sliceClampsAnEndIndexBeyondTheStringLength() throws Exception {
        assertEquals("bcde\n", run("print('abcde'[1:100]);"));
    }

    @Test
    public void sliceClampsAStartAndEndIndexBeyondTheStringLengthToEmpty() throws Exception {
        // both clamp to length()=5, so this reads as the empty string, not an error
        assertEquals("\n", run("print('abcde'[100:200]);"));
    }

    @Test
    public void sliceClampsAVeryNegativeStartToZero() throws Exception {
        assertEquals("abcde\n", run("print('abcde'[-100:]);"));
    }

    @Test
    public void sliceClampsAVeryNegativeEndToZero() throws Exception {
        assertEquals("\n", run("print('abcde'[:-100]);"));
    }

    // ---------------------------------------------------------------------
    // Target behavior: a descending range (no step) reads as empty, like Python -- not reversed.
    // ---------------------------------------------------------------------

    @Test
    public void descendingRangeReadsAsEmpty() throws Exception {
        // used to read as "dcb" (the old, non-Python "reverse a descending range" quirk)
        assertEquals("\n", run("print('abcde'[4:1]);"));
    }

    // ---------------------------------------------------------------------
    // Target behavior: stepped slices, matching Python, and matching what was just implemented for
    // JList (see JListSteppedSliceTestCase) -- this is the one, explicit, Python-idiomatic way to
    // get a string reversed now that a plain descending range reads as empty.
    // ---------------------------------------------------------------------

    @Test
    public void fullReverseWithNegativeStep() throws Exception {
        assertEquals("edcba\n", run("print('abcde'[::-1]);"));
    }

    @Test
    public void everySecondCharacterFromTheStart() throws Exception {
        assertEquals("ace\n", run("print('abcde'[::2]);"));
    }

    @Test
    public void everySecondCharacterReversed() throws Exception {
        assertEquals("eca\n", run("print('abcde'[::-2]);"));
    }

    @Test
    public void boundedRangeWithPositiveStep() throws Exception {
        // indices 1, 3, 5 out of a 7-character string: 'b', 'd', 'f'
        assertEquals("bdf\n", run("print('abcdefg'[1:6:2]);"));
    }

    @Test
    public void boundedRangeWithNegativeStep() throws Exception {
        // start at index 5 ('f'), step backward down to (but excluding) index 1: indices 5, 3 -> 'f', 'd'
        assertEquals("fd\n", run("print('abcdefg'[5:1:-2]);"));
    }

    @Test
    public void stepOfZeroRaisesAnError() {
        assertThrows(JanitorRuntimeException.class, () -> run("print('abc'[::0]);"));
    }

}
