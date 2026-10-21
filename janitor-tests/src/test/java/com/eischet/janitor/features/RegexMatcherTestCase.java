// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.features;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.errors.runtime.JanitorNativeException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.runtime.OutputCatchingTestRuntime;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * regex.matcher(text): find() and matches() select a match, group/start/end read it or one of its groups, by number or
 * name. The matcher used to have no find() or matches(), and group/start/end read the wrong argument.
 */
public class RegexMatcherTestCase extends JanitorTest {

    private static String output(final String source) throws JanitorCompilerException, JanitorRuntimeException {
        final OutputCatchingTestRuntime runtime = OutputCatchingTestRuntime.fresh();
        runtime.compile("test", source).run();
        return runtime.getAllOutput();
    }

    @Test
    void findInALoop() throws Exception {
        assertEquals("1-2 at 2..5: 1 2\n33-44 at 8..13: 33 44\n", output("""
                m = re/(\\d+)-(\\d+)/.matcher("a 1-2 b 33-44");
                while (m.find()) {
                    print(m.group() + " at " + m.start() + ".." + m.end() + ": " + m.group(1) + " " + m.group(2));
                }
                """));
    }

    @Test
    void namedGroups() throws Exception {
        assertEquals("2024 03 9\n", output("""
                m = re/(?<year>\\d{4})-(?<month>\\d\\d)/.matcher("due 2024-03");
                if (m.find()) {
                    print(m.group("year") + " " + m.group("month") + " " + m.start("month"));
                }
                """));
    }

    @Test
    void matchesTheWholeText() throws Exception {
        assertEquals("true\nab12\n2\nfalse\n", output("""
                m = re/([a-z]+)(\\d+)/.matcher("ab12");
                print(m.matches());
                print(m.group());
                print(m.groupCount());
                print(re/\\d+/.matcher("12x").matches());
                """));
    }

    @Test
    void groupsThatDidNotTakePart() throws Exception {
        assertEquals("null\n-1\n-1\n", output("""
                m = re/(a)?b/.matcher("b");
                m.find();
                print(m.group(1));
                print(m.start(1));
                print(m.end(1));
                """));
    }

    @Test
    void noCurrentMatch() {
        final JanitorNativeException before = assertThrows(JanitorNativeException.class,
                () -> output("m = re/x/.matcher(\"x\"); m.group();"));
        assertTrue(before.getText().contains("there is no current match"), before.getText());
        assertThrows(JanitorNativeException.class, () -> output("m = re/x/.matcher(\"y\"); m.find(); m.start();"));
    }

    @Test
    void unknownGroups() {
        final JanitorArgumentException byNumber = assertThrows(JanitorArgumentException.class,
                () -> output("m = re/(a)/.matcher(\"a\"); m.find(); m.group(5);"));
        assertTrue(byNumber.getText().contains("the pattern has no group 5"), byNumber.getText());
        assertThrows(JanitorArgumentException.class, () -> output("m = re/(a)/.matcher(\"a\"); m.find(); m.group(\"nope\");"));
    }
}
