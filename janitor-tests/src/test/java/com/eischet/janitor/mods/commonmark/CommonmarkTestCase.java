// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

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
            heading = commonmark.Heading({level: 2});
            // heading.level = 2;
            
            // ??? does not work ??? heading.appendChild(commonmark.Text({literal: "My heading"}));
            text = commonmark.Text({literal: "My heading"});
            // text.literal = "My heading";
            
            heading.appendChild(text);
            
            document = commonmark.Document();
            document.appendChild(heading);
            
            // Render to Markdown
            return document.toMarkdown();
            """;
        final JanitorObject result = evaluate(script, env -> env.addModule(CommonMarkModule.REGISTRATION), null);
        assertEquals("## My heading\n", result.janitorToString());
    }

    /**
     * GitHub-flavored tables are parsed into table nodes, with header flags, alignments and cell contents.
     */
    @Test
    void parseTable() throws JanitorRuntimeException, JanitorCompilerException {
        final @Language("Janitor") String script = """
            import commonmark;

            doc = commonmark.parse("| Name | Count |\\n|:-----|------:|\\n| apples | 3 |\\n| pears | *7* |\\n");
            table = doc.firstChild;
            head = table.firstChild;
            body = head.next;
            cells = [];
            for (row in [head.firstChild, body.firstChild, body.lastChild]) {
                cell = row.firstChild;
                while (cell) {
                    cells.add(cell.firstChild.toText() + "/" + cell.header + "/" + cell.alignment);
                    cell = cell.next;
                }
            }
            return [table.type, head.type, body.type, body.firstChild.type, body.firstChild.firstChild.type].join(",") + " " + cells.join(" ");
            """;
        final JanitorObject result = evaluate(script, env -> env.addModule(CommonMarkModule.REGISTRATION), null);
        assertEquals("TableBlock,TableHead,TableBody,TableRow,TableCell "
                + "Name/true/left Count/true/right apples/false/left 3/false/right pears/false/left 7/false/right",
                result.janitorToString());
    }

    /**
     * Tables render as HTML tables and survive a round trip through Markdown.
     */
    @Test
    void renderTable() throws JanitorRuntimeException, JanitorCompilerException {
        final @Language("Janitor") String script = """
            import commonmark;

            doc = commonmark.parse("| A | B |\\n|---|:-:|\\n| 1 | 2 |\\n");
            return doc.toHtml() + "---\\n" + commonmark.parse(doc.toMarkdown()).toHtml();
            """;
        final JanitorObject result = evaluate(script, env -> env.addModule(CommonMarkModule.REGISTRATION), null);
        final String html = """
            <table>
            <thead>
            <tr>
            <th>A</th>
            <th align="center">B</th>
            </tr>
            </thead>
            <tbody>
            <tr>
            <td>1</td>
            <td align="center">2</td>
            </tr>
            </tbody>
            </table>
            """;
        assertEquals(html + "---\n" + html, result.janitorToString());
    }

    /**
     * Scripts can build a table from nodes and render it.
     */
    @Test
    void buildTable() throws JanitorRuntimeException, JanitorCompilerException {
        final @Language("Janitor") String script = """
            import commonmark;

            function cell(text, header) {
                c = commonmark.TableCell();
                c.header = header;
                c.alignment = "right";
                t = commonmark.Text();
                t.literal = text;
                c.appendChild(t);
                return c;
            }
            headRow = commonmark.TableRow();
            headRow.appendChild(cell("Total", true));
            head = commonmark.TableHead();
            head.appendChild(headRow);
            bodyRow = commonmark.TableRow();
            bodyRow.appendChild(cell("42", false));
            body = commonmark.TableBody();
            body.appendChild(bodyRow);
            table = commonmark.TableBlock();
            table.appendChild(head);
            table.appendChild(body);
            doc = commonmark.Document();
            doc.appendChild(table);
            return doc.toHtml();
            """;
        final JanitorObject result = evaluate(script, env -> env.addModule(CommonMarkModule.REGISTRATION), null);
        assertEquals("""
            <table>
            <thead>
            <tr>
            <th align="right">Total</th>
            </tr>
            </thead>
            <tbody>
            <tr>
            <td align="right">42</td>
            </tr>
            </tbody>
            </table>
            """, result.janitorToString());
    }

    // TODO: in CommonMarkModule, passing initializing maps to the constructors does not work yet, e.g. commonmark.Heading({level: 2});
    // TODO: in CommonMarkModule, we need shorthands like commonmark.Text("My heading") instead of commonmark.Text({literal: "My heading"}), even which does not work yet
    // TOOD: in CMNode, appendChild(string) should automatically turn the string into a Text node
    // TODO: in CMNode, allow passing a map with additional options to toMarkdown(), toHtml(), toText() to customize the output
}
