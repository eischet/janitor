// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.pdf;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.jetbrains.annotations.NotNull;
import org.openpdf.text.Rectangle;

import java.awt.Color;

/**
 * Wraps org.openpdf.text.Rectangle, e.g. a Document's page size, or an area used for borders/background.
 * Instances obtained from pdf.PageSize(...) wrap a read-only rectangle (org.openpdf.text.RectangleReadOnly);
 * calling a setter on those will throw a runtime exception from within OpenPDF itself.
 */
public class JPDFRectangle extends JanitorWrapper<Rectangle> {

    public static final WrapperDispatchTable<Rectangle> DISPATCH_TABLE = new WrapperDispatchTable<>();

    static {
        DISPATCH_TABLE.addDoubleProperty("left", self -> self.janitorGetHostValue().getLeft(), (self, value) -> self.janitorGetHostValue().setLeft((float) value))
                .setMetaData(Janitor.MetaData.HELP, "left: The x coordinate of the left edge in points; writable, except for page sizes.");
        DISPATCH_TABLE.addDoubleProperty("right", self -> self.janitorGetHostValue().getRight(), (self, value) -> self.janitorGetHostValue().setRight((float) value))
                .setMetaData(Janitor.MetaData.HELP, "right: The x coordinate of the right edge in points; writable, except for page sizes.");
        DISPATCH_TABLE.addDoubleProperty("top", self -> self.janitorGetHostValue().getTop(), (self, value) -> self.janitorGetHostValue().setTop((float) value))
                .setMetaData(Janitor.MetaData.HELP, "top: The y coordinate of the top edge in points; writable, except for page sizes.");
        DISPATCH_TABLE.addDoubleProperty("bottom", self -> self.janitorGetHostValue().getBottom(), (self, value) -> self.janitorGetHostValue().setBottom((float) value))
                .setMetaData(Janitor.MetaData.HELP, "bottom: The y coordinate of the bottom edge in points; writable, except for page sizes.");
        DISPATCH_TABLE.addDoubleProperty("width", self -> self.janitorGetHostValue().getWidth())
                .setMetaData(Janitor.MetaData.HELP, "width: The width of the rectangle in points; read-only.");
        DISPATCH_TABLE.addDoubleProperty("height", self -> self.janitorGetHostValue().getHeight())
                .setMetaData(Janitor.MetaData.HELP, "height: The height of the rectangle in points; read-only.");
        DISPATCH_TABLE.addIntegerProperty("rotation", self -> self.janitorGetHostValue().getRotation(), (self, value) -> self.janitorGetHostValue().setRotation(value))
                .setMetaData(Janitor.MetaData.HELP, "rotation: The rotation in degrees; writable, except for page sizes.");
        DISPATCH_TABLE.addIntegerProperty("border", self -> self.janitorGetHostValue().getBorder(), (self, value) -> self.janitorGetHostValue().setBorder(value))
                .setMetaData(Janitor.MetaData.HELP, "border: The sides that have a border, as pdf.BORDER_* flags combined with |; writable, except for page sizes.");
        DISPATCH_TABLE.addDoubleProperty("borderWidth", self -> self.janitorGetHostValue().getBorderWidth(), (self, value) -> self.janitorGetHostValue().setBorderWidth((float) value))
                .setMetaData(Janitor.MetaData.HELP, "borderWidth: The width of the border in points; writable, except for page sizes.");
        DISPATCH_TABLE.addObjectProperty("borderColor",
                self -> nullableColor(self.janitorGetHostValue().getBorderColor()),
                (self, value) -> self.janitorGetHostValue().setBorderColor(value == null ? null : value.getColor()),
                () -> new JPDFColor(0, 0, 0))
                .setMetaData(Janitor.MetaData.HELP, "borderColor: The Color of the border, or null; writable, except for page sizes.");
        DISPATCH_TABLE.addObjectProperty("backgroundColor",
                self -> nullableColor(self.janitorGetHostValue().getBackgroundColor()),
                (self, value) -> self.janitorGetHostValue().setBackgroundColor(value == null ? null : value.getColor()),
                () -> new JPDFColor(0, 0, 0))
                .setMetaData(Janitor.MetaData.HELP, "backgroundColor: The background Color, or null; writable, except for page sizes.");
        DISPATCH_TABLE.addMethod("rotate", (self, process, args) -> new JPDFRectangle(self.janitorGetHostValue().rotate()))
                .setMetaData(Janitor.MetaData.HELP, "rotate(): Returns a new rectangle rotated by 90 degrees, e.g. to turn a page size into landscape.");
        // TODO: per-side border width/color (Left/Right/Top/Bottom), enableBorderSide/disableBorderSide, grayFill -- left out for brevity, add on request.
    }

    private static JPDFColor nullableColor(final Color color) {
        return color == null ? null : new JPDFColor(color);
    }

    public JPDFRectangle(final @NotNull Rectangle rectangle) {
        super(DISPATCH_TABLE, rectangle);
    }

    public JPDFRectangle(final float urx, final float ury) {
        super(DISPATCH_TABLE, new Rectangle(urx, ury));
    }

    public JPDFRectangle(final float llx, final float lly, final float urx, final float ury) {
        super(DISPATCH_TABLE, new Rectangle(llx, lly, urx, ury));
    }

    /**
     * @return the wrapped Rectangle object
     */
    public Rectangle getRectangle() {
        return wrapped;
    }

}
