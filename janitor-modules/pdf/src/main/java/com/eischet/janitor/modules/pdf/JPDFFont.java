package com.eischet.janitor.modules.pdf;

import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.jetbrains.annotations.NotNull;
import org.openpdf.text.Font;

import java.awt.Color;

/**
 * Wraps org.openpdf.text.Font: fontfamily, size, style and color.
 * <p>
 * Family and style are plain ints, matching Font.COURIER/HELVETICA/TIMES_ROMAN/SYMBOL/ZAPFDINGBATS and
 * Font.NORMAL/BOLD/ITALIC/UNDERLINE/STRIKETHRU/BOLDITALIC. These constants are exposed on the pdf module
 * itself (pdf.HELVETICA, pdf.BOLD, ...) so scripts don't need to look up the raw numbers.
 */
public class JPDFFont extends JanitorWrapper<Font> {

    public static final WrapperDispatchTable<Font> DISPATCH_TABLE = new WrapperDispatchTable<>();

    static {
        DISPATCH_TABLE.addIntegerProperty("family", self -> self.janitorGetHostValue().getFamily(), (self, value) -> self.janitorGetHostValue().setFamily(familyName(value)));
        DISPATCH_TABLE.addDoubleProperty("size", self -> self.janitorGetHostValue().getSize(), (self, value) -> self.janitorGetHostValue().setSize((float) value));
        DISPATCH_TABLE.addIntegerProperty("style", self -> self.janitorGetHostValue().getStyle(), (self, value) -> self.janitorGetHostValue().setStyle(value));
        DISPATCH_TABLE.addStringProperty("familyname", self -> self.janitorGetHostValue().getFamilyname());
        DISPATCH_TABLE.addDoubleProperty("calculatedSize", self -> self.janitorGetHostValue().getCalculatedSize());
        DISPATCH_TABLE.addBooleanProperty("bold", self -> self.janitorGetHostValue().isBold());
        DISPATCH_TABLE.addBooleanProperty("italic", self -> self.janitorGetHostValue().isItalic());
        DISPATCH_TABLE.addBooleanProperty("underlined", self -> self.janitorGetHostValue().isUnderlined());
        DISPATCH_TABLE.addBooleanProperty("strikethru", self -> self.janitorGetHostValue().isStrikethru());
        DISPATCH_TABLE.addObjectProperty("color",
                self -> {
                    final Color c = self.janitorGetHostValue().getColor();
                    return c == null ? null : new JPDFColor(c);
                },
                (self, value) -> self.janitorGetHostValue().setColor(value == null ? null : value.getColor()),
                () -> new JPDFColor(0, 0, 0));
        // TODO: getBaseFont()/setBaseFont() -- would require wrapping org.openpdf.text.pdf.BaseFont (see JPDFBaseFont) as a Font constructor argument.
        // TODO: compareTo(Object), difference(Font) -- not mapped, unclear how useful these are from scripts.
    }

    // Font.setFamily(String) does the same lookup Font.getFamilyIndex() does, but we're given an int here (our "family" property is an int),
    // so this is just a passthrough helper kept for symmetry with the getter; the real work happens via setFamily(String) in Font itself
    // when constructed from a name. Scripts are expected to use the pdf.COURIER/HELVETICA/... integer constants.
    private static String familyName(final int family) {
        return switch (family) {
            case Font.COURIER -> "Courier";
            case Font.HELVETICA -> "Helvetica";
            case Font.TIMES_ROMAN -> "Times New Roman";
            case Font.SYMBOL -> "Symbol";
            case Font.ZAPFDINGBATS -> "ZapfDingbats";
            default -> "Helvetica";
        };
    }

    public JPDFFont(final @NotNull Font font) {
        super(DISPATCH_TABLE, font);
    }

    public JPDFFont() {
        super(DISPATCH_TABLE, new Font());
    }

    public JPDFFont(final int family, final float size, final int style, final @org.jetbrains.annotations.Nullable Color color) {
        super(DISPATCH_TABLE, new Font(family, size, style, color));
    }

    public Font getFont() {
        return wrapped;
    }

}
