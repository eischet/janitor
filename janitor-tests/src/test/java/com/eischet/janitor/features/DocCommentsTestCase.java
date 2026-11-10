// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.features;

import com.eischet.janitor.api.scopes.VariableScope;
import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.modules.ScriptModule;
import com.eischet.janitor.api.scopes.Scope;
import com.eischet.janitor.api.types.dispatch.Dispatcher;
import com.eischet.janitor.runtime.modules.CollectionsModule;
import com.eischet.janitor.tooling.ScopeDescription;
import com.eischet.janitor.runtime.OutputCatchingTestRuntime;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Doc comments (starting with two asterisks) document the name that the statement after them defines; help(name) returns them.
 */
public class DocCommentsTestCase extends JanitorTest {

    private static String run(final String source) throws JanitorCompilerException, JanitorRuntimeException {
        return OutputCatchingTestRuntime.fresh().compile("test", source).run().janitorToString();
    }

    @Test
    void functionWithSignature() throws Exception {
        assertEquals("find(id, timeout = 30, *more, **filters): Finds a customer.", run("""
                /** Finds a customer. */
                function find(id, timeout = 30, *more, **filters) { return id; }
                return help(find);
                """));
    }

    @Test
    void signatureWithoutDocComment() throws Exception {
        assertEquals("add(a, b)", run("function add(a, b) { return a + b; } return help(add);"));
    }

    @Test
    void multiLineCommentsAndParameters() throws Exception {
        assertEquals("find(id, timeout = 30): Finds a customer.\nReturns null if there is none.\n    id: the customer number\n    timeout: in seconds", run("""
                /**
                 * Finds a customer.
                 * Returns null if there is none.
                 */
                function find(
                    /** the customer number */ id,
                    /** in seconds */ timeout = 30
                ) { return id; }
                return help(find);
                """));
    }

    @Test
    void anyValue() throws Exception {
        assertEquals("Connection settings", run("/** Connection settings */ config = {}; return help(config);"));
        assertEquals("Number of attempts", run("/** Number of attempts */ retries = 3; return help(retries);"));
    }

    @Test
    void theNameNotTheValue() throws Exception {
        assertEquals("null", run("/** Connection settings */ config = {}; copy = config; return help(copy);"));
        assertEquals("null", run("/** A */ a = 3; b = 3; return help(b);"));
    }

    @Test
    void reassigningKeepsTheDocumentation() throws Exception {
        assertEquals("Number of attempts", run("/** Number of attempts */ retries = 3; retries = 5; return help(retries);"));
        assertEquals("Newer", run("/** Older */ retries = 3; /** Newer */ retries = 5; return help(retries);"));
    }

    @Test
    void lambdas() throws Exception {
        assertEquals("twice(x): Doubles x.", run("/** Doubles x. */ twice = x -> x * 2; return help(twice);"));
        assertEquals("sum(a, b)\n    a: first", run("sum = (/** first */ a, b) -> a + b; return help(sum);"));
    }

    @Test
    void mapKeys() throws Exception {
        final String script = """
                /** Connection settings */
                config = {
                    /** Timeout in seconds */
                    timeout: 30,
                    "retry count": 3,
                    /** Event handlers */
                    handlers: {
                        /** Called after saving */
                        onSave: (row) -> row
                    }
                };
                """;
        assertEquals("Timeout in seconds", run(script + "return help(config.timeout);"));
        assertEquals("onSave(row): Called after saving", run(script + "return help(config.handlers.onSave);"));
        assertEquals("null", run(script + "return help(config.missing);"));
    }

    @Test
    void memberAssignments() throws Exception {
        assertEquals("Timeout in seconds", run("config = {}; /** Timeout in seconds */ config.timeout = 30; return help(config.timeout);"));
    }

    @Test
    void otherCommentsAreNoDocumentation() throws Exception {
        assertEquals("null", run("/* plain */ a = 1; return help(a);"));
        assertEquals("null", run("// line\na = 1; return help(a);"));
        assertEquals("null", run("/**/ a = 1; return help(a);"));
    }

    @Test
    void localVariables() throws Exception {
        assertEquals("inner", run("function f() { /** inner */ x = 1; return help(x); } return f();"));
    }

    @Test
    void builtinsStillHaveTheirHelp() throws Exception {
        assertEquals("size(): Returns the number of entries.", run("return help({}.size);"));
        assertEquals(true, run("return help(print);").startsWith("print("));
    }

    @Test
    void modules() throws Exception {
        final ScriptModule tools = new ScriptModule("tools", """
                /** Greets someone. */
                function greet(/** who to greet */ name) { return "Hello " + name; }
                /** private */
                function _secret() {}
                """, null);
        assertEquals("greet(name): Greets someone.\n    name: who to greet",
                evaluate("import tools; return help(tools.greet);", env -> env.addModule(tools), g -> {}).janitorToString());
    }

    @Test
    void multiLineParameterDocumentation() throws Exception {
        assertEquals("f(a)\n    a: first line\n        second line", run("""
                function f(
                    /**
                     * first line
                     * second line
                     */
                    a) { return a; }
                return help(f);
                """));
    }

    @Test
    void javaModulesDocumentThemselves() throws Exception {
        assertEquals("Factory functions for collections.",
                evaluate("import collections; return help(collections);", env -> env.addModule(CollectionsModule.REGISTRATION), g -> {}).janitorToString());
    }

    @Test
    void scopeDescriptionsUseTheDocumentation() throws Exception {
        final VariableScope scope = OutputCatchingTestRuntime.fresh().compile("tools", """
                /** Greets someone. */
                function greet(name) { return name; }
                function plain(a, b) { return a; }
                /** The default greeting */
                greeting = "Hello";
                """).runAndKeepGlobals().getScope();
        final Dispatcher<?> description = ScopeDescription.of("tools", scope);
        assertEquals("greet(name): Greets someone.", description.getMetaData("greet", Janitor.MetaData.HELP));
        assertEquals("plain(a, b): A function of the module tools.", description.getMetaData("plain", Janitor.MetaData.HELP));
        assertEquals("The default greeting", description.getMetaData("greeting", Janitor.MetaData.HELP));
    }

    @Test
    void moduleDocumentation() throws Exception {
        final ScriptModule tools = new ScriptModule("tools", """
                /*!
                 * Tools for customers.
                 */
                /** Greets someone. */
                function greet(name) { return "Hello " + name; }
                """, null);
        assertEquals("Tools for customers.", evaluate("import tools; return help(tools);", env -> env.addModule(tools), g -> {}).janitorToString());
        assertEquals("Tools for customers.", evaluate("import tools as t; return help(t);", env -> env.addModule(tools), g -> {}).janitorToString());
        assertEquals("greet(name): Greets someone.", evaluate("import tools; return help(tools.greet);", env -> env.addModule(tools), g -> {}).janitorToString());
        assertEquals("Tools for customers.", OutputCatchingTestRuntime.fresh().compile("test", "/*! Tools for customers. */ x = 1;").getDocumentation());
        assertEquals(null, OutputCatchingTestRuntime.fresh().compile("test", "/** not the module */ x = 1;").getDocumentation());
    }

}
