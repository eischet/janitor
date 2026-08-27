package com.eischet.janitor.internals;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorNameException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.modules.ScriptModule;
import com.eischet.janitor.runtime.OutputCatchingTestRuntime;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for ScriptModule (formerly a nested part of the old "ScriptModule" class that has since
 * been renamed to ScriptSource; this is the "simple" script-based module provider: every "import" of
 * it freshly compiles and runs the module's source, giving each importer its own private, unshared
 * copy -- see ScriptModuleShared/ScriptModulePrecompiled discussions for variants that trade that
 * isolation for caching).
 */
public class ScriptModuleTestCase extends JanitorTest {

    private String run(final ScriptModule module, @Language("Janitor") final String script) throws JanitorRuntimeException, JanitorCompilerException {
        final OutputCatchingTestRuntime rt = OutputCatchingTestRuntime.fresh(env -> env.addModule(module));
        rt.compile("test", script).run();
        return rt.getAllOutput();
    }

    private static ScriptModule sampleModule() {
        return new ScriptModule("probeModule", """
                thing = "hello from module";
                function greet() { return "hi"; }
                """, null);
    }

    @Test
    public void moduleMembersAreVisible() throws Exception {
        assertEquals("hello from module\n", run(sampleModule(), """
                import probeModule as m;
                print(m.thing);
                """));
    }

    @Test
    public void missingModuleMemberRaisesAnError() {
        assertThrows(JanitorNameException.class, () -> run(sampleModule(), """
                import probeModule as m;
                print(m.thisDoesNotExistAtAll);
                """));
    }

    @Test
    public void moduleDoesNotLeakGlobalBuiltinsAsMembers() {
        // Regression guard: ModuleFromScope.janitorGetAttribute() used to call Scope.lookup()
        // (which also walks the parent chain, ending at the environment's builtin scope) instead of
        // Scope.lookupLocally(), so every global builtin (print, assert, ...) incorrectly resolved
        // as if it were a member of any script-based module, even though the module itself never
        // defined it. A module's attributes must be exactly what it defines at its own top level.
        assertThrows(JanitorNameException.class, () -> run(sampleModule(), """
                import probeModule as m;
                print(m.print);
                """));
        assertThrows(JanitorNameException.class, () -> run(sampleModule(), """
                import probeModule as m;
                print(m.assert);
                """));
    }

}
