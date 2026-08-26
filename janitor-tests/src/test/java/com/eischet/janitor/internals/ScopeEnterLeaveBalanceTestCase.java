package com.eischet.janitor.internals;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.RunnableScript;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.runtime.OutputCatchingTestRuntime;
import com.eischet.janitor.toolbox.memory.RefCounter;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Regression test for a bug in {@code JanitorScript.run(Consumer<Scope>)}: it used to call
 * {@code globalScope.janitorLeaveScope()} in its own {@code finally} block, AFTER
 * {@code RunningScriptProcess.run()} had already called {@code getMainScope().janitorLeaveScope()}
 * in its own {@code finally} block on the very same {@code Scope} instance -- {@code RunningScriptProcess}
 * is constructed with {@code wrapScope=false} here, so {@code mainScope} IS {@code globalScope}, not a
 * fresh child of it.
 * <p>
 * The result: every object bound at the top level of a script got {@code janitorLeaveScope()} called
 * TWICE, but only ever got {@code janitorEnterScope()} called ONCE per binding. For a ref-counted
 * cleanup object -- exactly the pattern {@code JanitorHttpClient} uses via
 * {@code com.eischet.janitor.toolbox.memory.RefCounter} -- the first (correct) {@code release()} call
 * brings the counter to zero and runs the cleanup callback; the second, redundant call then hits
 * {@code RefCounter}'s "already zeroed" guard and throws {@code IllegalStateException}, which
 * {@code JanitorHttpClient.janitorLeaveScope()} catches and logs as a warning -- on essentially every
 * script run that uses an HTTP client, which is exactly the symptom reported.
 * <p>
 * This uses a minimal object with a real {@code RefCounter} (the same class {@code JanitorHttpClient}
 * uses) rather than {@code JanitorHttpClient} itself, to reproduce the bug without a network
 * dependency.
 */
public class ScopeEnterLeaveBalanceTestCase extends JanitorTest {

    private static final AtomicInteger idGen = new AtomicInteger();

    /**
     * A minimal stand-in for JanitorHttpClient's own janitorEnterScope()/janitorLeaveScope()
     * implementation: acquire()/release() a real RefCounter, and record (rather than merely log) any
     * exception from a mismatched release() so the test can assert on it directly.
     */
    static class RefCountedThing implements JanitorObject {
        final int id = idGen.incrementAndGet();
        final RefCounter refCounter = new RefCounter(() -> {
        });
        Exception enterError;
        Exception leaveError;

        @Override
        public void janitorEnterScope() {
            try {
                refCounter.acquire();
            } catch (IllegalStateException e) {
                enterError = e;
            }
        }

        @Override
        public void janitorLeaveScope() {
            try {
                refCounter.release();
            } catch (IllegalStateException e) {
                leaveError = e;
            }
        }

        @Override
        public String janitorToString() {
            return "RefCountedThing#" + id;
        }
    }

    private RefCountedThing runAndTrack(final String script) throws Exception {
        final RefCountedThing thing = new RefCountedThing();
        final OutputCatchingTestRuntime rt = OutputCatchingTestRuntime.fresh();
        final RunnableScript s = rt.compile("test", script);
        s.run(globals -> globals.bind("obj", thing));
        return thing;
    }

    @Test
    public void simpleTopLevelAssignmentDoesNotDoubleRelease() throws Exception {
        final RefCountedThing thing = runAndTrack("""
                x = obj;
                """);
        assertNull(thing.leaveError, "janitorLeaveScope() must not throw/warn for a plain top-level binding");
        assertEquals(0, thing.refCounter.get(), "the ref counter must end up exactly at zero, not negative");
    }

    @Test
    public void aliasingTheSameObjectUnderMultipleNamesStaysBalanced() throws Exception {
        final RefCountedThing thing = runAndTrack("""
                a = obj;
                b = a;
                c = a;
                """);
        assertNull(thing.leaveError, "aliasing the same object under several names must not cause a mismatched release()");
        assertEquals(0, thing.refCounter.get());
    }

    @Test
    public void reassigningTheSameNameRepeatedlyStaysBalanced() throws Exception {
        final RefCountedThing thing = runAndTrack("""
                a = obj;
                a = obj;
                a = obj;
                """);
        assertNull(thing.leaveError);
        assertEquals(0, thing.refCounter.get());
    }

    @Test
    public void bindingInsideAFunctionCallStaysBalanced() throws Exception {
        final RefCountedThing thing = runAndTrack("""
                function f() {
                    y = obj;
                }
                f();
                """);
        assertNull(thing.leaveError);
        assertEquals(0, thing.refCounter.get());
    }

    @Test
    public void bindingInsideALoopStaysBalanced() throws Exception {
        final RefCountedThing thing = runAndTrack("""
                i = 0;
                while (i < 3) {
                    z = obj;
                    i = i + 1;
                }
                """);
        assertNull(thing.leaveError);
        assertEquals(0, thing.refCounter.get());
    }

    @Test
    public void passingAsAFunctionArgumentStaysBalanced() throws Exception {
        final RefCountedThing thing = runAndTrack("""
                function f(p) {
                    q = p;
                }
                a = obj;
                f(a);
                """);
        assertNull(thing.leaveError);
        assertEquals(0, thing.refCounter.get());
    }

}
