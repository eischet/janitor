// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.features;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.errors.runtime.JanitorNameException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * What scripts can rely on for statements: scoping of loop and catch variables, the value of increments, guarded member
 * access, where errors are reported, and errors raised by host objects during compound assignments.
 */
public class StatementSemanticsTestCase extends JanitorTest {

    // --- names bound inside a loop or catch block end with it

    @ParameterizedTest(name = "{0}")
    @CsvSource(delimiter = '|', textBlock = """
            for (i from 1 to 3) { inner = i; }              | inner
            for (i from 1 to 3) { inner = i; }              | i
            for (e in [1, 2, 3]) { inner = e; }             | inner
            for (e in [1, 2, 3]) { inner = e; }             | e
            try { throw 1; } catch (e) { inner = e; }       | inner
            try { throw 1; } catch (e) { inner = e; }       | e
            """)
    void namesFromLoopAndCatchBlocksAreGoneAfterwards(final String statement, final String name) {
        final JanitorNameException e = assertThrows(JanitorNameException.class, () -> evaluate(statement + " return " + name + ";"));
        assertTrue(e.getText().contains("'" + name + "'"), e.getText());
    }

    @Test
    void outerVariablesAssignedInLoopsAndCatchBlocksKeepTheirValue() throws Exception {
        assertEquals("[6, 6, caught]", evaluate("""
                a = 0; b = 0; c = 0;
                for (i from 1 to 3) { a = a + i; }
                for (e in [1, 2, 3]) { b = b + e; }
                try { throw 1; } catch (e) { c = 'caught'; }
                return [a, b, c];
                """).janitorToString());
    }

    @Test
    void eachIterationStartsWithoutTheNamesOfThePreviousOne() throws Exception {
        assertEquals("[false, false, false]", evaluate("""
                seen = [];
                for (i from 1 to 3) {
                    try { seen.add(previous == i - 1); } catch (e) { seen.add(false); }
                    previous = i;
                }
                return seen;
                """).janitorToString());
    }

    // --- increments and decrements

    @ParameterizedTest(name = "{0}")
    @CsvSource(delimiter = '|', textBlock = """
            x = 5; y = ++x; return [x, y];  | [6, 6]
            x = 5; y = --x; return [x, y];  | [4, 4]
            x = 5; y = x++; return [x, y];  | [6, 5]
            x = 5; y = x--; return [x, y];  | [4, 5]
            x = 5; ++x; ++x; return x;      | 7
            x = 5; --x; --x; return x;      | 3
            """)
    void incrementsAndDecrementsReturnTheRightValue(final String script, final String expected) throws Exception {
        assertEquals(expected, evaluate(script).janitorToString());
    }

    // --- guarded member access and calls

    @Test
    void guardedAccessOnNullIsNull() throws Exception {
        assertEquals("true", evaluate("x = null; return x?.foo == null;").janitorToString());
        assertEquals("true", evaluate("x = null; return x?.foo() == null;").janitorToString());
        assertEquals("true", evaluate("x = null; return x?.foo?.bar == null;").janitorToString());
    }

    @Test
    void unguardedAccessOnNullFails() {
        assertThrows(JanitorNameException.class, () -> evaluate("x = null; return x.foo;"));
        assertThrows(JanitorNameException.class, () -> evaluate("x = null; return x.foo();"));
    }

    @Test
    void guardedAccessOnObjectsWorksLikeTheUnguardedOne() throws Exception {
        assertEquals("[1, 3]", evaluate("m = {a: 1}; l = [1, 2, 3]; return [m?.a, l?.size()];").janitorToString());
        assertThrows(JanitorNameException.class, () -> evaluate("s = 'text'; return s?.noSuchMember;"));
        assertThrows(JanitorNameException.class, () -> evaluate("s = 'text'; return s?.noSuchMethod();"));
    }

    // --- errors raised by the statement itself name its line

    @ParameterizedTest(name = "{0}")
    @CsvSource(delimiter = '|', textBlock = """
            for (i from 'a' to 3) { }
            for (i from 1 to 'b') { }
            x = 'text'; x.noSuchMember = 1;
            """)
    void errorsNameTheLineOfTheFailingStatement(final String statement) {
        final JanitorRuntimeException e = assertThrows(JanitorRuntimeException.class, () -> evaluate("a = 1;\n\n" + statement));
        assertTrue(e.getMessage().contains("line 3,"), e.getMessage());
    }

    // --- compound assignments on host objects

    private static class Failing extends JanitorComposed<Failing> {
        public static final DispatchTable<Failing> DISPATCH = new DispatchTable<>(Failing::new);

        static {
            DISPATCH.addIntegerProperty("n", Failing::getN, Failing::setN);
        }

        Failing() {
            super(DISPATCH);
        }

        int getN() {
            throw new IllegalStateException("cannot read n");
        }

        void setN(final int n) {
        }
    }

    @Test
    void aFailingGetterInACompoundAssignmentIsAScriptError() throws Exception {
        final JanitorRuntimeException e = assertThrows(JanitorRuntimeException.class,
                () -> evaluate("f.n += 1;", g -> g.bind("f", new Failing())));
        assertTrue(e.getMessage().contains("cannot read n"), e.getMessage());
        assertEquals("caught", evaluate("try { f.n += 1; } catch (e) { return 'caught'; }", g -> g.bind("f", new Failing())).janitorToString());
    }
}
