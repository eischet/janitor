// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.features;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.runtime.OutputCatchingTestRuntime;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The operators ~ and !~ with wildcard patterns: * and ? are wildcards, everything else is literal, case is ignored.
 */
public class WildcardOperatorTestCase extends JanitorTest {

    private static String output(final String source) throws JanitorCompilerException, JanitorRuntimeException {
        final OutputCatchingTestRuntime runtime = OutputCatchingTestRuntime.fresh();
        runtime.compile("test", source).run();
        return runtime.getAllOutput();
    }

    @Test
    void wildcards() throws Exception {
        assertEquals("true\nfalse\ntrue\nfalse\ntrue\ntrue\ntrue\n", output("""
                print("report.pdf" ~ "*.pdf");
                print("reportXpdf" ~ "*.pdf");
                print("abc" ~ "a?c");
                print("abc" ~ "a.c");
                print("(1)" ~ "(*)");
                print("x" ~ "*");
                print("Report.PDF" ~ "report.*");
                """));
    }

    @Test
    void negation() throws Exception {
        assertEquals("false\ntrue\n", output("""
                print("report.pdf" !~ "*.pdf");
                print("abc" !~ "a.c");
                """));
    }

    @Test
    void theCacheBelongsToThePattern() throws Exception {
        // the compiled pattern used to be cached with the text; with shared short strings, a text could later be used as a
        // pattern and find the matcher of another pattern
        assertEquals("true\nfalse\nfalse\ntrue\n", output("""
                print("abc" ~ "a*");
                print("axe" ~ "abc");
                print("abd" ~ "abc");
                print("abc" ~ "abc");
                """));
        assertEquals("3\n", output("""
                n = 0;
                for (name in ["a.txt", "b.pdf", "c.TXT", "d.txt"]) {
                    if (name ~ "*.txt") {
                        n++;
                    }
                }
                print(n);
                """));
    }

    @Test
    void regexLiteralsAreStillRegexAndCaseSensitive() throws Exception {
        assertEquals("true\nfalse\n", output("""
                print("abc" ~ re/a.c/);
                print("ABC" ~ re/abc/);
                """));
    }
}
