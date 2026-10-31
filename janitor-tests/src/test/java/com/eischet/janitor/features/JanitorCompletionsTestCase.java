// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.features;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.runtime.OutputCatchingTestRuntime;
import com.eischet.janitor.tooling.JanitorCompletions;
import com.eischet.janitor.tooling.JanitorCompletions.Completion;
import com.eischet.janitor.tooling.JanitorCompletions.Kind;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/** Suggestions for editors: keywords, globals and names of the script, and members after a dot. */
public class JanitorCompletionsTestCase extends JanitorTest {

    // built per test, after JanitorTest has set up the environment provider that the samples of the built-in types need
    private final JanitorCompletions suggestions = new JanitorCompletions(OutputCatchingTestRuntime.fresh());

    private static Optional<Completion> find(final List<Completion> completions, final String name) {
        return completions.stream().filter(c -> c.name().equals(name)).findFirst();
    }

    @Test
    void keywordsGlobalsAndNamesOfTheScript() {
        final String source = "function greet(name) {\n    return 'Hi ' + name\n}\ncount = 1\npri";
        final List<Completion> completions = suggestions.complete(source, 5, 3);
        assertEquals(Kind.KEYWORD, find(completions, "while").orElseThrow().kind());
        assertEquals(Kind.KEYWORD, find(completions, "function").orElseThrow().kind());
        assertEquals(Kind.FUNCTION, find(completions, "print").orElseThrow().kind());
        assertEquals(Kind.FUNCTION, find(completions, "greet").orElseThrow().kind());
        assertEquals(Kind.VARIABLE, find(completions, "count").orElseThrow().kind());
        assertEquals(Kind.VARIABLE, find(completions, "name").orElseThrow().kind());
        assertTrue(find(completions, "pri").isEmpty(), "the word being typed is not a suggestion");
        assertTrue(find(completions, "__builtin__").isEmpty());
        assertTrue(find(completions, "toUpperCase").isEmpty(), "no members without a dot");
    }

    @Test
    void globalsUsedInTheScriptStayFunctions() {
        final List<Completion> completions = suggestions.complete("print('x')\npr", 2, 2);
        assertEquals(1, completions.stream().filter(c -> c.name().equals("print")).count());
        assertEquals(Kind.FUNCTION, find(completions, "print").orElseThrow().kind());
    }

    @Test
    void membersOfAStringLiteral() {
        final List<Completion> completions = suggestions.complete("x = 'abc'.toU", 1, 13);
        final Completion upper = find(completions, "toUpperCase").orElseThrow();
        assertEquals(Kind.METHOD, upper.kind());
        assertEquals("string", upper.owners());
        assertFalse(upper.description().isEmpty());
        assertTrue(find(completions, "while").isEmpty(), "no keywords after a dot");
        assertTrue(find(completions, "size").isEmpty() || find(completions, "size").orElseThrow().owners().contains("string"),
                "only string members and those of every object");
    }

    @Test
    void membersOfAnUnknownReceiverComeFromAllTypes() {
        final List<Completion> completions = suggestions.complete("result = foo.", 1, 13);
        final Completion upper = find(completions, "toUpperCase").orElseThrow();
        assertEquals("string", upper.owners());
        assertTrue(completions.stream().anyMatch(c -> c.owners().contains("list")));
        assertTrue(completions.stream().anyMatch(c -> c.owners().contains("date")));
    }

    @Test
    void membersOfADateLiteralAfterTheOptionalDot() {
        final List<Completion> completions = suggestions.complete("d = @2024-01-01?.", 1, 17);
        assertFalse(completions.isEmpty());
        assertTrue(completions.stream().allMatch(c -> c.owners().contains("date") || c.owners().contains(JanitorCompletions.COMMON)),
                completions.toString());
    }
}
