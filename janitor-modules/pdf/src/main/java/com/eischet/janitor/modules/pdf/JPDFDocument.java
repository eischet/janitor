package com.eischet.janitor.modules.pdf;

import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.jetbrains.annotations.NotNull;
import org.openpdf.text.Document;

/**
 * Wraps org.openpdf.text.Document: the top-level container scripts build a PDF's content into.
 * <p>
 * Typical usage from a script:
 * <pre>
 *   import pdf;
 *   doc = pdf.Document();
 *   writer = pdf.Writer(doc, "output.pdf"); // or pdf.Writer(doc) to keep it in memory, see writer.bytes
 *   doc.open();
 *   doc.add(pdf.Paragraph("Hello, world!"));
 *   doc.close();
 * </pre>
 */
public class JPDFDocument extends JanitorWrapper<Document> {

    public static final WrapperDispatchTable<Document> DISPATCH_TABLE = new WrapperDispatchTable<>();

    static {
        DISPATCH_TABLE.addBuilderMethod("add", (self, process, args) -> self.janitorGetHostValue().add(PdfElements.requireElement(process, args, 0)));
        DISPATCH_TABLE.addVoidMethod("open", (self, process, args) -> self.janitorGetHostValue().open());
        DISPATCH_TABLE.addVoidMethod("close", (self, process, args) -> self.janitorGetHostValue().close());
        DISPATCH_TABLE.addBooleanProperty("isOpen", self -> self.janitorGetHostValue().isOpen());
        DISPATCH_TABLE.addMethod("newPage", (self, process, args) -> com.eischet.janitor.api.Janitor.toBool(self.janitorGetHostValue().newPage()));

        DISPATCH_TABLE.addObjectProperty("pageSize",
                self -> new JPDFRectangle(self.janitorGetHostValue().getPageSize()),
                (self, value) -> self.janitorGetHostValue().setPageSize(value.getRectangle()),
                () -> new JPDFRectangle(org.openpdf.text.PageSize.A4));
        DISPATCH_TABLE.addBuilderMethod("setMargins", (self, process, args) -> self.janitorGetHostValue().setMargins(
                PdfElements.floatArg(process, args, 0), PdfElements.floatArg(process, args, 1), PdfElements.floatArg(process, args, 2), PdfElements.floatArg(process, args, 3)));
        DISPATCH_TABLE.addDoubleProperty("marginLeft", self -> self.janitorGetHostValue().leftMargin());
        DISPATCH_TABLE.addDoubleProperty("marginRight", self -> self.janitorGetHostValue().rightMargin());
        DISPATCH_TABLE.addDoubleProperty("marginTop", self -> self.janitorGetHostValue().topMargin());
        DISPATCH_TABLE.addDoubleProperty("marginBottom", self -> self.janitorGetHostValue().bottomMargin());

        // Document has no public getHeader()/getFooter() (only set/reset), so these are exposed as methods, not a settable property.
        DISPATCH_TABLE.addBuilderMethod("setHeader", (self, process, args) -> self.janitorGetHostValue().setHeader(args.getRequired(0, JPDFHeaderFooter.class).getHeaderFooter()));
        DISPATCH_TABLE.addBuilderMethod("setFooter", (self, process, args) -> self.janitorGetHostValue().setFooter(args.getRequired(0, JPDFHeaderFooter.class).getHeaderFooter()));
        DISPATCH_TABLE.addVoidMethod("resetHeader", (self, process, args) -> self.janitorGetHostValue().resetHeader());
        DISPATCH_TABLE.addVoidMethod("resetFooter", (self, process, args) -> self.janitorGetHostValue().resetFooter());
        DISPATCH_TABLE.addIntegerProperty("pageNumber", self -> self.janitorGetHostValue().getPageNumber());

        DISPATCH_TABLE.addBuilderMethod("addTitle", (self, process, args) -> self.janitorGetHostValue().addTitle(args.getRequiredStringValue(0)));
        DISPATCH_TABLE.addBuilderMethod("addSubject", (self, process, args) -> self.janitorGetHostValue().addSubject(args.getRequiredStringValue(0)));
        DISPATCH_TABLE.addBuilderMethod("addKeywords", (self, process, args) -> self.janitorGetHostValue().addKeywords(args.getRequiredStringValue(0)));
        DISPATCH_TABLE.addBuilderMethod("addAuthor", (self, process, args) -> self.janitorGetHostValue().addAuthor(args.getRequiredStringValue(0)));
        DISPATCH_TABLE.addBuilderMethod("addCreator", (self, process, args) -> self.janitorGetHostValue().addCreator(args.getRequiredStringValue(0)));
        DISPATCH_TABLE.addBuilderMethod("addHeader", (self, process, args) -> self.janitorGetHostValue().addHeader(args.getRequiredStringValue(0), args.getRequiredStringValue(1)));
        DISPATCH_TABLE.addBuilderMethod("setMarginMirroring", (self, process, args) -> self.janitorGetHostValue().setMarginMirroring(args.getRequiredBooleanValue(0)));
        // TODO: addProducer, addCreationDate, addModificationDate -- OpenPDF fills these in automatically; only add if a script really needs to override them.
        // TODO: JavaScript onLoad/onUnload and htmlStyleClass are HTML-writer-only concerns, not relevant for the pdf.Writer path -- left out.
    }

    public JPDFDocument(@NotNull Document doc) {
        super(DISPATCH_TABLE, doc);
    }

    public JPDFDocument() {
        super(DISPATCH_TABLE, new Document());
    }

    public JPDFDocument(final @NotNull JPDFRectangle pageSize) {
        super(DISPATCH_TABLE, new Document(pageSize.getRectangle()));
    }

    public JPDFDocument(final @NotNull JPDFRectangle pageSize, final float marginLeft, final float marginRight, final float marginTop, final float marginBottom) {
        super(DISPATCH_TABLE, new Document(pageSize.getRectangle(), marginLeft, marginRight, marginTop, marginBottom));
    }

    public Document getDocument() {
        return wrapped;
    }

}
