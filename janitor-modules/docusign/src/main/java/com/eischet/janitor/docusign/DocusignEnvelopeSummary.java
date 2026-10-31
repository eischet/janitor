// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.docusign;

import com.docusign.esign.model.EnvelopeSummary;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.api.types.dispatch.Dispatcher;

/** Wrapper for the summary that DocuSign returns after an envelope was created. */
public class DocusignEnvelopeSummary extends JanitorComposed<DocusignEnvelopeSummary> {

    public static final DispatchTable<DocusignEnvelopeSummary> DISPATCHER = new DispatchTable<>();

    static {
        // String properties
        DISPATCHER.addStringProperty("envelopeId", self -> self.envelopeSummary.getEnvelopeId(), (self, value) -> self.envelopeSummary.setEnvelopeId(value))
                .setMetaData(Janitor.MetaData.HELP, "envelopeId: The ID of the created envelope; writable.");
        DISPATCHER.addStringProperty("recipientSigningUri", self -> self.envelopeSummary.getRecipientSigningUri(), (self, value) -> self.envelopeSummary.setRecipientSigningUri(value));
        DISPATCHER.addStringProperty("recipientSigningUriError", self -> self.envelopeSummary.getRecipientSigningUriError(), (self, value) -> self.envelopeSummary.setRecipientSigningUriError(value));
        DISPATCHER.addStringProperty("status", self -> self.envelopeSummary.getStatus(), (self, value) -> self.envelopeSummary.setStatus(value))
                .setMetaData(Janitor.MetaData.HELP, "status: The status of the envelope, e.g. \"sent\" or \"created\"; writable.");
        DISPATCHER.addStringProperty("statusDateTime", self -> self.envelopeSummary.getStatusDateTime(), (self, value) -> self.envelopeSummary.setStatusDateTime(value))
                .setMetaData(Janitor.MetaData.HELP, "statusDateTime: The date and time the envelope reached its status, as a string; writable.");
        DISPATCHER.addStringProperty("uri", self -> self.envelopeSummary.getUri(), (self, value) -> self.envelopeSummary.setUri(value))
                .setMetaData(Janitor.MetaData.HELP, "uri: The API URI of the envelope; writable.");

        // Object properties
        DISPATCHER.addObjectProperty("bulkEnvelopeStatus",
                self -> self.envelopeSummary.getBulkEnvelopeStatus() != null ? new DocusignBulkEnvelopeStatus(self.envelopeSummary.getBulkEnvelopeStatus()) : null,
                (self, value) -> self.envelopeSummary.setBulkEnvelopeStatus(value != null ? ((DocusignBulkEnvelopeStatus) value).getWrapped() : null),
                DocusignBulkEnvelopeStatus::new)
                .setMetaData(Janitor.MetaData.HELP, "bulkEnvelopeStatus: The status of a bulk send, or null if there is none; writable.");

        DISPATCHER.addObjectProperty("errorDetails",
                self -> self.envelopeSummary.getErrorDetails() != null ? new DocusignErrorDetails(self.envelopeSummary.getErrorDetails()) : null,
                (self, value) -> self.envelopeSummary.setErrorDetails(value != null ? ((DocusignErrorDetails) value).getWrapped() : null),
                DocusignErrorDetails::new)
                .setMetaData(Janitor.MetaData.HELP, "errorDetails: The details of an error, or null if there is none; writable.");
    }

    protected final EnvelopeSummary envelopeSummary;

    public DocusignEnvelopeSummary(final EnvelopeSummary envelopeSummary) {
        super(DISPATCHER);
        this.envelopeSummary = envelopeSummary;
    }

    /**
     * @return the wrapped DocuSign envelope summary
     */
    public EnvelopeSummary getEnvelopeSummary() {
        return envelopeSummary;
    }

}
