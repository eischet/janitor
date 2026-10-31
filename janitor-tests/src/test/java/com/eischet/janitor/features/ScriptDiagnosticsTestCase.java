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
        final List<ScriptDiagnostic> diagnostics = check("x = 1\ny = (2 + ]\n");
        assertFalse(diagnostics.isEmpty());
        final ScriptDiagnostic first = diagnostics.getFirst();
        assertEquals(ScriptDiagnostic.Severity.ERROR, first.severity());
        assertEquals(2, first.line());
        assertEquals(7, first.column()); // the parser misses ')' before the '+'
        assertEquals(8, first.endColumn());
        assertFalse(first.message().contains("line 2"), first.message());
    }

    @Test
    void longListsOfExpectedTokensAreLeftOut() throws JanitorCompilerException {
        final ScriptDiagnostic first = check("x = 1\ntotal += i ** 2\n").getFirst();
        assertEquals(2, first.line());
        assertTrue(first.message().startsWith("extraneous input '**'"), first.message());
        assertFalse(first.message().contains("expecting"), first.message());
    }

    @Test
    void lineBreaksInTheQuotedInputAreFlattened() throws JanitorCompilerException {
        final ScriptDiagnostic first = check("if (a) {\n    x = 1 +\n    throw 'no'\n}\n").getFirst();
        assertEquals("no viable alternative at input '+ throw'", first.message());
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
        assertTrue(escape.message().startsWith("invalid string"), escape.message()); // the lexer already rejects it
    }

    @Test
    void statementWithoutEffectIsLocated() throws JanitorCompilerException {
        final ScriptDiagnostic noEffect = check("function f() {\n    x == 1\n}\n").getFirst();
        assertEquals(2, noEffect.line());
        assertEquals(4, noEffect.column());
        assertTrue(noEffect.message().startsWith("this expression has no effect"), noEffect.message());
    }
}
