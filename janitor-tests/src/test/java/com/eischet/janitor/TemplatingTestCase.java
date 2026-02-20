package com.eischet.janitor;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.functions.JCallable;
import com.eischet.janitor.template.TemplateParser;
import org.junit.jupiter.api.Test;

public class TemplatingTestCase extends JanitorTest {

    @Test
    void template1() throws JanitorRuntimeException, JanitorCompilerException {
        final String text = "text ends with 'foo'";
        final TemplateParser parser = new TemplateParser(text);
        final String script = parser.toScript(TemplateParser::plainRenderer);
        evaluate(script, g -> g.bindF("__OUT__", (JCallable) (process, arguments) -> Janitor.NULL));
    }
}
