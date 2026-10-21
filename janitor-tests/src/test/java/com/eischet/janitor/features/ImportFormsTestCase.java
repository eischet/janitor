// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.features;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorNameException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.modules.ScriptModule;
import com.eischet.janitor.runtime.OutputCatchingTestRuntime;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The forms of import: a registered module by name, optionally with an alias, and a module that the host resolves from a
 * string. Dotted names are not part of the grammar.
 */
public class ImportFormsTestCase extends JanitorTest {

    private static String output(final OutputCatchingTestRuntime runtime, final String source) throws JanitorCompilerException, JanitorRuntimeException {
        runtime.compile("test", source).run();
        return runtime.getAllOutput();
    }

    @Test
    void registeredModules() throws Exception {
        assertEquals("[1, 2]\n2\n", output(OutputCatchingTestRuntime.fresh(), """
                import collections;
                import collections as c;
                print(collections.list(1, 2));
                print(c.set(1, 1, 2).size());
                """));
    }

    @Test
    void severalModulesInOneStatement() throws Exception {
        assertEquals("ok\n", output(OutputCatchingTestRuntime.fresh(), "import collections, collections as c; print(\"ok\");"));
    }

    @Test
    void stringNamesAskTheResolvers() throws Exception {
        final OutputCatchingTestRuntime runtime = OutputCatchingTestRuntime.fresh(env -> env.addModuleResolver((process, name) ->
                "acme.crm".equals(name) ? ScriptModule.moduleFromScript(process, name, "greeting = 'hello from crm';", null) : null));
        assertEquals("hello from crm\n", output(runtime, "import \"acme.crm\" as crm; print(crm.greeting);"));
    }

    @Test
    void missingModulesAreRuntimeErrors() {
        assertThrows(JanitorNameException.class, () -> output(OutputCatchingTestRuntime.fresh(), "import nosuchmodule;"));
        assertThrows(JanitorNameException.class, () -> output(OutputCatchingTestRuntime.fresh(), "import \"collections\" as c;"));
    }

    @Test
    void dottedNamesAreSyntaxErrors() {
        assertThrows(JanitorCompilerException.class, () -> OutputCatchingTestRuntime.fresh().compile("test", "import a.b;"));
        assertThrows(JanitorCompilerException.class, () -> OutputCatchingTestRuntime.fresh().compile("test", "import a.b as c;"));
    }
}
