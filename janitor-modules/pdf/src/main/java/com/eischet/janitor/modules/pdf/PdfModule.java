package com.eischet.janitor.modules.pdf;

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
        });
        dispatcher.addMethod("Writer", (self, process, args) -> JPDFWriter.fromArgs(process, args));
        dispatcher.addMethod("Reader", (self, process, args) -> JPDFReader.fromArgs(process, args));
        dispatcher.addVoidMethod("concat", (self, process, args) -> {
            args.require(2);
            final String targetFileName = args.getRequiredStringValue(0);
            final List<String> sourceFileNames = PdfElements.stringListArg(process, args.get(1));
            concat(process, targetFileName, sourceFileNames);
        });

        // Text building blocks
        dispatcher.addMethod("Chunk", (self, process, args) -> JPDFChunk.fromArgs(process, args));
        dispatcher.addMethod("Phrase", (self, process, args) -> JPDFPhrase.fromArgs(process, args));
        dispatcher.addMethod("Paragraph", (self, process, args) -> JPDFParagraph.fromArgs(process, args));
        dispatcher.addMethod("Anchor", (self, process, args) -> JPDFAnchor.fromArgs(process, args));
        dispatcher.addMethod("List", (self, process, args) -> JPDFList.fromArgs(process, args));
        dispatcher.addMethod("ListItem", (self, process, args) -> JPDFListItem.fromArgs(process, args));
        dispatcher.addMethod("Chapter", (self, process, args) -> JPDFChapter.fromArgs(process, args));
        // Section has no public factory here on purpose: OpenPDF only creates one via Chapter/Section.addSection(...).
        dispatcher.addMethod("HeaderFooter", (self, process, args) -> JPDFHeaderFooter.fromArgs(process, args));
        dispatcher.addMethod("Meta", (self, process, args) -> new JPDFMeta(args.getRequiredStringValue(0), args.getRequiredStringValue(1)));
        dispatcher.addMethod("Annotation", (self, process, args) -> JPDFAnnotation.fromArgs(process, args));
        dispatcher.addMethod("Image", (self, process, args) -> JPDFImage.fromArgs(process, args));

        // Layout / styling primitives
        dispatcher.addMethod("Font", (self, process, args) -> switch (args.size()) {
            case 0 -> new JPDFFont();
            case 1 -> new JPDFFont(args.getRequiredIntValue(0), Font.UNDEFINED, Font.UNDEFINED, null);
            case 2 -> new JPDFFont(args.getRequiredIntValue(0), PdfElements.floatArg(process, args, 1), Font.UNDEFINED, null);
            case 3 -> new JPDFFont(args.getRequiredIntValue(0), PdfElements.floatArg(process, args, 1), args.getRequiredIntValue(2), null);
            case 4 -> new JPDFFont(args.getRequiredIntValue(0), PdfElements.floatArg(process, args, 1), args.getRequiredIntValue(2), args.getRequired(3, JPDFColor.class).getColor());
            default -> throw new JanitorArgumentException(process, "Font() takes 0-4 arguments: ([family, [size, [style, [color]]]])");
        });
        dispatcher.addMethod("BaseFont", (self, process, args) -> JPDFBaseFont.fromArgs(process, args));
        dispatcher.addMethod("Color", (self, process, args) -> switch (args.size()) {
            case 3 -> new JPDFColor(args.getRequiredIntValue(0), args.getRequiredIntValue(1), args.getRequiredIntValue(2));
            case 4 -> new JPDFColor(args.getRequiredIntValue(0), args.getRequiredIntValue(1), args.getRequiredIntValue(2), args.getRequiredIntValue(3));
            default -> throw new JanitorArgumentException(process, "Color() takes 3 (red, green, blue) or 4 (red, green, blue, alpha) arguments");
        });
        dispatcher.addMethod("Rectangle", (self, process, args) -> switch (args.size()) {
            case 2 -> new JPDFRectangle(PdfElements.floatArg(process, args, 0), PdfElements.floatArg(process, args, 1));
            case 4 -> new JPDFRectangle(PdfElements.floatArg(process, args, 0), PdfElements.floatArg(process, args, 1), PdfElements.floatArg(process, args, 2), PdfElements.floatArg(process, args, 3));
            default -> throw new JanitorArgumentException(process, "Rectangle() takes 2 (width, height) or 4 (llx, lly, urx, ury) arguments");
        });
        dispatcher.addMethod("PageSize", (self, process, args) -> new JPDFRectangle(pageSizeByName(process, args.getRequiredStringValue(0))));

        // Tables (the modern PdfPTable/PdfPCell API; the legacy Table/Cell/Row classes are not mapped)
        dispatcher.addMethod("PTable", (self, process, args) -> JPDFPTable.fromArgs(process, args));
        dispatcher.addMethod("PCell", (self, process, args) -> JPDFPCell.fromArgs(process, args));

        // Font family constants, for pdf.Font(pdf.HELVETICA, ...)
        dispatcher.addIntegerProperty("COURIER", self -> Font.COURIER);
        dispatcher.addIntegerProperty("HELVETICA", self -> Font.HELVETICA);
        dispatcher.addIntegerProperty("TIMES_ROMAN", self -> Font.TIMES_ROMAN);
        dispatcher.addIntegerProperty("SYMBOL", self -> Font.SYMBOL);
        dispatcher.addIntegerProperty("ZAPFDINGBATS", self -> Font.ZAPFDINGBATS);

        // Font style constants (bit flags), for pdf.Font(family, size, pdf.BOLD | pdf.ITALIC)
        dispatcher.addIntegerProperty("NORMAL", self -> Font.NORMAL);
        dispatcher.addIntegerProperty("BOLD", self -> Font.BOLD);
        dispatcher.addIntegerProperty("ITALIC", self -> Font.ITALIC);
        dispatcher.addIntegerProperty("UNDERLINE", self -> Font.UNDERLINE);
        dispatcher.addIntegerProperty("STRIKETHRU", self -> Font.STRIKETHRU);
        dispatcher.addIntegerProperty("BOLDITALIC", self -> Font.BOLDITALIC);

        // Alignment constants (org.openpdf.text.Element), for Paragraph.alignment, PdfPCell.horizontalAlignment, ...
        dispatcher.addIntegerProperty("ALIGN_LEFT", self -> Element.ALIGN_LEFT);
        dispatcher.addIntegerProperty("ALIGN_CENTER", self -> Element.ALIGN_CENTER);
        dispatcher.addIntegerProperty("ALIGN_RIGHT", self -> Element.ALIGN_RIGHT);
        dispatcher.addIntegerProperty("ALIGN_JUSTIFIED", self -> Element.ALIGN_JUSTIFIED);
        dispatcher.addIntegerProperty("ALIGN_TOP", self -> Element.ALIGN_TOP);
        dispatcher.addIntegerProperty("ALIGN_MIDDLE", self -> Element.ALIGN_MIDDLE);
        dispatcher.addIntegerProperty("ALIGN_BOTTOM", self -> Element.ALIGN_BOTTOM);

        // Border constants (org.openpdf.text.Rectangle), for Rectangle.border / PdfPCell.border
        dispatcher.addIntegerProperty("BORDER_NONE", self -> Rectangle.NO_BORDER);
        dispatcher.addIntegerProperty("BORDER_TOP", self -> Rectangle.TOP);
        dispatcher.addIntegerProperty("BORDER_BOTTOM", self -> Rectangle.BOTTOM);
        dispatcher.addIntegerProperty("BORDER_LEFT", self -> Rectangle.LEFT);
        dispatcher.addIntegerProperty("BORDER_RIGHT", self -> Rectangle.RIGHT);
        dispatcher.addIntegerProperty("BORDER_BOX", self -> Rectangle.BOX);
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
