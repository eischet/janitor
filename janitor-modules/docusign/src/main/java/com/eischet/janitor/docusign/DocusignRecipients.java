package com.eischet.janitor.docusign;

import com.docusign.esign.model.Recipients;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;

public class DocusignRecipients extends JanitorComposed<DocusignRecipients> {

    public static final DispatchTable<DocusignRecipients> DISPATCHER = new DispatchTable<>();

    static {
        DISPATCHER.addMethod("addSigner", (self, process, args) -> {
            DocusignSigner signer = args.getRequired(0, DocusignSigner.class);
            self.wrapped.addSignersItem(signer.getWrapped());
            return self;
        });

        DISPATCHER.addMethod("addCarbonCopy", (self, process, args) -> {
            DocusignCarbonCopy carbonCopy = args.getRequired(0, DocusignCarbonCopy.class);
            self.wrapped.addCarbonCopiesItem(carbonCopy.getWrapped());
            return self;
        });
    }

    private final Recipients wrapped;

    public DocusignRecipients() {
        this(new Recipients());
    }

    public DocusignRecipients(Recipients wrapped) {
        super(DISPATCHER);
        this.wrapped = wrapped;
    }

    public Recipients getWrapped() {
        return wrapped;
    }

    @Override
    public String toString() {
        return "DocusignRecipients [wrapped=" + wrapped + "]";
    }

}
