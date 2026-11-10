// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.pdf;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.errors.runtime.JanitorNativeException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import org.openpdf.text.Document;
import org.openpdf.text.ExceptionConverter;
import org.openpdf.text.pdf.PRStream;
import org.openpdf.text.pdf.PdfName;
import org.openpdf.text.pdf.PdfObject;
import org.openpdf.text.pdf.PdfPageEventHelper;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.PdfWriter;
import org.openpdf.text.pdf.PdfXConformanceException;

import java.awt.color.ColorSpace;
import java.awt.color.ICC_Profile;
import java.io.IOException;
import java.util.Locale;

/**
 * PDF/A support shared by Writer.setPdfA and concat: OpenPDF sets the PDF version and checks fonts and colors itself,
 * but it neither adds an output intent nor the XMP metadata, so this class does both.
 */
final class PdfA {

    private PdfA() {
    }

    /**
     * Switches a writer (or a PdfCopy) to PDF/A; it must not be open yet. The output intent is added when the document
     * opens, because it goes into the body, which only exists then. The XMP metadata is created when the document
     * closes, so that it matches the info dictionary with everything the script added (title, author, ...); PDF/A
     * requires both to agree.
     *
     * @param caller the script function, for error messages
     */
    static void enable(final JanitorScriptProcess process, final String caller, final PdfWriter writer, final String level) throws JanitorRuntimeException {
        final int conformance = switch (level.toUpperCase(Locale.ROOT)) {
            case "1B" -> PdfWriter.PDFA1B;
            case "2B" -> PdfWriter.PDFA2B;
            case "3B" -> PdfWriter.PDFA3B;
            default -> throw new JanitorArgumentException(process, caller + ": unsupported level '" + level + "', expected \"1B\", \"2B\" or \"3B\"");
        };
        try {
            writer.setPDFXConformance(conformance);
        } catch (PdfXConformanceException e) {
            throw new JanitorNativeException(process, caller + ": cannot switch to PDF/A " + level + " (the document must not be open yet)", e);
        }
        writer.setPageEvent(new PdfPageEventHelper() {
            @Override
            public void onOpenDocument(final PdfWriter openedWriter, final Document document) {
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

    /**
     * Removes the Interpolate flag from all images of a source document before its pages are copied. PDF/A forbids it,
     * and it is only a hint for viewers to smooth enlarged images; PDFs from Word on macOS set it, for example.
     *
     * @return the number of images changed
     */
    static int removeImageInterpolation(final PdfReader reader) {
        int changed = 0;
        for (int i = 1; i < reader.getXrefSize(); i++) {
            final PdfObject object = reader.getPdfObject(i);
            if (object instanceof PRStream stream && PdfName.IMAGE.equals(stream.get(PdfName.SUBTYPE)) && stream.contains(PdfName.INTERPOLATE)) {
                stream.remove(PdfName.INTERPOLATE);
                changed++;
            }
        }
        return changed;
    }

}
