package com.eischet.janitor.internals;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.RunnableScript;
import com.eischet.janitor.runtime.OutputCatchingTestRuntime;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * These tests document a second, independent bug in the closure/scope handling, distinct from the
 * "shared ScriptFunction instance" bug covered by ClosureScopesTestCase: even a single closure that
 * is never aliased with another one cannot durably mutate a variable it captured from an enclosing
 * function.
 * <p>
 * Root cause: Assignment.execute() (the shared base class behind "=", "+=", "-=", "*=", "/=", "%=")
 * resolves the scope to assign into by walking only {@code scope.getParent()}:
 * <pre>
 *     while (scope != null && scope.lookupLocally(process, id) == null) {
 *         scope = scope.getParent();
 *     }
 * </pre>
 * This never consults the process's closure-scope stack (the list pushed by
 * {@code process.pushClosureScope(...)} in ScriptFunction.call()), which is exactly what
 * {@code Scope.lookup(...)} (used for *reading* a variable) and {@code JanitorScriptProcess.lookupScopedVar(...)}
 * (used correctly already by PostfixIncrement/AnyFixOperator, i.e. i++/++i/--i) do consult.
 * <p>
 * So when a variable lives only in a closure's captured scope (not reachable by walking
 * getParent() from inside the closure body, since a function call's scope chain does not connect
 * back to the lexical/defining scope that way), assignment fails to find it, falls through to the
 * "scope == null" branch in Assignment.execute(), and silently creates a *new local* variable in
 * the closure body's own call-time scope instead -- shadowing, rather than updating, the captured
 * variable. The captured variable is left untouched forever, and the shadow copy is thrown away
 * when the call returns.
 * <p>
 * There's already a comment marking this exact spot: Assignment.java:49
 * "// LATER: turn into :   runningScript.lookupScopedVar(id);" -- and PostfixIncrement.java:51 has
 * a matching "// vorher falsch: ..." comment showing the same fix was already applied there, just
 * never carried over to plain/compound assignment.
 */
public class ClosureVariableAssignmentTestCase extends JanitorTest {

    @Test
    // FIXED: Assignment.execute() now uses process.lookupScopedVar(id), like AnyFixOperator/PostfixIncrement already did.
    public void testSingleClosureCannotAccumulateStateViaPlainAssignment() throws Exception {
        // Only ONE closure is ever created here, so the "shared ScriptFunction instance" bug
        // (ClosureScopesTestCase) cannot be the cause of any wrong behavior below -- this isolates
        // the Assignment.java bug specifically.
        final OutputCatchingTestRuntime runtime = OutputCatchingTestRuntime.fresh();
        final RunnableScript script = runtime.compile("singleCounterPlainAssign", """
                function makeCounter() {
                    n = 0;
                    return () -> { n = n + 1; return n; };
                }
                counter = makeCounter();
                print(counter());
                print(counter());
                print(counter());
                """);
        script.run();
        assertEquals("1\n2\n3\n", runtime.getAllOutput());
    }

    @Test
    // FIXED: same fix as above; PlusAssignment shares Assignment.execute() with "=".
    public void testSingleClosureCannotAccumulateStateViaCompoundAssignment() throws Exception {
        final OutputCatchingTestRuntime runtime = OutputCatchingTestRuntime.fresh();
        final RunnableScript script = runtime.compile("singleCounterCompoundAssign", """
                function makeCounter() {
                    n = 0;
                    return () -> { n += 1; return n; };
                }
                counter = makeCounter();
                print(counter());
                print(counter());
                print(counter());
                """);
        script.run();
        assertEquals("1\n2\n3\n", runtime.getAllOutput());
    }

    @Test
    // Control test, passes today: postfix increment already goes through the correct
    // process.lookupScopedVar(id) path (see PostfixIncrement.java), so it correctly mutates a
    // variable captured from an enclosing function. This is the reference behavior that plain/
    // compound assignment on identifiers should match once Assignment.java is fixed.
    public void testPostfixIncrementInClosureAlreadyWorksCorrectly() throws Exception {
        final OutputCatchingTestRuntime runtime = OutputCatchingTestRuntime.fresh();
        final RunnableScript script = runtime.compile("singleCounterPostfixIncrement", """
                function makeCounter() {
                    n = 0;
                    return () -> { n++; return n; };
                }
                counter = makeCounter();
                print(counter());
                print(counter());
                print(counter());
                """);
        script.run();
        assertEquals("1\n2\n3\n", runtime.getAllOutput());
    }

    @Test
    // Control test, passes today: mutating an outer variable from a nested BLOCK (if/while/etc.)
    // within the *same* function call frame works fine, because in that case scope.getParent()
    // does connect directly to the scope that owns the variable -- no closure-scope stack lookup
    // is needed. This confirms the bug above is specific to crossing a function-call boundary into
    // a real (lexical) closure, not assignment-to-outer-scope in general.
    public void testAssignmentToOuterVariableWithinSameCallFrameWorks() throws Exception {
        final OutputCatchingTestRuntime runtime = OutputCatchingTestRuntime.fresh();
        final RunnableScript script = runtime.compile("nestedBlockSameFrame", """
                function f() {
                    n = 0;
                    if (true) {
                        n = n + 1;
                    }
                    return n;
                }
                print(f());
                """);
        script.run();
        assertEquals("1\n", runtime.getAllOutput());
    }

    @Test
    // FIXED: required BOTH the ScriptFunction aliasing fix (ClosureScopesTestCase) AND the Assignment.java fix to pass correctly.
    public void testTwoIndependentCountersCorruptEachOthersState() throws Exception {
        // The worst-case combination of both bugs: two "independent" stateful closures used to end
        // up sharing state, because (a) they were aliased to the same underlying object (the
        // ScriptFunction bug), and (b) even the one shared captured scope they ended up using could
        // never accumulate a mutation (the Assignment.java bug).
        final OutputCatchingTestRuntime runtime = OutputCatchingTestRuntime.fresh();
        final RunnableScript script = runtime.compile("twoCounters", """
                function makeCounter() {
                    n = 0;
                    return () -> { n = n + 1; return n; };
                }
                c1 = makeCounter();
                c2 = makeCounter();
                print(c1());
                print(c1());
                print(c2());
                print(c1());
                """);
        script.run();
        assertEquals("1\n2\n1\n3\n", runtime.getAllOutput());
    }

}
