package com.eischet.janitor.docusign;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.modules.JanitorModule;
import com.eischet.janitor.api.modules.JanitorModuleRegistration;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JMap;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.api.types.functions.JCallArgs;

public class DocusignModule extends JanitorComposed<DocusignModule> implements JanitorModule {

    public static final JanitorModuleRegistration REGISTRATION = new JanitorModuleRegistration("docusign", DocusignModule::new);

    private static final DispatchTable<DocusignModule> DISPATCHER = new DispatchTable<>(DocusignModule::new, false);

    static {
        DISPATCHER.addMethod("Signer", (self, process, args) -> enableApply(process, new DocusignSigner(), args));
        DISPATCHER.addMethod("CarbonCopy", (self, process, args) -> enableApply(process, new DocusignCarbonCopy(), args)    );
        DISPATCHER.addMethod("Tabs", (self, process, args) -> enableApply(process, new DocusignTabs(), args));
        DISPATCHER.addMethod("SignHere", (self, process, args) -> enableApply(process, new DocusignSignHere(), args));
        DISPATCHER.addMethod("Document", (self, process, args) -> enableApply(process, new DocusignDocument(), args));
        DISPATCHER.addMethod("Recipients", (self, process, args) -> enableApply(process, new DocusignRecipients(), args));
        DISPATCHER.addMethod("EnvelopeDefinition", (self, process, args) -> enableApply(process, new DocusignEnvelopeDefinition(), args));
        DISPATCHER.addMethod("Client", (self, process, args) -> enableApply(process, new DocusignApiClient(), args));
        DISPATCHER.addMethod("TemplateRole", (self, process, args) -> enableApply(process, new DocusignTemplateRole(), args));
    }

    /**
     * Allows passing a Map of key/values to the constructor of a JanitorObject.
     * @param process process
     * @param created JanitorObject instance
     * @param args Map of key/values
     * @return  the instance
     */
    public static JanitorObject enableApply(final JanitorScriptProcess process, final JanitorObject created, final JCallArgs args) throws JanitorRuntimeException {
        if (args.size() == 0) {
            return created;
        } else if (args.size() == 1) {
            args.getRequired(0, JMap.class).applyTo(process, created);
            return created;
        } else {
            throw new JanitorArgumentException(process, "Too many constructor arguments");
        }
    }

    public DocusignModule() {
        super(DISPATCHER);
    }

}
