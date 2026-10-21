// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.features;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.FilterPredicate;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.errors.runtime.JanitorInstructionLimitExceededException;
import com.eischet.janitor.api.modules.ScriptModule;
import com.eischet.janitor.env.JanitorDefaultEnvironment;
import com.eischet.janitor.runtime.OutputCatchingTestRuntime;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The run methods hand the process to a setup callback, which binds globals and configures the process; the environment
 * gives every process a default instruction limit, which a run can override.
 */
public class ProcessSetupTestCase extends JanitorTest {

    private static final String ENDLESS = "while (true) { }";
    private static final String FIVE_THOUSAND_STEPS = "n = 0; while (n < 5000) { n++; } return n;";

    private static OutputCatchingTestRuntime runtimeWithLimit(final long limit) {
        return OutputCatchingTestRuntime.fresh(env -> ((JanitorDefaultEnvironment) env).setMaxInstructionCount(limit));
    }

    @Test
    void bindThroughTheProcess() throws Exception {
        final OutputCatchingTestRuntime runtime = OutputCatchingTestRuntime.fresh();
        final String result = runtime.compile("test", "return a + \" \" + b + \" \" + c + \" \" + twice(21);")
                .run(process -> process
                        .bind("a", 1)
                        .bind("b", "two")
                        .bind("c", true)
                        .bindF("twice", (p, args) -> Janitor.integer(args.getRequiredLongValue(0) * 2)))
                .janitorToString();
        assertEquals("1 two true 42", result);
    }

    @Test
    void limitPerRun() throws Exception {
        final OutputCatchingTestRuntime runtime = OutputCatchingTestRuntime.fresh();
        assertThrows(JanitorInstructionLimitExceededException.class,
                () -> runtime.compile("test", ENDLESS).run(process -> process.setMaxInstructionCount(1000)));
    }

    @Test
    void defaultLimitOfTheEnvironment() throws Exception {
        final OutputCatchingTestRuntime runtime = runtimeWithLimit(1000);
        assertThrows(JanitorInstructionLimitExceededException.class, () -> runtime.compile("test", ENDLESS).run());
        assertThrows(JanitorInstructionLimitExceededException.class, () -> runtime.compile("test", FIVE_THOUSAND_STEPS).run());
        // a run can override the default, here with "no limit"
        assertEquals(5000L, runtime.compile("test", FIVE_THOUSAND_STEPS).run(process -> process.setMaxInstructionCount(0)).janitorGetHostValue());
    }

    @Test
    void noLimitByDefault() throws Exception {
        assertEquals(5000L, OutputCatchingTestRuntime.fresh().compile("test", FIVE_THOUSAND_STEPS).run().janitorGetHostValue());
    }

    @Test
    void filterScripts() throws Exception {
        final OutputCatchingTestRuntime runtime = OutputCatchingTestRuntime.fresh();
        final FilterPredicate bigger = runtime.getEnvironment().filterScript("bigger", "value > limit", process -> process.bind("limit", 5));
        assertTrue(bigger.test(Janitor.integer(7)));
        assertFalse(bigger.test(Janitor.integer(3)));
    }

    @Test
    void theDefaultLimitAppliesToFilterScripts() {
        final OutputCatchingTestRuntime runtime = runtimeWithLimit(1000);
        final FilterPredicate endless = runtime.getEnvironment().filterScript("endless", "while (true) { } return true;", null);
        assertFalse(endless.test(Janitor.integer(1)), "a filter that exceeds the limit fails, and filter errors count as false");
    }

    @Test
    void scriptModuleBindings() throws Exception {
        final OutputCatchingTestRuntime runtime = OutputCatchingTestRuntime.fresh(env -> env.addModuleResolver((process, name) ->
                "base".equals(name) ? ScriptModule.moduleFromScript(process, name, "answer = start + 1;", p -> p.bind("start", 41)) : null));
        assertEquals("42", runtime.compile("test", "import \"base\" as base; return base.answer;").run().janitorToString());
    }
}
