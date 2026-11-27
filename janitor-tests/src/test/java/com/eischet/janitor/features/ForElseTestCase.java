// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.features;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorNameException;
import com.eischet.janitor.runtime.OutputCatchingTestRuntime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * A for loop can have an else block, which runs if and only if the loop body did not run a single time.
 */
public class ForElseTestCase extends JanitorTest {

    @ParameterizedTest(name = "{0}")
    @CsvSource(delimiter = '|', textBlock = """
            r = 'body never ran'; for (x in []) { r = 'body'; } else { r = 'else'; } return r;              | else
            r = 'body never ran'; for (x in {}) { r = 'body'; } else { r = 'else'; } return r;              | else
            r = 'body never ran'; for (x in [1].filter(n -> n > 1)) { r = 'body'; } else { r = 'else'; } return r; | else
            r = 'body never ran'; for (i from 3 to 1) { r = 'body'; } else { r = 'else'; } return r;        | else
            r = 'body never ran'; for (i from 1 to 0) { r = 'body'; } else { r = 'else'; } return r;        | else
            """)
    void elseRunsWhenThereWasNoIteration(final String script, final String expected) throws Exception {
        assertEquals(expected, evaluate(script).janitorToString());
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource(delimiter = '|', textBlock = """
            r = []; for (x in [1, 2]) { r.add(x); } else { r.add('else'); } return r;                       | [1, 2]
            r = []; for (x in [null]) { r.add(x == null); } else { r.add('else'); } return r;               | [true]
            r = []; for (x in {a: 1}) { r.add(x); } else { r.add('else'); } return r;                       | [a]
            r = []; for (i from 1 to 2) { r.add(i); } else { r.add('else'); } return r;                     | [1, 2]
            r = []; for (i from 1 to 1) { r.add(i); } else { r.add('else'); } return r;                     | [1]
            """)
    void elseDoesNotRunAfterIterations(final String script, final String expected) throws Exception {
        assertEquals(expected, evaluate(script).janitorToString());
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource(delimiter = '|', textBlock = """
            r = []; for (x in [1, 2]) { break; } else { r.add('else'); } return r;                          | []
            r = []; for (x in [1, 2]) { continue; } else { r.add('else'); } return r;                       | []
            r = []; for (i from 1 to 2) { break; } else { r.add('else'); } return r;                        | []
            r = []; for (i from 1 to 2) { continue; } else { r.add('else'); } return r;                     | []
            """)
    void anIterationEndedByBreakOrContinueCounts(final String script, final String expected) throws Exception {
        assertEquals(expected, evaluate(script).janitorToString());
    }

    @Test
    void anIterationEndedByAnErrorCounts() throws Exception {
        assertEquals("[caught]", evaluate("""
                r = [];
                try {
                    for (x in [1]) { throw 'oops'; } else { r.add('else'); }
                } catch (e) {
                    r.add('caught');
                }
                return r;
                """).janitorToString());
    }

    @Test
    void loopsWithoutElseWorkAsBefore() throws Exception {
        assertEquals("[1, 2, 1, 2, after]", evaluate("""
                r = [];
                for (x in [1, 2]) { r.add(x); }
                for (x in []) { r.add(x); }
                for (i from 1 to 2) { r.add(i); }
                for (i from 2 to 1) { r.add(i); }
                r.add('after');
                return r;
                """).janitorToString());
    }

    @Test
    void breakAndContinueInElseBelongToTheEnclosingLoop() throws Exception {
        assertEquals("[1, 3]", evaluate("""
                r = [];
                for (n in [1, 2, 3, 4, 5]) {
                    for (x in (n == 2 ? [] : [n])) {
                        if (n == 4) { break; }
                        r.add(x);
                    } else {
                        continue;
                    }
                    if (n == 4) {
                        for (i from 1 to 0) { } else { break; }
                    }
                }
                return r;
                """).janitorToString());
    }

    @Test
    void elseCanReturnFromAFunction() throws Exception {
        assertEquals("[first: a, nothing]", evaluate("""
                function describe(items) {
                    for (item in items) {
                        return "first: " + item;
                    } else {
                        return "nothing";
                    }
                }
                return [describe(["a", "b"]), describe([])];
                """).janitorToString());
    }

    @Test
    void manyLoopsEachDecideOnTheirOwn() throws Exception {
        assertEquals("[5000, 5000]", evaluate("""
                empty = 0; filled = 0;
                for (n from 1 to 10000) {
                    for (x in (n % 2 == 0 ? [] : [n])) { filled++; } else { empty++; }
                }
                return [empty, filled];
                """).janitorToString());
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {
            "for (x in []) { } else { return x; }",
            "for (i from 1 to 0) { } else { return i; }",
    })
    void theLoopVariableDoesNotExistInElse(final String script) {
        assertThrows(JanitorNameException.class, () -> evaluate(script));
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {
            "for (x in []) { } else print('no braces');",
            "for (i from 1 to 0) { } else print('no braces');",
            "for (x in []) { } else if (true) { }",
            "for (x in []) { } else { } else { }",
            "while (false) { } else { }",
            "do { } while (false) else { }",
    })
    void elseNeedsABlockAndExistsOnlyForForLoops(final String script) {
        assertThrows(JanitorCompilerException.class, () -> OutputCatchingTestRuntime.fresh().compile("test", script), script);
    }

}
