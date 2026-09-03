package com.eischet.janitor.modules.pdf;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.functions.JCallArgs;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.jetbrains.annotations.NotNull;
import org.openpdf.text.pdf.PdfPCell;

/**
 * Wraps org.openpdf.text.pdf.PdfPCell: one cell of a PdfPTable.
 */
public class JPDFPCell extends JanitorWrapper<PdfPCell> {

    public static final WrapperDispatchTable<PdfPCell> DISPATCH_TABLE = new WrapperDispatchTable<>();

    static {
        DISPATCH_TABLE.addBuilderMethod("setPhrase", (self, process, args) -> self.janitorGetHostValue().setPhrase(args.getRequired(0, JPDFPhrase.class).getPhrase()));
        DISPATCH_TABLE.addBuilderMethod("setImage", (self, process, args) -> self.janitorGetHostValue().setImage(args.getRequired(0, JPDFImage.class).getImage()));
        // setTable(PdfPTable) is package-private in PdfPCell -- use the PCell(table) constructor instead.
        DISPATCH_TABLE.addIntegerProperty("horizontalAlignment", self -> self.janitorGetHostValue().getHorizontalAlignment(), (self, value) -> self.janitorGetHostValue().setHorizontalAlignment(value));
        DISPATCH_TABLE.addIntegerProperty("verticalAlignment", self -> self.janitorGetHostValue().getVerticalAlignment(), (self, value) -> self.janitorGetHostValue().setVerticalAlignment(value));
        DISPATCH_TABLE.addDoubleProperty("paddingLeft", self -> self.janitorGetHostValue().getPaddingLeft(), (self, value) -> self.janitorGetHostValue().setPaddingLeft((float) value));
        DISPATCH_TABLE.addDoubleProperty("paddingRight", self -> self.janitorGetHostValue().getPaddingRight(), (self, value) -> self.janitorGetHostValue().setPaddingRight((float) value));
        DISPATCH_TABLE.addDoubleProperty("paddingTop", self -> self.janitorGetHostValue().getPaddingTop(), (self, value) -> self.janitorGetHostValue().setPaddingTop((float) value));
        DISPATCH_TABLE.addDoubleProperty("paddingBottom", self -> self.janitorGetHostValue().getPaddingBottom(), (self, value) -> self.janitorGetHostValue().setPaddingBottom((float) value));
        DISPATCH_TABLE.addBuilderMethod("setPadding", (self, process, args) -> self.janitorGetHostValue().setPadding(PdfElements.floatArg(process, args, 0)));
        DISPATCH_TABLE.addIntegerProperty("colspan", self -> self.janitorGetHostValue().getColspan(), (self, value) -> self.janitorGetHostValue().setColspan(value));
        DISPATCH_TABLE.addIntegerProperty("rowspan", self -> self.janitorGetHostValue().getRowspan(), (self, value) -> self.janitorGetHostValue().setRowspan(value));
        DISPATCH_TABLE.addDoubleProperty("fixedHeight", self -> self.janitorGetHostValue().getFixedHeight(), (self, value) -> self.janitorGetHostValue().setFixedHeight((float) value));
        DISPATCH_TABLE.addDoubleProperty("minimumHeight", self -> self.janitorGetHostValue().getMinimumHeight(), (self, value) -> self.janitorGetHostValue().setMinimumHeight((float) value));
        DISPATCH_TABLE.addBooleanProperty("noWrap", self -> self.janitorGetHostValue().isNoWrap(), (self, value) -> self.janitorGetHostValue().setNoWrap(value));
        DISPATCH_TABLE.addBooleanProperty("useAscender", self -> self.janitorGetHostValue().isUseAscender(), (self, value) -> self.janitorGetHostValue().setUseAscender(value));
        DISPATCH_TABLE.addBooleanProperty("useDescender", self -> self.janitorGetHostValue().isUseDescender(), (self, value) -> self.janitorGetHostValue().setUseDescender(value));
        DISPATCH_TABLE.addIntegerProperty("border", self -> self.janitorGetHostValue().getBorder(), (self, value) -> self.janitorGetHostValue().setBorder(value));
        DISPATCH_TABLE.addDoubleProperty("borderWidth", self -> self.janitorGetHostValue().getBorderWidth(), (self, value) -> self.janitorGetHostValue().setBorderWidth((float) value));
        DISPATCH_TABLE.addObjectProperty("backgroundColor",
                self -> { final var c = self.janitorGetHostValue().getBackgroundColor(); return c == null ? null : new JPDFColor(c); },
                (self, value) -> self.janitorGetHostValue().setBackgroundColor(value == null ? null : value.getColor()),
                () -> new JPDFColor(0, 0, 0));
        DISPATCH_TABLE.addBooleanProperty("useBorderPadding", self -> self.janitorGetHostValue().isUseBorderPadding(), (self, value) -> self.janitorGetHostValue().setUseBorderPadding(value));
        // TODO: setLeading(fixed, multiplied), setCellEvent, setColumn, setArabicOptions, per-side padding niceties -- left out, add on request.
    }

    public JPDFPCell(final @NotNull PdfPCell cell) {
        super(DISPATCH_TABLE, cell);
    }

    public JPDFPCell() {
        super(DISPATCH_TABLE, new PdfPCell());
    }

    public JPDFPCell(final @NotNull JPDFPhrase phrase) {
        super(DISPATCH_TABLE, new PdfPCell(phrase.getPhrase()));
    }

    public JPDFPCell(final @NotNull JPDFImage image) {
        super(DISPATCH_TABLE, new PdfPCell(image.getImage()));
    }

    public JPDFPCell(final @NotNull JPDFPTable table) {
        super(DISPATCH_TABLE, new PdfPCell(table.getTable()));
    }

    public PdfPCell getCell() {
        return wrapped;
    }

    static JPDFPCell fromArgs(final JanitorScriptProcess process, final JCallArgs args) throws JanitorRuntimeException {
        if (args.size() == 0) {
            return new JPDFPCell();
        }
        if (args.size() == 1 && args.get(0) instanceof com.eischet.janitor.api.types.wrapped.JanitorWrapper<?> wrapper) {
            if (wrapper.janitorGetHostValue() instanceof org.openpdf.text.Phrase) {
                return new JPDFPCell(args.getRequired(0, JPDFPhrase.class));
            } else if (wrapper.janitorGetHostValue() instanceof org.openpdf.text.Image) {
                return new JPDFPCell(args.getRequired(0, JPDFImage.class));
            } else if (wrapper.janitorGetHostValue() instanceof org.openpdf.text.pdf.PdfPTable) {
                return new JPDFPCell(args.getRequired(0, JPDFPTable.class));
            }
        }
        throw new JanitorArgumentException(process, "PCell() takes 0 arguments, or 1 (Phrase, Image or PTable)");
    }

}
