// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorNameException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.builtin.JString;
import com.eischet.janitor.api.types.functions.JCallable;
import com.eischet.janitor.runtime.OutputCatchingTestRuntime;
import com.eischet.janitor.template.TemplateParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TemplatingTestCase extends JanitorTest {

    @Test
    void template1() throws JanitorRuntimeException, JanitorCompilerException {
        final String text = "text ends with 'foo'";
        final TemplateParser parser = new TemplateParser(text);
        final String script = parser.toScript(TemplateParser::plainRenderer);
        evaluate(script, g -> g.bindF("__OUT__", (JCallable) (process, arguments) -> Janitor.NULL));
    }

    /**
     * Test having a global function that can look up template variables.
     * The use case in my main app like this: there's a list of 'macros' that an admin defines, use for example in
     * SQL statements: SUPPORT_TEAM_IDS=(1,2,3,4), then in templates: where item_id in <%= SUPPORT_TEAM_IDS %>.
     * It's unfeasible to pre-add all those to a Map, so the environment's template globals are an object that looks up
     * each name when a template asks for it.
     *
     * @throws JanitorRuntimeException when it fails
     * @throws JanitorCompilerException when it fails
     */
    @Test
    void templateWithImplicitGlobals() throws JanitorRuntimeException, JanitorCompilerException {
        // without a provider, this will throw:
        assertThrows(JanitorNameException.class, () -> evaluate("return '<%= FOO %>'.expand()"));
        // with template globals, it will work:
        assertEquals("bar!", evaluate("return '<%= FOO %>'.expand()", env -> env.setTemplateGlobals(new JanitorObject() {
            @Override
            public JanitorObject janitorGetAttribute(final JanitorScriptProcess process, final String name, final boolean required) {
                return name.equals("FOO") ? Janitor.string("bar!") : null;
            }
        }), g -> {}).janitorToString());
    }









    /**
     * A template literal keeps its compiled template, so a template in a loop is compiled once, and still expands with the
     * values of each call. It keeps the code for the environment, not a runtime: the template runs with the runtime of the
     * script that expands it.
     */
    @Test
    void templateLiteralsAreCompiledOnce() throws JanitorRuntimeException, JanitorCompilerException {
        final OutputCatchingTestRuntime runtime = OutputCatchingTestRuntime.fresh();
        assertEquals("1 2 3", runtime.compile("loop", """
                parts = [];
                for (i from 1 to 3) {
                    parts.add("<%= x %>".expand({"x": i}));
                }
                return parts.join(" ");
                """).run().janitorToString());
        final JanitorObject literal = runtime.compile("literal", """
                t = "<%= x %>";
                t.expand({"x": 1});
                return t;
                """).run();
        final JString template = assertInstanceOf(JString.class, literal);
        assertNotNull(template.getCompiledTemplate(runtime.getEnvironment()));
        assertNull(template.getCompiledTemplate(OutputCatchingTestRuntime.fresh().getEnvironment()), "kept per environment");
    }

    /**
     * A template that was compiled under one runtime runs with the runtime of the script that expands it later.
     */
    @Test
    void templatesRunWithTheRuntimeOfTheirCaller() throws JanitorRuntimeException, JanitorCompilerException {
        final OutputCatchingTestRuntime first = OutputCatchingTestRuntime.fresh();
        final JanitorObject literal = first.compile("first", """
                t = "<% print('in template'); %>";
                t.expand();
                return t;
                """).run();
        final OutputCatchingTestRuntime second = first.withSameEnvironment();
        second.compile("second", "t.expand();").run(g -> g.bind("t", literal));
        assertEquals("in template\n", first.getAllOutput());
        assertEquals("in template\n", second.getAllOutput(), "the second expansion prints through the second runtime");
    }
}
