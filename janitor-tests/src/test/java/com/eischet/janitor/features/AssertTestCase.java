// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.features;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.errors.runtime.JanitorAssertionException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.runtime.OutputCatchingTestRuntime;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * assert(condition) and assert(condition, message). The form with a message used to fail with an argument error, whether
 * the condition held or not, because assert accepted at most one argument.
 */
public class AssertTestCase extends JanitorTest {

    private static JanitorObject run(final String source) throws JanitorCompilerException, JanitorRuntimeException {
        return OutputCatchingTestRuntime.fresh().compile("test", source).run();
    }

    @Test
    void conditionOnly() throws Exception {
        assertEquals("true", run("assert(1 == 1)").janitorToString());
        final JanitorAssertionException e = assertThrows(JanitorAssertionException.class, () -> run("assert(1 == 2);"));
        assertEquals("assertion failed!", e.getText());
    }

    @Test
    void withMessage() throws Exception {
        assertEquals("true", run("assert(1 == 1, \"one is one\")").janitorToString());
        final JanitorAssertionException e = assertThrows(JanitorAssertionException.class, () -> run("assert(1 == 2, \"one is not two\");"));
        assertEquals("assertion failed: one is not two", e.getText());
    }

    @Test
    void theMessageIsVisibleInCatch() throws Exception {
        final OutputCatchingTestRuntime runtime = OutputCatchingTestRuntime.fresh();
        runtime.compile("test", "try { assert(false, \"total must be positive\"); } catch (e) { print(e.message); }").run();
        assertEquals("assertion failed: total must be positive\n", runtime.getAllOutput());
    }

    @Test
    void wrongNumberOfArguments() {
        assertThrows(JanitorArgumentException.class, () -> run("assert();"));
        assertThrows(JanitorArgumentException.class, () -> run("assert(true, \"a\", \"b\");"));
    }
}
