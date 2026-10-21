// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.features;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.runtime.OutputCatchingTestRuntime;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * list.removeAll(other), which used to fail on every call because it required zero arguments.
 */
public class ListRemoveAllTestCase extends JanitorTest {

    private static JanitorObject run(final String source) throws JanitorCompilerException, JanitorRuntimeException {
        return OutputCatchingTestRuntime.fresh().compile("test", source).run();
    }

    @Test
    void removesEveryOccurrence() throws Exception {
        assertEquals("[1, 3]", run("l = [1, 2, 3, 2, 4]; l.removeAll([2, 4]); return l;").janitorToString());
        assertEquals("[a]", run("l = [\"a\", \"b\"]; l.removeAll([\"b\", \"x\"]); return l;").janitorToString());
        assertEquals("[2]", run("l = [1, 2, 1.0]; l.removeAll([1]); return l;").janitorToString()); // 1 == 1.0
    }

    @Test
    void returnsNull() throws Exception {
        assertEquals("null", run("[1].removeAll([1])").janitorToString());
    }

    @Test
    void edgeCases() throws Exception {
        assertEquals("[1, 2]", run("l = [1, 2]; l.removeAll([]); return l;").janitorToString());
        assertEquals("[]", run("l = [1, 2, 1]; l.removeAll(l); return l;").janitorToString());
    }

    @Test
    void theArgumentMustBeAList() {
        assertThrows(JanitorArgumentException.class, () -> run("[1].removeAll();"));
        assertThrows(JanitorArgumentException.class, () -> run("[1].removeAll(1);"));
        assertThrows(JanitorArgumentException.class, () -> run("[1].removeAll([1], [2]);"));
    }
}
