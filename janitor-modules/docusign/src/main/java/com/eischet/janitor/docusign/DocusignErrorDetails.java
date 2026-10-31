// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.docusign;

import com.docusign.esign.model.ErrorDetails;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;

/** Wrapper for the DocuSign error details. */
public class DocusignErrorDetails extends JanitorComposed<DocusignErrorDetails> {

    public static final DispatchTable<DocusignErrorDetails> DISPATCHER = new DispatchTable<>();

    static {
        // String properties
        DISPATCHER.addStringProperty("errorCode", self -> self.wrapped.getErrorCode(), (self, value) -> self.wrapped.setErrorCode(value))
                .setMetaData(Janitor.MetaData.HELP, "errorCode: The code of the error; writable.");
        DISPATCHER.addStringProperty("message", self -> self.wrapped.getMessage(), (self, value) -> self.wrapped.setMessage(value))
                .setMetaData(Janitor.MetaData.HELP, "message: The description of the error; writable.");
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

    /**
     * @return the wrapped DocuSign object
     */
    public ErrorDetails getWrapped() {
        return wrapped;
    }

    @Override
    public String toString() {
        return "DocusignErrorDetails [wrapped=" + wrapped + "]";
    }
}
