// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.pdf;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.errors.runtime.JanitorNativeException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.builtin.JBinary;
import com.eischet.janitor.api.types.functions.JCallArgs;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.jetbrains.annotations.NotNull;
import org.openpdf.text.BadElementException;
import org.openpdf.text.Image;

import java.io.IOException;

/**
 * Wraps org.openpdf.text.Image. Image has no public Java constructor -- instances are only obtained via
 * the static Image.getInstance(...) factory family, so pdf.Image(...) below dispatches to those.
 */
public class JPDFImage extends JanitorWrapper<Image> {

    public static final WrapperDispatchTable<Image> DISPATCH_TABLE = new WrapperDispatchTable<>();

    static {
        DISPATCH_TABLE.addIntegerProperty("alignment", self -> self.janitorGetHostValue().getAlignment(), (self, value) -> self.janitorGetHostValue().setAlignment(value))
                .setMetaData(Janitor.MetaData.HELP, "alignment: The horizontal alignment of the image, e.g. pdf.ALIGN_LEFT, ALIGN_CENTER or ALIGN_RIGHT; writable.");
        // Image has no getAlt(), only setAlt() -- so this property is write-only; reading it always yields null.
        DISPATCH_TABLE.addStringProperty("alt", self -> null, (self, value) -> self.janitorGetHostValue().setAlt(value))
                .setMetaData(Janitor.MetaData.HELP, "alt: The alternative text of the image; write-only, reading it always returns null.");
        DISPATCH_TABLE.addDoubleProperty("absoluteX", self -> self.janitorGetHostValue().getAbsoluteX())
                .setMetaData(Janitor.MetaData.HELP, "absoluteX: The absolute x position on the page in points; read-only, see setAbsolutePosition().");
        DISPATCH_TABLE.addDoubleProperty("absoluteY", self -> self.janitorGetHostValue().getAbsoluteY())
                .setMetaData(Janitor.MetaData.HELP, "absoluteY: The absolute y position on the page in points; read-only, see setAbsolutePosition().");
        DISPATCH_TABLE.addDoubleProperty("scaledWidth", self -> self.janitorGetHostValue().getScaledWidth())
                .setMetaData(Janitor.MetaData.HELP, "scaledWidth: The width of the image in points after scaling and rotation; read-only.");
        DISPATCH_TABLE.addDoubleProperty("scaledHeight", self -> self.janitorGetHostValue().getScaledHeight())
                .setMetaData(Janitor.MetaData.HELP, "scaledHeight: The height of the image in points after scaling and rotation; read-only.");
        DISPATCH_TABLE.addDoubleProperty("plainWidth", self -> self.janitorGetHostValue().getPlainWidth())
                .setMetaData(Janitor.MetaData.HELP, "plainWidth: The width of the image in points after scaling, without rotation; read-only.");
        DISPATCH_TABLE.addDoubleProperty("plainHeight", self -> self.janitorGetHostValue().getPlainHeight())
                .setMetaData(Janitor.MetaData.HELP, "plainHeight: The height of the image in points after scaling, without rotation; read-only.");
        DISPATCH_TABLE.addDoubleProperty("widthPercentage", self -> self.janitorGetHostValue().getWidthPercentage(), (self, value) -> self.janitorGetHostValue().setWidthPercentage((float) value))
                .setMetaData(Janitor.MetaData.HELP, "widthPercentage: The width of the image as a percentage of the available width; writable.");
        DISPATCH_TABLE.addDoubleProperty("indentationLeft", self -> self.janitorGetHostValue().getIndentationLeft(), (self, value) -> self.janitorGetHostValue().setIndentationLeft((float) value))
                .setMetaData(Janitor.MetaData.HELP, "indentationLeft: The left indentation in points; writable.");
        DISPATCH_TABLE.addDoubleProperty("indentationRight", self -> self.janitorGetHostValue().getIndentationRight(), (self, value) -> self.janitorGetHostValue().setIndentationRight((float) value))
                .setMetaData(Janitor.MetaData.HELP, "indentationRight: The right indentation in points; writable.");
        DISPATCH_TABLE.addDoubleProperty("spacingBefore", self -> self.janitorGetHostValue().getSpacingBefore(), (self, value) -> self.janitorGetHostValue().setSpacingBefore((float) value))
                .setMetaData(Janitor.MetaData.HELP, "spacingBefore: The space before the image in points; writable.");
        DISPATCH_TABLE.addDoubleProperty("spacingAfter", self -> self.janitorGetHostValue().getSpacingAfter(), (self, value) -> self.janitorGetHostValue().setSpacingAfter((float) value))
                .setMetaData(Janitor.MetaData.HELP, "spacingAfter: The space after the image in points; writable.");
        DISPATCH_TABLE.addBuilderMethod("setAbsolutePosition", (self, process, args) -> self.janitorGetHostValue().setAbsolutePosition(PdfElements.floatArg(process, args, 0), PdfElements.floatArg(process, args, 1)))
                .setMetaData(Janitor.MetaData.HELP, "setAbsolutePosition(x, y): Places the image at a fixed position on the page, in points from the lower left corner; returns the image.");
        DISPATCH_TABLE.addBuilderMethod("setRotationDegrees", (self, process, args) -> self.janitorGetHostValue().setRotationDegrees(PdfElements.floatArg(process, args, 0)))
                .setMetaData(Janitor.MetaData.HELP, "setRotationDegrees(degrees): Rotates the image by the given angle in degrees; returns the image.");
        DISPATCH_TABLE.addBuilderMethod("scaleAbsolute", (self, process, args) -> self.janitorGetHostValue().scaleAbsolute(PdfElements.floatArg(process, args, 0), PdfElements.floatArg(process, args, 1)))
                .setMetaData(Janitor.MetaData.HELP, "scaleAbsolute(width, height): Scales the image to exactly this width and height in points; returns the image.");
        DISPATCH_TABLE.addBuilderMethod("scalePercent", (self, process, args) -> {
            if (args.size() >= 2) {
                self.janitorGetHostValue().scalePercent(PdfElements.floatArg(process, args, 0), PdfElements.floatArg(process, args, 1));
            } else {
                self.janitorGetHostValue().scalePercent(PdfElements.floatArg(process, args, 0));
            }
        }).setMetaData(Janitor.MetaData.HELP, "scalePercent(percentX, percentY): Scales the image by a percentage of its original size; with one argument, both directions use it; returns the image.");
        DISPATCH_TABLE.addBuilderMethod("scaleToFit", (self, process, args) -> self.janitorGetHostValue().scaleToFit(PdfElements.floatArg(process, args, 0), PdfElements.floatArg(process, args, 1)))
                .setMetaData(Janitor.MetaData.HELP, "scaleToFit(width, height): Scales the image proportionally to fit into the given width and height in points; returns the image.");
        // TODO: setImageMask, setLayer, setTransparency, setSmask, setDpi, setCompressionLevel -- advanced/rarely scripted; add on request.
    }

    public JPDFImage(final @NotNull Image image) {
        super(DISPATCH_TABLE, image);
    }

    /**
     * @return the wrapped Image object
     */
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
        // A String argument is always treated as a local file name, never as a URL/URI.
        final String fileName = args.getRequiredStringValue(0);
        try {
            return new JPDFImage(Image.getInstance(fileName));
        } catch (BadElementException | IOException e) {
            throw new JanitorNativeException(process, "error loading image '" + fileName + "'", e);
        }
    }

}
