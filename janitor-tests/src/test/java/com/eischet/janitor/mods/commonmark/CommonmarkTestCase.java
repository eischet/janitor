package com.eischet.janitor.mods.commonmark;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.modules.commonmark.CommonMarkModule;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test the CommonMark parser and renderer.
 * Initial tests are simply translated from <a href="https://github.com/commonmark/commonmark-java">commonmark-java</a>.
 */
public class CommonmarkTestCase extends JanitorTest {



    /**
     * Test parsing and rendering CommonMark content to HTML.
     */
    @Test
    void parseAndRenderToHtml() throws JanitorRuntimeException, JanitorCompilerException {
        final @Language("Janitor") String script = """
            import commonmark;
            
            return commonmark.parse("This is *Markdown*").toHtml();
            """;

        final JanitorObject result = evaluate(script, env -> env.addModule(CommonMarkModule.REGISTRATION), null);
        assertEquals("<p>This is <em>Markdown</em></p>\n", result.janitorToString());
    }

    @Test void renderToMarkdown() throws JanitorRuntimeException, JanitorCompilerException {
        final @Language("Janitor") String script = """
            import commonmark;
            
            // Build document
            heading = commonmark.Heading(); // {level: 2});
            heading.level = 2;
            
            // ??? does not work ??? heading.appendChild(commonmark.Text({literal: "My heading"}));
            text = commonmark.Text();
            text.literal = "My heading";
            
            heading.appendChild(text);
            
            document = commonmark.Document();
            document.appendChild(heading);
            
            // Render to Markdown
            return document.toMarkdown();
            """;
        final JanitorObject result = evaluate(script, env -> env.addModule(CommonMarkModule.REGISTRATION), null);
        assertEquals("## My heading\n", result.janitorToString());
    }

    // TODO: in CommonMarkModule, passing initializing maps to the constructors does not work yet, e.g. commonmark.Heading({level: 2});
    // TODO: in CommonMarkModule, we need shorthands like commonmark.Text("My heading") instead of commonmark.Text({literal: "My heading"}), even which does not work yet
    // TOOD: in CMNode, appendChild(string) should automatically turn the string into a Text node
    // TODO: in CMNode, allow passing a map with additional options to toMarkdown(), toHtml(), toText() to customize the output
}
