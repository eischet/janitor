package com.eischet.janitor.modules.pdf;

import com.eischet.janitor.api.modules.JanitorModule;
import com.eischet.janitor.api.modules.JanitorModuleRegistration;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;

public class PdfModule extends JanitorComposed<PdfModule> implements JanitorModule  {

    public static final DispatchTable<PdfModule> dispatcher = new DispatchTable<>(PdfModule::new, false);

    public static final JanitorModuleRegistration REGISTRATION = new JanitorModuleRegistration("pdf", PdfModule::new);

    static {
        dispatcher.addMethod("Document", (self, process, args) -> new JPDFDocument());
    }

    public PdfModule() {
        super(dispatcher);
    }



}
