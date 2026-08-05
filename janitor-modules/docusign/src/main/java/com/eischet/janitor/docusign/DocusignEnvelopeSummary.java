package com.eischet.janitor.docusign;

import com.docusign.esign.model.EnvelopeSummary;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.api.types.dispatch.Dispatcher;

public class DocusignEnvelopeSummary extends JanitorComposed<DocusignEnvelopeSummary> {

    public static final DispatchTable<DocusignEnvelopeSummary> DISPATCHER = new DispatchTable<>();

    static {
        // String properties
        DISPATCHER.addStringProperty("envelopeId", self -> self.envelopeSummary.getEnvelopeId(), (self, value) -> self.envelopeSummary.setEnvelopeId(value));
        DISPATCHER.addStringProperty("recipientSigningUri", self -> self.envelopeSummary.getRecipientSigningUri(), (self, value) -> self.envelopeSummary.setRecipientSigningUri(value));
        DISPATCHER.addStringProperty("recipientSigningUriError", self -> self.envelopeSummary.getRecipientSigningUriError(), (self, value) -> self.envelopeSummary.setRecipientSigningUriError(value));
        DISPATCHER.addStringProperty("status", self -> self.envelopeSummary.getStatus(), (self, value) -> self.envelopeSummary.setStatus(value));
        DISPATCHER.addStringProperty("statusDateTime", self -> self.envelopeSummary.getStatusDateTime(), (self, value) -> self.envelopeSummary.setStatusDateTime(value));
        DISPATCHER.addStringProperty("uri", self -> self.envelopeSummary.getUri(), (self, value) -> self.envelopeSummary.setUri(value));

        // Object properties
        DISPATCHER.addObjectProperty("bulkEnvelopeStatus",
                self -> self.envelopeSummary.getBulkEnvelopeStatus() != null ? new DocusignBulkEnvelopeStatus(self.envelopeSummary.getBulkEnvelopeStatus()) : null,
                (self, value) -> self.envelopeSummary.setBulkEnvelopeStatus(value != null ? ((DocusignBulkEnvelopeStatus) value).getWrapped() : null),
                DocusignBulkEnvelopeStatus::new);

        DISPATCHER.addObjectProperty("errorDetails",
                self -> self.envelopeSummary.getErrorDetails() != null ? new DocusignErrorDetails(self.envelopeSummary.getErrorDetails()) : null,
                (self, value) -> self.envelopeSummary.setErrorDetails(value != null ? ((DocusignErrorDetails) value).getWrapped() : null),
                DocusignErrorDetails::new);
    }

    protected final EnvelopeSummary envelopeSummary;

    public DocusignEnvelopeSummary(final EnvelopeSummary envelopeSummary) {
        super(DISPATCHER);
        this.envelopeSummary = envelopeSummary;
    }

    public EnvelopeSummary getEnvelopeSummary() {
        return envelopeSummary;
    }

}
