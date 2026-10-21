// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.features;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.runtime.OutputCatchingTestRuntime;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * dir(value) lists the members of wrapped values, too (numbers, maps, sets, binary values, regular expressions), now
 * that JanitorWrapper implements HasDispatcher. It used to return an empty list for them.
 */
public class DirAndHelpTestCase extends JanitorTest {

    private static String run(final String source) throws JanitorCompilerException, JanitorRuntimeException {
        final JanitorObject result = OutputCatchingTestRuntime.fresh().compile("test", source).run();
        return result.janitorToString();
    }

    @Test
    void dirListsTheMembersOfWrappedValues() throws Exception {
        assertTrue(run("dir(1)").contains("epoch"), run("dir(1)"));
        assertTrue(run("dir(1.5)").contains("int"));
        assertTrue(run("return dir({a: 1});").contains("containsKey"));
        assertTrue(run("dir([1].toSet())").contains("toList"));
        assertTrue(run("dir(\"x\".toBinaryUtf8())").contains("encodeBase64"));
        assertTrue(run("dir(re/x/)").contains("extractAll"));
        assertTrue(run("dir(re/x/.matcher(\"x\"))").contains("find"));
    }

    @Test
    void dirStillWorksForComposedValues() throws Exception {
        assertTrue(run("dir(\"x\")").contains("toUpperCase"));
        assertTrue(run("dir([])").contains("size"));
    }

    @Test
    void valuesWithoutMembers() throws Exception {
        assertEquals("[]", run("dir(true)"));
        assertEquals("[]", run("dir(null)"));
    }

    @Test
    void helpForMethodsOfWrappedValues() throws Exception {
        assertEquals("size(): Returns the number of entries.", run("return help({}.size);"));
    }
}
