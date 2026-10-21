// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.features;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.runtime.OutputCatchingTestRuntime;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Syntax errors after a statement must be reported. The error listener used to mute the errors around the optional
 * semicolon, and ANTLR's error recovery then silently dropped tokens: "import a.b;" became "import a" plus "b;", and in
 * "print(1) } print(2);" the first statement disappeared. Statements without a semicolon must still work.
 */
public class SyntaxErrorsAreReportedTestCase extends JanitorTest {

    private static String output(final String source) throws JanitorCompilerException, JanitorRuntimeException {
        final OutputCatchingTestRuntime runtime = OutputCatchingTestRuntime.fresh();
        runtime.compile("test", source).run();
        return runtime.getAllOutput();
    }

    private static void assertSyntaxError(final String source) {
        assertThrows(JanitorCompilerException.class, () -> OutputCatchingTestRuntime.fresh().compile("test", source), source);
    }

    @Test
    void strayTokensAfterStatements() {
        assertSyntaxError("print(1) } print(2);");
        assertSyntaxError("import collections . print(1);");
        assertSyntaxError("import a.b;");
        assertSyntaxError("print(1) ] print(2);");
        assertSyntaxError("print(1) , print(2);");
        assertSyntaxError("return 1 .");
        assertSyntaxError("function f() { return 1 . } print(f());");
    }

    @Test
    void closingBraceEndsAStatement() throws Exception {
        assertEquals("1\n", output("{ print(1) }"));
        assertEquals("yes\n", output("if (true) { print(\"yes\") } else { print(\"no\") }"));
        assertEquals("2\n", output("function f() { return 2 } print(f())"));
        assertEquals("3\n", output("x = 0\nfor (i in [1, 2]) { x += i }\nprint(x)"));
        assertEquals("ok\n", output("try { throw \"x\" } catch (e) { print(\"ok\") }"));
    }

    @Test
    void lineBreaksEndStatements() throws Exception {
        assertEquals("1\n2\n", output("print(1)\nprint(2)"));
        assertEquals("[1, 2]\n", output("import collections\nprint(collections.list(1, 2))"));
    }
}
