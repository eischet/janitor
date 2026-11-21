// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.features;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.VariableScope;
import com.eischet.janitor.api.types.JanitorObject;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Named arguments ({@code f(name = value)}) are bound to the parameter of that name, as in Python, whatever their order,
 * and native functions see them apart from the positional arguments.
 */
public class NamedArgumentsTestCase extends JanitorTest {

    private String eval(final @Language("Janitor") String script) throws JanitorCompilerException, JanitorRuntimeException {
        return evaluate(script).janitorToString();
    }

    private String argumentError(final @Language("Janitor") String script) {
        return assertThrows(JanitorArgumentException.class, () -> evaluate(script)).getText();
    }

    @Test
    void namesBindToTheirParameterInAnyOrder() throws Exception {
        assertEquals("1..10 by 1", eval("""
                function range(from, to, step = 1) { return from + ".." + to + " by " + step; }
                return range(to = 10, from = 1);
                """));
        assertEquals("A B", eval("""
                function f(a, b) { return a + " " + b; }
                return f(b = "B", a = "A");
                """));
        assertEquals("2-1", eval("""
                g = (a, b) -> a + "-" + b;
                return g(b = 1, a = 2);
                """));
    }

    @Test
    void aNameSkipsParametersWithDefaults() throws Exception {
        assertEquals("0 1 5", eval("""
                function f(a, b = 1, c = 2) { return a + " " + b + " " + c; }
                return f(0, c = 5);
                """));
        assertEquals("x 1 y", eval("""
                function f(a, b = 1, c = 2) { return a + " " + b + " " + c; }
                return f(c = "y", a = "x");
                """));
    }

    @Test
    void defaultsSeeTheParametersBeforeThem() throws Exception {
        assertEquals("6", eval("function f(a, b = a * 2) { return b; } return f(3);"));
        assertEquals("6", eval("function f(a, b = a * 2) { return b; } return f(a = 3);"));
    }

    @Test
    void restGetsOnlyPositionalArgumentsAndOptionsOnlyUnusedNames() throws Exception {
        assertEquals("1 [2] {sep=-}", eval("""
                function describe(first, *rest, **options) { return first + " " + rest + " " + options; }
                return describe(1, 2, sep = "-");
                """));
        assertEquals("2 {c=3}", eval("""
                function f(a, b = 1, **options) { return b + " " + options; }
                return f(1, b = 2, c = 3);
                """));
    }

    @Test
    void wrongNamesAreErrors() {
        assertTrue(argumentError("function f(a) { return a; } f(1, x = 2);").contains("named argument 'x'"));
        assertTrue(argumentError("function f(a) { return a; } f(1, a = 2);").contains("more than one value for 'a'"));
        assertTrue(argumentError("function f(a, b) { return a; } f(b = 1);").contains("missing argument 'a'"));
        assertTrue(argumentError("function f(a, b = 1) { return a; } f(1, 2, 3);").contains("at most 2"));
    }

    @Test
    void aNameTwiceInOneCallDoesNotCompile() {
        assertThrows(JanitorCompilerException.class, () -> evaluate("function f(a) { return a; } f(a = 1, a = 2);"));
    }

    @Test
    void nativeFunctionsSeeNamedArgumentsApartFromPositionalOnes() throws Exception {
        final Consumer<VariableScope> globals = g -> g.bindF("probe", (process, args) ->
                Janitor.string(args.size() + " " + args.get(args.size() - 1).janitorToString() + " " + args.getByName("k")));
        assertEquals("2 b 3", evaluate("return probe(\"a\", \"b\", k = 3);", globals).janitorToString());
    }

    @Test
    void printJoinsTheValuesWithTheSeparator() throws Exception {
        assertEquals("a/b/c\n", getOutput("a = \"a\"; b = \"b\"; c = \"c\"; print(a, b, c, sep = '/');"));
        assertEquals("1, [2, 3], null\n", getOutput("print(1, [2, 3], null, sep = \", \");"));
        assertEquals("12\n", getOutput("print(1, 2, sep = \"\");"));
        assertEquals("1 2\n", getOutput("print(1, 2, sep = null);"));
        assertEquals("1 2\n", getOutput("print(1, 2);"));
        assertTrue(argumentError("print(1, 2, sep = 3);").contains("sep"));
        assertTrue(argumentError("print(1, 2, end = \"\");").contains("named argument 'end'"));
    }

    @Test
    void namesThatANativeFunctionDoesNotReadAreErrors() throws Exception {
        assertEquals("1/2", eval("return [1, 2].join(\"/\");"));
        assertTrue(argumentError("return [1, 2].join(sep = \"/\");").contains("named argument 'sep'"));
        final Consumer<VariableScope> globals = g -> g.bindF("probe", (process, args) -> {
            final JanitorObject k = args.getByName("k");
            return k == null ? Janitor.NULL : k;
        });
        assertEquals("3", evaluate("return probe(k = 3);", globals).janitorToString());
        assertThrows(JanitorArgumentException.class, () -> evaluate("return probe(k = 3, other = 4);", globals));
    }
}
