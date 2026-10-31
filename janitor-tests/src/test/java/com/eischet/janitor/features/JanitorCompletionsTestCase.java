// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.features;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.scopes.ScriptBindings;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
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

    // a ticket and a registry that leads to it, like a collection of DAOs
    private static final class Ticket extends JanitorComposed<Ticket> {
        private static final DispatchTable<Ticket> DISPATCH = new DispatchTable<>();

        static {
            DISPATCH.addStringProperty("summary", self -> "")
                    .setMetaData(Janitor.MetaData.HELP, "summary: The summary of the ticket.");
        }

        private Ticket() {
            super(DISPATCH);
        }
    }

    private static final class Registry extends JanitorComposed<Registry> {
        private static final DispatchTable<Registry> DISPATCH = new DispatchTable<>();

        static {
            DISPATCH.addObjectProperty("Ticket", self -> new Ticket(), Ticket.DISPATCH)
                    .setMetaData(Janitor.MetaData.HELP, "Ticket: The tickets.");
            DISPATCH.addMethod("lookup", (self, process, args) -> Janitor.NULL)
                    .setMetaData(Janitor.MetaData.HELP, "lookup(key): Find an entry.");
        }

        private Registry() {
            super(DISPATCH);
        }
    }

    private static final ScriptBindings<String> BINDINGS = ScriptBindings.<String>create()
            .declare("registry", "Registry", Registry.DISPATCH, "The registry.", context -> new Registry())
            .declare("user", "string", "The user's name.", Janitor::nullableString);

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
    void globalsHaveTheirHelpTexts() {
        final Completion print = find(suggestions.complete("pri", 1, 3), "print").orElseThrow();
        assertEquals("print(values...)", print.signature());
        assertFalse(print.description().isEmpty());
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

    @Test
    void declaredGlobalsAreSuggested() {
        final Completion registry = find(suggestions.complete("reg", 1, 3, BINDINGS.declarations()), "registry").orElseThrow();
        assertEquals(Kind.VARIABLE, registry.kind());
        assertEquals("Registry", registry.owners());
        assertEquals("The registry.", registry.description());
    }

    @Test
    void membersOfADeclaredGlobal() {
        final List<Completion> completions = suggestions.complete("x = registry.", 1, 13, BINDINGS.declarations());
        assertEquals("Ticket", find(completions, "Ticket").orElseThrow().signature());
        assertEquals("lookup(key)", find(completions, "lookup").orElseThrow().signature());
        assertEquals("Registry", find(completions, "lookup").orElseThrow().owners());
        assertEquals(JanitorCompletions.COMMON, find(completions, "class").orElseThrow().owners());
        assertTrue(find(completions, "toUpperCase").isEmpty(), "no members of other types");
    }

    @Test
    void membersAlongAChainOfProperties() {
        final List<Completion> completions = suggestions.complete("x = registry.Ticket.su", 1, 22, BINDINGS.declarations());
        assertEquals("The summary of the ticket.", find(completions, "summary").orElseThrow().description());
        assertTrue(find(completions, "lookup").isEmpty());
    }

    @Test
    void membersOfADeclaredBuiltinType() {
        final List<Completion> completions = suggestions.complete("x = user.", 1, 9, BINDINGS.declarations());
        assertEquals("string", find(completions, "toUpperCase").orElseThrow().owners());
        assertTrue(find(completions, "Ticket").isEmpty());
    }

    @Test
    void aChainThatDoesNotStartAtAGlobalHasNoKnownType() {
        final List<Completion> completions = suggestions.complete("x = foo().registry.", 1, 19, BINDINGS.declarations());
        assertTrue(find(completions, "Ticket").isEmpty());
        assertTrue(find(completions, "toUpperCase").isPresent(), "the members of all built-in types");
    }

    @Test
    void bindingsBindEveryDeclaredNameAndNullForMissingValues() throws Exception {
        final ScriptBindings<String> bindings = ScriptBindings.extending(BINDINGS)
                .declare("user", "string", "The user's name, upper case.", context -> context == null ? null : Janitor.string(context.toUpperCase()));
        assertEquals("ADA", OutputCatchingTestRuntime.fresh().compile("test", "return user;").run(bindings.binder("ada")).janitorToString());
        assertEquals(Janitor.NULL, OutputCatchingTestRuntime.fresh().compile("test", "return user;").run(bindings.binder(null)));
        assertEquals("registry", OutputCatchingTestRuntime.fresh().compile("test", "return registry.class;").run(bindings.binder(null)).janitorToString().toLowerCase());
        assertEquals(List.of("registry", "user"), bindings.declarations().stream().map(ScriptBindings.Declaration::name).toList());
    }
}
