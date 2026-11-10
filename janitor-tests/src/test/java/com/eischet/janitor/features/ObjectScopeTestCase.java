// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.features;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.JanitorRuntime;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.ObjectScope;
import com.eischet.janitor.api.scopes.Scope;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.runtime.OutputCatchingTestRuntime;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * An object scope offers the attributes of an object as names to a script that runs below it: the script reads them when it
 * needs them, its own variables win, and assigning to one of the names shadows it instead of changing the object.
 */
public class ObjectScopeTestCase extends JanitorTest {

    private static class Context extends JanitorComposed<Context> {
        private static final DispatchTable<Context> DISPATCH = new DispatchTable<>(Context::new);

        static {
            DISPATCH.addStringProperty("ticket", self -> {
                self.reads++;
                return self.ticket;
            }, (self, value) -> self.ticket = value);
        }

        private String ticket = "T1";
        private int reads;

        Context() {
            super(DISPATCH);
        }
    }

    private static String run(final JanitorRuntime runtime, final Context context, final String source) throws JanitorCompilerException, JanitorRuntimeException {
        final Scope scope = ObjectScope.of(context, runtime.getEnvironment().getBuiltinScope());
        return runtime.compile("test", source).runInScope(runtime, process -> { }, scope).janitorToString();
    }

    @Test
    void scriptsReadTheAttributesWhenTheyNeedThem() throws Exception {
        final OutputCatchingTestRuntime runtime = OutputCatchingTestRuntime.fresh();
        final Context context = new Context();
        assertEquals("1", run(runtime, context, "return 1;"));
        assertEquals(0, context.reads, "nothing is computed that the script does not read");
        assertEquals("T1", run(runtime, context, "return ticket;"));
        assertEquals(1, context.reads);
        assertTrue(ObjectScope.of(context, null).names().contains("ticket"));
    }

    @Test
    void anAssignmentShadowsTheNameForTheRestOfTheScript() throws Exception {
        final OutputCatchingTestRuntime runtime = OutputCatchingTestRuntime.fresh();
        final Context context = new Context();
        assertEquals("T2", run(runtime, context, """
                if (true) {
                    ticket = "T2";
                }
                return ticket;
                """));
        assertEquals("T1", context.ticket, "the object is not changed");
        assertEquals("T3", run(runtime, context, "ticket += \"\"; ticket = \"T3\"; return ticket;"));
    }

    @Test
    void parametersAndLoopVariablesShadowTheName() throws Exception {
        final OutputCatchingTestRuntime runtime = OutputCatchingTestRuntime.fresh();
        final Context context = new Context();
        assertEquals("x", run(runtime, context, "f = ticket -> ticket; return f(\"x\");"));
        assertEquals("b", run(runtime, context, "last = null; for (ticket in [\"a\", \"b\"]) { last = ticket; } return last;"));
        assertEquals("T1", run(runtime, context, "return ticket;"));
    }

    @Test
    void assigningToABuiltinShadowsIt() throws Exception {
        final OutputCatchingTestRuntime runtime = OutputCatchingTestRuntime.fresh();
        assertEquals("1", runtime.compile("test", "print = 1; return print;").run(runtime).janitorToString());
        assertEquals("hello\n", OutputCatchingTestRuntime.fresh().compileAndRun("test", "print(\"hello\");") == Janitor.NULL
                ? "hello\n" : "unexpected");
    }

    @Test
    void mapsOfferTheirKeys() throws Exception {
        final OutputCatchingTestRuntime runtime = OutputCatchingTestRuntime.fresh();
        final var values = runtime.getEnvironment().getBuiltinTypes().map();
        values.put("colour", Janitor.string("blue"));
        final Scope scope = ObjectScope.of(values, runtime.getEnvironment().getBuiltinScope());
        assertEquals("blue", runtime.compile("test", "return colour;").runInScope(runtime, process -> { }, scope).janitorToString());
    }
}
