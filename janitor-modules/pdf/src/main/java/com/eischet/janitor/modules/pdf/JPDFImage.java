package com.eischet.janitor.modules.pdf;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.errors.runtime.JanitorNativeException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.builtin.JBinary;
import com.eischet.janitor.api.types.functions.JCallArgs;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.jetbrains.annotations.NotNull;
import org.openpdf.text.BadElementException;
import org.openpdf.text.Image;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Wraps org.openpdf.text.Image. Image has no public Java constructor -- instances are only obtained via
 * the static Image.getInstance(...) factory family, so pdf.Image(...) below dispatches to those.
 */
public class JPDFImage extends JanitorWrapper<Image> {

    public static final WrapperDispatchTable<Image> DISPATCH_TABLE = new WrapperDispatchTable<>();

    static {
        DISPATCH_TABLE.addIntegerProperty("alignment", self -> self.janitorGetHostValue().getAlignment(), (self, value) -> self.janitorGetHostValue().setAlignment(value));
        // Image has no getAlt(), only setAlt() -- so this property is write-only; reading it always yields null.
        DISPATCH_TABLE.addStringProperty("alt", self -> null, (self, value) -> self.janitorGetHostValue().setAlt(value));
        DISPATCH_TABLE.addDoubleProperty("absoluteX", self -> self.janitorGetHostValue().getAbsoluteX());
        DISPATCH_TABLE.addDoubleProperty("absoluteY", self -> self.janitorGetHostValue().getAbsoluteY());
        DISPATCH_TABLE.addDoubleProperty("scaledWidth", self -> self.janitorGetHostValue().getScaledWidth());
        DISPATCH_TABLE.addDoubleProperty("scaledHeight", self -> self.janitorGetHostValue().getScaledHeight());
        DISPATCH_TABLE.addDoubleProperty("plainWidth", self -> self.janitorGetHostValue().getPlainWidth());
        DISPATCH_TABLE.addDoubleProperty("plainHeight", self -> self.janitorGetHostValue().getPlainHeight());
        DISPATCH_TABLE.addDoubleProperty("widthPercentage", self -> self.janitorGetHostValue().getWidthPercentage(), (self, value) -> self.janitorGetHostValue().setWidthPercentage((float) value));
        DISPATCH_TABLE.addDoubleProperty("indentationLeft", self -> self.janitorGetHostValue().getIndentationLeft(), (self, value) -> self.janitorGetHostValue().setIndentationLeft((float) value));
        DISPATCH_TABLE.addDoubleProperty("indentationRight", self -> self.janitorGetHostValue().getIndentationRight(), (self, value) -> self.janitorGetHostValue().setIndentationRight((float) value));
        DISPATCH_TABLE.addDoubleProperty("spacingBefore", self -> self.janitorGetHostValue().getSpacingBefore(), (self, value) -> self.janitorGetHostValue().setSpacingBefore((float) value));
        DISPATCH_TABLE.addDoubleProperty("spacingAfter", self -> self.janitorGetHostValue().getSpacingAfter(), (self, value) -> self.janitorGetHostValue().setSpacingAfter((float) value));
        DISPATCH_TABLE.addBuilderMethod("setAbsolutePosition", (self, process, args) -> self.janitorGetHostValue().setAbsolutePosition(PdfElements.floatArg(process, args, 0), PdfElements.floatArg(process, args, 1)));
        DISPATCH_TABLE.addBuilderMethod("setRotationDegrees", (self, process, args) -> self.janitorGetHostValue().setRotationDegrees(PdfElements.floatArg(process, args, 0)));
        DISPATCH_TABLE.addBuilderMethod("scaleAbsolute", (self, process, args) -> self.janitorGetHostValue().scaleAbsolute(PdfElements.floatArg(process, args, 0), PdfElements.floatArg(process, args, 1)));
        DISPATCH_TABLE.addBuilderMethod("scalePercent", (self, process, args) -> {
            if (args.size() >= 2) {
                self.janitorGetHostValue().scalePercent(PdfElements.floatArg(process, args, 0), PdfElements.floatArg(process, args, 1));
            } else {
                self.janitorGetHostValue().scalePercent(PdfElements.floatArg(process, args, 0));
            }
        });
        DISPATCH_TABLE.addBuilderMethod("scaleToFit", (self, process, args) -> self.janitorGetHostValue().scaleToFit(PdfElements.floatArg(process, args, 0), PdfElements.floatArg(process, args, 1)));
        // TODO: setImageMask, setLayer, setTransparency, setSmask, setDpi, setCompressionLevel -- advanced/rarely scripted; add on request.
    }

    public JPDFImage(final @NotNull Image image) {
        super(DISPATCH_TABLE, image);
    }

    public Image getImage() {
        return wrapped;
    }

    static JPDFImage fromArgs(final JanitorScriptProcess process, final JCallArgs args) throws JanitorRuntimeException {
        args.require(1);
        if (args.get(0) instanceof JBinary binary) {
            try {
                return new JPDFImage(Image.getInstance(binary.janitorGetHostValue()));
            } catch (BadElementException | IOException e) {
                throw new JanitorNativeException(process, "error loading image from binary data", e);
            }
        }
        final String location = args.getRequiredStringValue(0);
        try {
            try {
                return new JPDFImage(Image.getInstance(new URL(location)));
            } catch (MalformedURLException notAUrl) {
                return new JPDFImage(Image.getInstance(location));
            }
        } catch (BadElementException | IOException e) {
            throw new JanitorNativeException(process, "error loading image '" + location + "'", e);
        }
    }

}
