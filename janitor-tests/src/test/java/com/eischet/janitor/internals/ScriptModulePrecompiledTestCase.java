package com.eischet.janitor.internals;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorNameException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.modules.ScriptModulePrecompiled;
import com.eischet.janitor.runtime.OutputCatchingTestRuntime;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for ScriptModulePrecompiled: compiles its source immediately, in the constructor -- meant
 * for a module whose source ships as a fixed part of the application, where a syntax error should
 * fail as early and loudly as possible (at startup), unlike ScriptModuleLazy, which is meant for
 * source that might still be corrected by an application user at runtime, and so only fails once the
 * module is actually first imported and used.
 */
public class ScriptModulePrecompiledTestCase extends JanitorTest {

    private static ScriptModulePrecompiled sampleModule(final OutputCatchingTestRuntime bootstrapRuntime) throws JanitorCompilerException {
        return new ScriptModulePrecompiled("precompiledProbeModule", bootstrapRuntime, """
                thing = "hello from module";
                function greet() { return "hi"; }
                """, null);
    }

    private static ScriptModulePrecompiled counterModule(final OutputCatchingTestRuntime bootstrapRuntime) throws JanitorCompilerException {
        return new ScriptModulePrecompiled("precompiledCounterModule", bootstrapRuntime, """
                n = 0;
                function increment() {
                    n = n + 1;
                    return n;
                }
                """, null);
    }

    private String run(final ScriptModulePrecompiled module, @Language("Janitor") final String script) throws JanitorRuntimeException, JanitorCompilerException {
        final OutputCatchingTestRuntime rt = OutputCatchingTestRuntime.fresh(env -> env.addModule(module));
        rt.compile("test", script).run();
        return rt.getAllOutput();
    }

    @Test
    public void constructingWithBrokenSourceFailsImmediatelyInTheConstructor() {
        final OutputCatchingTestRuntime bootstrapRuntime = OutputCatchingTestRuntime.fresh();
        assertThrows(JanitorCompilerException.class, () -> new ScriptModulePrecompiled(
                "brokenPrecompiledModule", bootstrapRuntime, "this is } not { valid Janitor syntax at all (((", null));
    }

    @Test
    public void moduleMembersAreVisible() throws Exception {
        final OutputCatchingTestRuntime bootstrapRuntime = OutputCatchingTestRuntime.fresh();
        assertEquals("hello from module\n", run(sampleModule(bootstrapRuntime), """
                import precompiledProbeModule as m;
                print(m.thing);
                """));
    }

    @Test
    public void missingModuleMemberRaisesAnError() throws Exception {
        final OutputCatchingTestRuntime bootstrapRuntime = OutputCatchingTestRuntime.fresh();
        final ScriptModulePrecompiled module = sampleModule(bootstrapRuntime);
        assertThrows(JanitorNameException.class, () -> run(module, """
                import precompiledProbeModule as m;
                print(m.thisDoesNotExistAtAll);
                """));
    }

    @Test
    public void moduleDoesNotLeakGlobalBuiltinsAsMembers() throws Exception {
        final OutputCatchingTestRuntime bootstrapRuntime = OutputCatchingTestRuntime.fresh();
        final ScriptModulePrecompiled module = sampleModule(bootstrapRuntime);
        assertThrows(JanitorNameException.class, () -> run(module, """
                import precompiledProbeModule as m;
                print(m.print);
                """));
    }

    @Test
    public void repeatedCallsWithinOneImportAccumulateState() throws Exception {
        final OutputCatchingTestRuntime bootstrapRuntime = OutputCatchingTestRuntime.fresh();
        assertEquals("1\n2\n3\n", run(counterModule(bootstrapRuntime), """
                import precompiledCounterModule as m;
                print(m.increment());
                print(m.increment());
                print(m.increment());
                """));
    }

    @Test
    public void eachImportGetsAFreshPrivateScopeNotSharedWithOtherImports() throws Exception {
        final OutputCatchingTestRuntime bootstrapRuntime = OutputCatchingTestRuntime.fresh();
        final ScriptModulePrecompiled module = counterModule(bootstrapRuntime);

        assertEquals("1\n2\n", run(module, """
                import precompiledCounterModule as m;
                print(m.increment());
                print(m.increment());
                """));

        // A second, unrelated script importing the very same registration must start fresh at 1,
        // not continue from 3 -- proving this import's scope is private, not shared with the first.
        assertEquals("1\n", run(module, """
                import precompiledCounterModule as m;
                print(m.increment());
                """));
    }

}
