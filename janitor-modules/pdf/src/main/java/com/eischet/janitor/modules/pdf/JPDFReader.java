package com.eischet.janitor.modules.pdf;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorNativeException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.builtin.JBinary;
import com.eischet.janitor.api.types.functions.JCallArgs;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.jetbrains.annotations.NotNull;
import org.openpdf.text.pdf.PdfReader;

import java.io.IOException;

/**
 * Wraps org.openpdf.text.pdf.PdfReader for reading an existing PDF's basic structure (page count, version, ...).
 * Like pdf.Writer(document, path), reading an arbitrary filesystem path is a privileged, unsandboxed capability.
 */
public class JPDFReader extends JanitorWrapper<PdfReader> {

    public static final WrapperDispatchTable<PdfReader> DISPATCH_TABLE = new WrapperDispatchTable<>();

    static {
        DISPATCH_TABLE.addIntegerProperty("numberOfPages", self -> self.janitorGetHostValue().getNumberOfPages());
        DISPATCH_TABLE.addStringProperty("pdfVersion", self -> String.valueOf(self.janitorGetHostValue().getPdfVersion()));
        DISPATCH_TABLE.addBooleanProperty("encrypted", self -> self.janitorGetHostValue().isEncrypted());
        DISPATCH_TABLE.addBooleanProperty("rebuilt", self -> self.janitorGetHostValue().isRebuilt());
        DISPATCH_TABLE.addVoidMethod("close", (self, process, args) -> self.janitorGetHostValue().close());
        // TODO: getPageSize(pageNumber) (needs a JPDFRectangle), getInfo() (document meta-data map), getPageContent(pageNumber) (raw bytes) -- not mapped yet.
    }

    public JPDFReader(final @NotNull PdfReader reader) {
        super(DISPATCH_TABLE, reader);
    }

    public PdfReader getReader() {
        return wrapped;
    }

    static JPDFReader fromArgs(final JanitorScriptProcess process, final JCallArgs args) throws JanitorRuntimeException {
        args.require(1);
        try {
            if (args.get(0) instanceof JBinary binary) {
                return new JPDFReader(new PdfReader(binary.janitorGetHostValue()));
            }
            return new JPDFReader(new PdfReader(args.getRequiredStringValue(0)));
        } catch (IOException e) {
            throw new JanitorNativeException(process, "error reading pdf", e);
        }
    }

}
