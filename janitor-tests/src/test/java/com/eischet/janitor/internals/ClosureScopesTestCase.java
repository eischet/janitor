package com.eischet.janitor.internals;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.RunnableScript;
import com.eischet.janitor.runtime.OutputCatchingTestRuntime;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ClosureScopesTestCase extends JanitorTest {

    @Test
    public void testCaptureArgumentScopes() throws Exception {
        final OutputCatchingTestRuntime runtime = OutputCatchingTestRuntime.fresh();
        // old workaround: reassign function argument to get it into the propers scope
        final RunnableScript script = runtime.compile("capturingWorkaround", """
                function foo(arg) {
                    bar = arg;
                    return (x) -> bar + x;
                }
                stage1 = foo(10);
                print(stage1(7));
                """);
        script.run();
        assertEquals("17\n", runtime.getAllOutput());
        // This now works, thanks to Block::executeFunctionCall, which finally improves closure scope handling.
        // The problem was that arguments are bound into a scope, but the block created yet another additional scope.
        runtime.resetOutput();
        final RunnableScript script2 = runtime.compile("capturing", """
                function foo(arg) {
                    return (x) -> arg + x;
                }
                stage1 = foo(10);
                print(stage1(7));
                """);
        script2.run();
        assertEquals("17\n", runtime.getAllOutput());



    }

    @Test
    // FIXED: ScriptFunction.evaluate() now returns a fresh Closure per evaluation instead of
    // mutating a shared field on the AST node.
    public void testFreshScopePerInvocation() throws Exception {
        final OutputCatchingTestRuntime runtime = OutputCatchingTestRuntime.fresh();
        final RunnableScript script = runtime.compile("capturing", """
                function foo(prefix) {
                    return text -> prefix + text;
                }
                pa = foo("a");
                print(pa("x"));
                pb = foo("b");
                print(pb("x"));
                print(pa("x")); // Problem: returns bx at the moment, instead of ba.
                """);
        script.run();
        assertEquals("ax\nbx\nax\n", runtime.getAllOutput());
    }

    /*
     * Root cause of testFreshScopePerInvocation and the tests below: ScriptFunction (see
     * ScriptFunction.java) is an AST node, created exactly once per lambda/function literal at
     * compile time. Its evaluate() method mutates a private, mutable "closureScope" instance field
     * and returns "this" as the closure value:
     *
     *     this.closureScope = process.getCurrentScope().capture();
     *     return this;
     *
     * Because the same node instance is returned every time that piece of source is evaluated,
     * every closure created from the same literal (e.g. by calling an outer function multiple
     * times, or evaluating a lambda literal more than once) is literally the same Java object.
     * They therefore all share whichever "closureScope" was captured *last*, not the one that was
     * active when each individual closure was created.
     *
     * These tests only ever *read* captured variables (never assign to them), so they exercise
     * this bug in isolation, without touching the separate bug in Assignment.java covered by
     * ClosureVariableAssignmentTestCase.
     */

    @Test
    // FIXED: same root cause as testFreshScopePerInvocation.
    public void testThreeSequentialClosuresAllAlias() throws Exception {
        final OutputCatchingTestRuntime runtime = OutputCatchingTestRuntime.fresh();
        final RunnableScript script = runtime.compile("threeStage", """
                function foo(prefix) {
                    return text -> prefix + text;
                }
                pa = foo("a");
                pb = foo("b");
                pc = foo("c");
                print(pa("x"));
                print(pb("x"));
                print(pc("x"));
                """);
        script.run();
        // Actual (buggy) output as of this writing: "cx\ncx\ncx\n" -- pa, pb and pc are the
        // same object, so all three calls use pc's captured scope ("c").
        assertEquals("ax\nbx\ncx\n", runtime.getAllOutput());
    }

    @Test
    // FIXED: same root cause as testFreshScopePerInvocation.
    public void testHandlerRegistryPatternAliases() throws Exception {
        // A realistic production pattern: registering per-key callback closures in a map/registry,
        // to be invoked later, possibly out of registration order.
        final OutputCatchingTestRuntime runtime = OutputCatchingTestRuntime.fresh();
        final RunnableScript script = runtime.compile("handlerRegistry", """
                handlers = {};
                function register(key, prefix) {
                    handlers.put(key, text -> prefix + ": " + text);
                }
                register("a", "Alpha");
                register("b", "Beta");
                ha = handlers.get("a");
                hb = handlers.get("b");
                print(ha("hello"));
                print(hb("hello"));
                """);
        script.run();
        // Actual (buggy) output as of this writing: "Beta: hello\nBeta: hello\n" -- both handlers
        // are the same object and both use "Beta"'s captured scope, regardless of which key was used.
        assertEquals("Alpha: hello\nBeta: hello\n", runtime.getAllOutput());
    }

    @Test
    // FIXED: same root cause as testFreshScopePerInvocation, but for a named nested "function", not just an arrow lambda.
    public void testNamedNestedFunctionAliasesToo() throws Exception {
        // "function foo(...) { ... }" compiles to an assignment of a ScriptFunction expression
        // (JanitorAntlrCompiler.visitFunctionDeclaration), so a *named* function nested inside
        // another function is affected by exactly the same bug as an anonymous lambda literal.
        final OutputCatchingTestRuntime runtime = OutputCatchingTestRuntime.fresh();
        final RunnableScript script = runtime.compile("namedNested", """
                function foo(prefix) {
                    function inner(text) {
                        return prefix + text;
                    }
                    return inner;
                }
                pa = foo("a");
                pb = foo("b");
                print(pa("x"));
                print(pb("x"));
                """);
        script.run();
        // Actual (buggy) output as of this writing: "bx\nbx\n"
        assertEquals("ax\nbx\n", runtime.getAllOutput());
    }

}
