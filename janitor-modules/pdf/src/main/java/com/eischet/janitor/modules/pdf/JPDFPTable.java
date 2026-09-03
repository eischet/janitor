package com.eischet.janitor.modules.pdf;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.errors.runtime.JanitorNativeException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.functions.JCallArgs;
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
        });
        DISPATCH_TABLE.addIntegerProperty("numberOfColumns", self -> self.janitorGetHostValue().getNumberOfColumns());
        DISPATCH_TABLE.addIntegerProperty("size", self -> self.janitorGetHostValue().size());
        DISPATCH_TABLE.addIntegerProperty("headerRows", self -> self.janitorGetHostValue().getHeaderRows(), (self, value) -> self.janitorGetHostValue().setHeaderRows(value));
        DISPATCH_TABLE.addDoubleProperty("widthPercentage", self -> self.janitorGetHostValue().getWidthPercentage(), (self, value) -> self.janitorGetHostValue().setWidthPercentage((float) value));
        DISPATCH_TABLE.addDoubleProperty("totalWidth", self -> self.janitorGetHostValue().getTotalWidth());
        DISPATCH_TABLE.addBuilderMethod("setTotalWidth", (self, process, args) -> self.janitorGetHostValue().setTotalWidth(PdfElements.floatArg(process, args, 0)));
        DISPATCH_TABLE.addIntegerProperty("horizontalAlignment", self -> self.janitorGetHostValue().getHorizontalAlignment(), (self, value) -> self.janitorGetHostValue().setHorizontalAlignment(value));
        DISPATCH_TABLE.addBooleanProperty("lockedWidth", self -> self.janitorGetHostValue().isLockedWidth(), (self, value) -> self.janitorGetHostValue().setLockedWidth(value));
        DISPATCH_TABLE.addBooleanProperty("splitRows", self -> self.janitorGetHostValue().isSplitRows(), (self, value) -> self.janitorGetHostValue().setSplitRows(value));
        DISPATCH_TABLE.addBooleanProperty("skipFirstHeader", self -> self.janitorGetHostValue().isSkipFirstHeader(), (self, value) -> self.janitorGetHostValue().setSkipFirstHeader(value));
        DISPATCH_TABLE.addBooleanProperty("skipLastFooter", self -> self.janitorGetHostValue().isSkipLastFooter(), (self, value) -> self.janitorGetHostValue().setSkipLastFooter(value));
        DISPATCH_TABLE.addBuilderMethod("setSpacingBefore", (self, process, args) -> self.janitorGetHostValue().setSpacingBefore(PdfElements.floatArg(process, args, 0)));
        DISPATCH_TABLE.addBuilderMethod("setSpacingAfter", (self, process, args) -> self.janitorGetHostValue().setSpacingAfter(PdfElements.floatArg(process, args, 0)));
        DISPATCH_TABLE.addBuilderMethod("setWidths", (self, process, args) -> {
            final float[] widths = PdfElements.floatArrayArg(process, args.get(0));
            try {
                self.janitorGetHostValue().setWidths(widths);
            } catch (DocumentException e) {
                throw new JanitorNativeException(process, "error setting column widths", e);
            }
        });
        DISPATCH_TABLE.addMethod("deleteRow", (self, process, args) -> com.eischet.janitor.api.Janitor.toBool(self.janitorGetHostValue().deleteRow(args.getRequiredIntValue(0))));
        DISPATCH_TABLE.addMethod("deleteLastRow", (self, process, args) -> com.eischet.janitor.api.Janitor.toBool(self.janitorGetHostValue().deleteLastRow()));
        DISPATCH_TABLE.addVoidMethod("deleteBodyRows", (self, process, args) -> self.janitorGetHostValue().deleteBodyRows());
        DISPATCH_TABLE.addVoidMethod("completeRow", (self, process, args) -> self.janitorGetHostValue().completeRow());
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
