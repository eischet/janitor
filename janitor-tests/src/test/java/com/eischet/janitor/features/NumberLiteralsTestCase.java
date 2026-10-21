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
 * Integer and float literals: hexadecimal, binary, digit separators, and the forms that are rejected (octal, suffix L).
 * Scripts are compiled the way hosts do it, so that syntax errors fail; JanitorTest.evaluate only logs them.
 */
public class NumberLiteralsTestCase extends JanitorTest {

    private static JanitorObject run(final String source) throws JanitorCompilerException, JanitorRuntimeException {
        return OutputCatchingTestRuntime.fresh().compile("test", source).run();
    }

    private static Object value(final String source) throws JanitorCompilerException, JanitorRuntimeException {
        return run(source).janitorGetHostValue();
    }

    private static String compileError(final String source) {
        final JanitorCompilerException e = assertThrows(JanitorCompilerException.class, () -> run(source));
        final StringBuilder messages = new StringBuilder();
        for (Throwable t = e; t != null; t = t.getCause()) {
            messages.append(t.getMessage()).append('\n');
        }
        return messages.toString();
    }

    @Test
    void decimal() throws Exception {
        assertEquals(0L, value("0"));
        assertEquals(42L, value("42"));
        assertEquals(1_000_000L, value("1_000_000"));
        assertEquals(Long.MAX_VALUE, value("9223372036854775807"));
        assertEquals(-17L, value("-17"));
        assertEquals("int", run("(1_000).class").janitorToString());
    }

    @Test
    void hexadecimal() throws Exception {
        assertEquals(31L, value("0x1F"));
        assertEquals(255L, value("0XfF"));
        assertEquals(65535L, value("0xFF_FF"));
        assertEquals(-16L, value("-0x10"));
        assertEquals("int", run("(0x10).class").janitorToString());
    }

    @Test
    void binary() throws Exception {
        assertEquals(5L, value("0b101"));
        assertEquals(1L, value("0B1"));
        assertEquals(170L, value("0b1010_1010"));
    }

    @Test
    void hexAndBinaryAreBitPatterns() throws Exception {
        assertEquals(Long.MAX_VALUE, value("0x7FFF_FFFF_FFFF_FFFF"));
        assertEquals(Long.MIN_VALUE, value("0x8000_0000_0000_0000"));
        assertEquals(-1L, value("0xFFFF_FFFF_FFFF_FFFF"));
        assertEquals(-1L, value("0b" + "1".repeat(64)));
    }

    @Test
    void outOfRange() {
        assertTrue(compileError("9223372036854775808").contains("out of range"));
        assertTrue(compileError("0x1_0000_0000_0000_0000").contains("out of range"));
        assertTrue(compileError("0b1" + "0".repeat(64)).contains("out of range"));
    }

    @Test
    void octalIsRejected() {
        assertTrue(compileError("017").contains("octal literals are not supported"));
        assertTrue(compileError("x = 0_7;").contains("octal literals are not supported"));
        assertTrue(compileError("00").contains("write 0"));
    }

    @Test
    void suffixLIsRejected() {
        assertThrows(JanitorCompilerException.class, () -> run("10L"));
        assertThrows(JanitorCompilerException.class, () -> run("0x10L"));
    }

    @Test
    void floats() throws Exception {
        assertEquals(1000.5, value("1_000.5"));
        assertEquals(1e10, value("1e1_0"));
        assertEquals(2.5, value("2.5f"));
        assertEquals(0.5, value(".5"));
    }
}
