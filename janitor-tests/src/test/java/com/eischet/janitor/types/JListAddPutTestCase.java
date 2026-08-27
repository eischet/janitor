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
 * Regression tests for JList.add(JInt, ...)/put(JInt, ...) (the explicit "list.add(i, x)"/
 * "list.put(i, x)" method calls -- distinct from "list[i] = x" syntax, which already went through
 * getIndexed()): they used to read the raw index via {@code index.janitorGetHostValue().intValue()}
 * instead of {@link com.eischet.janitor.api.types.builtin.JList#toIndex}, so negative ("pythonical")
 * indices weren't translated at all -- e.g. {@code list.put(-1, x)} raised immediately instead of
 * replacing the last element, unlike the equivalent {@code list[-1] = x}.
 * <p>
 * put() now shares its bounds-check with getIndexed() (backing "list[i] = x"): both require an
 * existing index, exactly like Python. add() (insertion, like Python's {@code list.insert(i, x)}) is
 * deliberately forgiving instead: out-of-range indices are clamped into {@code [0, size()]} rather
 * than raising, matching Python's insert().
 */
public class JListAddPutTestCase extends JanitorTest {

    private String run(@Language("Janitor") final String script) throws Exception {
        final OutputCatchingTestRuntime rt = OutputCatchingTestRuntime.fresh();
        final RunnableScript runnableScript = rt.compile("test", script);
        runnableScript.run();
        return rt.getAllOutput();
    }

    @Test
    public void putWithInRangePositiveIndexReplacesTheElement() throws Exception {
        assertEquals("[1, x, 3]\n", run("""
                li = [1, 2, 3];
                li.put(1, "x");
                print(li);
                """));
    }

    @Test
    public void putWithNegativeIndexReplacesFromTheEnd() throws Exception {
        // used to raise immediately: the raw index -1 was passed straight to java.util.List.set(),
        // which rejects negative indices outright, instead of resolving it Python-style.
        assertEquals("[1, 2, x]\n", run("""
                li = [1, 2, 3];
                li.put(-1, "x");
                print(li);
                """));
    }

    @Test
    public void putMatchesIndexAssignmentForNegativeIndices() throws Exception {
        // list.put(i, x) and list[i] = x must now agree, for any index, positive or negative.
        assertEquals("[1, 2, x]\n[1, 2, y]\n", run("""
                a = [1, 2, 3];
                a.put(-1, "x");
                print(a);
                b = [1, 2, 3];
                b[-1] = "y";
                print(b);
                """));
    }

    @Test
    public void putWithOutOfRangeIndexRaisesAnError() {
        assertThrows(JanitorRuntimeException.class, () -> run("""
                li = [1, 2, 3];
                li.put(17, "x");
                """));
    }

    @Test
    public void putWithOutOfRangeNegativeIndexRaisesAnError() {
        assertThrows(JanitorRuntimeException.class, () -> run("""
                li = [1, 2, 3];
                li.put(-17, "x");
                """));
    }

    @Test
    public void addWithInRangePositiveIndexInsertsWithoutRemoving() throws Exception {
        assertEquals("[1, x, 2, 3]\n", run("""
                li = [1, 2, 3];
                li.add(1, "x");
                print(li);
                """));
    }

    @Test
    public void addWithNegativeIndexInsertsRelativeToTheEnd() throws Exception {
        // list.add(-1, x) on [1,2,3]: -1 resolves to index 2 (the last element's position), so "x"
        // is inserted right before it, matching Python's list.insert(-1, x).
        assertEquals("[1, 2, x, 3]\n", run("""
                li = [1, 2, 3];
                li.add(-1, "x");
                print(li);
                """));
    }

    @Test
    public void addWithFarOutOfRangePositiveIndexClampsToAppend() throws Exception {
        assertEquals("[1, 2, 3, x]\n", run("""
                li = [1, 2, 3];
                li.add(1000, "x");
                print(li);
                """));
    }

    @Test
    public void addWithFarOutOfRangeNegativeIndexClampsToPrepend() throws Exception {
        assertEquals("[x, 1, 2, 3]\n", run("""
                li = [1, 2, 3];
                li.add(-1000, "x");
                print(li);
                """));
    }

}
