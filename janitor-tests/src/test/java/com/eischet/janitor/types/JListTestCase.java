package com.eischet.janitor.types;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.RunnableScript;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JList;
import com.eischet.janitor.runtime.OutputCatchingTestRuntime;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Regression tests for JList.getRange(), which used to return a live java.util.List.subList()
 * view of the source list instead of a copy: mutating the "slice" mutated the source list too,
 * and merely reading a reversed range (e.g. list[5:2]) permanently reordered the source list as a
 * side effect of Collections.reverse() running on the backing array. Fixed by copying the sublist
 * into a fresh ArrayList before wrapping it in the returned JList.
 */
public class JListTestCase extends JanitorTest {

    @Test
    public void getRangeReturnsACopyNotALiveView() {
        final JList source = Janitor.list(List.of(Janitor.integer(1), Janitor.integer(2), Janitor.integer(3), Janitor.integer(4), Janitor.integer(5)));
        final JList slice = (JList) source.getRange(Janitor.integer(1), Janitor.integer(4)); // elements at index 1..3: [2, 3, 4]

        // mutating the slice must not affect the source list
        slice.add(Janitor.integer(999));

        assertEquals(4, slice.size(), "slice should now have 4 elements: 2, 3, 4, 999");
        assertEquals(5, source.size(), "mutating the returned slice must not change the source list's size");
        assertEquals(Janitor.integer(1), source.get(0));
        assertEquals(Janitor.integer(2), source.get(1));
        assertEquals(Janitor.integer(3), source.get(2));
        assertEquals(Janitor.integer(4), source.get(3));
        assertEquals(Janitor.integer(5), source.get(4), "source list must be untouched");
    }

    // getRange() used to reverse a descending range instead of returning an empty list. That was
    // intentional/documented behavior, but it turns out real Python does NOT do this: li[4:1] reads
    // as [] in Python (a descending range with the default step of 1 selects nothing). We aligned
    // with Python here, in exchange for adding real step support ("li[::-1]") to get a clean,
    // explicit way to read a reversed range -- see JListSteppedSliceTestCase.
    @Test
    public void readingADescendingRangeReturnsEmptyAndDoesNotReorderTheSourceList() {
        final JList source = Janitor.list(List.of(Janitor.integer(1), Janitor.integer(2), Janitor.integer(3), Janitor.integer(4), Janitor.integer(5)));

        // a descending range (end index before start index, default step 1) -- must read as empty,
        // like real Python, and must not mutate the source list order as a side effect of reading it.
        final JList slice = (JList) source.getRange(Janitor.integer(4), Janitor.integer(1));

        assertEquals(List.of(), toJavaList(slice), "a descending range with no step selects nothing");
        assertEquals(List.of(Janitor.integer(1), Janitor.integer(2), Janitor.integer(3), Janitor.integer(4), Janitor.integer(5)), toJavaList(source), "the source list must keep its original order");
    }

    private static List<JanitorObject> toJavaList(final JList list) {
        return list.stream().toList();
    }

    @Test
    public void scriptLevelSliceMutationDoesNotAffectSourceList() throws JanitorCompilerException, JanitorRuntimeException {
        final OutputCatchingTestRuntime rt = OutputCatchingTestRuntime.fresh();
        @Language("Janitor") final String script = """
                source = [1, 2, 3, 4, 5];
                slice = source[1:4];
                slice.add(999);
                print(source);
                print(slice);
                """;
        final RunnableScript runnableScript = rt.compile("sliceMutation", script);
        runnableScript.run();
        assertEquals("[1, 2, 3, 4, 5]\n[2, 3, 4, 999]\n", rt.getAllOutput());
    }

}
