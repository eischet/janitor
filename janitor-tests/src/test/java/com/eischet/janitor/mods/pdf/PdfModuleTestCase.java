package com.eischet.janitor.mods.pdf;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JBinary;
import com.eischet.janitor.modules.pdf.PdfModule;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openpdf.text.pdf.PdfReader;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * End-to-end smoke test for the "pdf" module: build a small document from a script, entirely in memory,
 * and check that real PDF bytes come out the other end.
 */
public class PdfModuleTestCase extends JanitorTest {

    @Test
    void buildASimpleDocumentInMemory() throws JanitorRuntimeException, JanitorCompilerException {
        final @Language("Janitor") String script = """
            import pdf;

            doc = pdf.Document();
            writer = pdf.Writer(doc);
            doc.open();
            doc.addTitle("Test Document");

            font = pdf.Font(pdf.HELVETICA, 18, pdf.BOLD);
            doc.add(pdf.Paragraph("Hello, world!", font));

            list = pdf.List(true);
            list.add(pdf.ListItem("first"));
            list.add(pdf.ListItem("second"));
            doc.add(list);

            table = pdf.PTable(2);
            table.addCell("a");
            table.addCell("b");
            doc.add(table);

            doc.close();
            return writer.bytes;
            """;

        final JanitorObject result = evaluate(script, env -> env.addModule(PdfModule.REGISTRATION), null);
        assertTrue(result instanceof JBinary, "expected the writer's bytes property to be Binary, got " + result.janitorClassName());
        final byte[] pdfBytes = ((JBinary) result).janitorGetHostValue();
        assertTrue(pdfBytes.length > 0, "the produced PDF must not be empty");
        assertEquals("%PDF-", new String(pdfBytes, 0, 5, java.nio.charset.StandardCharsets.US_ASCII), "output must start with the PDF magic header");
    }

    @Test
    void concatMergesSeveralFilesIntoOne(@TempDir Path tempDir) throws JanitorRuntimeException, JanitorCompilerException, IOException {
        final Path fileA = tempDir.resolve("a.pdf");
        final Path fileB = tempDir.resolve("b.pdf");
        final Path merged = tempDir.resolve("merged.pdf");

        writeSinglePagePdf(fileA, "Page from A", 2);
        writeSinglePagePdf(fileB, "Page from B", 3);

        final @Language("Janitor") String script = """
            import pdf;
            pdf.concat(target, [fileA, fileB]);
            """;
        evaluate(script, env -> env.addModule(PdfModule.REGISTRATION), globals -> {
            globals.bind("target", merged.toString());
            globals.bind("fileA", fileA.toString());
            globals.bind("fileB", fileB.toString());
        });

        assertTrue(java.nio.file.Files.exists(merged), "concat() must create the target file");
        // Read via byte[] rather than PdfReader(path): on Windows, PdfReader(path) memory-maps the file,
        // and @TempDir's cleanup can then fail because the mapping isn't released until GC runs.
        try (PdfReader reader = new PdfReader(java.nio.file.Files.readAllBytes(merged))) {
            assertEquals(5, reader.getNumberOfPages(), "merged file must contain the pages of both inputs (2 + 3)");
        }
    }

    /**
     * Builds a tiny multi-page PDF at the given path, directly through the pdf module, for use as
     * concat() input.
     */
    private void writeSinglePagePdf(final Path path, final String text, final int pages) throws JanitorRuntimeException, JanitorCompilerException {
        final @Language("Janitor") String script = """
            import pdf;
            doc = pdf.Document();
            writer = pdf.Writer(doc, path);
            doc.open();
            for (i from 1 to pages) {
                if (i > 1) {
                    doc.newPage();
                }
                doc.add(pdf.Paragraph(text + " " + i));
            }
            doc.close();
            """;
        evaluate(script, env -> env.addModule(PdfModule.REGISTRATION), globals -> {
            globals.bind("path", path.toString());
            globals.bind("text", text);
            globals.bind("pages", pages);
        });
    }

    @Test
    void fontFamilyAndStyleConstantsAreExposed() throws JanitorRuntimeException, JanitorCompilerException {
        final @Language("Janitor") String script = """
            import pdf;
            return [pdf.HELVETICA, pdf.BOLD, pdf.ALIGN_CENTER, pdf.BORDER_BOX];
            """;
        final JanitorObject result = evaluate(script, env -> env.addModule(PdfModule.REGISTRATION), null);
        assertEquals("[1, 1, 1, 15]", result.janitorToString());
    }

}
