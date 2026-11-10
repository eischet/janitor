// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

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

    /**
     * A PDF/A-1b document from a script must pass the PDF/A-1 check that Mustang runs before it turns a PDF into a
     * ZUGFeRD invoice (PDFBox Preflight), which is the main reason scripts need PDF/A at all.
     */
    @Test
    void setPdfAProducesValidPdfA1b() throws JanitorRuntimeException, JanitorCompilerException, IOException {
        // a font that OpenPDF bundles; BaseFont finds it as a class path resource, a temp file would stay locked on Windows
        final String font = "font-fallback/LiberationSans-Regular.ttf";
        final byte[] pdfA = buildDocument(font, true);
        assertEquals("%PDF-1.4", new String(pdfA, 0, 8, java.nio.charset.StandardCharsets.US_ASCII));
        // throws "File is not a valid PDF/A-1 input file" if Preflight finds any error
        new org.mustangproject.ZUGFeRD.ZUGFeRDExporterFromA1().load(pdfA);

        // the counter-check: without setPdfA, the same document is rejected, so the check above really checks something
        final byte[] plain = buildDocument(font, false);
        org.junit.jupiter.api.Assertions.assertThrows(IOException.class, () -> new org.mustangproject.ZUGFeRD.ZUGFeRDExporterFromA1().load(plain));
    }

    private byte[] buildDocument(final String font, final boolean pdfA) throws JanitorRuntimeException, JanitorCompilerException {
        final @Language("Janitor") String script = """
            import pdf;
            doc = pdf.Document(pdf.PageSize("A4"));
            writer = pdf.Writer(doc);
            if (pdfA) {
                writer.setPdfA("1B");
            }
            doc.addTitle("PDF/A test");
            doc.open();
            doc.add(pdf.Paragraph("Hello, PDF/A! Grüße, 12,50 €", pdf.BaseFont(fontPath, "Cp1252", true).getFont(12.0)));
            doc.close();
            return writer.bytes;
            """;
        final JanitorObject result = evaluate(script, env -> env.addModule(PdfModule.REGISTRATION), globals -> {
            globals.bind("fontPath", font);
            globals.bind("pdfA", pdfA);
        });
        return ((JBinary) result).janitorGetHostValue();
    }

    /**
     * concat with a PDF/A level must give a file that Mustang's PDF/A-1 preflight accepts, even when a source has an
     * image with the Interpolate flag (as PDFs from Word on macOS do); without the level, the same sources are rejected.
     */
    @Test
    void concatWithPdfALevelProducesValidPdfA1b(@TempDir Path tempDir) throws Exception {
        final Path source = tempDir.resolve("interpolated.pdf");
        final java.awt.image.BufferedImage picture = new java.awt.image.BufferedImage(40, 20, java.awt.image.BufferedImage.TYPE_INT_RGB);
        picture.getGraphics().fillRect(5, 5, 30, 10);
        final org.openpdf.text.Document document = new org.openpdf.text.Document();
        try (var out = java.nio.file.Files.newOutputStream(source)) {
            org.openpdf.text.pdf.PdfWriter.getInstance(document, out);
            document.open();
            final org.openpdf.text.Image image = org.openpdf.text.Image.getInstance(picture, null);
            image.setInterpolation(true);
            document.add(image);
            document.close();
        }

        final Path pdfA = tempDir.resolve("pdfa.pdf");
        final Path plain = tempDir.resolve("plain.pdf");
        final @Language("Janitor") String script = """
            import pdf;
            pdf.concat(pdfA, [source], "1B");
            pdf.concat(plain, [source]);
            """;
        evaluate(script, env -> env.addModule(PdfModule.REGISTRATION), globals -> {
            globals.bind("source", source.toString());
            globals.bind("pdfA", pdfA.toString());
            globals.bind("plain", plain.toString());
        });

        new org.mustangproject.ZUGFeRD.ZUGFeRDExporterFromA1().load(java.nio.file.Files.readAllBytes(pdfA));
        final byte[] plainBytes = java.nio.file.Files.readAllBytes(plain);
        org.junit.jupiter.api.Assertions.assertThrows(IOException.class, () -> new org.mustangproject.ZUGFeRD.ZUGFeRDExporterFromA1().load(plainBytes));
        assertEquals(1, interpolatedImages(plain), "the plain copy keeps the flag of the source");
        assertEquals(0, interpolatedImages(pdfA), "the PDF/A copy must not have it");
    }

    private static int interpolatedImages(final Path file) throws IOException {
        final PdfReader reader = new PdfReader(java.nio.file.Files.readAllBytes(file));
        try {
            int count = 0;
            for (int i = 1; i < reader.getXrefSize(); i++) {
                if (reader.getPdfObject(i) instanceof org.openpdf.text.pdf.PRStream stream
                        && org.openpdf.text.pdf.PdfName.IMAGE.equals(stream.get(org.openpdf.text.pdf.PdfName.SUBTYPE))
                        && stream.contains(org.openpdf.text.pdf.PdfName.INTERPOLATE)) {
                    count++;
                }
            }
            return count;
        } finally {
            reader.close();
        }
    }

    @Test
    void setPdfARejectsUnknownLevels() throws JanitorCompilerException {
        final @Language("Janitor") String script = """
            import pdf;
            doc = pdf.Document();
            pdf.Writer(doc).setPdfA("1A");
            """;
        final JanitorRuntimeException e = org.junit.jupiter.api.Assertions.assertThrows(JanitorRuntimeException.class,
                () -> evaluate(script, env -> env.addModule(PdfModule.REGISTRATION), null));
        assertTrue(e.getMessage().contains("unsupported level"), e.getMessage());
    }

}
