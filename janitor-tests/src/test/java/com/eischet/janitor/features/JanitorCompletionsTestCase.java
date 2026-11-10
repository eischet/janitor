// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.features;

import com.eischet.janitor.api.scopes.VariableScope;
import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.metadata.MetaDataMap;
import com.eischet.janitor.api.metadata.ScriptType;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.modules.JanitorModule;
import com.eischet.janitor.api.modules.JanitorModuleRegistration;
import com.eischet.janitor.api.modules.ModuleResolver;
import com.eischet.janitor.api.scopes.Scope;
import com.eischet.janitor.runtime.modules.CollectionsModule;
import com.eischet.janitor.api.scopes.ScriptBindings;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.api.types.dispatch.Dispatcher;
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
            DISPATCH.setMetaData(Janitor.MetaData.CLASS, "Ticket");
            DISPATCH.addStringProperty("summary", self -> "")
                    .setMetaData(Janitor.MetaData.HELP, "summary: The summary of the ticket.");
        }

        private Ticket() {
            super(DISPATCH);
        }
    }

    // like a DAO: the methods every DAO has, those of one DAO class, and the entities they hold
    private static final class Tickets extends JanitorComposed<Tickets> {
        private static final DispatchTable<Tickets> COMMON = new DispatchTable<>();
        private static final DispatchTable<Tickets> OWN = new DispatchTable<>();
        private static final Dispatcher<?> TYPE;

        static {
            COMMON.addMethod("select", (self, process, args) -> Janitor.NULL)
                    .setMetaData(Janitor.MetaData.HELP, "select(condition): The entities for which the condition holds.")
                    .setMetaData(Janitor.MetaData.PARAMETERS, ScriptType.parameters(
                            ScriptType.parameter("condition", ScriptType.function(ScriptType.BOOL, ScriptType.ENTITY))))
                    .setMetaData(Janitor.MetaData.RETURNS, ScriptType.listOf(ScriptType.ENTITY));
            OWN.addMethod("open", (self, process, args) -> Janitor.NULL)
                    .setMetaData(Janitor.MetaData.HELP, "open(): The open tickets.");
            final MetaDataMap metaData = new MetaDataMap();
            metaData.put(Janitor.MetaData.ENTITY, Ticket.DISPATCH);
            TYPE = Dispatcher.inherit(COMMON, OWN, metaData);
        }

        private Tickets() {
            super(OWN);
        }
    }

    private static final class Registry extends JanitorComposed<Registry> {
        private static final DispatchTable<Registry> DISPATCH = new DispatchTable<>();

        static {
            DISPATCH.addObjectProperty("Ticket", self -> new Ticket(), Ticket.DISPATCH)
                    .setMetaData(Janitor.MetaData.HELP, "Ticket: The tickets.");
            DISPATCH.addObjectProperty("Tickets", self -> new Tickets(), castDispatcher(Tickets.TYPE))
                    .setMetaData(Janitor.MetaData.HELP, "Tickets: The DAO of the tickets.");
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

    @SuppressWarnings("unchecked")
    private static <X extends JanitorObject> Dispatcher<X> castDispatcher(final Dispatcher<?> dispatcher) {
        return (Dispatcher<X>) dispatcher;
    }

    // the suggestions at the end of the source, with the declared globals
    private List<Completion> atEnd(final String source) {
        final String[] lines = source.split("\n", -1);
        return suggestions.complete(source, lines.length, lines[lines.length - 1].length(), BINDINGS.declarations());
    }

    @Test
    void membersOfALambdaParameter() {
        final List<Completion> completions = atEnd("x = registry.Tickets.select(t -> t.");
        assertEquals("Ticket", find(completions, "summary").orElseThrow().owners());
        assertTrue(find(completions, "toUpperCase").isEmpty(), "no members of other types");
        assertTrue(find(atEnd("registry.Tickets.select(t -> t.summary == 'x' and t."), "summary").isPresent());
        assertTrue(find(atEnd("registry.Tickets.select((t) -> {\n    print(1);\n    return t."), "summary").isPresent());
    }

    @Test
    void aLambdaParameterIsOnlyKnownInsideTheLambda() {
        assertTrue(find(atEnd("registry.Tickets.select(t -> true)\nt."), "summary").isEmpty());
        assertTrue(find(atEnd("foo(registry.Tickets.select(t -> true), t."), "summary").isEmpty());
        assertTrue(find(atEnd("registry.Tickets.open(t -> t."), "summary").isEmpty(), "open has no parameter types");
    }

    @Test
    void elementsOfWhatAMethodReturns() {
        assertTrue(find(atEnd("tickets = registry.Tickets.select(t -> true)\nfor (t in tickets) {\n    t."), "summary").isPresent());
        assertTrue(find(atEnd("for (t in registry.Tickets.select(t -> true)) {\n    t."), "summary").isPresent());
        assertTrue(find(atEnd("first = registry.Tickets.select(t -> true)[0];\nfirst."), "summary").isPresent());
        assertEquals("list", find(atEnd("registry.Tickets.select(t -> true)."), "size").orElseThrow().owners());
    }

    @Test
    void propertiesWithATypeHint() {
        assertEquals("string", find(atEnd("registry.Tickets.select(t -> t.summary."), "toUpperCase").orElseThrow().owners());
    }

    @Test
    void membersOfAListLiteralAndOfAnIndex() {
        assertTrue(find(atEnd("[1]."), "size").orElseThrow().owners().contains("list"));
        assertTrue(find(atEnd("[1][0]."), "size").orElseThrow().owners().contains("list"), "the element is unknown: all types");
    }

    @Test
    void modulesAfterImport() {
        final Completion collections = find(atEnd("import col"), "collections").orElseThrow();
        assertEquals(JanitorCompletions.MODULE, collections.owners());
        assertTrue(find(atEnd("import col"), "while").isEmpty(), "only modules after import");
        assertTrue(find(atEnd("import collections, "), "collections").isPresent());
        assertTrue(find(atEnd("import collections as c, "), "collections").isPresent());
        assertTrue(find(atEnd("foo(a, "), "while").isPresent(), "a comma elsewhere is no import");
    }

    @Test
    void membersOfAnImportedModule() {
        assertEquals(Kind.METHOD, find(atEnd("import collections;\ncollections."), "Set").orElseThrow().kind());
        assertEquals("collections", find(atEnd("import collections as c;\nc."), "Set").orElseThrow().owners());
        assertTrue(find(atEnd("collections."), "Set").isEmpty(), "not imported");
        final JanitorCompletions withPlainModule = new JanitorCompletions(OutputCatchingTestRuntime.fresh(env ->
                env.addModule(new JanitorModuleRegistration("plain", CollectionsModule.REGISTRATION.getModuleSupplier()))));
        final String plain = "import plain;\nplain.";
        assertTrue(withPlainModule.complete(plain, 2, 6).stream().noneMatch(c -> c.name().equals("Set") && c.owners().equals("plain")),
                "a registration without a dispatcher tells nothing");
    }

    @Test
    void membersOfAModuleImportedByAString() throws Exception {
        final VariableScope greetings = OutputCatchingTestRuntime.fresh().compile("greetings", """
                title = "Hello";
                _secret = 42;
                function greet(name, polite) {
                    return title + " " + name;
                }
                """).runAndKeepGlobals(g -> { }).getScope();
        final ModuleResolver resolver = new ModuleResolver() {
            @Override
            public JanitorModule resolveModuleByStringName(final JanitorScriptProcess process, final String name) {
                return null;
            }

            @Override
            public VariableScope describeModuleByStringName(final String name) {
                return "greetings".equals(name) ? greetings : null;
            }
        };
        final JanitorCompletions completions = new JanitorCompletions(OutputCatchingTestRuntime.fresh(env -> env.addModuleResolver(resolver)));
        final String members = "import \"greetings\" as g;\ng.";
        final List<Completion> found = completions.complete(members, 2, 2);
        final Completion greet = find(found, "greet").orElseThrow();
        assertEquals(Kind.METHOD, greet.kind());
        assertEquals("greet(name, polite)", greet.signature());
        assertEquals("greetings", greet.owners());
        assertEquals(Kind.PROPERTY, find(found, "title").orElseThrow().kind());
        assertTrue(find(found, "_secret").isEmpty(), "private names are not suggested");
        final String title = "import 'greetings' as g;\ng.title.";
        final List<Completion> titleMembers = completions.complete(title, 2, 8);
        assertTrue(find(titleMembers, "toUpperCase").isPresent(), "the type of what a value holds");
        assertTrue(titleMembers.stream().noneMatch(c -> c.owners().equals("date")), "not the members of all types");
        final String unknown = "import \"farewells\" as f;\nf.";
        assertTrue(find(completions.complete(unknown, 2, 2), "greet").isEmpty(), "a module no resolver describes is unknown");
    }

    @Test
    void inheritedMembersKeepTheirHelpTexts() {
        final Completion select = find(atEnd("registry.Tickets."), "select").orElseThrow();
        assertEquals(Kind.METHOD, select.kind());
        assertEquals("select(condition)", select.signature());
        assertTrue(find(atEnd("registry.Tickets."), "open").isPresent());
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
    void docCommentsOfTheScript() {
        final String source = "/** Greets someone. */\nfunction greet(name) {\n    return 'Hi ' + name\n}\n/** How often */\ncount = 1\n/* plain */\nother = 2\npri";
        final List<Completion> completions = suggestions.complete(source, 9, 3);
        assertEquals("Greets someone.", find(completions, "greet").orElseThrow().description());
        assertEquals("How often", find(completions, "count").orElseThrow().description());
        assertEquals("", find(completions, "other").orElseThrow().description());
    }

    @Test
    void documentedSignaturesAndMembers() {
        final String head = String.join("\n",
                "/** Finds a customer. */",
                "function find(/** the number */ id, timeout = 30) { return id; }",
                "/** Doubles */ twice = x -> x * 2;",
                "/** Settings */",
                "config = {",
                "    /** Timeout in seconds */ timeout: 30,",
                "    /** Handlers */ handlers: { /** After saving */ onSave: (row) -> row }",
                "};",
                "");
        final List<Completion> names = suggestions.complete(head + "fi", 9, 2);
        assertEquals("find(id, timeout = 30)", find(names, "find").orElseThrow().signature());
        assertEquals("twice(x)", find(names, "twice").orElseThrow().signature());
        assertEquals(Kind.FUNCTION, find(names, "twice").orElseThrow().kind());
        final List<Completion> members = suggestions.complete(head + "config.", 9, 7);
        assertEquals("Timeout in seconds", find(members, "timeout").orElseThrow().description());
        assertEquals(Kind.PROPERTY, find(members, "timeout").orElseThrow().kind());
        assertTrue(find(members, "size").isPresent(), "the members of the map stay");
        final List<Completion> nested = suggestions.complete(head + "config.handlers.", 9, 16);
        assertEquals("onSave(row)", find(nested, "onSave").orElseThrow().signature());
        assertEquals("After saving", find(nested, "onSave").orElseThrow().description());
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
