// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.features;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.runtime.OutputCatchingTestRuntime;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Inside a block, an expression may only stand alone as a statement if it does something: a call, an assignment or ++/--.
 * At the top level, every expression is allowed, and the last one is the script's result.
 */
public class ExpressionStatementsTestCase extends JanitorTest {

    private static JanitorObject run(final String source) throws JanitorCompilerException, JanitorRuntimeException {
        return OutputCatchingTestRuntime.fresh().compile("test", source).run();
    }

    private static void assertNoEffectError(final String source) {
        final JanitorCompilerException e = assertThrows(JanitorCompilerException.class, () -> run(source), source);
        assertTrue(e.getMessage().contains("this expression has no effect"), e.getMessage());
    }

    @Test
    void expressionsWithoutEffectAreRejectedInBlocks() {
        assertNoEffectError("x = 1; if (true) { x; }");
        assertNoEffectError("x = 1; if (true) { x == 1; }");
        assertNoEffectError("if (true) { 1 + 2; }");
        assertNoEffectError("if (true) { \"text\"; }");
        assertNoEffectError("m = {a: 1}; if (true) { m.a; }");
        assertNoEffectError("l = [1]; if (true) { l[0]; }");
        assertNoEffectError("l = [1, 2]; if (true) { l[0:1]; }");
        assertNoEffectError("function f(x) { x * 2; }");
        assertNoEffectError("for (i in [1]) { i; }");
        assertNoEffectError("f = x -> { x; }; f(1);");
    }

    @Test
    void theMessageNamesTheLocation() {
        final JanitorCompilerException e = assertThrows(JanitorCompilerException.class, () -> run("x = 1;\nif (true) {\n    x == 1;\n}"));
        assertTrue(e.getMessage().contains("line 3"), e.getMessage());
    }

    @Test
    void statementsWithEffectAreAllowedInBlocks() throws Exception {
        assertEquals("[1, 2]", run("""
                l = [];
                x = 0;
                if (true) {
                    l.add(1);
                    l?.add(2);
                    x = 5;
                    x += 1;
                    x++;
                    --x;
                    print(x);
                }
                return l;
                """).janitorToString());
    }

    @Test
    void everyExpressionIsAllowedAtTheTopLevel() throws Exception {
        assertEquals("true", run("x = 1; x == 1").janitorToString());
        assertEquals("1", run("l = [1]; l[0]").janitorToString());
        assertEquals("3", run("1 + 2;").janitorToString());
    }

    @Test
    void callsInBlocksDoNotSetTheResult() throws Exception {
        assertEquals("before", run("""
                "before";
                if (true) {
                    "abc".length();
                }
                """).janitorToString());
    }
}
