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
 * Target behavior for bringing JList indexing/assignment up to modern Python's list semantics.
 * <p>
 * Two distinct rules, deliberately chosen to NOT match real CPython in one respect:
 * <ul>
 *     <li><b>Single-index assignment ({@code li[17] = "foo"}) never grows the list.</b> Just like
 *     real Python, the index must reference an existing element (after resolving a negative index);
 *     otherwise it's an error. There is no "auto-grow with null padding" here -- that was an earlier,
 *     rejected design idea; single-index assignment only ever replaces an existing slot.</li>
 *     <li><b>Range/slice assignment ({@code li[a:b] = [...]}) may grow or shrink the list</b> --
 *     that's the whole point of slice assignment, e.g. {@code li[1:-1] = [22, 33]} -- but only when
 *     both bounds resolve to a position "between existing indices", i.e. into {@code [0, len(li)]}
 *     (that upper bound itself is valid: it's the position right after the last element, which is
 *     where a pure insertion happens). A bound that resolves outside that range (e.g. {@code 10} on
 *     a 5-element list) is an error -- it is deliberately NOT silently clamped down to {@code len(li)}
 *     the way real Python does. This is an intentional deviation from CPython, chosen for
 *     predictability over permissiveness.</li>
 * </ul>
 * <p>
 * As of this writing, none of the slice-assignment tests below pass yet, because slice assignment
 * ({@code li[a:b] = ...}) isn't implemented at all: {@code JListClass.__getSliced()} returns a
 * plain, non-assignable {@code JList} for the 2-argument (range) case, so {@code Assignment.execute()}
 * falls through to "cannot assign value ... to ...". There's already a comment marking this:
 * {@code JListClass}'s "LATER: make ranges assignable, too?" and {@code JList.getRange()}'s
 * "LATER: wrap in TemporaryAssignable for things like list[10:] = [...]".
 * <p>
 * Implementing "bounds must resolve into [0, len]" for slice assignment will also need to touch
 * {@code JList.getRange()}/{@code toIndex()}, which currently don't validate bounds at all (they're
 * passed straight into {@code List.subList()}, which throws a raw {@code IndexOutOfBoundsException}
 * for anything out of range) -- see the still-open "Slice indices are not consistently clamped" item
 * in todo.md. That item's resolution, in light of the "no silent clamping" rule above, is to make
 * out-of-range slice bounds raise a clean, consistent {@code JanitorArgumentException} everywhere,
 * rather than clamping them (which is what the todo.md item originally assumed the fix would be).
 * <p>
 * Single-index out-of-range assignment, on the other hand, needs no code change at all: it already
 * raises an error today, in both directions (see the control tests below) -- it just wasn't
 * previously understood/documented as the deliberately correct, permanent behavior.
 * <p>
 * Resolved: for a slice assignment where the (in-bounds) start resolves to something greater than
 * the (in-bounds) end, e.g. {@code li[3:1] = [...]}, we follow real Python: this is treated as an
 * empty selection, and the replacement is inserted at {@code start} without removing anything (see
 * {@code sliceAssignmentWithDescendingBoundsInsertsAtStart} below). Note that {@code JList.getRange()}
 * has its own, separate, already-shipped, non-Python quirk for *reads* where a descending range
 * (end &lt; start) is returned reversed instead of empty -- that quirk is intentionally left alone
 * here (existing, relied-upon read behavior is a bigger, separate decision than how to implement a
 * not-yet-existing write path), but is worth revisiting later under the same "when in doubt, follow
 * Python" principle.
 */
public class JListPythonIndexingTestCase extends JanitorTest {

    private String run(@Language("Janitor") final String script) throws Exception {
        final OutputCatchingTestRuntime rt = OutputCatchingTestRuntime.fresh();
        final RunnableScript runnableScript = rt.compile("test", script);
        runnableScript.run();
        return rt.getAllOutput();
    }

    // ---------------------------------------------------------------------
    // Single-index assignment: never grows the list. Already correct today -- control tests.
    // ---------------------------------------------------------------------

    @Test
    public void inRangePositiveIndexAssignmentAlreadyWorks() throws Exception {
        assertEquals("[1, 2, x, 4, 5]\n", run("""
                li = [1, 2, 3, 4, 5];
                li[2] = "x";
                print(li);
                """));
    }

    @Test
    public void inRangeNegativeIndexAssignmentAlreadyWorks() throws Exception {
        assertEquals("[1, 2, 3, 4, x]\n", run("""
                li = [1, 2, 3, 4, 5];
                li[-1] = "x";
                print(li);
                """));
    }

    @Test
    public void outOfRangePositiveIndexAssignmentRaisesAnError() {
        assertThrows(JanitorRuntimeException.class, () -> run("""
                li = [1, 2, 3];
                li[17] = "foo";
                """));
    }

    @Test
    public void outOfRangeNegativeIndexAssignmentRaisesAnError() {
        assertThrows(JanitorRuntimeException.class, () -> run("""
                li = [1, 2, 3];
                li[-10] = "x";
                """));
    }

    @Test
    public void assigningExactlyOnePastTheEndAlsoRaisesAnError() {
        // unlike slice assignment, single-index assignment must NOT silently append -- index 3 does
        // not (yet) exist on a 3-element list, so this must be an error, exactly like real Python.
        assertThrows(JanitorRuntimeException.class, () -> run("""
                li = [1, 2, 3];
                li[3] = "x";
                """));
    }

    // ---------------------------------------------------------------------
    // Target behavior: slice assignment "li[a:b] = iterable" may grow/shrink the list, as long as
    // both bounds resolve to a position between existing indices ([0, len]).
    // ---------------------------------------------------------------------

    @Test
    public void sliceAssignmentWithLongerReplacementGrowsTheList() throws Exception {
        // replace 2 elements (index 1,2) with 4 -> list grows by 2
        assertEquals("[1, 10, 20, 30, 40, 4, 5]\n", run("""
                li = [1, 2, 3, 4, 5];
                li[1:3] = [10, 20, 30, 40];
                print(li);
                """));
    }

    @Test
    public void sliceAssignmentWithShorterReplacementShrinksTheList() throws Exception {
        // replace 2 elements (index 1,2) with 1 -> list shrinks by 1
        assertEquals("[1, 10, 4, 5]\n", run("""
                li = [1, 2, 3, 4, 5];
                li[1:3] = [10];
                print(li);
                """));
    }

    @Test
    public void sliceAssignmentWithNegativeEndBoundExample() throws Exception {
        // li[1:-1] selects index 1..5 (elements 2..6, five elements) out of a 7-element list;
        // replacing them with two elements shrinks the list overall.
        assertEquals("[1, 22, 33, 7]\n", run("""
                li = [1, 2, 3, 4, 5, 6, 7];
                li[1:-1] = [22, 33];
                print(li);
                """));
    }

    @Test
    public void sliceAssignmentWithEmptyListDeletesTheRange() throws Exception {
        assertEquals("[1, 4, 5]\n", run("""
                li = [1, 2, 3, 4, 5];
                li[1:3] = [];
                print(li);
                """));
    }

    @Test
    public void sliceAssignmentIntoAnEmptyRangeInsertsWithoutRemoving() throws Exception {
        // li[2:2] selects zero elements -- assigning into it is a pure insertion
        assertEquals("[1, 2, 10, 20, 3, 4, 5]\n", run("""
                li = [1, 2, 3, 4, 5];
                li[2:2] = [10, 20];
                print(li);
                """));
    }

    @Test
    public void sliceAssignmentAtExactlyOnePastTheEndAppends() throws Exception {
        // li[5:5] on a 5-element list is a valid "between existing indices" position (right after
        // the last element) -- unlike single-index assignment, this is a legitimate pure append.
        assertEquals("[1, 2, 3, 4, 5, 10, 20]\n", run("""
                li = [1, 2, 3, 4, 5];
                li[5:5] = [10, 20];
                print(li);
                """));
    }

    @Test
    public void sliceAssignmentWithDescendingBoundsInsertsAtStart() throws Exception {
        // li[3:1]: start(3) > end(1), both individually in-bounds -- Python treats the selection as
        // empty and inserts the replacement right at index 3, removing nothing.
        assertEquals("[1, 2, 3, 10, 20, 4, 5]\n", run("""
                li = [1, 2, 3, 4, 5];
                li[3:1] = [10, 20];
                print(li);
                """));
    }

    @Test
    public void sliceAssignmentWithNegativeBounds() throws Exception {
        // li[-3:-1] selects index 2,3 (elements 3,4) out of [1,2,3,4,5]
        assertEquals("[1, 2, 10, 20, 5]\n", run("""
                li = [1, 2, 3, 4, 5];
                li[-3:-1] = [10, 20];
                print(li);
                """));
    }

    @Test
    public void sliceAssignmentWithOpenStartReplacesFromTheBeginning() throws Exception {
        // li[:2] selects the first two elements (1, 2); the rest of the list (3, 4, 5) is untouched.
        assertEquals("[10, 20, 3, 4, 5]\n", run("""
                li = [1, 2, 3, 4, 5];
                li[:2] = [10, 20];
                print(li);
                """));
    }

    @Test
    public void sliceAssignmentWithOpenEndReplacesToTheEnd() throws Exception {
        assertEquals("[1, 2, 10, 20]\n", run("""
                li = [1, 2, 3, 4, 5];
                li[2:] = [10, 20];
                print(li);
                """));
    }

    @Test
    public void sliceAssignmentWithBothBoundsOpenReplacesEverything() throws Exception {
        assertEquals("[10, 20, 30]\n", run("""
                li = [1, 2, 3, 4, 5];
                li[:] = [10, 20, 30];
                print(li);
                """));
    }

    // ---------------------------------------------------------------------
    // Target behavior: a slice bound that doesn't resolve into [0, len] is an error, not silently
    // clamped (this deliberately deviates from real Python's clamping).
    // ---------------------------------------------------------------------

    @Test
              // unclamped List.subList() IndexOutOfBoundsException; make sure this stays an error
              // (a clean one) once slice bounds get real handling, i.e. do NOT clamp this to len(li).
    public void sliceAssignmentWithEndIndexBeyondLengthRaisesAnError() {
        assertThrows(JanitorRuntimeException.class, () -> run("""
                li = [1, 2, 3, 4, 5];
                li[1:10] = [1, 2, 3, 4];
                """));
    }

    @Test
    public void sliceAssignmentWithStartIndexBeyondLengthRaisesAnError() {
        assertThrows(JanitorRuntimeException.class, () -> run("""
                li = [1, 2, 3];
                li[10:15] = [10, 20, 30];
                """));
    }

}
