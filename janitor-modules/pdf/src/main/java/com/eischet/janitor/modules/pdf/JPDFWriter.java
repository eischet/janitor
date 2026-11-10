// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.pdf;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.errors.runtime.JanitorNativeException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JBinary;
import com.eischet.janitor.api.types.functions.JCallArgs;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.jetbrains.annotations.NotNull;
import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.ExceptionConverter;
import org.openpdf.text.pdf.PdfPageEventHelper;
import org.openpdf.text.pdf.PdfWriter;
import org.openpdf.text.pdf.PdfXConformanceException;

import java.awt.color.ColorSpace;
import java.awt.color.ICC_Profile;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
        DISPATCH_TABLE.addVoidMethod("close", (self, process, args) -> self.janitorGetHostValue().close())
                .setMetaData(Janitor.MetaData.HELP, "close(): Closes the writer.");
        DISPATCH_TABLE.addBuilderMethod("addJavaScript", (self, process, args) -> self.janitorGetHostValue().addJavaScript(args.getRequiredStringValue(0)))
                .setMetaData(Janitor.MetaData.HELP, "addJavaScript(code): Adds document-level JavaScript code to the PDF; returns the writer.");
        DISPATCH_TABLE.addObjectProperty("bytes", self -> {
            if (!(self instanceof JPDFWriter jpdfWriter)) {
                return null;
            }
            final byte[] bytes = jpdfWriter.getBufferedBytes();
            return bytes == null ? null : com.eischet.janitor.api.Janitor.binary(bytes);
        }).setMetaData(Janitor.MetaData.HELP, "bytes: The finished PDF as binary for a writer created without a path, available once the document has been closed; null for a writer that writes to a file; read-only.");
        DISPATCH_TABLE.addBuilderMethod("setPdfA", (self, process, args) -> enablePdfA(process, self.janitorGetHostValue(), args.require(1).getRequiredStringValue(0)))
                .setMetaData(Janitor.MetaData.HELP, "setPdfA(level): Makes the writer produce PDF/A, level \"1B\", \"2B\" or \"3B\", with the matching PDF version, an sRGB output intent and XMP metadata; call it before document.open(), and use only embedded fonts (BaseFont with embedded true). Returns the writer.");
        // TODO: getDirectContent()/getDirectContentUnder() -- would need a PdfContentByte wrapper for low-level canvas drawing (lines, shapes, raw text positioning). Not mapped yet.
        // TODO: setPageEvent(PdfPageEvent) -- would need a script-side callback bridge. Not mapped yet.
        // TODO: encryption (setEncryption), PDF/A levels A and U (need a tagged structure), digital signatures -- not mapped yet.
    }

    private final @org.jetbrains.annotations.Nullable ByteArrayOutputStream buffer;

    private JPDFWriter(final @NotNull PdfWriter writer, final @org.jetbrains.annotations.Nullable ByteArrayOutputStream buffer) {
        super(DISPATCH_TABLE, writer);
        this.buffer = buffer;
    }

    /**
     * @return the wrapped PdfWriter object
     */
    public PdfWriter getWriter() {
        return wrapped;
    }

    /**
     * @return the bytes that the writer buffered, or null if it does not buffer
     */
    public byte @org.jetbrains.annotations.Nullable [] getBufferedBytes() {
        return buffer == null ? null : buffer.toByteArray();
    }

    /**
     * Switches the writer to PDF/A. OpenPDF sets the PDF version and checks fonts and colors itself, but it neither adds
     * an output intent nor the XMP metadata. The metadata is created when the document closes, so that it matches the
     * info dictionary with everything the script added (title, author, ...); PDF/A requires both to agree.
     */
    private static void enablePdfA(final JanitorScriptProcess process, final PdfWriter writer, final String level) throws JanitorRuntimeException {
        final int conformance = switch (level.toUpperCase(java.util.Locale.ROOT)) {
            case "1B" -> PdfWriter.PDFA1B;
            case "2B" -> PdfWriter.PDFA2B;
            case "3B" -> PdfWriter.PDFA3B;
            default -> throw new JanitorArgumentException(process, "setPdfA: unsupported level '" + level + "', expected \"1B\", \"2B\" or \"3B\"");
        };
        try {
            writer.setPDFXConformance(conformance);
        } catch (PdfXConformanceException e) {
            throw new JanitorNativeException(process, "setPdfA: cannot switch the writer to PDF/A " + level + " (it must be called before document.open())", e);
        }
        writer.setPageEvent(new PdfPageEventHelper() {
            @Override
            public void onOpenDocument(final PdfWriter openedWriter, final Document document) {
                // the output intent goes into the body, which only exists once the writer is open
                try {
                    openedWriter.setOutputIntents("Custom", "", "http://www.color.org", "sRGB IEC61966-2.1",
                            ICC_Profile.getInstance(ColorSpace.CS_sRGB));
                } catch (IOException e) {
                    throw new ExceptionConverter(e);
                }
            }

            @Override
            public void onCloseDocument(final PdfWriter closingWriter, final Document document) {
                closingWriter.createXmpMetadata();
            }
        });
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
