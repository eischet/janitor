// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.pdf;

import com.eischet.janitor.api.Janitor;
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
        DISPATCH_TABLE.addBuilderMethod("add", (self, process, args) -> self.janitorGetHostValue().add(PdfElements.requireElement(process, args, 0)))
                .setMetaData(Janitor.MetaData.HELP, "add(element): Adds a pdf element (Paragraph, PTable, Image, ...) to the document, which must be open; returns the document.");
        DISPATCH_TABLE.addVoidMethod("open", (self, process, args) -> self.janitorGetHostValue().open())
                .setMetaData(Janitor.MetaData.HELP, "open(): Opens the document for adding content; create the Writer before.");
        DISPATCH_TABLE.addVoidMethod("close", (self, process, args) -> self.janitorGetHostValue().close())
                .setMetaData(Janitor.MetaData.HELP, "close(): Closes the document, which finishes writing the PDF.");
        DISPATCH_TABLE.addBooleanProperty("isOpen", self -> self.janitorGetHostValue().isOpen())
                .setMetaData(Janitor.MetaData.HELP, "isOpen: True if the document is open; read-only.");
        DISPATCH_TABLE.addMethod("newPage", (self, process, args) -> com.eischet.janitor.api.Janitor.toBool(self.janitorGetHostValue().newPage()))
                .setMetaData(Janitor.MetaData.HELP, "newPage(): Starts a new page; returns true if a new page was started.");

        DISPATCH_TABLE.addObjectProperty("pageSize",
                self -> new JPDFRectangle(self.janitorGetHostValue().getPageSize()),
                (self, value) -> self.janitorGetHostValue().setPageSize(value.getRectangle()),
                () -> new JPDFRectangle(org.openpdf.text.PageSize.A4))
                .setMetaData(Janitor.MetaData.HELP, "pageSize: The page size as a Rectangle; writable, e.g. doc.pageSize = pdf.PageSize(\"A4\").");
        DISPATCH_TABLE.addBuilderMethod("setMargins", (self, process, args) -> self.janitorGetHostValue().setMargins(
                PdfElements.floatArg(process, args, 0), PdfElements.floatArg(process, args, 1), PdfElements.floatArg(process, args, 2), PdfElements.floatArg(process, args, 3)))
                .setMetaData(Janitor.MetaData.HELP, "setMargins(left, right, top, bottom): Sets the page margins in points; returns the document.");
        DISPATCH_TABLE.addDoubleProperty("marginLeft", self -> self.janitorGetHostValue().leftMargin())
                .setMetaData(Janitor.MetaData.HELP, "marginLeft: The left page margin in points; read-only, see setMargins().");
        DISPATCH_TABLE.addDoubleProperty("marginRight", self -> self.janitorGetHostValue().rightMargin())
                .setMetaData(Janitor.MetaData.HELP, "marginRight: The right page margin in points; read-only, see setMargins().");
        DISPATCH_TABLE.addDoubleProperty("marginTop", self -> self.janitorGetHostValue().topMargin())
                .setMetaData(Janitor.MetaData.HELP, "marginTop: The top page margin in points; read-only, see setMargins().");
        DISPATCH_TABLE.addDoubleProperty("marginBottom", self -> self.janitorGetHostValue().bottomMargin())
                .setMetaData(Janitor.MetaData.HELP, "marginBottom: The bottom page margin in points; read-only, see setMargins().");

        // Document has no public getHeader()/getFooter() (only set/reset), so these are exposed as methods, not a settable property.
        DISPATCH_TABLE.addBuilderMethod("setHeader", (self, process, args) -> self.janitorGetHostValue().setHeader(args.getRequired(0, JPDFHeaderFooter.class).getHeaderFooter()))
                .setMetaData(Janitor.MetaData.HELP, "setHeader(headerFooter): Sets the HeaderFooter shown at the top of each page; returns the document.");
        DISPATCH_TABLE.addBuilderMethod("setFooter", (self, process, args) -> self.janitorGetHostValue().setFooter(args.getRequired(0, JPDFHeaderFooter.class).getHeaderFooter()))
                .setMetaData(Janitor.MetaData.HELP, "setFooter(headerFooter): Sets the HeaderFooter shown at the bottom of each page; returns the document.");
        DISPATCH_TABLE.addVoidMethod("resetHeader", (self, process, args) -> self.janitorGetHostValue().resetHeader())
                .setMetaData(Janitor.MetaData.HELP, "resetHeader(): Removes the page header.");
        DISPATCH_TABLE.addVoidMethod("resetFooter", (self, process, args) -> self.janitorGetHostValue().resetFooter())
                .setMetaData(Janitor.MetaData.HELP, "resetFooter(): Removes the page footer.");
        DISPATCH_TABLE.addIntegerProperty("pageNumber", self -> self.janitorGetHostValue().getPageNumber())
                .setMetaData(Janitor.MetaData.HELP, "pageNumber: The number of the current page; read-only.");

        DISPATCH_TABLE.addBuilderMethod("addTitle", (self, process, args) -> self.janitorGetHostValue().addTitle(args.getRequiredStringValue(0)))
                .setMetaData(Janitor.MetaData.HELP, "addTitle(title): Sets the title in the document's meta-information; returns the document.");
        DISPATCH_TABLE.addBuilderMethod("addSubject", (self, process, args) -> self.janitorGetHostValue().addSubject(args.getRequiredStringValue(0)))
                .setMetaData(Janitor.MetaData.HELP, "addSubject(subject): Sets the subject in the document's meta-information; returns the document.");
        DISPATCH_TABLE.addBuilderMethod("addKeywords", (self, process, args) -> self.janitorGetHostValue().addKeywords(args.getRequiredStringValue(0)))
                .setMetaData(Janitor.MetaData.HELP, "addKeywords(keywords): Sets the keywords in the document's meta-information; returns the document.");
        DISPATCH_TABLE.addBuilderMethod("addAuthor", (self, process, args) -> self.janitorGetHostValue().addAuthor(args.getRequiredStringValue(0)))
                .setMetaData(Janitor.MetaData.HELP, "addAuthor(author): Sets the author in the document's meta-information; returns the document.");
        DISPATCH_TABLE.addBuilderMethod("addCreator", (self, process, args) -> self.janitorGetHostValue().addCreator(args.getRequiredStringValue(0)))
                .setMetaData(Janitor.MetaData.HELP, "addCreator(creator): Sets the creator (the creating application) in the document's meta-information; returns the document.");
        DISPATCH_TABLE.addBuilderMethod("addHeader", (self, process, args) -> self.janitorGetHostValue().addHeader(args.getRequiredStringValue(0), args.getRequiredStringValue(1)))
                .setMetaData(Janitor.MetaData.HELP, "addHeader(name, content): Adds a custom entry to the document's meta-information (not a page header, see setHeader()); returns the document.");
        DISPATCH_TABLE.addBuilderMethod("setMarginMirroring", (self, process, args) -> self.janitorGetHostValue().setMarginMirroring(args.getRequiredBooleanValue(0)))
                .setMetaData(Janitor.MetaData.HELP, "setMarginMirroring(mirror): If true, the left and right margins are swapped on even pages, e.g. for double-sided printing; returns the document.");
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

    /**
     * @return the wrapped Document object
     */
    public Document getDocument() {
        return wrapped;
    }

}
