// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.docusign;

import com.eischet.janitor.api.Janitor;
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

/** The script module "docusign", which offers the factories for DocuSign objects such as signers, tabs and envelope definitions. */
public class DocusignModule extends JanitorComposed<DocusignModule> implements JanitorModule {

    public static final JanitorModuleRegistration REGISTRATION = new JanitorModuleRegistration("docusign", DocusignModule::new);

    private static final DispatchTable<DocusignModule> DISPATCHER = new DispatchTable<>(DocusignModule::new, false);

    static {
        DISPATCHER.addMethod("Signer", (self, process, args) -> enableApply(process, new DocusignSigner(), args))
                .setMetaData(Janitor.MetaData.HELP, "Signer(properties): Creates a new signer recipient; an optional map argument is applied to its properties.");
        DISPATCHER.addMethod("CarbonCopy", (self, process, args) -> enableApply(process, new DocusignCarbonCopy(), args)    )
                .setMetaData(Janitor.MetaData.HELP, "CarbonCopy(properties): Creates a new carbon copy recipient; an optional map argument is applied to its properties.");
        DISPATCHER.addMethod("Tabs", (self, process, args) -> enableApply(process, new DocusignTabs(), args))
                .setMetaData(Janitor.MetaData.HELP, "Tabs(properties): Creates a new, empty set of tabs; an optional map argument is applied to its properties.");
        DISPATCHER.addMethod("SignHere", (self, process, args) -> enableApply(process, new DocusignSignHere(), args))
                .setMetaData(Janitor.MetaData.HELP, "SignHere(properties): Creates a new sign here tab; an optional map argument is applied to its properties.");
        DISPATCHER.addMethod("Document", (self, process, args) -> enableApply(process, new DocusignDocument(), args))
                .setMetaData(Janitor.MetaData.HELP, "Document(properties): Creates a new document for an envelope; an optional map argument is applied to its properties.");
        DISPATCHER.addMethod("Recipients", (self, process, args) -> enableApply(process, new DocusignRecipients(), args))
                .setMetaData(Janitor.MetaData.HELP, "Recipients(properties): Creates a new, empty set of recipients; an optional map argument is applied to its properties.");
        DISPATCHER.addMethod("EnvelopeDefinition", (self, process, args) -> enableApply(process, new DocusignEnvelopeDefinition(), args))
                .setMetaData(Janitor.MetaData.HELP, "EnvelopeDefinition(properties): Creates a new envelope definition; an optional map argument is applied to its properties.");
        DISPATCHER.addMethod("Client", (self, process, args) -> enableApply(process, new DocusignApiClient(), args))
                .setMetaData(Janitor.MetaData.HELP, "Client(properties): Creates a new DocuSign API client; an optional map argument is applied to its properties.");
        DISPATCHER.addMethod("TemplateRole", (self, process, args) -> enableApply(process, new DocusignTemplateRole(), args))
                .setMetaData(Janitor.MetaData.HELP, "TemplateRole(properties): Creates a new template role; an optional map argument is applied to its properties.");
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
