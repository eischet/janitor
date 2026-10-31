// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.docusign;

import com.docusign.esign.model.Recipients;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;

/** Wrapper for the DocuSign recipients. */
public class DocusignRecipients extends JanitorComposed<DocusignRecipients> {

    public static final DispatchTable<DocusignRecipients> DISPATCHER = new DispatchTable<>();

    static {
        DISPATCHER.addMethod("addSigner", (self, process, args) -> {
            DocusignSigner signer = args.getRequired(0, DocusignSigner.class);
            self.wrapped.addSignersItem(signer.getWrapped());
            return self;
        })
                .setMetaData(Janitor.MetaData.HELP, "addSigner(signer): Adds the signer and returns the recipients.");

        DISPATCHER.addMethod("addCarbonCopy", (self, process, args) -> {
            DocusignCarbonCopy carbonCopy = args.getRequired(0, DocusignCarbonCopy.class);
            self.wrapped.addCarbonCopiesItem(carbonCopy.getWrapped());
            return self;
        })
                .setMetaData(Janitor.MetaData.HELP, "addCarbonCopy(carbonCopy): Adds the carbon copy recipient and returns the recipients.");
    }

    private final Recipients wrapped;

    public DocusignRecipients() {
        this(new Recipients());
    }

    public DocusignRecipients(Recipients wrapped) {
        super(DISPATCHER);
        this.wrapped = wrapped;
    }

    /**
     * @return the wrapped DocuSign object
     */
    public Recipients getWrapped() {
        return wrapped;
    }

    @Override
    public String toString() {
        return "DocusignRecipients [wrapped=" + wrapped + "]";
    }

}
