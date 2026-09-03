package com.eischet.janitor.modules.pdf;

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
        DISPATCH_TABLE.addDoubleProperty("left", self -> self.janitorGetHostValue().getLeft(), (self, value) -> self.janitorGetHostValue().setLeft((float) value));
        DISPATCH_TABLE.addDoubleProperty("right", self -> self.janitorGetHostValue().getRight(), (self, value) -> self.janitorGetHostValue().setRight((float) value));
        DISPATCH_TABLE.addDoubleProperty("top", self -> self.janitorGetHostValue().getTop(), (self, value) -> self.janitorGetHostValue().setTop((float) value));
        DISPATCH_TABLE.addDoubleProperty("bottom", self -> self.janitorGetHostValue().getBottom(), (self, value) -> self.janitorGetHostValue().setBottom((float) value));
        DISPATCH_TABLE.addDoubleProperty("width", self -> self.janitorGetHostValue().getWidth());
        DISPATCH_TABLE.addDoubleProperty("height", self -> self.janitorGetHostValue().getHeight());
        DISPATCH_TABLE.addIntegerProperty("rotation", self -> self.janitorGetHostValue().getRotation(), (self, value) -> self.janitorGetHostValue().setRotation(value));
        DISPATCH_TABLE.addIntegerProperty("border", self -> self.janitorGetHostValue().getBorder(), (self, value) -> self.janitorGetHostValue().setBorder(value));
        DISPATCH_TABLE.addDoubleProperty("borderWidth", self -> self.janitorGetHostValue().getBorderWidth(), (self, value) -> self.janitorGetHostValue().setBorderWidth((float) value));
        DISPATCH_TABLE.addObjectProperty("borderColor",
                self -> nullableColor(self.janitorGetHostValue().getBorderColor()),
                (self, value) -> self.janitorGetHostValue().setBorderColor(value == null ? null : value.getColor()),
                () -> new JPDFColor(0, 0, 0));
        DISPATCH_TABLE.addObjectProperty("backgroundColor",
                self -> nullableColor(self.janitorGetHostValue().getBackgroundColor()),
                (self, value) -> self.janitorGetHostValue().setBackgroundColor(value == null ? null : value.getColor()),
                () -> new JPDFColor(0, 0, 0));
        DISPATCH_TABLE.addMethod("rotate", (self, process, args) -> new JPDFRectangle(self.janitorGetHostValue().rotate()));
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

    public Rectangle getRectangle() {
        return wrapped;
    }

}
