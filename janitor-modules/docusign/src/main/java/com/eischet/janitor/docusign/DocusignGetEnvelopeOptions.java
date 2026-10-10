// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.docusign;

import com.docusign.esign.api.EnvelopesApi;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;

/** Wrapper for the DocuSign get envelope options. */
public class DocusignGetEnvelopeOptions extends JanitorComposed<DocusignGetEnvelopeOptions> {
    public static DispatchTable<DocusignGetEnvelopeOptions> DISPATCH = new  DispatchTable<>();

    protected final EnvelopesApi.GetEnvelopeOptions wrapped;

    static {
        DISPATCH.addStringProperty("advancedUpdate", self -> self.wrapped.getAdvancedUpdate(), (self, value) -> self.wrapped.setAdvancedUpdate(value));
        DISPATCH.addStringProperty("include", self -> self.wrapped.getInclude(), (self, value) -> self.wrapped.setInclude(value));
        DISPATCH.addStringProperty("includeAnchorTabLocations", self -> self.wrapped.getIncludeAnchorTabLocations(), (self, value) -> self.wrapped.setIncludeAnchorTabLocations(value));
    }

    public DocusignGetEnvelopeOptions(final EnvelopesApi.GetEnvelopeOptions wrapped) {
        super(DISPATCH);
        this.wrapped = wrapped;
    }

    /**
     * @return the wrapped DocuSign options
     */
    public EnvelopesApi.GetEnvelopeOptions getWrapped() {
        return wrapped;
    }

}
