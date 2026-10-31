// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.pdf;

import com.eischet.janitor.api.Janitor;
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
        DISPATCH_TABLE.addIntegerProperty("red", self -> self.janitorGetHostValue().getRed())
                .setMetaData(Janitor.MetaData.HELP, "red: The red component of the color, 0 to 255; read-only.");
        DISPATCH_TABLE.addIntegerProperty("green", self -> self.janitorGetHostValue().getGreen())
                .setMetaData(Janitor.MetaData.HELP, "green: The green component of the color, 0 to 255; read-only.");
        DISPATCH_TABLE.addIntegerProperty("blue", self -> self.janitorGetHostValue().getBlue())
                .setMetaData(Janitor.MetaData.HELP, "blue: The blue component of the color, 0 to 255; read-only.");
        DISPATCH_TABLE.addIntegerProperty("alpha", self -> self.janitorGetHostValue().getAlpha())
                .setMetaData(Janitor.MetaData.HELP, "alpha: The alpha (opacity) component of the color, 0 to 255; read-only.");
        DISPATCH_TABLE.addStringProperty("rgb", self -> String.format("#%06X", self.janitorGetHostValue().getRGB() & 0xFFFFFF))
                .setMetaData(Janitor.MetaData.HELP, "rgb: The color as a hex string like \"#FF8000\", without alpha; read-only.");
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

    /**
     * @return the wrapped Color object
     */
    public Color getColor() {
        return wrapped;
    }

}
