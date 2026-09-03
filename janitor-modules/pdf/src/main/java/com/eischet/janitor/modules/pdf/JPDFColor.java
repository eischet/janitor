package com.eischet.janitor.modules.pdf;

import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;

/**
 * Wraps java.awt.Color, which is used throughout OpenPDF/LibrePDF (Font, Rectangle borders, PdfPCell, ...)
 * to describe colors.
 */
public class JPDFColor extends JanitorWrapper<Color> {

    public static final WrapperDispatchTable<Color> DISPATCH_TABLE = new WrapperDispatchTable<>();

    static {
        DISPATCH_TABLE.addIntegerProperty("red", self -> self.janitorGetHostValue().getRed());
        DISPATCH_TABLE.addIntegerProperty("green", self -> self.janitorGetHostValue().getGreen());
        DISPATCH_TABLE.addIntegerProperty("blue", self -> self.janitorGetHostValue().getBlue());
        DISPATCH_TABLE.addIntegerProperty("alpha", self -> self.janitorGetHostValue().getAlpha());
        DISPATCH_TABLE.addStringProperty("rgb", self -> String.format("#%06X", self.janitorGetHostValue().getRGB() & 0xFFFFFF));
    }

    public JPDFColor(final @NotNull Color color) {
        super(DISPATCH_TABLE, color);
    }

    public JPDFColor(final int red, final int green, final int blue) {
        super(DISPATCH_TABLE, new Color(red, green, blue));
    }

    public JPDFColor(final int red, final int green, final int blue, final int alpha) {
        super(DISPATCH_TABLE, new Color(red, green, blue, alpha));
    }

    public Color getColor() {
        return wrapped;
    }

}
