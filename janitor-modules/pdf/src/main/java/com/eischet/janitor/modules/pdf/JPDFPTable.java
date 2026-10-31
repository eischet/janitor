// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.pdf;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.errors.runtime.JanitorNativeException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.functions.JCallArgs;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.jetbrains.annotations.NotNull;
import org.openpdf.text.DocumentException;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;

/**
 * Wraps org.openpdf.text.pdf.PdfPTable: the modern, recommended way of adding tables to a Document
 * (the legacy Table/Cell/Row classes from org.openpdf.text are not mapped by this module).
 */
public class JPDFPTable extends JanitorWrapper<PdfPTable> {

    public static final WrapperDispatchTable<PdfPTable> DISPATCH_TABLE = new WrapperDispatchTable<>();

    static {
        DISPATCH_TABLE.addMethod("addCell", (self, process, args) -> {
            final JanitorObjectOrCell value = JanitorObjectOrCell.of(process, args, 0);
            self.janitorGetHostValue().addCell(value.cell);
            return self;
        }).setMetaData(Janitor.MetaData.HELP, "addCell(cell): Adds the next cell, given as a PCell, Phrase or string; returns the table.");
        DISPATCH_TABLE.addIntegerProperty("numberOfColumns", self -> self.janitorGetHostValue().getNumberOfColumns())
                .setMetaData(Janitor.MetaData.HELP, "numberOfColumns: The number of columns of the table; read-only.");
        DISPATCH_TABLE.addIntegerProperty("size", self -> self.janitorGetHostValue().size())
                .setMetaData(Janitor.MetaData.HELP, "size: The number of complete rows in the table; read-only.");
        DISPATCH_TABLE.addIntegerProperty("headerRows", self -> self.janitorGetHostValue().getHeaderRows(), (self, value) -> self.janitorGetHostValue().setHeaderRows(value))
                .setMetaData(Janitor.MetaData.HELP, "headerRows: The number of leading rows that are repeated as a header on every page; writable.");
        DISPATCH_TABLE.addDoubleProperty("widthPercentage", self -> self.janitorGetHostValue().getWidthPercentage(), (self, value) -> self.janitorGetHostValue().setWidthPercentage((float) value))
                .setMetaData(Janitor.MetaData.HELP, "widthPercentage: The width of the table as a percentage of the available width; writable.");
        DISPATCH_TABLE.addDoubleProperty("totalWidth", self -> self.janitorGetHostValue().getTotalWidth())
                .setMetaData(Janitor.MetaData.HELP, "totalWidth: The absolute width of the table in points; read-only, see setTotalWidth().");
        DISPATCH_TABLE.addBuilderMethod("setTotalWidth", (self, process, args) -> self.janitorGetHostValue().setTotalWidth(PdfElements.floatArg(process, args, 0)))
                .setMetaData(Janitor.MetaData.HELP, "setTotalWidth(width): Sets the absolute width of the table in points, used when lockedWidth is true; returns the table.");
        DISPATCH_TABLE.addIntegerProperty("horizontalAlignment", self -> self.janitorGetHostValue().getHorizontalAlignment(), (self, value) -> self.janitorGetHostValue().setHorizontalAlignment(value))
                .setMetaData(Janitor.MetaData.HELP, "horizontalAlignment: The horizontal alignment of the table on the page, e.g. pdf.ALIGN_LEFT, ALIGN_CENTER or ALIGN_RIGHT; writable.");
        DISPATCH_TABLE.addBooleanProperty("lockedWidth", self -> self.janitorGetHostValue().isLockedWidth(), (self, value) -> self.janitorGetHostValue().setLockedWidth(value))
                .setMetaData(Janitor.MetaData.HELP, "lockedWidth: True if the table uses its totalWidth instead of widthPercentage; writable.");
        DISPATCH_TABLE.addBooleanProperty("splitRows", self -> self.janitorGetHostValue().isSplitRows(), (self, value) -> self.janitorGetHostValue().setSplitRows(value))
                .setMetaData(Janitor.MetaData.HELP, "splitRows: True if a row may be split across pages; writable.");
        DISPATCH_TABLE.addBooleanProperty("skipFirstHeader", self -> self.janitorGetHostValue().isSkipFirstHeader(), (self, value) -> self.janitorGetHostValue().setSkipFirstHeader(value))
                .setMetaData(Janitor.MetaData.HELP, "skipFirstHeader: True if the header rows are not shown on the first page of the table; writable.");
        DISPATCH_TABLE.addBooleanProperty("skipLastFooter", self -> self.janitorGetHostValue().isSkipLastFooter(), (self, value) -> self.janitorGetHostValue().setSkipLastFooter(value))
                .setMetaData(Janitor.MetaData.HELP, "skipLastFooter: True if the footer rows are not shown on the last page of the table; writable.");
        DISPATCH_TABLE.addBuilderMethod("setSpacingBefore", (self, process, args) -> self.janitorGetHostValue().setSpacingBefore(PdfElements.floatArg(process, args, 0)))
                .setMetaData(Janitor.MetaData.HELP, "setSpacingBefore(spacing): Sets the space before the table in points; returns the table.");
        DISPATCH_TABLE.addBuilderMethod("setSpacingAfter", (self, process, args) -> self.janitorGetHostValue().setSpacingAfter(PdfElements.floatArg(process, args, 0)))
                .setMetaData(Janitor.MetaData.HELP, "setSpacingAfter(spacing): Sets the space after the table in points; returns the table.");
        DISPATCH_TABLE.addBuilderMethod("setWidths", (self, process, args) -> {
            final float[] widths = PdfElements.floatArrayArg(process, args.get(0));
            try {
                self.janitorGetHostValue().setWidths(widths);
            } catch (DocumentException e) {
                throw new JanitorNativeException(process, "error setting column widths", e);
            }
        }).setMetaData(Janitor.MetaData.HELP, "setWidths(widths): Sets the relative widths of the columns from a list of numbers, one per column; returns the table.");
        DISPATCH_TABLE.addMethod("deleteRow", (self, process, args) -> com.eischet.janitor.api.Janitor.toBool(self.janitorGetHostValue().deleteRow(args.getRequiredIntValue(0))))
                .setMetaData(Janitor.MetaData.HELP, "deleteRow(index): Deletes the row with the zero-based index; returns true if a row was deleted.");
        DISPATCH_TABLE.addMethod("deleteLastRow", (self, process, args) -> com.eischet.janitor.api.Janitor.toBool(self.janitorGetHostValue().deleteLastRow()))
                .setMetaData(Janitor.MetaData.HELP, "deleteLastRow(): Deletes the last row; returns true if a row was deleted.");
        DISPATCH_TABLE.addVoidMethod("deleteBodyRows", (self, process, args) -> self.janitorGetHostValue().deleteBodyRows())
                .setMetaData(Janitor.MetaData.HELP, "deleteBodyRows(): Deletes all rows except the header rows.");
        DISPATCH_TABLE.addVoidMethod("completeRow", (self, process, args) -> self.janitorGetHostValue().completeRow())
                .setMetaData(Janitor.MetaData.HELP, "completeRow(): Fills the current, incomplete row with empty default cells.");
        // TODO: writeSelectedRows/getRowHeight/setTableEvent/getAbsoluteRowHeight -- low-level PdfContentByte APIs, not mapped yet.
    }

    public JPDFPTable(final @NotNull PdfPTable table) {
        super(DISPATCH_TABLE, table);
    }

    public JPDFPTable(final int numColumns) {
        super(DISPATCH_TABLE, new PdfPTable(numColumns));
    }

    public JPDFPTable(final float[] relativeWidths) {
        super(DISPATCH_TABLE, new PdfPTable(relativeWidths));
    }

    /**
     * @return the wrapped PdfPTable object
     */
    public PdfPTable getTable() {
        return wrapped;
    }

    static JPDFPTable fromArgs(final JanitorScriptProcess process, final JCallArgs args) throws JanitorRuntimeException {
        args.require(1);
        if (args.get(0) instanceof com.eischet.janitor.api.types.builtin.JInt) {
            return new JPDFPTable(args.getRequiredIntValue(0));
        }
        return new JPDFPTable(PdfElements.floatArrayArg(process, args.get(0)));
    }

    /**
     * addCell() accepts a JPDFPCell, but also a JPDFPhrase/String directly for convenience, matching
     * PdfPTable's own overloaded addCell(PdfPCell) / addCell(Phrase) / addCell(String) methods.
     */
    private record JanitorObjectOrCell(PdfPCell cell) {
        static JanitorObjectOrCell of(final JanitorScriptProcess process, final JCallArgs args, final int position) throws JanitorRuntimeException {
            final var value = args.get(position);
            if (value instanceof JanitorWrapper<?> wrapper) {
                if (wrapper.janitorGetHostValue() instanceof PdfPCell cell) {
                    return new JanitorObjectOrCell(cell);
                } else if (wrapper.janitorGetHostValue() instanceof org.openpdf.text.Phrase phrase) {
                    return new JanitorObjectOrCell(new PdfPCell(phrase));
                }
            }
            if (value instanceof com.eischet.janitor.api.types.builtin.JString s) {
                return new JanitorObjectOrCell(new PdfPCell(new org.openpdf.text.Phrase(s.janitorGetHostValue())));
            }
            throw new JanitorArgumentException(process, "addCell() expects a PCell, Phrase or String");
        }
    }

}
