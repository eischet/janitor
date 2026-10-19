// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.features;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.errors.runtime.JanitorTypeException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.functions.JCallable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The 'in' operator: {@code element in container} calls the container's {@code __in__} method.
 */
public class InOperatorTestCase extends JanitorTest {

    @Test
    public void lists() throws JanitorCompilerException, JanitorRuntimeException {
        assertEquals(Janitor.TRUE, evaluate("17 in [16, 17, 18]"));
        assertEquals(Janitor.FALSE, evaluate("19 in [16, 17, 18]"));
        assertEquals(Janitor.FALSE, evaluate("1 in []"));
        assertEquals(Janitor.TRUE, evaluate("'b' in ['a', 'b']"));
        assertEquals(Janitor.TRUE, evaluate("null in [1, null]"));
        assertEquals(Janitor.TRUE, evaluate("l = [1, 2]; return 2 in l;"));
    }

    @Test
    public void sets() throws JanitorCompilerException, JanitorRuntimeException {
        assertEquals(Janitor.TRUE, evaluate("s = [1, 2, 3].toSet(); return 2 in s;"));
        assertEquals(Janitor.FALSE, evaluate("s = [1, 2, 3].toSet(); return 4 in s;"));
    }

    @Test
    public void mapsLookAtKeys() throws JanitorCompilerException, JanitorRuntimeException {
        assertEquals(Janitor.TRUE, evaluate("'a' in {a: 1, b: 2}"));
        assertEquals(Janitor.FALSE, evaluate("'c' in {a: 1, b: 2}"));
        assertEquals(Janitor.FALSE, evaluate("1 in {a: 1, b: 2}"));
    }

    @Test
    public void strings() throws JanitorCompilerException, JanitorRuntimeException {
        assertEquals(Janitor.TRUE, evaluate("'bar' in 'foobar'"));
        assertEquals(Janitor.FALSE, evaluate("'baz' in 'foobar'"));
        assertEquals(Janitor.TRUE, evaluate("'' in 'foobar'"));
    }

    @Test
    public void notIn() throws JanitorCompilerException, JanitorRuntimeException {
        assertEquals(Janitor.FALSE, evaluate("17 not in [16, 17, 18]"));
        assertEquals(Janitor.TRUE, evaluate("19 not in [16, 17, 18]"));
        assertEquals(Janitor.TRUE, evaluate("'c' not in {a: 1}"));
        assertEquals(Janitor.TRUE, evaluate("'baz' not in 'foobar'"));
    }

    @Test
    public void precedence() throws JanitorCompilerException, JanitorRuntimeException {
        assertEquals(Janitor.TRUE, evaluate("17 in [16, 17, 18] == true"));
        assertEquals(Janitor.TRUE, evaluate("19 in [16, 17, 18] == false"));
        assertEquals(Janitor.TRUE, evaluate("1 + 1 in [2]"));
        assertEquals(Janitor.FALSE, evaluate("not 1 in [1]"));
        assertEquals(Janitor.TRUE, evaluate("1 in [1] and 2 not in [1]"));
        assertEquals(Janitor.TRUE, evaluate("1 in [1] == 2 in [2]"));
        assertEquals(Janitor.TRUE, evaluate("x = 3; return if x in [1, 2, 3] then true else false;"));
        assertEquals(Janitor.TRUE, evaluate("f = x -> x in [1, 2]; return f(2);"));
    }

    @Test
    public void inIsStillUsableElsewhere() throws JanitorCompilerException, JanitorRuntimeException {
        assertEquals("1\n2\n", getOutput("for (x in [1, 2]) { print(x); }"));
        assertEquals("1\n", getOutput("for (x in [1, 2, 3]) { if (x in [1]) { print(x); } }"));
        assertEquals(Janitor.TRUE, evaluate("in = [1]; return 1 in in;"));
    }

    @Test
    public void unsupportedContainersAreErrors() {
        assertThrows(JanitorTypeException.class, () -> evaluate("1 in 5"));
        assertThrows(JanitorTypeException.class, () -> evaluate("1 in null"));
        assertThrows(JanitorTypeException.class, () -> evaluate("1 not in true"));
    }

    @Test
    public void hostObjectWithCustomIn() throws JanitorCompilerException, JanitorRuntimeException {
        final JanitorObject evens = new Container(element -> Janitor.toBool(element.janitorGetHostValue() instanceof Long l && l % 2 == 0));
        assertEquals(Janitor.TRUE, evaluate("4 in evens", g -> g.bind("evens", evens)));
        assertEquals(Janitor.FALSE, evaluate("5 in evens", g -> g.bind("evens", evens)));
        assertEquals(Janitor.TRUE, evaluate("5 not in evens", g -> g.bind("evens", evens)));
    }

    @Test
    public void customInMustReturnBool() {
        final JanitorObject broken = new Container(element -> Janitor.getBuiltins().integer(29));
        final JanitorTypeException e = assertThrows(JanitorTypeException.class, () -> evaluate("1 in broken", g -> g.bind("broken", broken)));
        assertTrue(e.getMessage().contains("must return true or false"), e.getMessage());
    }

    /**
     * A host object that only provides the {@code __in__} method.
     */
    private record Container(Function<JanitorObject, JanitorObject> test) implements JanitorObject {
        @Override
        public @Nullable JanitorObject janitorGetAttribute(final @NotNull JanitorScriptProcess process, final @NotNull String name, final boolean required) throws JanitorRuntimeException {
            if (Janitor.IN_OPERATOR_METHOD.equals(name)) {
                final JCallable in = (p, args) -> test.apply(args.require(1).get(0));
                return in.asObject(name);
            }
            return JanitorObject.super.janitorGetAttribute(process, name, required);
        }
    }

}
