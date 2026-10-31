// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.features;

import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.compiler.ScriptDiagnostic;
import com.eischet.janitor.runtime.OutputCatchingTestRuntime;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Diagnostics with positions, as an editor shows them: syntax errors from the parser, and errors from the compiler. */
public class ScriptDiagnosticsTestCase {

    private static List<ScriptDiagnostic> check(final String source) throws JanitorCompilerException {
        return OutputCatchingTestRuntime.fresh().checkCompile("test", source).getDiagnostics();
    }

    @Test
    void validScriptHasNone() throws JanitorCompilerException {
        assertEquals(List.of(), check("x = 1\nprint(x)\n"));
    }

    @Test
    void syntaxErrorMarksTheOffendingToken() throws JanitorCompilerException {
        final List<ScriptDiagnostic> diagnostics = check("x = 1\ny = foo(1, 2 bar)\n");
        assertEquals(1, diagnostics.size());
        final ScriptDiagnostic first = diagnostics.getFirst();
        assertEquals(ScriptDiagnostic.Severity.ERROR, first.severity());
        assertEquals(2, first.line());
        assertEquals(13, first.column());
        assertEquals(16, first.endColumn());
        assertEquals("unexpected 'bar' after '2'; is an operator, a comma or a line break missing before it?", first.message());
    }

    @Test
    void longListsOfExpectedTokensAreLeftOut() throws JanitorCompilerException {
        final ScriptDiagnostic first = check("x = 1\ntotal += i ** 2\n").getFirst();
        assertEquals(2, first.line());
        assertEquals("unexpected '**'", first.message());
    }

    @Test
    void expectedTokensAreNamedPlainly() throws JanitorCompilerException {
        final ScriptDiagnostic missing = check("x = [1, 2\nprint(x)\n").get(1);
        assertEquals("unexpected 'print', expected ']' or ','", missing.message());
    }

    @Test
    void theTokenBeforeIsNamed() throws JanitorCompilerException {
        final ScriptDiagnostic first = check("if (a) {\n    x = 1 +\n    throw 'no'\n}\n").getFirst();
        assertEquals("unexpected 'throw' after '+'", first.message());
        assertEquals("unexpected \"hallo\" after 'print'; is an operator, a comma or a line break missing before it?",
                check("print \"hallo\"\n").getFirst().message());
    }

    @Test
    void onlyTheFirstProblemOfALineIsReported() throws JanitorCompilerException {
        final List<ScriptDiagnostic> diagnostics = check("x = foo(1, 2 bar baz qux and more\nprint(x)\n");
        assertEquals(1, diagnostics.size(), diagnostics.toString());
    }

    @Test
    void unclosedBracketIsNamed() throws JanitorCompilerException {
        final ScriptDiagnostic paren = check("print(1)\nx = foo(1, 2 bar\n").getFirst();
        assertEquals(2, paren.line());
        assertEquals(7, paren.column());
        assertEquals("'(' is not closed", paren.message());

        final ScriptDiagnostic brace = check("function f() {\n    print(1)\n\nprint(2)\n").getFirst();
        assertEquals(1, brace.line());
        assertEquals("'{' is not closed", brace.message());

        assertEquals("'(' is not closed", check("if (x > 1 {\n    print(x)\n}\n").getFirst().message());
        assertEquals("'[' is not closed before ')'", check("print([1, 2)\n").getFirst().message());
        assertEquals("')' has no matching '(' before it", check("print(x))\n").getFirst().message());
    }

    @Test
    void unclosedStringIsNamed() throws JanitorCompilerException {
        final ScriptDiagnostic string = check("s = \"not closed\nprint(s)\n").getFirst();
        assertEquals(1, string.line());
        assertEquals(4, string.column());
        assertEquals("string not closed: \" is missing at the end of the line", string.message());
    }

    @Test
    void unknownCharacterIsNamed() throws JanitorCompilerException {
        assertEquals("unknown character '#'", check("x = 1 # comment\n").getFirst().message());
    }

    @Test
    void compilerErrorHasThePositionOfTheLiteral() throws JanitorCompilerException {
        final ScriptDiagnostic octal = check("a = 1\nb = 017\n").getFirst();
        assertEquals(2, octal.line());
        assertEquals(4, octal.column());
        assertTrue(octal.message().startsWith("integer literals must not start with 0"), octal.message());
        assertFalse(octal.message().contains(" at Module"), octal.message());

        final ScriptDiagnostic date = check("\n\nd = @2026-02-30\n").getFirst();
        assertEquals(3, date.line());
        assertEquals(4, date.column());
    }

    @Test
    void invalidEscapeIsLocated() throws JanitorCompilerException {
        final ScriptDiagnostic escape = check("s = 'ok'\nt = 'bad \\x'\n").getFirst();
        assertEquals(2, escape.line());
        assertEquals(4, escape.column());
        assertTrue(escape.message().startsWith("invalid escape sequence '\\x'"), escape.message()); // the lexer already rejects it
    }

    @Test
    void statementWithoutEffectIsLocated() throws JanitorCompilerException {
        final ScriptDiagnostic noEffect = check("function f() {\n    x == 1\n}\n").getFirst();
        assertEquals(2, noEffect.line());
        assertEquals(4, noEffect.column());
        assertTrue(noEffect.message().startsWith("this expression has no effect"), noEffect.message());
    }
}
