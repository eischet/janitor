package com.eischet.janitor;

import com.eischet.janitor.api.RunnableScript;
import com.eischet.janitor.runtime.OutputCatchingTestRuntime;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ClosureScopesTestCase {

    @Test
    public void testCaptureArgumentScopes() throws Exception {
        // TODO: this should work fine, but throws: name 'arg' is not defined
        // Likely cause: while the outside scope is captured, the arg scope is not
        /*
        final OutputCatchingTestRuntime runtime = OutputCatchingTestRuntime.fresh();
        final RunnableScript script = runtime.compile("capturing", """
                function foo(arg) {
                    return (x) -> arg + x;
                }
                stage1 = foo(10);
                print(stage1(7));
                """);
        script.run();
        assertEquals("17\n", runtime.getAllOutput());
        */
    }

    @Test
    public void testFreshScopePerInvocation() throws Exception {
        // TODO: multiple invocations of function that return functions do not work correctly
        /*
        final OutputCatchingTestRuntime runtime = OutputCatchingTestRuntime.fresh();
        final RunnableScript script = runtime.compile("capturing", """
                function foo(prefix) {
                    pre = prefix; // workaround for bug at testCaptureArgumentScopes
                    return text -> pre + text;
                }
                pa = foo("a");
                //pb = foo("b");
                //assert(pb("x") == "bx");
                assert(pa("x") == "ax"); // right now it returns bx, because we somehow clobbered the previous scope!?
                """);
        script.run();
        */
    }

}
