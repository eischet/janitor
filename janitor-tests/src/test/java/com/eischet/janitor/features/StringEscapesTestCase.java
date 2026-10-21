// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.features;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JMap;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.compiler.JanitorAntlrCompiler;
import com.eischet.janitor.runtime.OutputCatchingTestRuntime;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Escape sequences in string literals, especially unicode escapes, which used to be a compile error because the compiler
 * relied on String.translateEscapes(). The backslash is built from BS on purpose: a unicode escape written directly in
 * this file would already be translated by the Java compiler.
 */
public class StringEscapesTestCase extends JanitorTest {

    private static final String BS = String.valueOf((char) 92);

    private static JanitorObject run(final String source) throws JanitorCompilerException, JanitorRuntimeException {
        return OutputCatchingTestRuntime.fresh().compile("test", source).run();
    }

    private static String unescape(final String literal) {
        return JanitorAntlrCompiler.unescapeJava(literal);
    }

    @Test
    void simpleEscapes() {
        assertEquals("a\tb\nc\"d'e" + BS + "f", unescape("a" + BS + "tb" + BS + "nc" + BS + "\"d" + BS + "'e" + BS + BS + "f"));
        assertEquals("\b\f\r", unescape(BS + "b" + BS + "f" + BS + "r"));
        assertEquals("no escapes", unescape("no escapes"));
    }

    @Test
    void unicodeEscapes() {
        assertEquals(String.valueOf((char) 0xE4), unescape(BS + "u00e4"));
        assertEquals(String.valueOf((char) 0x20AC), unescape(BS + "u20AC"));
        assertEquals("x" + (char) 0xE4 + "y", unescape("x" + BS + "uu00e4y")); // several u's, as in Java
        // a surrogate pair, written as two escapes, gives one code point
        assertEquals(new String(Character.toChars(0x1F600)), unescape(BS + "ud83d" + BS + "ude00"));
    }

    @Test
    void escapedBackslashDoesNotStartAnEscape() {
        // the unicode escape of the backslash is a plain backslash, followed by a plain n, not a line break
        assertEquals(BS + "n", unescape(BS + "u005cn"));
        // an escaped backslash followed by u0041 is a backslash and the text u0041
        assertEquals(BS + "u0041", unescape(BS + BS + "u0041"));
    }

    @Test
    void octalEscapes() {
        assertEquals("A", unescape(BS + "101"));
        assertEquals(String.valueOf((char) 0), unescape(BS + "0"));
        assertEquals(String.valueOf((char) 255), unescape(BS + "377"));
        assertEquals(" 0", unescape(BS + "400")); // as in Java: \40 followed by the digit 0
    }

    @Test
    void invalidEscapes() {
        assertThrows(IllegalArgumentException.class, () -> unescape(BS + "x"));
        assertThrows(IllegalArgumentException.class, () -> unescape(BS + "u00"));
        assertThrows(IllegalArgumentException.class, () -> unescape("trailing" + BS));
    }

    @Test
    void unicodeEscapesInScripts() throws JanitorCompilerException, JanitorRuntimeException {
        final String ae = String.valueOf((char) 0xE4);
        assertEquals(ae, evaluate("\"" + BS + "u00e4\"").janitorToString());
        assertEquals(ae, evaluate("'" + BS + "u00e4'").janitorToString());
        assertEquals(ae, evaluate("\"\"\"" + BS + "u00e4\"\"\"").janitorToString());
        assertEquals("1", evaluate("m = {\"" + BS + "u00e4\": 1}; m[\"" + ae + "\"]").janitorToString());
        assertEquals(ae + "\n", getOutput("print(\"" + BS + "u00e4\");"));
    }

    @Test
    void escapedSlash() throws JanitorCompilerException, JanitorRuntimeException {
        assertEquals("a/b", unescape("a" + BS + "/b"));
        // compiled the way hosts do it, so that a syntax error fails the test (JanitorTest.evaluate only logs it)
        assertEquals("a/b", run("\"a" + BS + "/b\"").janitorToString());
        // JSON as PHP's json_encode writes it, pasted into a script as a literal
        final JanitorObject site = run("return {\"url\": \"https:" + BS + "/" + BS + "/example.com" + BS + "/shop\", \"tags\": [\"a" + BS + "/b\"]};");
        assertEquals("https://example.com/shop", ((JMap) site).get(Janitor.string("url")).janitorToString());
        assertEquals("a/b", run("x = {\"tags\": [\"a" + BS + "/b\"]}; x.tags[0]").janitorToString());
    }

    @Test
    void parseJsonAcceptsEscapedSlash() throws JanitorCompilerException, JanitorRuntimeException {
        // the script's literal "a\\/b" is the text a\/b, i.e. JSON with an escaped slash
        final String script = "{}.parseJson(\"{\\\"url\\\": \\\"https:" + BS + BS + "/" + BS + BS + "/example.com\\\"}\").url";
        assertEquals("https://example.com", evaluate(script).janitorToString());
        assertEquals("[a/b]", evaluate("[].parseJson(\"[\\\"a" + BS + BS + "/b\\\"]\")").janitorToString());
    }

    @Test
    void invalidUnicodeEscapeInScriptIsACompileError() {
        // compiled the way hosts do it; JanitorTest.evaluate only logs syntax errors
        assertThrows(JanitorCompilerException.class, () -> OutputCatchingTestRuntime.fresh().compile("test", "\"" + BS + "u00g4\""));
    }
}
