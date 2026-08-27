package com.eischet.janitor.internals;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorNameException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.RunnableScript;
import com.eischet.janitor.api.modules.JanitorModuleSupplier;
import com.eischet.janitor.api.modules.ScriptModuleLazy;
import com.eischet.janitor.runtime.OutputCatchingTestRuntime;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for ScriptModuleLazy: compiles its source exactly once (lazily, on first import),
 * then runs that one compiled script fresh -- with its own private scope -- on every import.
 */
public class ScriptModuleLazyTestCase extends JanitorTest {

    private String run(final ScriptModuleLazy module, @Language("Janitor") final String script) throws JanitorRuntimeException, JanitorCompilerException {
        final OutputCatchingTestRuntime rt = OutputCatchingTestRuntime.fresh(env -> env.addModule(module));
        rt.compile("test", script).run();
        return rt.getAllOutput();
    }

    private static ScriptModuleLazy sampleModule() {
        return new ScriptModuleLazy("lazyProbeModule", """
                thing = "hello from module";
                function greet() { return "hi"; }
                """, null);
    }

    private static ScriptModuleLazy counterModule() {
        return new ScriptModuleLazy("lazyCounterModule", """
                n = 0;
                function increment() {
                    n = n + 1;
                    return n;
                }
                """, null);
    }

    private static RunnableScript getCachedCompiledScript(final ScriptModuleLazy module) throws Exception {
        final JanitorModuleSupplier supplier = module.getModuleSupplier();
        final Field f = supplier.getClass().getDeclaredField("compiled");
        f.setAccessible(true);
        return (RunnableScript) f.get(supplier);
    }

    @Test
    public void moduleMembersAreVisible() throws Exception {
        assertEquals("hello from module\n", run(sampleModule(), """
                import lazyProbeModule as m;
                print(m.thing);
                """));
    }

    @Test
    public void missingModuleMemberRaisesAnError() {
        assertThrows(JanitorNameException.class, () -> run(sampleModule(), """
                import lazyProbeModule as m;
                print(m.thisDoesNotExistAtAll);
                """));
    }

    @Test
    public void moduleDoesNotLeakGlobalBuiltinsAsMembers() {
        assertThrows(JanitorNameException.class, () -> run(sampleModule(), """
                import lazyProbeModule as m;
                print(m.print);
                """));
    }

    @Test
    public void nothingIsCompiledUntilTheFirstImport() throws Exception {
        final ScriptModuleLazy module = sampleModule();
        assertNull(getCachedCompiledScript(module), "the source must not be compiled before it's ever imported");
    }

    @Test
    public void sourceIsCompiledOnlyOnceAcrossMultipleImports() throws Exception {
        final ScriptModuleLazy module = counterModule();

        run(module, "import lazyCounterModule as m; print(m.increment());");
        final RunnableScript afterFirstImport = getCachedCompiledScript(module);
        assertNotNull(afterFirstImport);

        run(module, "import lazyCounterModule as m; print(m.increment());");
        final RunnableScript afterSecondImport = getCachedCompiledScript(module);

        assertSame(afterFirstImport, afterSecondImport, "the same compiled script instance must be reused, not recompiled, on subsequent imports");
    }

    @Test
    public void repeatedCallsWithinOneImportAccumulateState() throws Exception {
        // A single import's scope is its own long-lived instance for that one script run: calling a
        // function that mutates module-level state repeatedly must accumulate normally.
        assertEquals("1\n2\n3\n", run(counterModule(), """
                import lazyCounterModule as m;
                print(m.increment());
                print(m.increment());
                print(m.increment());
                """));
    }

    @Test
    public void eachImportGetsAFreshPrivateScopeNotSharedWithOtherImports() throws Exception {
        // The key isolation guarantee: even though the compiled script is reused, running it again
        // for a second, independent import must NOT continue the first import's accumulated state.
        final ScriptModuleLazy module = counterModule();

        assertEquals("1\n2\n", run(module, """
                import lazyCounterModule as m;
                print(m.increment());
                print(m.increment());
                """));

        // A second, unrelated script importing the very same registration must start fresh at 1,
        // not continue from 3 -- proving this import's scope is private, not shared with the first.
        assertEquals("1\n", run(module, """
                import lazyCounterModule as m;
                print(m.increment());
                """));
    }

    @Test
    public void concurrentFirstImportsCompileExactlyOnce() throws Exception {
        // Smoke test for the double-checked-locking compile: several scripts importing the module
        // for the very first time, at the same time, must not race into compiling it more than once.
        final ScriptModuleLazy module = sampleModule();
        final int threads = 8;
        final Thread[] workers = new Thread[threads];
        final AtomicReference<Throwable> failure = new AtomicReference<>();
        for (int i = 0; i < threads; i++) {
            workers[i] = new Thread(() -> {
                try {
                    run(module, "import lazyProbeModule as m; print(m.thing);");
                } catch (Throwable t) {
                    failure.compareAndSet(null, t);
                }
            });
        }
        for (final Thread t : workers) {
            t.start();
        }
        for (final Thread t : workers) {
            t.join();
        }
        if (failure.get() != null) {
            throw new AssertionError("concurrent first imports threw", failure.get());
        }
        assertNotNull(getCachedCompiledScript(module));
    }

}
