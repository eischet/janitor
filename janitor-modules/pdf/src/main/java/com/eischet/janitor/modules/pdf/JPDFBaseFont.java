package com.eischet.janitor.modules.pdf;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorNativeException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.functions.JCallArgs;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.jetbrains.annotations.NotNull;
import org.openpdf.text.DocumentException;
import org.openpdf.text.pdf.BaseFont;

import java.io.IOException;

/**
 * Wraps org.openpdf.text.pdf.BaseFont: the low-level font used to embed a specific typeface (as opposed
 * to org.openpdf.text.Font, which is family/size/style/color and defers to a BaseFont for the actual glyphs).
 * <p>
 * Use pdf.BaseFont.HELVETICA etc. (see BaseFont's own String constants, exposed unchanged) as the "name" argument
 * for the standard 14 fonts, or a TrueType/OTF file path to embed a custom font.
 */
public class JPDFBaseFont extends JanitorWrapper<BaseFont> {

    public static final WrapperDispatchTable<BaseFont> DISPATCH_TABLE = new WrapperDispatchTable<>();

    static {
        DISPATCH_TABLE.addStringProperty("postscriptFontName", self -> self.janitorGetHostValue().getPostscriptFontName());
        DISPATCH_TABLE.addMethod("getWidthPoint", (self, process, args) -> com.eischet.janitor.api.Janitor.floatingPoint(
                self.janitorGetHostValue().getWidthPoint(args.getRequiredStringValue(0), PdfElements.floatArg(process, args, 1))));
        DISPATCH_TABLE.addMethod("getFont", (self, process, args) -> new JPDFFont(
                new org.openpdf.text.Font(self.janitorGetHostValue(), PdfElements.optionalFloatArg(process, args, 0, org.openpdf.text.Font.UNDEFINED))));
        // TODO: getFullFontName, getFamilyFontName, setSubset/isSubset, and the CJK/embedding-related APIs -- left out, add on request.
    }

    public JPDFBaseFont(final @NotNull BaseFont baseFont) {
        super(DISPATCH_TABLE, baseFont);
    }

    public BaseFont getBaseFont() {
        return wrapped;
    }

    static JPDFBaseFont fromArgs(final JanitorScriptProcess process, final JCallArgs args) throws JanitorRuntimeException {
        final String name = args.getRequiredStringValue(0);
        final String encoding = args.getOptionalStringValue(1, BaseFont.WINANSI);
        final boolean embedded = args.size() >= 3 && args.getRequiredBooleanValue(2);
        try {
            return new JPDFBaseFont(BaseFont.createFont(name, encoding, embedded));
        } catch (DocumentException | IOException e) {
            throw new JanitorNativeException(process, "error creating font '" + name + "'", e);
        }
    }

}
