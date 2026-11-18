// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.features;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.ValueGuard;
import com.eischet.janitor.api.errors.runtime.JanitorPermissionException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JString;
import com.eischet.janitor.runtime.OutputCatchingTestRuntime;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * A host can set a guard on a script process that decides what the script gets to see. However a script reaches a value
 * it must not see (a variable, a member, a method, a function, an index, a loop, a lambda), it only gets what the guard
 * returns.
 */
public class ValueGuardTestCase extends JanitorTest {

    private static final String SECRET = "secret";

    /** Counts how often a guard was shown the secret. */
    private final AtomicInteger secretsSeen = new AtomicInteger();

    /** Replaces the secret by "hidden" and leaves everything else alone. */
    private final ValueGuard hiding = (process, value) -> isSecret(value) ? process.getBuiltins().string("hidden") : value;

    private boolean isSecret(final JanitorObject value) {
        if (value.janitorUnpack() instanceof JString string && SECRET.equals(string.janitorGetHostValue())) {
            secretsSeen.incrementAndGet();
            return true;
        }
        return false;
    }

    /**
     * Runs a script that has the secret in the variable {@code x} and can get it from the function {@code f()}.
     */
    private static String run(final @Language("Janitor") String script, final Consumer<JanitorScriptProcess> setup) throws Exception {
        final OutputCatchingTestRuntime runtime = OutputCatchingTestRuntime.fresh();
        return runtime.compile("test", script)
                .run(runtime, process -> {
                    process.bind("x", SECRET);
                    process.bindF("f", (p, args) -> p.getBuiltins().string(SECRET));
                    setup.accept(process);
                })
                .janitorToString();
    }

    private String guarded(final @Language("Janitor") String script) throws Exception {
        return run(script, process -> process.setValueGuard(hiding));
    }

    @Test
    void withoutAGuardScriptsSeeEverything() throws Exception {
        assertEquals("secret", run("return x;", process -> { }));
        assertEquals("secret", run("return f();", process -> { }));
    }

    @Test
    void variable() throws Exception {
        assertEquals("hidden", guarded("return x;"));
        assertEquals("hidden!", guarded("return x + '!';"));
    }

    @Test
    void functionCall() throws Exception {
        assertEquals("hidden", guarded("return f();"));
    }

    @Test
    void memberOfAnObjectTheScriptMayHold() throws Exception {
        // the map is built natively, so the script never had the secret in a variable of its own
        assertEquals("hidden", guarded("o = {}.parseJson('{\"a\": \"secret\"}'); return o.a;"));
        assertEquals("hidden", guarded("o = {}.parseJson('{\"a\": {\"b\": \"secret\"}}'); return o.a.b;"));
    }

    @Test
    void methodCall() throws Exception {
        assertEquals("hidden", guarded("return 'SECRET'.toLowerCase();"));
    }

    @Test
    void index() throws Exception {
        assertEquals("hidden", guarded("l = [].parseJson('[\"secret\"]'); return l[0];"));
    }

    @Test
    void loopVariable() throws Exception {
        assertEquals("hidden", guarded("r = ''; for (e in [].parseJson('[\"secret\"]')) { r = r + e; } return r;"));
    }

    @Test
    void lambdaParameter() throws Exception {
        assertEquals("[hidden!]", guarded("return [].parseJson('[\"secret\"]').map(e -> e + '!');"));
    }

    @Test
    void theGuardCanHideAValueCompletely() throws Exception {
        assertEquals("true", run("return x == null;", process -> process.setValueGuard((p, value) -> isSecret(value) ? Janitor.NULL : value)));
    }

    @Test
    void theGuardCanRefuse() {
        final ValueGuard refusing = (p, value) -> {
            if (isSecret(value)) {
                throw new JanitorPermissionException(p, "not for you");
            }
            return value;
        };
        final JanitorRuntimeException refused = assertThrows(JanitorRuntimeException.class, () -> run("return x;", process -> process.setValueGuard(refusing)));
        assertEquals(JanitorPermissionException.class, refused.getClass());
    }

    @Test
    void assigningToAMemberDoesNotReadIt() throws Exception {
        assertEquals("done", guarded("o = {}.parseJson('{\"a\": \"secret\"}'); o.a = 'new'; return 'done';"));
        assertEquals(0, secretsSeen.get(), "the old value of the member was shown to the guard");
    }

    @Test
    void literalsOfTheScriptAreNotGuarded() throws Exception {
        assertEquals("secret", guarded("return 'secret';"));
        assertEquals(0, secretsSeen.get());
    }

    @Test
    void processesStartedForAGuardedScriptInheritTheGuard() throws Exception {
        final AtomicReference<JanitorScriptProcess> parent = new AtomicReference<>();
        run("return 1;", process -> {
            process.setValueGuard(hiding);
            parent.set(process);
        });
        assertEquals("hidden", run("return x;", process -> process.inheritMetaData(parent.get())));
        assertEquals("secret", run("return x;", process -> { }));
    }

}
