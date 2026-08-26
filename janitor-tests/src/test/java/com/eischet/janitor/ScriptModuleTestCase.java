package com.eischet.janitor;

import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.modules.ScriptModule;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;

public class ScriptModuleTestCase extends JanitorTest {

    @Language("Janitor")
    private static final String SIMPLE_MODULE = """
            function add(a, b) {
                return a + b;
            }
            """;

    @Test
    void testSimpleModule() throws JanitorRuntimeException, JanitorCompilerException {
        ScriptModule simple = new ScriptModule("simple", SIMPLE_MODULE, null);
        evaluate("""
                import simple;
                assert(3 == simple.add(1, 2));
                """, env -> env.addModule(simple), g -> {
        });
    }

}
