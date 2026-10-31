// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.pdf;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.functions.JCallArgs;
import com.eischet.janitor.api.Janitor;
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
        DISPATCH_TABLE.addBuilderMethod("setPhrase", (self, process, args) -> self.janitorGetHostValue().setPhrase(args.getRequired(0, JPDFPhrase.class).getPhrase()))
                .setMetaData(Janitor.MetaData.HELP, "setPhrase(phrase): Sets the content of the cell to the Phrase; returns the cell.");
        DISPATCH_TABLE.addBuilderMethod("setImage", (self, process, args) -> self.janitorGetHostValue().setImage(args.getRequired(0, JPDFImage.class).getImage()))
                .setMetaData(Janitor.MetaData.HELP, "setImage(image): Sets the content of the cell to the Image; returns the cell.");
        // setTable(PdfPTable) is package-private in PdfPCell -- use the PCell(table) constructor instead.
        DISPATCH_TABLE.addIntegerProperty("horizontalAlignment", self -> self.janitorGetHostValue().getHorizontalAlignment(), (self, value) -> self.janitorGetHostValue().setHorizontalAlignment(value))
                .setMetaData(Janitor.MetaData.HELP, "horizontalAlignment: The horizontal alignment of the content, e.g. pdf.ALIGN_LEFT, ALIGN_CENTER or ALIGN_RIGHT; writable.");
        DISPATCH_TABLE.addIntegerProperty("verticalAlignment", self -> self.janitorGetHostValue().getVerticalAlignment(), (self, value) -> self.janitorGetHostValue().setVerticalAlignment(value))
                .setMetaData(Janitor.MetaData.HELP, "verticalAlignment: The vertical alignment of the content, one of pdf.ALIGN_TOP, ALIGN_MIDDLE or ALIGN_BOTTOM; writable.");
        DISPATCH_TABLE.addDoubleProperty("paddingLeft", self -> self.janitorGetHostValue().getPaddingLeft(), (self, value) -> self.janitorGetHostValue().setPaddingLeft((float) value))
                .setMetaData(Janitor.MetaData.HELP, "paddingLeft: The left padding in points; writable.");
        DISPATCH_TABLE.addDoubleProperty("paddingRight", self -> self.janitorGetHostValue().getPaddingRight(), (self, value) -> self.janitorGetHostValue().setPaddingRight((float) value))
                .setMetaData(Janitor.MetaData.HELP, "paddingRight: The right padding in points; writable.");
        DISPATCH_TABLE.addDoubleProperty("paddingTop", self -> self.janitorGetHostValue().getPaddingTop(), (self, value) -> self.janitorGetHostValue().setPaddingTop((float) value))
                .setMetaData(Janitor.MetaData.HELP, "paddingTop: The top padding in points; writable.");
        DISPATCH_TABLE.addDoubleProperty("paddingBottom", self -> self.janitorGetHostValue().getPaddingBottom(), (self, value) -> self.janitorGetHostValue().setPaddingBottom((float) value))
                .setMetaData(Janitor.MetaData.HELP, "paddingBottom: The bottom padding in points; writable.");
        DISPATCH_TABLE.addBuilderMethod("setPadding", (self, process, args) -> self.janitorGetHostValue().setPadding(PdfElements.floatArg(process, args, 0)))
                .setMetaData(Janitor.MetaData.HELP, "setPadding(padding): Sets the padding on all four sides in points; returns the cell.");
        DISPATCH_TABLE.addIntegerProperty("colspan", self -> self.janitorGetHostValue().getColspan(), (self, value) -> self.janitorGetHostValue().setColspan(value))
                .setMetaData(Janitor.MetaData.HELP, "colspan: The number of columns the cell spans; writable.");
        DISPATCH_TABLE.addIntegerProperty("rowspan", self -> self.janitorGetHostValue().getRowspan(), (self, value) -> self.janitorGetHostValue().setRowspan(value))
                .setMetaData(Janitor.MetaData.HELP, "rowspan: The number of rows the cell spans; writable.");
        DISPATCH_TABLE.addDoubleProperty("fixedHeight", self -> self.janitorGetHostValue().getFixedHeight(), (self, value) -> self.janitorGetHostValue().setFixedHeight((float) value))
                .setMetaData(Janitor.MetaData.HELP, "fixedHeight: The fixed height of the cell in points; writable.");
        DISPATCH_TABLE.addDoubleProperty("minimumHeight", self -> self.janitorGetHostValue().getMinimumHeight(), (self, value) -> self.janitorGetHostValue().setMinimumHeight((float) value))
                .setMetaData(Janitor.MetaData.HELP, "minimumHeight: The minimum height of the cell in points; writable.");
        DISPATCH_TABLE.addBooleanProperty("noWrap", self -> self.janitorGetHostValue().isNoWrap(), (self, value) -> self.janitorGetHostValue().setNoWrap(value))
                .setMetaData(Janitor.MetaData.HELP, "noWrap: True if the text in the cell is not wrapped; writable.");
        DISPATCH_TABLE.addBooleanProperty("useAscender", self -> self.janitorGetHostValue().isUseAscender(), (self, value) -> self.janitorGetHostValue().setUseAscender(value))
                .setMetaData(Janitor.MetaData.HELP, "useAscender: True if the font's ascender is used to position the first line of text; writable.");
        DISPATCH_TABLE.addBooleanProperty("useDescender", self -> self.janitorGetHostValue().isUseDescender(), (self, value) -> self.janitorGetHostValue().setUseDescender(value))
                .setMetaData(Janitor.MetaData.HELP, "useDescender: True if the font's descender is taken into account below the last line of text; writable.");
        DISPATCH_TABLE.addIntegerProperty("border", self -> self.janitorGetHostValue().getBorder(), (self, value) -> self.janitorGetHostValue().setBorder(value))
                .setMetaData(Janitor.MetaData.HELP, "border: The sides that have a border, as pdf.BORDER_* flags combined with |; writable.");
        DISPATCH_TABLE.addDoubleProperty("borderWidth", self -> self.janitorGetHostValue().getBorderWidth(), (self, value) -> self.janitorGetHostValue().setBorderWidth((float) value))
                .setMetaData(Janitor.MetaData.HELP, "borderWidth: The width of the border in points; writable.");
        DISPATCH_TABLE.addObjectProperty("backgroundColor",
                self -> { final var c = self.janitorGetHostValue().getBackgroundColor(); return c == null ? null : new JPDFColor(c); },
                (self, value) -> self.janitorGetHostValue().setBackgroundColor(value == null ? null : value.getColor()),
                () -> new JPDFColor(0, 0, 0))
                .setMetaData(Janitor.MetaData.HELP, "backgroundColor: The background Color of the cell, or null; writable.");
        DISPATCH_TABLE.addBooleanProperty("useBorderPadding", self -> self.janitorGetHostValue().isUseBorderPadding(), (self, value) -> self.janitorGetHostValue().setUseBorderPadding(value))
                .setMetaData(Janitor.MetaData.HELP, "useBorderPadding: True if the border width is added to the padding; writable.");
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

    /**
     * @return the wrapped PdfPCell object
     */
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
