package com.eischet.janitor;

import com.eischet.janitor.api.RunnableScript;
import com.eischet.janitor.runtime.OutputCatchingTestRuntime;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ClosureScopesTestCase {

    @Test
    public void testCaptureArgumentScopes() throws Exception {
        // TODO: this should work fine, but throws: name 'arg' is not defined
        // Likely cause: while the outside scope is captured, the arg scope is not

        final OutputCatchingTestRuntime runtime = OutputCatchingTestRuntime.fresh();

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

        // This now works, thanks to Block::executeFunctionCall, which finally improves closure scope handling:
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
    @Disabled // TODO: re-enable this
    public void testFreshScopePerInvocation() throws Exception {
        // TODO: multiple invocations of function that return functions do not work correctly... clobbered scopes!?

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

}
