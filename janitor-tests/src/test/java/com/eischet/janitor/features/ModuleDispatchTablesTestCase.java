// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.features;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.modules.httpclient.HttpClientModule;
import com.eischet.janitor.runtime.OutputCatchingTestRuntime;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The modules collections and httpclient use dispatch tables, like the other modules, so that dir() and help() work for
 * them. collections offers Set, List and Map, with set, list and map as aliases.
 */
public class ModuleDispatchTablesTestCase extends JanitorTest {

    private static String run(final String source) throws JanitorCompilerException, JanitorRuntimeException {
        return OutputCatchingTestRuntime.fresh(env -> env.addModule(HttpClientModule.REGISTRATION))
                .compile("test", source).run().janitorToString();
    }

    @Test
    void collections() throws Exception {
        assertEquals("[1, 2]", run("import collections; collections.List(1, 2)"));
        assertEquals("2", run("import collections; collections.Set(1, 1, 2).size()"));
        assertEquals("{}", run("import collections; collections.Map()"));
        assertThrows(JanitorArgumentException.class, () -> run("import collections; collections.Map(1);"));
    }

    @Test
    void lowerCaseAliases() throws Exception {
        assertEquals("[1, 2]", run("import collections; collections.list(1, 2)"));
        assertEquals("2", run("import collections; collections.set(1, 1, 2).size()"));
        assertEquals("{}", run("import collections; collections.map()"));
    }

    @Test
    void dirAndHelpForCollections() throws Exception {
        assertEquals("[List, Map, Set]", run("import collections; dir(collections)"));
        assertTrue(run("import collections; help(collections.Set)").startsWith("Set(values...):"));
    }

    @Test
    void httpclient() throws Exception {
        assertEquals("[build, fetch]", run("import httpclient; dir(httpclient)"));
        assertEquals("http-client", run("import httpclient; httpclient.build().class"));
        assertFalse(run("import httpclient; dir(httpclient.build())").contains("_test_"), "the test method is gone");
        assertThrows(JanitorArgumentException.class, () -> run("import httpclient; httpclient.fetch();"));
        assertThrows(JanitorArgumentException.class, () -> run("import httpclient; httpclient.build(1);"));
    }
}
