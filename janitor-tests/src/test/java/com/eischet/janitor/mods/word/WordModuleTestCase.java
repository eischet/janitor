// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.mods.word;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JBinary;
import com.eischet.janitor.modules.word.WordModule;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.apache.poi.wp.usermodel.HeaderFooterType;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The "word" module: a template made in Word is filled in completely, and what a script builds arrives in the document.
 */
public class WordModuleTestCase extends JanitorTest {

    /**
     * A template as Word saves it: placeholders split into several runs, in the text, in a table, in the header and footer.
     */
    private static byte[] template() throws IOException {
        try (XWPFDocument document = new XWPFDocument()) {
            final XWPFParagraph greeting = document.createParagraph();
            greeting.createRun().setText("Dear {{cus");
            final XWPFRun bold = greeting.createRun();
            bold.setBold(true);
            bold.setText("tom");
            greeting.createRun().setText("er}}, your order {{number}} of {{customer}}:");
            document.createParagraph().createRun().setText("Delivery to: {{address}}");

            final XWPFTable table = document.createTable(3, 2);
            table.getRow(0).getCell(0).setText("Item");
            table.getRow(0).getCell(1).setText("Price");
            final XWPFParagraph item = table.getRow(1).getCell(0).getParagraphs().get(0);
            item.createRun().setText("{{it");
            item.createRun().setText("em}}");
            table.getRow(1).getCell(1).setText("{{price}} EUR");
            table.getRow(2).getCell(0).setText("Sum");
            table.getRow(2).getCell(1).setText("{{sum}} EUR");

            document.createHeader(HeaderFooterType.DEFAULT).createParagraph().createRun().setText("Order {{number}}");
            document.createFooter(HeaderFooterType.DEFAULT).createParagraph().createRun().setText("{{customer}}, page 1");

            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.write(out);
            return out.toByteArray();
        }
    }

    private JanitorObject run(final @Language("Janitor") String script, final byte[] template) throws Exception {
        return evaluate(script, env -> env.addModule(WordModule.REGISTRATION), globals -> {
            if (template != null) {
                globals.bind("template", Janitor.binary(template));
            }
        });
    }

    private XWPFDocument document(final JanitorObject result) throws IOException {
        return new XWPFDocument(new ByteArrayInputStream(assertInstanceOf(JBinary.class, result).janitorGetHostValue()));
    }

    @Test
    void fillsATemplateEverywhere() throws Exception {
        final byte[] template = template();
        try (XWPFDocument filled = document(run("""
                import word;
                doc = word.open(template);
                n = doc.replace("{{customer}}", "ACME Corp.");
                if (n != 3) { throw "customer was replaced " + n + " times"; }
                n = doc.replaceAll({"{{number}}": 4711, "{{address}}": "Main Street 1\\nSpringfield", "{{sum}}": "89.80"});
                if (n != 4) { throw "replaceAll replaced " + n; }
                if (doc.replace("{{missing}}", "x") != 0) { throw "found a placeholder that is not there"; }
                rows = doc.repeatRow("{{item}}", [
                    {"{{item}}": "Toner", "{{price}}": "59.90"},
                    {"{{item}}": "Paper", "{{price}}": "29.90"}
                ]);
                if (rows != 2) { throw "repeatRow wrote " + rows; }
                return doc.toBinary();
                """, template))) {
            assertEquals("Dear ACME Corp., your order 4711 of ACME Corp.:", filled.getParagraphs().get(0).getText());
            assertFalse(filled.getParagraphs().get(0).getRuns().get(0).isBold(), "the value takes the formatting of the placeholder's start");
            assertEquals("Delivery to: Main Street 1\nSpringfield", filled.getParagraphs().get(1).getText());

            final XWPFTable table = filled.getTables().get(0);
            assertEquals(4, table.getRows().size(), "header, two items, sum");
            assertEquals("Toner|59.90 EUR", rowText(table.getRow(1)));
            assertEquals("Paper|29.90 EUR", rowText(table.getRow(2)));
            assertEquals("Sum|89.80 EUR", rowText(table.getRow(3)));

            assertEquals("Order 4711", filled.getHeaderList().get(0).getText().trim());
            assertEquals("ACME Corp., page 1", filled.getFooterList().get(0).getText().trim());
        }
        try (XWPFDocument untouched = new XWPFDocument(new ByteArrayInputStream(template))) {
            assertTrue(untouched.getParagraphs().get(0).getText().contains("{{customer}}"), "the template itself is not changed");
        }
    }

    @Test
    void manyRowsAndAValueThatContainsItsPlaceholder() throws Exception {
        try (XWPFDocument filled = document(run("""
                import word;
                doc = word.open(template);
                items = [];
                for (i from 1 to 500) {
                    items.add({"{{item}}": "Item " + i, "{{price}}": i});
                }
                doc.repeatRow("{{price}}", items);
                doc.replace("{{customer}}", "{{customer}} & Sons");
                return doc.toBinary();
                """, template()))) {
            final XWPFTable table = filled.getTables().get(0);
            assertEquals(502, table.getRows().size());
            assertEquals("Item 1|1 EUR", rowText(table.getRow(1)));
            assertEquals("Item 500|500 EUR", rowText(table.getRow(500)));
            assertEquals("Sum|{{sum}} EUR", rowText(table.getRow(501)));
            assertEquals("Dear {{customer}} & Sons, your order {{number}} of {{customer}} & Sons:", filled.getParagraphs().get(0).getText());
        }
        try (XWPFDocument emptied = document(run("""
                import word;
                doc = word.open(template);
                doc.repeatRow("{{item}}", []);
                return doc.toBinary();
                """, template()))) {
            assertEquals(2, emptied.getTables().get(0).getRows().size(), "without items, the row is removed");
        }
    }

    @Test
    void buildsADocumentAndReadsIt(@TempDir Path tempDir) throws Exception {
        final BufferedImage picture = new BufferedImage(200, 100, BufferedImage.TYPE_INT_RGB);
        final ByteArrayOutputStream png = new ByteArrayOutputStream();
        ImageIO.write(picture, "png", png);
        final Path file = tempDir.resolve("report.docx");
        final JanitorObject result = evaluate("""
                import word;
                doc = word.create();
                doc.heading("Report").heading("Details", level = 2);
                doc.paragraph("first line\\nsecond line", bold = true, size = 14, align = "center");
                doc.table([["Item", "Price"], ["Toner", 59.9]]);
                doc.image(png, width = 5.08);
                doc.pageBreak().paragraph("last page");
                doc.save(path);

                again = word.open(path);
                return [again.paragraphs[0], again.paragraphs[2], again.tables, again.text.contains("Toner")];
                """, env -> env.addModule(WordModule.REGISTRATION), globals -> {
            globals.bind("png", Janitor.binary(png.toByteArray()));
            globals.bind("path", file.toString());
        });
        assertEquals("[Report, first line\nsecond line, [[[Item, Price], [Toner, 59.9]]], true]", result.janitorToString());

        try (XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(Files.readAllBytes(file)))) {
            final XWPFParagraph heading = document.getParagraphs().get(0);
            assertEquals("heading 1", document.getStyles().getStyle(heading.getStyle()).getName(), "a real heading, not just bold text");
            assertEquals("heading 2", document.getStyles().getStyle(document.getParagraphs().get(1).getStyle()).getName());
            final XWPFRun run = document.getParagraphs().get(2).getRuns().get(0);
            assertTrue(run.isBold());
            assertEquals(14.0, run.getFontSizeAsDouble());
            assertTrue(document.getTables().get(0).getRow(0).getCell(0).getParagraphs().get(0).getRuns().get(0).isBold(), "the header row is bold");
            assertEquals(1, document.getAllPictures().size());
            final var shown = document.getParagraphs().stream().flatMap(p -> p.getRuns().stream()).flatMap(r -> r.getEmbeddedPictures().stream()).findFirst().orElseThrow();
            assertEquals(144.0, shown.getWidth(), 0.01, "5.08 cm are 2 inches, 144 points");
            assertEquals(72.0, shown.getDepth(), 0.01, "the height follows the proportions");
        }
    }

    @Test
    void mistakesAreReported() throws Exception {
        final byte[] template = template();
        final JanitorRuntimeException noRow = assertThrows(JanitorRuntimeException.class, () -> run("""
                import word;
                return word.open(template).repeatRow("{{nowhere}}", []);
                """, template));
        assertTrue(noRow.getMessage().contains("no table row contains '{{nowhere}}'"), noRow.getMessage());
        final JanitorRuntimeException closed = assertThrows(JanitorRuntimeException.class, () -> run("""
                import word;
                doc = word.create();
                doc.close();
                doc.paragraph("x");
                """, null));
        assertTrue(closed.getMessage().contains("the document is closed"), closed.getMessage());
        final JanitorRuntimeException notWord = assertThrows(JanitorRuntimeException.class, () -> run("""
                import word;
                return word.open("not a document".toBinaryUtf8());
                """, null));
        assertTrue(notWord.getMessage().contains("not a Word document"), notWord.getMessage());
        final JanitorRuntimeException level = assertThrows(JanitorRuntimeException.class, () -> run("""
                import word;
                return word.create().heading("x", level = 12);
                """, null));
        assertTrue(level.getMessage().contains("level must be between 1 and 9"), level.getMessage());
    }

    private static String rowText(final XWPFTableRow row) {
        return row.getCell(0).getText() + "|" + row.getCell(1).getText();
    }

}
