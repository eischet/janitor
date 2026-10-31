// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.mustang;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.errors.runtime.JanitorNativeException;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import com.eischet.janitor.logging.JanitorLogger;
import org.jetbrains.annotations.Nullable;
import org.mustangproject.ZUGFeRD.IExportableTransaction;
import org.mustangproject.ZUGFeRD.IZUGFeRDExporter;
import org.mustangproject.ZUGFeRD.ZUGFeRDExporterFromA1;
import org.mustangproject.ZUGFeRD.ZUGFeRDExporterFromA3;

import java.io.IOException;

/** Wrapper for the Mustang ZUGFeRD exporter, which embeds invoice data into PDF files. */
public class MustangExporter extends JanitorWrapper<IZUGFeRDExporter> {

    public static final JanitorLogger log = JanitorLogger.getLogger(MustangExporter.class);

    public static WrapperDispatchTable<IZUGFeRDExporter> DISPATCH = new WrapperDispatchTable<>(MustangExporter::new);

    static {
        DISPATCH.addBuilderMethod("setProducer", (self, process, args) ->
            self.janitorGetHostValue().setProducer(args.require(1).getRequiredStringValue(0)))
                .setMetaData(Janitor.MetaData.HELP, "setProducer(producer): Sets the producer in the PDF's metadata. Returns this exporter.");
        DISPATCH.addBuilderMethod("setCreator", (self, process, args) ->
            self.janitorGetHostValue().setCreator(args.require(1).getRequiredStringValue(0)))
                .setMetaData(Janitor.MetaData.HELP, "setCreator(creator): Sets the creator in the PDF's metadata. Returns this exporter.");
        DISPATCH.addBuilderMethod("setZUGFeRDVersion", (self, process, args) ->
            self.janitorGetHostValue().setZUGFeRDVersion(args.require(1).getRequiredIntValue(0)))
                .setMetaData(Janitor.MetaData.HELP, "setZUGFeRDVersion(version): Sets the ZUGFeRD version to write, as a number. Returns this exporter.");
        // same but less room for typing errors:
        DISPATCH.addBuilderMethod("setVersion", (self, process, args) ->
            self.janitorGetHostValue().setZUGFeRDVersion(args.require(1).getRequiredIntValue(0)))
                .setMetaData(Janitor.MetaData.HELP, "setVersion(version): Same as setZUGFeRDVersion(version). Returns this exporter.");
        DISPATCH.addBuilderMethod("ignorePDFAErrors", (self, process, args) -> {
            if (self.janitorGetHostValue() instanceof ZUGFeRDExporterFromA3 a3) {
                a3.ignorePDFAErrors();
            } else {
                log.warn("ignorePDFAErrors called on non-ZUGFeRDExporterFromA3 exporter");
            }
        }).setMetaData(Janitor.MetaData.HELP, "ignorePDFAErrors(): Accepts a source PDF that is not valid PDF/A; only works for exporters from PDF/A-3, otherwise it only logs a warning. Returns this exporter.");
        DISPATCH.addBuilderMethod("load", (self, process, args) -> {
            try {
                self.janitorGetHostValue().load(args.require(1).getRequiredStringValue(0));
            } catch (IOException e) {
                throw new JanitorNativeException(process, "error loading file '"+args.get(0).janitorToString()+"'", e);
            }
        }).setMetaData(Janitor.MetaData.HELP, "load(path): Loads the PDF file to embed the invoice into. Returns this exporter.");
        DISPATCH.addBuilderMethod("setTransaction", (self, process, args) -> {
            @Nullable final Object transaction = args.require(1).get(0).janitorGetHostValue();
            try {
                if (transaction instanceof IExportableTransaction transporter) {
                    self.janitorGetHostValue().setTransaction(transporter);
                } else {
                    throw new JanitorNativeException(process, "setTransaction called with non-IExporter object: " + transaction, null);
                }
            } catch (IOException e) {
                throw new JanitorNativeException(process, "setTransaction failed with: " + transaction, e);
            }
        }).setMetaData(Janitor.MetaData.HELP, "setTransaction(invoice): Sets the invoice whose data is embedded into the PDF. Returns this exporter.");
        DISPATCH.addVoidMethod("export", (self, process, args) -> {
            try {
                self.janitorGetHostValue().export(args.getRequiredStringValue(0));
            } catch (IOException e) {
                throw new JanitorNativeException(process, "export failed", e);
            }
        }).setMetaData(Janitor.MetaData.HELP, "export(path): Writes the PDF with the embedded invoice data to a file.");

        DISPATCH.addBuilderMethod("setProfile", (self, process, args) -> {
            final String profileName = args.getRequiredStringValue(0);
            if (self.janitorGetHostValue() instanceof ZUGFeRDExporterFromA1 a1) {
                a1.setProfile(profileName);
            } else if (self.janitorGetHostValue() instanceof ZUGFeRDExporterFromA3 a3) {
                a3.setProfile(profileName);
            } else {
                log.warn("setProfile called on unsupported exporter type {}", self.janitorGetHostValue().getClass());
            }
        }).setMetaData(Janitor.MetaData.HELP, "setProfile(profileName): Sets the ZUGFeRD/Factur-X profile by name; on other exporter types, it only logs a warning. Returns this exporter.");
        DISPATCH.addBuilderMethod("disableAutoClose", (self, process, args) ->
            self.janitorGetHostValue().disableAutoClose(args.getRequiredBooleanValue(0)))
                .setMetaData(Janitor.MetaData.HELP, "disableAutoClose(yes): If true, the PDF is not closed after export, so close() must be called. Returns this exporter.");
        DISPATCH.addBuilderMethod("disableFacturX", (self, process, args) -> self.janitorGetHostValue().disableFacturX());
        DISPATCH.addBuilderMethod("setFacturX", (self, process, args) -> {
            if (self.janitorGetHostValue() instanceof ZUGFeRDExporterFromA3 a3) {
                a3.setFacturX();
            } else {
                log.warn("setFacturX called on non-ZUGFeRDExporterFromA3 exporter");
            }
        });
        DISPATCH.addBuilderMethod("attachFile", (self, process, args) ->
            self.janitorGetHostValue().attachFile(args.getRequired(0, MustangFileAttachment.class).janitorGetHostValue()))
                .setMetaData(Janitor.MetaData.HELP, "attachFile(attachment): Attaches an additional file to the PDF. Returns this exporter.");
        DISPATCH.addVoidMethod("close", (self, process, args) -> {
            try {
                self.janitorGetHostValue().close();
            } catch (IOException e) {
                throw new JanitorNativeException(process, "error closing exporter", e);
            }
        }).setMetaData(Janitor.MetaData.HELP, "close(): Closes the exporter and its PDF.");

    }

    public MustangExporter() {
        super(DISPATCH, new ZUGFeRDExporterFromA1());
    }

    public MustangExporter(final IZUGFeRDExporter exporter) {
        super(DISPATCH, exporter);
    }

}
