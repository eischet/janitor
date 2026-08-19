package com.eischet.janitor;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorNameException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.functions.JCallable;
import com.eischet.janitor.template.TemplateParser;
import org.junit.jupiter.api.Test;

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
     * It's unfeasible to pre-add all those to a Map, so the object provider is instead called with the name of the
     * variable to look up.
     *
     * @throws JanitorRuntimeException when it fails
     * @throws JanitorCompilerException when it fails
     */
    @Test
    void templateWithImplicitGlobals() throws JanitorRuntimeException, JanitorCompilerException {
        // without a provider, this will throw:
        assertThrows(JanitorNameException.class, () -> evaluate("return '<%= FOO %>'.expand()"));
        // with a provider, it will work:
        evaluate("return '<%= FOO %>'.expand()", env -> env.setImplicitTemplateObjectProvider((p, n) -> {
            if (n.equals("FOO")) {
                return Janitor.string("bar!");
            }
            return null;
        }), g -> {});
    }








}
