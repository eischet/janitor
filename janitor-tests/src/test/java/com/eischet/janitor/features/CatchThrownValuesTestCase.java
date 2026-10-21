// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.features;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.errors.runtime.JanitorScriptThrownException;
import com.eischet.janitor.runtime.OutputCatchingTestRuntime;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * What a catch block sees: e.value is the thrown value, other attributes are read from it, e.message is the text without the
 * traceback, and e.traceback is the whole text.
 */
public class CatchThrownValuesTestCase extends JanitorTest {

    private static String output(final String source) throws JanitorCompilerException, JanitorRuntimeException {
        final OutputCatchingTestRuntime runtime = OutputCatchingTestRuntime.fresh();
        runtime.compile("test", source).run();
        return runtime.getAllOutput();
    }

    @Test
    void thrownMap() throws Exception {
        assertEquals("404\nmissing\nJanitorScriptThrownException\n", output("""
                try {
                    throw {code: 404, reason: "missing"};
                } catch (e) {
                    print(e.code);
                    print(e.value.reason);
                    print(e.type);
                }
                """));
    }

    @Test
    void thrownString() throws Exception {
        assertEquals("boom\nboom\ntrue\ntrue\n", output("""
                try {
                    throw "boom";
                } catch (e) {
                    print(e.message);
                    print(e.value);
                    print(e.traceback.contains("Traceback"));
                    print(e.traceback.contains("boom"));
                }
                """));
    }

    @Test
    void thrownValueKeepsItsType() throws Exception {
        assertEquals("18\nint\n", output("""
                try { throw 17; } catch (e) { print(e.value + 1); print(e.value.class); }
                """));
    }

    @Test
    void errorAttributesTakePrecedence() throws Exception {
        assertEquals("from the map\nfalse\n", output("""
                try {
                    throw {message: "from the map"};
                } catch (e) {
                    print(e.value.message);
                    print(e.message.contains("Traceback"));
                }
                """));
    }

    @Test
    void rethrowKeepsTheValue() throws Exception {
        assertEquals("1\n", output("""
                try {
                    try { throw {code: 1}; } catch (e) { throw e; }
                } catch (outer) {
                    print(outer.code);
                }
                """));
    }

    @Test
    void runtimeErrors() throws Exception {
        assertEquals("name 'nothing' is not defined\nnull\nJanitorNameException\n", output("""
                try { print(nothing); } catch (e) { print(e.message); print(e.value); print(e.type); }
                """));
        assertEquals("true\nfalse\n", output("""
                try { x = [1][5]; } catch (e) { print(e.message.contains("out of range")); print(e.message.contains("Traceback")); }
                """));
        assertEquals("assertion failed!\n", output("""
                try { assert(1 == 2); } catch (e) { print(e.message); }
                """));
    }

    @Test
    void javaMessageIsUnchanged() {
        final JanitorScriptThrownException thrown = assertThrows(JanitorScriptThrownException.class,
                () -> OutputCatchingTestRuntime.fresh().compile("test", "throw 'foo';").run());
        assertTrue(thrown.getMessage().startsWith("Traceback (most recent call last):"));
        assertTrue(thrown.getMessage().endsWith("ScriptThrownException: foo"));
        assertEquals("foo", thrown.getText());
    }
}
