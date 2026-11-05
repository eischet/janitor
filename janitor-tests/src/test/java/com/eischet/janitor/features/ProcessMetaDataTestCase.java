// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.features;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.metadata.MetaDataKey;
import com.eischet.janitor.runtime.OutputCatchingTestRuntime;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Hosts can attach meta-data to the environment and to a script process. Native code running for a script sees the
 * process's own value or else the environment's.
 */
public class ProcessMetaDataTestCase extends JanitorTest {

    private static final MetaDataKey<String> KEY = new MetaDataKey<>("key", String.class);
    private static final MetaDataKey<Integer> OTHER_KEY = new MetaDataKey<>("otherKey", Integer.class);

    /** Runs a script whose native function reads the key from the process that calls it. */
    private static String seenByNativeCode(final OutputCatchingTestRuntime runtime, final Consumer<JanitorScriptProcess> setup) throws Exception {
        return runtime.compile("test", "return read();")
                .run(process -> {
                    setup.accept(process);
                    process.bindF("read", (p, args) -> p.getBuiltins().nullableString(p.getMetaData(KEY)));
                })
                .janitorToString();
    }

    @Test
    void noValueByDefault() throws Exception {
        final OutputCatchingTestRuntime runtime = OutputCatchingTestRuntime.fresh();
        assertNull(runtime.getEnvironment().getMetaData(KEY));
        assertEquals("null", seenByNativeCode(runtime, process -> { }));
    }

    @Test
    void processSeesTheValueOfTheEnvironment() throws Exception {
        final OutputCatchingTestRuntime runtime = OutputCatchingTestRuntime.fresh(env -> env.setMetaData(KEY, "environment"));
        assertEquals("environment", seenByNativeCode(runtime, process -> { }));
    }

    @Test
    void environmentValueIsReadLive() throws Exception {
        final OutputCatchingTestRuntime runtime = OutputCatchingTestRuntime.fresh();
        assertEquals("changed", seenByNativeCode(runtime, process -> runtime.getEnvironment().setMetaData(KEY, "changed")));
    }

    @Test
    void valueOfTheProcessStaysInThatProcess() throws Exception {
        final OutputCatchingTestRuntime runtime = OutputCatchingTestRuntime.fresh(env -> env.setMetaData(KEY, "environment"));
        assertEquals("process", seenByNativeCode(runtime, process -> process.setMetaData(KEY, "process")));
        assertEquals("environment", runtime.getEnvironment().getMetaData(KEY));
        assertEquals("environment", seenByNativeCode(runtime, process -> { }));
    }

    @Test
    void removingTheValueOfTheProcessRevealsTheEnvironment() throws Exception {
        final OutputCatchingTestRuntime runtime = OutputCatchingTestRuntime.fresh(env -> env.setMetaData(KEY, "environment"));
        assertEquals("environment", seenByNativeCode(runtime, process -> {
            process.setMetaData(KEY, "process");
            process.setMetaData(KEY, null);
        }));
    }

    @Test
    void removingFromTheEnvironment() {
        final OutputCatchingTestRuntime runtime = OutputCatchingTestRuntime.fresh(env -> env.setMetaData(KEY, "environment"));
        runtime.getEnvironment().setMetaData(KEY, null);
        assertNull(runtime.getEnvironment().getMetaData(KEY));
    }

    @Test
    void keysAreIndependent() throws Exception {
        final OutputCatchingTestRuntime runtime = OutputCatchingTestRuntime.fresh(env -> env.setMetaData(OTHER_KEY, 42));
        final AtomicReference<Integer> seen = new AtomicReference<>();
        assertEquals("null", seenByNativeCode(runtime, process -> seen.set(process.getMetaData(OTHER_KEY))));
        assertEquals(42, seen.get());
    }

}
