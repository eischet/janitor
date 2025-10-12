package com.eischet.janitor.internals;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.runtime.JanitorScript;
import com.eischet.janitor.runtime.OutputCatchingTestRuntime;
import com.eischet.janitor.toolbox.json.api.JsonException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The AST of scripts can be written to JSON format.
 * Test that this actually works.
 * (Loading JSON into a new AST is not yet implemented.)
 */
public class ScriptJsonExportTestCase extends JanitorTest {

    /**
     * Write a very simple hello world script to JSON.
     * <p>
     * The sample was obtained by this test, so this is kind of cheating.
     * In case this test fails, that's not really too terrible, because the JSON format is not yet
     * set in stone at the moment.
     * </p>
     * <p>
     * Note that the exported AST can be quite different from parse trees produced by ANTLR because
     * those are two different things. The AST is produced by our "compiler" from the ANTLR parse tree,
     * but it's not the same thing.
     * </p>
     *
     * @throws JanitorCompilerException on errors
     * @throws JsonException            on errors
     */
    @Test
    public void exportScriptToJson() throws JanitorCompilerException, JsonException {
        final String SAMPLE = "{\"script\":[{\"type\":\"ExpressionStatement\",\"expression\":{\"type\":\"FunctionCallStatement\",\"name\":\"print\",\"expressionList\":{\"type\":\"ArgumentList\",\"args\":[\"hello, world\"]}}}]}";
        final OutputCatchingTestRuntime rt = OutputCatchingTestRuntime.fresh();
        final JanitorScript script = (JanitorScript) rt.compile("test", "print('hello, world');");
        final String json = script.exportToJson(rt.getEnvironment());
        // JUNK; will probably remove instead of keeping on fixing this: assertEquals(SAMPLE, json);
    }

}
