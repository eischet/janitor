// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.features;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Compound assignments (+=, -=, *=, /=, %=) must work on members and indexed elements, not only on plain variables.
 */
public class CompoundMemberAssignmentTestCase extends JanitorTest {

    private static class Counter extends JanitorComposed<Counter> {
        private static final DispatchTable<Counter> DISPATCH = new DispatchTable<>(Counter::new);

        static {
            DISPATCH.addIntegerProperty("n", Counter::getN, Counter::setN);
        }

        private int n = 5;
        private int reads;

        Counter() {
            super(DISPATCH);
        }

        int getN() {
            reads++;
            return n;
        }

        void setN(final int n) {
            this.n = n;
        }
    }

    @Test
    void mapMember() throws Exception {
        assertEquals("6", evaluate("m = {n: 5}; m.n += 1; return m.n;").janitorToString());
        assertEquals("4", evaluate("m = {n: 5}; m.n -= 1; return m.n;").janitorToString());
        assertEquals("10", evaluate("m = {n: 5}; m.n *= 2; return m.n;").janitorToString());
        assertEquals("1", evaluate("m = {n: 5}; m.n %= 2; return m.n;").janitorToString());
        assertEquals("2.5", evaluate("m = {n: 5.0}; m.n /= 2; return m.n;").janitorToString());
        assertEquals("foobar", evaluate("m = {s: 'foo'}; m.s += 'bar'; return m.s;").janitorToString());
    }

    @Test
    void mapIndex() throws Exception {
        assertEquals("6", evaluate("m = {n: 5}; m['n'] += 1; return m.n;").janitorToString());
    }

    @Test
    void listIndex() throws Exception {
        assertEquals("[1, 12, 3]", evaluate("l = [1, 2, 3]; l[1] += 10; return l;").janitorToString());
    }

    @Test
    void hostObjectProperty() throws Exception {
        final Counter counter = new Counter();
        evaluate("c.n += 2;", g -> g.bind("c", counter));
        assertEquals(7, counter.n);
        assertEquals(1, counter.reads);
    }

    @Test
    void plainAssignmentStillDoesNotRead() throws Exception {
        final Counter counter = new Counter();
        evaluate("c.n = 2;", g -> g.bind("c", counter));
        assertEquals(2, counter.n);
        assertEquals(0, counter.reads);
    }

    @Test
    void targetIsEvaluatedOnlyOnce() throws Exception {
        assertEquals("1 6", evaluate("""
                calls = 0;
                m = {n: 5};
                f = () -> { calls = calls + 1; return m; };
                f().n += 1;
                return "" + calls + " " + m.n;
                """).janitorToString());
    }

}
