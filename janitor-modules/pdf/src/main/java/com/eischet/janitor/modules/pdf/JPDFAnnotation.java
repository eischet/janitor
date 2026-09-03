package com.eischet.janitor.modules.pdf;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.functions.JCallArgs;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.jetbrains.annotations.NotNull;
import org.openpdf.text.Annotation;

/**
 * Wraps org.openpdf.text.Annotation. OpenPDF has many Annotation(...) constructors (popup text notes,
 * URL links, file attachments, named actions, ...); only the two most common ones (a popup text note,
 * and a URL link over a rectangular area) are mapped as pdf.Annotation(...) factories below.
 * TODO: the remaining Annotation(llx, lly, urx, ury, file, ...) / Annotation(..., int named) / Annotation(..., PdfAction) constructors are not mapped yet.
 */
public class JPDFAnnotation extends JanitorWrapper<Annotation> {

    public static final WrapperDispatchTable<Annotation> DISPATCH_TABLE = new WrapperDispatchTable<>();

    static {
        DISPATCH_TABLE.addStringProperty("title", self -> self.janitorGetHostValue().title());
        DISPATCH_TABLE.addStringProperty("content", self -> self.janitorGetHostValue().content());
        DISPATCH_TABLE.addDoubleProperty("llx", self -> self.janitorGetHostValue().llx());
        DISPATCH_TABLE.addDoubleProperty("lly", self -> self.janitorGetHostValue().lly());
        DISPATCH_TABLE.addDoubleProperty("urx", self -> self.janitorGetHostValue().urx());
        DISPATCH_TABLE.addDoubleProperty("ury", self -> self.janitorGetHostValue().ury());
        DISPATCH_TABLE.addBuilderMethod("setDimensions", (self, process, args) -> self.janitorGetHostValue().setDimensions(
                PdfElements.floatArg(process, args, 0), PdfElements.floatArg(process, args, 1), PdfElements.floatArg(process, args, 2), PdfElements.floatArg(process, args, 3)));
    }

    public JPDFAnnotation(final @NotNull Annotation annotation) {
        super(DISPATCH_TABLE, annotation);
    }

    public JPDFAnnotation(final @NotNull String title, final @NotNull String text) {
        super(DISPATCH_TABLE, new Annotation(title, text));
    }

    public JPDFAnnotation(final float llx, final float lly, final float urx, final float ury, final @NotNull String url) {
        super(DISPATCH_TABLE, new Annotation(llx, lly, urx, ury, url));
    }

    public Annotation getAnnotation() {
        return wrapped;
    }

    static JPDFAnnotation fromArgs(final JanitorScriptProcess process, final JCallArgs args) throws JanitorRuntimeException {
        return switch (args.size()) {
            case 2 -> new JPDFAnnotation(args.getRequiredStringValue(0), args.getRequiredStringValue(1));
            case 5 -> new JPDFAnnotation(PdfElements.floatArg(process, args, 0), PdfElements.floatArg(process, args, 1), PdfElements.floatArg(process, args, 2), PdfElements.floatArg(process, args, 3), args.getRequiredStringValue(4));
            default -> throw new JanitorArgumentException(process, "Annotation() takes 2 (title, text) or 5 (llx, lly, urx, ury, url) arguments");
        };
    }

}
