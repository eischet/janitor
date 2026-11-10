// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.features;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.RunnableScript;
import com.eischet.janitor.runtime.OutputCatchingTestRuntime;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A compiled script holds no runtime: a host can keep it, e.g. in a cache, and run it with the runtime of each job, so that
 * nothing of a finished job, such as its log, stays reachable through the script.
 */
public class CompiledScriptRuntimeTestCase extends JanitorTest {

    @Test
    void everyRunUsesTheRuntimeItIsGiven() throws Exception {
        final OutputCatchingTestRuntime first = OutputCatchingTestRuntime.fresh();
        final OutputCatchingTestRuntime second = first.withSameEnvironment();
        final RunnableScript script = first.compile("job", "print(\"ran\");");
        script.run(first);
        script.run(second);
        script.run(second);
        assertEquals("ran\n", first.getAllOutput());
        assertEquals("ran\nran\n", second.getAllOutput());
    }
}
