package com.eischet.janitor.modules.mustang;

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

public class MustangExporter extends JanitorWrapper<IZUGFeRDExporter> {

    public static final JanitorLogger log = JanitorLogger.getLogger(MustangExporter.class);

    public static WrapperDispatchTable<IZUGFeRDExporter> DISPATCH = new WrapperDispatchTable<>(MustangExporter::new);

    static {
        DISPATCH.addBuilderMethod("setProducer", (self, process, args) ->
            self.janitorGetHostValue().setProducer(args.require(1).getRequiredStringValue(0)));
        DISPATCH.addStringProperty("producer", self -> null, (self, value) -> self.janitorGetHostValue().setProducer(value));
        DISPATCH.addBuilderMethod("setCreator", (self, process, args) ->
            self.janitorGetHostValue().setCreator(args.require(1).getRequiredStringValue(0)));
        DISPATCH.addBuilderMethod("setZUGFeRDVersion", (self, process, args) ->
            self.janitorGetHostValue().setZUGFeRDVersion(args.require(1).getRequiredIntValue(0)));
        // same but less room for typing errors:
        DISPATCH.addBuilderMethod("setVersion", (self, process, args) ->
            self.janitorGetHostValue().setZUGFeRDVersion(args.require(1).getRequiredIntValue(0)));
        DISPATCH.addBuilderMethod("ignorePDFAErrors", (self, process, args) -> {
            if (self.janitorGetHostValue() instanceof ZUGFeRDExporterFromA3 a3) {
                a3.ignorePDFAErrors();
            } else {
                log.warn("ignorePDFAErrors called on non-ZUGFeRDExporterFromA3 exporter");
            }
        });
        DISPATCH.addBuilderMethod("load", (self, process, args) -> {
            try {
                self.janitorGetHostValue().load(args.require(1).getRequiredStringValue(0));
            } catch (IOException e) {
                throw new JanitorNativeException(process, "error loading file '"+args.get(0).janitorToString()+"'", e);
            }
        });
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
        });
        DISPATCH.addVoidMethod("export", (self, process, args) -> {
            try {
                self.janitorGetHostValue().export(args.getRequiredStringValue(0));
            } catch (IOException e) {
                throw new JanitorNativeException(process, "export failed", e);
            }
        });

    }

    public MustangExporter() {
        super(DISPATCH, new ZUGFeRDExporterFromA1());
    }

    public MustangExporter(final IZUGFeRDExporter exporter) {
        super(DISPATCH, exporter);
    }

}
