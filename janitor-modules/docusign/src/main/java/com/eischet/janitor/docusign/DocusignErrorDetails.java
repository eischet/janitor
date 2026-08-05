package com.eischet.janitor.docusign;

import com.docusign.esign.model.ErrorDetails;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;

public class DocusignErrorDetails extends JanitorComposed<DocusignErrorDetails> {

    public static final DispatchTable<DocusignErrorDetails> DISPATCHER = new DispatchTable<>();

    static {
        // String properties
        DISPATCHER.addStringProperty("errorCode", self -> self.wrapped.getErrorCode(), (self, value) -> self.wrapped.setErrorCode(value));
        DISPATCHER.addStringProperty("message", self -> self.wrapped.getMessage(), (self, value) -> self.wrapped.setMessage(value));
    }

    private final ErrorDetails wrapped;

    public DocusignErrorDetails() {
        super(DISPATCHER);
        this.wrapped = new ErrorDetails();
    }

    public DocusignErrorDetails(final ErrorDetails wrapped) {
        super(DISPATCHER);
        this.wrapped = wrapped;
    }

    public ErrorDetails getWrapped() {
        return wrapped;
    }

    @Override
    public String toString() {
        return "DocusignErrorDetails [wrapped=" + wrapped + "]";
    }
}
