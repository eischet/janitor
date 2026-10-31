// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.pdf;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorNativeException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.builtin.JBinary;
import com.eischet.janitor.api.types.functions.JCallArgs;
import com.eischet.janitor.api.Janitor;
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
        DISPATCH_TABLE.addIntegerProperty("numberOfPages", self -> self.janitorGetHostValue().getNumberOfPages())
                .setMetaData(Janitor.MetaData.HELP, "numberOfPages: The number of pages of the PDF; read-only.");
        DISPATCH_TABLE.addStringProperty("pdfVersion", self -> String.valueOf(self.janitorGetHostValue().getPdfVersion()))
                .setMetaData(Janitor.MetaData.HELP, "pdfVersion: The PDF version from the file header, e.g. \"4\" for PDF 1.4; read-only.");
        DISPATCH_TABLE.addBooleanProperty("encrypted", self -> self.janitorGetHostValue().isEncrypted())
                .setMetaData(Janitor.MetaData.HELP, "encrypted: True if the PDF is encrypted; read-only.");
        DISPATCH_TABLE.addBooleanProperty("rebuilt", self -> self.janitorGetHostValue().isRebuilt())
                .setMetaData(Janitor.MetaData.HELP, "rebuilt: True if the reader had to rebuild the file's cross-reference table, i.e. the PDF was damaged; read-only.");
        DISPATCH_TABLE.addVoidMethod("close", (self, process, args) -> self.janitorGetHostValue().close())
                .setMetaData(Janitor.MetaData.HELP, "close(): Closes the reader and releases the PDF.");
        // TODO: getPageSize(pageNumber) (needs a JPDFRectangle), getInfo() (document meta-data map), getPageContent(pageNumber) (raw bytes) -- not mapped yet.
    }

    public JPDFReader(final @NotNull PdfReader reader) {
        super(DISPATCH_TABLE, reader);
    }

    /**
     * @return the wrapped PdfReader object
     */
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
