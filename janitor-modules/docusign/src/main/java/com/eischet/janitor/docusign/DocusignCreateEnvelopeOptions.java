package com.eischet.janitor.docusign;

import com.docusign.esign.api.EnvelopesApi;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;

public class DocusignCreateEnvelopeOptions extends JanitorComposed<DocusignEnvelopeDefinition> {

    public static final DispatchTable<DocusignEnvelopeDefinition> DISPATCHER = new DispatchTable<>();

    protected final EnvelopesApi.CreateEnvelopeOptions wrapped;

    /**
     * Create a new JanitorComposed.
     *
     * @param options the options
     */
    public DocusignCreateEnvelopeOptions(final EnvelopesApi.CreateEnvelopeOptions options) {
        super(DISPATCHER);
        this.wrapped = options;
    }

}
