package com.eischet.janitor.modules.pdf;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.errors.runtime.JanitorNativeException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JBinary;
import com.eischet.janitor.api.types.functions.JCallArgs;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.jetbrains.annotations.NotNull;
import org.openpdf.text.DocumentException;
import org.openpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * Wraps org.openpdf.text.pdf.PdfWriter, the DocListener that turns Document.add(...) calls into actual
 * PDF bytes.
 * <p>
 * Like the "files"/"os" modules, writing to an arbitrary filesystem path is a privileged capability:
 * pdf.Writer(document, "some/path.pdf") writes directly to that path with no sandboxing at all. Hosts
 * that don't want that should either not register this module, or prefer pdf.Writer(document) (no path),
 * which writes into memory and exposes the finished PDF via the read-only "bytes" property once the
 * document has been closed.
 */
public class JPDFWriter extends JanitorWrapper<PdfWriter> {

    public static final WrapperDispatchTable<PdfWriter> DISPATCH_TABLE = new WrapperDispatchTable<>();

    static {
        DISPATCH_TABLE.addVoidMethod("close", (self, process, args) -> self.janitorGetHostValue().close());
        DISPATCH_TABLE.addBuilderMethod("addJavaScript", (self, process, args) -> self.janitorGetHostValue().addJavaScript(args.getRequiredStringValue(0)));
        DISPATCH_TABLE.addObjectProperty("bytes", self -> {
            if (!(self instanceof JPDFWriter jpdfWriter)) {
                return null;
            }
            final byte[] bytes = jpdfWriter.getBufferedBytes();
            return bytes == null ? null : com.eischet.janitor.api.Janitor.binary(bytes);
        });
        // TODO: getDirectContent()/getDirectContentUnder() -- would need a PdfContentByte wrapper for low-level canvas drawing (lines, shapes, raw text positioning). Not mapped yet.
        // TODO: setPageEvent(PdfPageEvent) -- would need a script-side callback bridge. Not mapped yet.
        // TODO: encryption (setEncryption), PDF/A, digital signatures -- not mapped yet.
    }

    private final @org.jetbrains.annotations.Nullable ByteArrayOutputStream buffer;

    private JPDFWriter(final @NotNull PdfWriter writer, final @org.jetbrains.annotations.Nullable ByteArrayOutputStream buffer) {
        super(DISPATCH_TABLE, writer);
        this.buffer = buffer;
    }

    public PdfWriter getWriter() {
        return wrapped;
    }

    public byte @org.jetbrains.annotations.Nullable [] getBufferedBytes() {
        return buffer == null ? null : buffer.toByteArray();
    }

    static JPDFWriter fromArgs(final JanitorScriptProcess process, final JCallArgs args) throws JanitorRuntimeException {
        args.require(1, 2);
        final JPDFDocument document = args.getRequired(0, JPDFDocument.class);
        try {
            if (args.size() == 2) {
                final String path = args.getRequiredStringValue(1);
                final OutputStream out = new FileOutputStream(path);
                return new JPDFWriter(PdfWriter.getInstance(document.getDocument(), out), null);
            } else {
                final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                return new JPDFWriter(PdfWriter.getInstance(document.getDocument(), buffer), buffer);
            }
        } catch (DocumentException | FileNotFoundException e) {
            throw new JanitorNativeException(process, "error creating pdf writer", e);
        }
    }

}
