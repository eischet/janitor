// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.features;

import com.eischet.janitor.api.scopes.VariableScope;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.metadata.MetaDataKey;
import com.eischet.janitor.api.modules.ScriptModule;
import com.eischet.janitor.api.scopes.ResultAndScope;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.functions.JCallable;
import com.eischet.janitor.runtime.OutputCatchingTestRuntime;
import org.junit.jupiter.api.Test;

import java.util.List;
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
    private static final MetaDataKey<Boolean> RIGHT = new MetaDataKey<>("right", Boolean.class);

    /** Runs a script whose native function reads the key from the process that calls it. */
    private static String seenByNativeCode(final OutputCatchingTestRuntime runtime, final Consumer<JanitorScriptProcess> setup) throws Exception {
        return runtime.compile("test", "return read();")
                .run(runtime, process -> {
                    setup.accept(process);
                    process.bindF("read", (p, args) -> p.getBuiltins().nullableString(p.getMetaData(KEY)));
                })
                .janitorToString();
    }

    /** Runs a script whose native function requires the right, and returns what the script saw. */
    private static String requireSeenByScript(final OutputCatchingTestRuntime runtime, final Consumer<JanitorScriptProcess> setup) throws Exception {
        return runtime.compile("test", """
                        try {
                            guarded();
                            return "allowed";
                        } catch (e) {
                            return e.type + ": " + e.message;
                        }""")
                .run(runtime, process -> {
                    setup.accept(process);
                    process.bindF("guarded", (p, args) -> {
                        p.require(RIGHT);
                        return Janitor.NULL;
                    });
                })
                .janitorToString();
    }

    private static final String REFUSED = "JanitorPermissionException: requires right";

    @Test
    void requireRefusesAMissingRight() throws Exception {
        assertEquals(REFUSED, requireSeenByScript(OutputCatchingTestRuntime.fresh(), process -> { }));
    }

    @Test
    void requireRefusesARightSetToFalse() throws Exception {
        assertEquals(REFUSED, requireSeenByScript(OutputCatchingTestRuntime.fresh(), process -> process.setMetaData(RIGHT, false)));
    }

    @Test
    void requireAllowsARightOfTheProcess() throws Exception {
        assertEquals("allowed", requireSeenByScript(OutputCatchingTestRuntime.fresh(), process -> process.setMetaData(RIGHT, true)));
    }

    @Test
    void requireAllowsARightOfTheEnvironment() throws Exception {
        assertEquals("allowed", requireSeenByScript(OutputCatchingTestRuntime.fresh(env -> env.setMetaData(RIGHT, true)), process -> { }));
    }

    @Test
    void falseOnTheProcessOverridesTrueInTheEnvironment() throws Exception {
        assertEquals(REFUSED, requireSeenByScript(OutputCatchingTestRuntime.fresh(env -> env.setMetaData(RIGHT, true)), process -> process.setMetaData(RIGHT, false)));
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

    /** Runs a script just to get a process with the given value; the process has ended when this returns. */
    private static JanitorScriptProcess endedProcessWith(final OutputCatchingTestRuntime runtime, final String value) throws Exception {
        final AtomicReference<JanitorScriptProcess> parent = new AtomicReference<>();
        runtime.compile("parent", "return 1;").run(runtime, process -> {
            process.setMetaData(KEY, value);
            parent.set(process);
        });
        return parent.get();
    }

    @Test
    void newProcessesDoNotInheritByThemselves() throws Exception {
        final OutputCatchingTestRuntime runtime = OutputCatchingTestRuntime.fresh();
        endedProcessWith(runtime, "parent");
        assertEquals("null", seenByNativeCode(runtime, process -> { }));
    }

    @Test
    void inheritFromAParentThatHasEnded() throws Exception {
        final OutputCatchingTestRuntime runtime = OutputCatchingTestRuntime.fresh();
        final JanitorScriptProcess parent = endedProcessWith(runtime, "parent");
        assertEquals("parent", seenByNativeCode(runtime, process -> process.inheritMetaData(parent)));
    }

    @Test
    void childCanOverrideWithoutChangingTheParent() throws Exception {
        final OutputCatchingTestRuntime runtime = OutputCatchingTestRuntime.fresh();
        final JanitorScriptProcess parent = endedProcessWith(runtime, "parent");
        assertEquals("child", seenByNativeCode(runtime, process -> {
            process.inheritMetaData(parent);
            process.setMetaData(KEY, "child");
        }));
        assertEquals("parent", parent.getMetaData(KEY));
    }

    @Test
    void laterChangesOfTheParentDoNotReachTheChild() throws Exception {
        final OutputCatchingTestRuntime runtime = OutputCatchingTestRuntime.fresh();
        final JanitorScriptProcess parent = endedProcessWith(runtime, "parent");
        assertEquals("parent", seenByNativeCode(runtime, process -> {
            process.inheritMetaData(parent);
            parent.setMetaData(KEY, "changed");
        }));
    }

    @Test
    void inheritingKeepsTheFallbackToTheEnvironment() throws Exception {
        final OutputCatchingTestRuntime runtime = OutputCatchingTestRuntime.fresh(env -> env.setMetaData(KEY, "environment"));
        final AtomicReference<JanitorScriptProcess> parent = new AtomicReference<>();
        runtime.compile("parent", "return 1;").run(runtime, parent::set);
        assertEquals("environment", seenByNativeCode(runtime, process -> process.inheritMetaData(parent.get())));
    }

    @Test
    void callbackInheritsOnlyThroughTheSetup() throws Exception {
        final OutputCatchingTestRuntime runtime = OutputCatchingTestRuntime.fresh();
        final AtomicReference<JanitorScriptProcess> parent = new AtomicReference<>();
        final ResultAndScope result = runtime.compile("parent", "callback = it -> read();").runAndKeepGlobals(runtime, process -> {
            process.setMetaData(KEY, "parent");
            process.bindF("read", (p, args) -> p.getBuiltins().nullableString(p.getMetaData(KEY)));
            parent.set(process);
        });
        final JCallable callback = (JCallable) result.getScope().getVariable("callback");
        final List<JanitorObject> args = List.of(Janitor.integer(1));

        assertEquals("null", runtime.executeCallback(result.getScope(), callback, args).janitorToString());
        assertEquals("parent", runtime.executeCallback(result.getScope(), callback, args, p -> p.inheritMetaData(parent.get())).janitorToString());
        assertEquals("explicit", runtime.executeCallback(result.getScope(), callback, args, p -> p.setMetaData(KEY, "explicit")).janitorToString());
    }

    @Test
    void templateSeesTheMetaDataOfTheCaller() throws Exception {
        final OutputCatchingTestRuntime runtime = OutputCatchingTestRuntime.fresh();
        final String result = runtime.compile("test", "return \"${read()}\".expand();")
                .run(runtime, process -> {
                    process.setMetaData(KEY, "caller");
                    process.bindF("read", (p, args) -> p.getBuiltins().nullableString(p.getMetaData(KEY)));
                })
                .janitorToString();
        assertEquals("caller", result);
    }

    @Test
    void moduleCodeDoesNotInheritButItsFunctionsRunInTheCallingProcess() throws Exception {
        final OutputCatchingTestRuntime runtime = OutputCatchingTestRuntime.fresh(env -> env.addModuleResolver((process, name) ->
                "base".equals(name)
                        ? ScriptModule.moduleFromScript(process, name, "atImport = read(); function later() { return read(); }",
                                p -> p.bindF("read", (rs, args) -> rs.getBuiltins().nullableString(rs.getMetaData(KEY))))
                        : null));
        final String result = runtime.compile("test", "import \"base\" as base; return \"\" + base.atImport + \"/\" + base.later();")
                .run(runtime, process -> process.setMetaData(KEY, "importer"))
                .janitorToString();
        assertEquals("null/importer", result);
    }

}
