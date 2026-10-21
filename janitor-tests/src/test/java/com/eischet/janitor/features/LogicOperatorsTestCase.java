// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.features;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JBool;
import com.eischet.janitor.runtime.OutputCatchingTestRuntime;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The logical operators, which are asymmetric on purpose: "a or b" returns one of its operands, so that "value or default"
 * replaces empty values; "a and b" always returns a boolean, so that conditions give hosts a real boolean. Both evaluate
 * their right side only when needed, and &amp;&amp; and || are the same as and and or.
 */
public class LogicOperatorsTestCase extends JanitorTest {

    private static JanitorObject run(final String source) throws JanitorCompilerException, JanitorRuntimeException {
        return OutputCatchingTestRuntime.fresh().compile("test", source).run();
    }

    @Test
    void orReturnsTheFirstTruthyOperandOrTheLastOne() throws Exception {
        assertEquals("default", run("null or \"default\"").janitorToString());
        assertEquals("default", run("\"\" or \"default\"").janitorToString());
        assertEquals("x", run("\"x\" or \"y\"").janitorToString());
        assertEquals(5L, run("0 or 5").janitorGetHostValue());
        assertEquals("[1]", run("[] or [1]").janitorToString());
        assertEquals("[]", run("{} or []").janitorToString()); // both falsy: the right operand, not false
        assertEquals("c", run("null or \"\" or \"c\"").janitorToString());
        assertEquals("list", run("x = [1]; (x or null).class").janitorToString()); // the operand itself, not a boolean
    }

    @Test
    void andReturnsABoolean() throws Exception {
        assertSame(JBool.TRUE, run("\"x\" and \"y\""));
        assertSame(JBool.FALSE, run("null and \"y\""));
        assertSame(JBool.FALSE, run("\"x\" and \"\""));
        assertSame(JBool.TRUE, run("1 < 2 and 3 < 4"));
    }

    @Test
    void symbolsAreTheSame() throws Exception {
        assertEquals("default", run("null || \"default\"").janitorToString());
        assertSame(JBool.TRUE, run("\"x\" && \"y\""));
        assertSame(JBool.FALSE, run("!\"x\""));
        assertSame(JBool.TRUE, run("not null"));
    }

    @Test
    void theRightSideIsOnlyEvaluatedWhenNeeded() throws Exception {
        // undefinedName would be a NameException if it were evaluated
        assertEquals("x", run("\"x\" or undefinedName").janitorToString());
        assertSame(JBool.FALSE, run("null and undefinedName"));
        assertEquals("ok", run("customer = null; (customer and customer.name) or \"ok\"").janitorToString());
    }
}
