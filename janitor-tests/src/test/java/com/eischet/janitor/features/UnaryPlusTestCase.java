// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.features;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorNotImplementedException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.runtime.OutputCatchingTestRuntime;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unary plus, the counterpart of unary minus: it returns numbers unchanged and, unlike JavaScript, does not convert other
 * values to numbers.
 */
public class UnaryPlusTestCase extends JanitorTest {

    private static JanitorObject run(final String source) throws JanitorCompilerException, JanitorRuntimeException {
        return OutputCatchingTestRuntime.fresh().compile("test", source).run();
    }

    @Test
    void numbers() throws Exception {
        assertEquals(3L, run("+3").janitorGetHostValue());
        assertEquals(1.5, run("+1.5").janitorGetHostValue());
        assertEquals(-3L, run("+-3").janitorGetHostValue());
        assertEquals(-3L, run("-+3").janitorGetHostValue());
        assertEquals(7L, run("x = 7; +x").janitorGetHostValue());
        assertEquals("int", run("(+3).class").janitorToString());
        assertEquals("float", run("(+1.5).class").janitorToString());
    }

    @Test
    void symmetricToMinus() throws Exception {
        assertEquals("[1, -1]", run("[+1, -1]").janitorToString());
        assertEquals(3L, run("1 + +2").janitorGetHostValue());
        assertEquals(3L, run("1 - -2").janitorGetHostValue());
        assertEquals(6L, run("+2 * 3").janitorGetHostValue());
    }

    @Test
    void noConversionToNumbers() {
        assertThrows(JanitorNotImplementedException.class, () -> run("+\"3\""));
        assertThrows(JanitorNotImplementedException.class, () -> run("+null"));
        assertThrows(JanitorNotImplementedException.class, () -> run("+[]"));
    }
}
