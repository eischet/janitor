// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.pdf;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.errors.runtime.JanitorNativeException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.modules.JanitorModule;
import com.eischet.janitor.api.modules.JanitorModuleRegistration;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.PageSize;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.PdfSmartCopy;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * The "pdf" module: a factory for building PDF documents on top of LibrePDF/OpenPDF.
 * <p>
 * Typical usage:
 * <pre>
 *   import pdf;
 *   doc = pdf.Document();
 *   writer = pdf.Writer(doc, "out.pdf");
 *   doc.open();
 *   font = pdf.Font(pdf.HELVETICA, 18, pdf.BOLD);
 *   doc.add(pdf.Paragraph("Hello, world!", font));
 *   doc.close();
 * </pre>
 * <p>
 * Not every class/method of OpenPDF is mapped -- the individual JPDF* wrapper classes in this package
 * carry TODO comments (naming the OpenPDF field/method) wherever a mapping is missing or was skipped as
 * too advanced/rarely needed for scripting (e.g. PdfContentByte low-level canvas drawing, digital
 * signatures, encryption, page events). Add to those as the need arises.
 */
public class PdfModule extends JanitorComposed<PdfModule> implements JanitorModule {

    public static final DispatchTable<PdfModule> dispatcher = new DispatchTable<>(PdfModule::new, false);

    public static final JanitorModuleRegistration REGISTRATION = new JanitorModuleRegistration("pdf", PdfModule::new);

    static {
        // Document & output
        dispatcher.addMethod("Document", (self, process, args) -> switch (args.size()) {
            case 0 -> new JPDFDocument();
            case 1 -> new JPDFDocument(args.getRequired(0, JPDFRectangle.class));
            case 5 -> new JPDFDocument(args.getRequired(0, JPDFRectangle.class),
                    PdfElements.floatArg(process, args, 1), PdfElements.floatArg(process, args, 2), PdfElements.floatArg(process, args, 3), PdfElements.floatArg(process, args, 4));
            default -> throw new JanitorArgumentException(process, "Document() takes 0, 1 (pageSize) or 5 (pageSize, marginLeft, marginRight, marginTop, marginBottom) arguments");
        }).setMetaData(Janitor.MetaData.HELP, "Document(pageSize, marginLeft, marginRight, marginTop, marginBottom): Creates a new PDF document; pageSize (a Rectangle) and the margins in points are optional, but the margins only together with a page size.");
        dispatcher.addMethod("Writer", (self, process, args) -> JPDFWriter.fromArgs(process, args))
                .setMetaData(Janitor.MetaData.HELP, "Writer(document, path): Creates a writer that writes the document to the file path; without a path, the PDF is kept in memory and available as writer.bytes after closing the document.");
        dispatcher.addMethod("Reader", (self, process, args) -> JPDFReader.fromArgs(process, args))
                .setMetaData(Janitor.MetaData.HELP, "Reader(source): Reads an existing PDF from a file path or from binary data.");
        dispatcher.addVoidMethod("concat", (self, process, args) -> {
            args.require(2);
            final String targetFileName = args.getRequiredStringValue(0);
            final List<String> sourceFileNames = PdfElements.stringListArg(process, args.get(1));
            concat(process, targetFileName, sourceFileNames);
        }).setMetaData(Janitor.MetaData.HELP, "concat(targetFile, sourceFiles): Concatenates the pages of the PDF files in the list sourceFiles into the new file targetFile.");

        // Text building blocks
        dispatcher.addMethod("Chunk", (self, process, args) -> JPDFChunk.fromArgs(process, args))
                .setMetaData(Janitor.MetaData.HELP, "Chunk(text, font): Creates a Chunk, the smallest piece of text with a Font; text and font are optional.");
        dispatcher.addMethod("Phrase", (self, process, args) -> JPDFPhrase.fromArgs(process, args))
                .setMetaData(Janitor.MetaData.HELP, "Phrase(text, font): Creates a Phrase, a series of chunks; text and font are optional.");
        dispatcher.addMethod("Paragraph", (self, process, args) -> JPDFParagraph.fromArgs(process, args))
                .setMetaData(Janitor.MetaData.HELP, "Paragraph(text, font): Creates a Paragraph, a phrase with alignment, indentation and spacing; text and font are optional.");
        dispatcher.addMethod("Anchor", (self, process, args) -> JPDFAnchor.fromArgs(process, args))
                .setMetaData(Janitor.MetaData.HELP, "Anchor(text, font): Creates an Anchor, a phrase that can be a link or a named destination; text and font are optional.");
        dispatcher.addMethod("List", (self, process, args) -> JPDFList.fromArgs(process, args))
                .setMetaData(Janitor.MetaData.HELP, "List(numbered, lettered): Creates a List of ListItems; both arguments are optional booleans, without them the list is bulleted.");
        dispatcher.addMethod("ListItem", (self, process, args) -> JPDFListItem.fromArgs(process, args))
                .setMetaData(Janitor.MetaData.HELP, "ListItem(text, font): Creates a ListItem for a List; text and font are optional.");
        dispatcher.addMethod("Chapter", (self, process, args) -> JPDFChapter.fromArgs(process, args))
                .setMetaData(Janitor.MetaData.HELP, "Chapter(title, number): Creates a chapter with the chapter number; the title is optional.");
        // Section has no public factory here on purpose: OpenPDF only creates one via Chapter/Section.addSection(...).
        dispatcher.addMethod("HeaderFooter", (self, process, args) -> JPDFHeaderFooter.fromArgs(process, args))
                .setMetaData(Janitor.MetaData.HELP, "HeaderFooter(before, numberedOrAfter): Creates a page header or footer from the Phrase before and either a boolean (show the page number after it) or a Phrase shown after the page number.");
        dispatcher.addMethod("Meta", (self, process, args) -> new JPDFMeta(args.getRequiredStringValue(0), args.getRequiredStringValue(1)))
                .setMetaData(Janitor.MetaData.HELP, "Meta(tag, content): Creates a piece of document meta-information.");
        dispatcher.addMethod("Annotation", (self, process, args) -> JPDFAnnotation.fromArgs(process, args))
                .setMetaData(Janitor.MetaData.HELP, "Annotation(title, text): Creates a popup text note; Annotation(llx, lly, urx, ury, url) instead creates a link to the URL over the given area.");
        dispatcher.addMethod("Image", (self, process, args) -> JPDFImage.fromArgs(process, args))
                .setMetaData(Janitor.MetaData.HELP, "Image(source): Loads an Image from a local file path or from binary data.");

        // Layout / styling primitives
        dispatcher.addMethod("Font", (self, process, args) -> switch (args.size()) {
            case 0 -> new JPDFFont();
            case 1 -> new JPDFFont(args.getRequiredIntValue(0), Font.UNDEFINED, Font.UNDEFINED, null);
            case 2 -> new JPDFFont(args.getRequiredIntValue(0), PdfElements.floatArg(process, args, 1), Font.UNDEFINED, null);
            case 3 -> new JPDFFont(args.getRequiredIntValue(0), PdfElements.floatArg(process, args, 1), args.getRequiredIntValue(2), null);
            case 4 -> new JPDFFont(args.getRequiredIntValue(0), PdfElements.floatArg(process, args, 1), args.getRequiredIntValue(2), args.getRequired(3, JPDFColor.class).getColor());
            default -> throw new JanitorArgumentException(process, "Font() takes 0-4 arguments: ([family, [size, [style, [color]]]])");
        }).setMetaData(Janitor.MetaData.HELP, "Font(family, size, style, color): Creates a Font; all arguments are optional, e.g. pdf.Font(pdf.HELVETICA, 12, pdf.BOLD, pdf.Color(0, 0, 255)).");
        dispatcher.addMethod("BaseFont", (self, process, args) -> JPDFBaseFont.fromArgs(process, args))
                .setMetaData(Janitor.MetaData.HELP, "BaseFont(name, encoding, embedded): Creates a BaseFont from a standard font name or a TrueType/OpenType file path; encoding defaults to Cp1252 (WinAnsi), embedded to false.");
        dispatcher.addMethod("Color", (self, process, args) -> switch (args.size()) {
            case 3 -> new JPDFColor(args.getRequiredIntValue(0), args.getRequiredIntValue(1), args.getRequiredIntValue(2));
            case 4 -> new JPDFColor(args.getRequiredIntValue(0), args.getRequiredIntValue(1), args.getRequiredIntValue(2), args.getRequiredIntValue(3));
            default -> throw new JanitorArgumentException(process, "Color() takes 3 (red, green, blue) or 4 (red, green, blue, alpha) arguments");
        }).setMetaData(Janitor.MetaData.HELP, "Color(red, green, blue, alpha): Creates a Color from components 0 to 255; alpha is optional.");
        dispatcher.addMethod("Rectangle", (self, process, args) -> switch (args.size()) {
            case 2 -> new JPDFRectangle(PdfElements.floatArg(process, args, 0), PdfElements.floatArg(process, args, 1));
            case 4 -> new JPDFRectangle(PdfElements.floatArg(process, args, 0), PdfElements.floatArg(process, args, 1), PdfElements.floatArg(process, args, 2), PdfElements.floatArg(process, args, 3));
            default -> throw new JanitorArgumentException(process, "Rectangle() takes 2 (width, height) or 4 (llx, lly, urx, ury) arguments");
        }).setMetaData(Janitor.MetaData.HELP, "Rectangle(width, height): Creates a Rectangle of the size in points; Rectangle(llx, lly, urx, ury) instead takes the lower left and upper right corners.");
        dispatcher.addMethod("PageSize", (self, process, args) -> new JPDFRectangle(pageSizeByName(process, args.getRequiredStringValue(0))))
                .setMetaData(Janitor.MetaData.HELP, "PageSize(name): Returns the read-only Rectangle of a standard page size like \"A4\", \"A5\", \"LETTER\" or \"LEGAL\" (case-insensitive); unknown names are an error.");

        // Tables (the modern PdfPTable/PdfPCell API; the legacy Table/Cell/Row classes are not mapped)
        dispatcher.addMethod("PTable", (self, process, args) -> JPDFPTable.fromArgs(process, args))
                .setMetaData(Janitor.MetaData.HELP, "PTable(columns): Creates a table with the given number of columns, or with one column per entry of a list of relative column widths.");
        dispatcher.addMethod("PCell", (self, process, args) -> JPDFPCell.fromArgs(process, args))
                .setMetaData(Janitor.MetaData.HELP, "PCell(content): Creates a table cell; content is optional and can be a Phrase, an Image or a nested PTable.");

        // Font family constants, for pdf.Font(pdf.HELVETICA, ...)
        dispatcher.addIntegerProperty("COURIER", self -> Font.COURIER)
                .setMetaData(Janitor.MetaData.HELP, "COURIER: Font family constant for Courier.");
        dispatcher.addIntegerProperty("HELVETICA", self -> Font.HELVETICA)
                .setMetaData(Janitor.MetaData.HELP, "HELVETICA: Font family constant for Helvetica.");
        dispatcher.addIntegerProperty("TIMES_ROMAN", self -> Font.TIMES_ROMAN)
                .setMetaData(Janitor.MetaData.HELP, "TIMES_ROMAN: Font family constant for Times Roman.");
        dispatcher.addIntegerProperty("SYMBOL", self -> Font.SYMBOL)
                .setMetaData(Janitor.MetaData.HELP, "SYMBOL: Font family constant for the Symbol font.");
        dispatcher.addIntegerProperty("ZAPFDINGBATS", self -> Font.ZAPFDINGBATS)
                .setMetaData(Janitor.MetaData.HELP, "ZAPFDINGBATS: Font family constant for ZapfDingbats.");

        // Font style constants (bit flags), for pdf.Font(family, size, pdf.BOLD | pdf.ITALIC)
        dispatcher.addIntegerProperty("NORMAL", self -> Font.NORMAL)
                .setMetaData(Janitor.MetaData.HELP, "NORMAL: Font style constant for normal text.");
        dispatcher.addIntegerProperty("BOLD", self -> Font.BOLD)
                .setMetaData(Janitor.MetaData.HELP, "BOLD: Font style flag for bold text; combine flags with |.");
        dispatcher.addIntegerProperty("ITALIC", self -> Font.ITALIC)
                .setMetaData(Janitor.MetaData.HELP, "ITALIC: Font style flag for italic text; combine flags with |.");
        dispatcher.addIntegerProperty("UNDERLINE", self -> Font.UNDERLINE)
                .setMetaData(Janitor.MetaData.HELP, "UNDERLINE: Font style flag for underlined text; combine flags with |.");
        dispatcher.addIntegerProperty("STRIKETHRU", self -> Font.STRIKETHRU)
                .setMetaData(Janitor.MetaData.HELP, "STRIKETHRU: Font style flag for struck-through text; combine flags with |.");
        dispatcher.addIntegerProperty("BOLDITALIC", self -> Font.BOLDITALIC)
                .setMetaData(Janitor.MetaData.HELP, "BOLDITALIC: Font style constant for bold italic text, the same as BOLD | ITALIC.");

        // Alignment constants (org.openpdf.text.Element), for Paragraph.alignment, PdfPCell.horizontalAlignment, ...
        dispatcher.addIntegerProperty("ALIGN_LEFT", self -> Element.ALIGN_LEFT)
                .setMetaData(Janitor.MetaData.HELP, "ALIGN_LEFT: Horizontal alignment constant: left.");
        dispatcher.addIntegerProperty("ALIGN_CENTER", self -> Element.ALIGN_CENTER)
                .setMetaData(Janitor.MetaData.HELP, "ALIGN_CENTER: Horizontal alignment constant: centered.");
        dispatcher.addIntegerProperty("ALIGN_RIGHT", self -> Element.ALIGN_RIGHT)
                .setMetaData(Janitor.MetaData.HELP, "ALIGN_RIGHT: Horizontal alignment constant: right.");
        dispatcher.addIntegerProperty("ALIGN_JUSTIFIED", self -> Element.ALIGN_JUSTIFIED)
                .setMetaData(Janitor.MetaData.HELP, "ALIGN_JUSTIFIED: Horizontal alignment constant: justified.");
        dispatcher.addIntegerProperty("ALIGN_TOP", self -> Element.ALIGN_TOP)
                .setMetaData(Janitor.MetaData.HELP, "ALIGN_TOP: Vertical alignment constant: top.");
        dispatcher.addIntegerProperty("ALIGN_MIDDLE", self -> Element.ALIGN_MIDDLE)
                .setMetaData(Janitor.MetaData.HELP, "ALIGN_MIDDLE: Vertical alignment constant: middle.");
        dispatcher.addIntegerProperty("ALIGN_BOTTOM", self -> Element.ALIGN_BOTTOM)
                .setMetaData(Janitor.MetaData.HELP, "ALIGN_BOTTOM: Vertical alignment constant: bottom.");

        // Border constants (org.openpdf.text.Rectangle), for Rectangle.border / PdfPCell.border
        dispatcher.addIntegerProperty("BORDER_NONE", self -> Rectangle.NO_BORDER)
                .setMetaData(Janitor.MetaData.HELP, "BORDER_NONE: Border constant: no border.");
        dispatcher.addIntegerProperty("BORDER_TOP", self -> Rectangle.TOP)
                .setMetaData(Janitor.MetaData.HELP, "BORDER_TOP: Border flag for the top side; combine flags with |.");
        dispatcher.addIntegerProperty("BORDER_BOTTOM", self -> Rectangle.BOTTOM)
                .setMetaData(Janitor.MetaData.HELP, "BORDER_BOTTOM: Border flag for the bottom side; combine flags with |.");
        dispatcher.addIntegerProperty("BORDER_LEFT", self -> Rectangle.LEFT)
                .setMetaData(Janitor.MetaData.HELP, "BORDER_LEFT: Border flag for the left side; combine flags with |.");
        dispatcher.addIntegerProperty("BORDER_RIGHT", self -> Rectangle.RIGHT)
                .setMetaData(Janitor.MetaData.HELP, "BORDER_RIGHT: Border flag for the right side; combine flags with |.");
        dispatcher.addIntegerProperty("BORDER_BOX", self -> Rectangle.BOX)
                .setMetaData(Janitor.MetaData.HELP, "BORDER_BOX: Border constant for all four sides.");
    }

    private static Rectangle pageSizeByName(final JanitorScriptProcess process, final String name) throws JanitorArgumentException {
        return switch (name.toUpperCase(java.util.Locale.ROOT)) {
            case "A0" -> PageSize.A0;
            case "A1" -> PageSize.A1;
            case "A2" -> PageSize.A2;
            case "A3" -> PageSize.A3;
            case "A4" -> PageSize.A4;
            case "A5" -> PageSize.A5;
            case "A6" -> PageSize.A6;
            case "A7" -> PageSize.A7;
            case "A8" -> PageSize.A8;
            case "A9" -> PageSize.A9;
            case "A10" -> PageSize.A10;
            case "B0" -> PageSize.B0;
            case "B1" -> PageSize.B1;
            case "B2" -> PageSize.B2;
            case "B3" -> PageSize.B3;
            case "B4" -> PageSize.B4;
            case "B5" -> PageSize.B5;
            case "LETTER" -> PageSize.LETTER;
            case "LEGAL" -> PageSize.LEGAL;
            case "TABLOID" -> PageSize.TABLOID;
            case "EXECUTIVE" -> PageSize.EXECUTIVE;
            case "POSTCARD" -> PageSize.POSTCARD;
            case "HALFLETTER" -> PageSize.HALFLETTER;
            case "NOTE" -> PageSize.NOTE;
            default -> throw new JanitorArgumentException(process, "unknown page size '" + name + "'");
        };
    }

    /**
     * Concatenates a list of existing PDF files into one new file, using PdfSmartCopy (which -- unlike
     * plain PdfCopy -- also deduplicates identical objects shared across the source files, e.g. fonts
     * or images repeated in every input, keeping the merged file smaller).
     * <p>
     * Like pdf.Writer(document, path) / pdf.Reader(path), this reads and writes arbitrary filesystem
     * paths directly, with no sandboxing.
     */
    private static void concat(final JanitorScriptProcess process, final String targetFileName, final List<String> sourceFileNames) throws JanitorRuntimeException {
        final Document document = new Document();
        try (FileOutputStream out = new FileOutputStream(targetFileName)) {
            final PdfSmartCopy copy = new PdfSmartCopy(document, out);
            document.open();
            for (final String sourceFileName : sourceFileNames) {
                // Read fully into memory rather than PdfReader(filename), which memory-maps the file --
                // on Windows, closing a memory-mapped PdfReader does not necessarily release the OS-level
                // file lock right away (the mapping is only released on GC), which would leave the source
                // file locked for a while after concat() returns.
                final PdfReader reader = new PdfReader(java.nio.file.Files.readAllBytes(java.nio.file.Path.of(sourceFileName)));
                try {
                    final int pages = reader.getNumberOfPages();
                    for (int page = 1; page <= pages; page++) {
                        copy.addPage(copy.getImportedPage(reader, page));
                    }
                } finally {
                    reader.close();
                }
            }
            document.close();
        } catch (DocumentException | IOException e) {
            throw new JanitorNativeException(process, "error concatenating pdf files into '" + targetFileName + "'", e);
        }
    }

    public PdfModule() {
        super(dispatcher);
    }

}
