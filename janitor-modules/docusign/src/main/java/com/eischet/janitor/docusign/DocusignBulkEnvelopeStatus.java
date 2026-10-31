// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.docusign;

import com.docusign.esign.model.BulkEnvelopeStatus;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;

/** Wrapper for the DocuSign bulk envelope status. */
public class DocusignBulkEnvelopeStatus extends JanitorComposed<DocusignBulkEnvelopeStatus> {

    public static final DispatchTable<DocusignBulkEnvelopeStatus> DISPATCHER = new DispatchTable<>();

    static {
        // String properties
        DISPATCHER.addStringProperty("batchId", self -> self.wrapped.getBatchId(), (self, value) -> self.wrapped.setBatchId(value))
                .setMetaData(Janitor.MetaData.HELP, "batchId: The ID of the bulk send batch, which can be used to fetch its detailed status; writable.");
        DISPATCHER.addStringProperty("batchSize", self -> self.wrapped.getBatchSize(), (self, value) -> self.wrapped.setBatchSize(value))
                .setMetaData(Janitor.MetaData.HELP, "batchSize: The number of items in the batch, as a string; writable.");
        DISPATCHER.addStringProperty("bulkEnvelopesBatchUri", self -> self.wrapped.getBulkEnvelopesBatchUri(), (self, value) -> self.wrapped.setBulkEnvelopesBatchUri(value));
        DISPATCHER.addStringProperty("endPosition", self -> self.wrapped.getEndPosition(), (self, value) -> self.wrapped.setEndPosition(value))
                .setMetaData(Janitor.MetaData.HELP, "endPosition: The last position in the result set, as a string; writable.");
        DISPATCHER.addStringProperty("failed", self -> self.wrapped.getFailed(), (self, value) -> self.wrapped.setFailed(value))
                .setMetaData(Janitor.MetaData.HELP, "failed: The number of entries with the status failed, as a string; writable.");
        DISPATCHER.addStringProperty("nextUri", self -> self.wrapped.getNextUri(), (self, value) -> self.wrapped.setNextUri(value))
                .setMetaData(Janitor.MetaData.HELP, "nextUri: The URI of the next chunk of results, or null if there is none; writable.");
        DISPATCHER.addStringProperty("previousUri", self -> self.wrapped.getPreviousUri(), (self, value) -> self.wrapped.setPreviousUri(value))
                .setMetaData(Janitor.MetaData.HELP, "previousUri: The URI of the previous chunk of results; writable.");
        DISPATCHER.addStringProperty("queued", self -> self.wrapped.getQueued(), (self, value) -> self.wrapped.setQueued(value))
                .setMetaData(Janitor.MetaData.HELP, "queued: The number of entries with the status queued, as a string; writable.");
        DISPATCHER.addStringProperty("resultSetSize", self -> self.wrapped.getResultSetSize(), (self, value) -> self.wrapped.setResultSetSize(value))
                .setMetaData(Janitor.MetaData.HELP, "resultSetSize: The number of results in this response, as a string; writable.");
        DISPATCHER.addStringProperty("sent", self -> self.wrapped.getSent(), (self, value) -> self.wrapped.setSent(value))
                .setMetaData(Janitor.MetaData.HELP, "sent: The number of entries with the status sent, as a string; writable.");
        DISPATCHER.addStringProperty("startPosition", self -> self.wrapped.getStartPosition(), (self, value) -> self.wrapped.setStartPosition(value))
                .setMetaData(Janitor.MetaData.HELP, "startPosition: The first position of this response in the result set, as a string; writable.");
        DISPATCHER.addStringProperty("submittedDate", self -> self.wrapped.getSubmittedDate(), (self, value) -> self.wrapped.setSubmittedDate(value))
                .setMetaData(Janitor.MetaData.HELP, "submittedDate: The date the batch was submitted, as a string; writable.");
        DISPATCHER.addStringProperty("totalSetSize", self -> self.wrapped.getTotalSetSize(), (self, value) -> self.wrapped.setTotalSetSize(value))
                .setMetaData(Janitor.MetaData.HELP, "totalSetSize: The total number of items available in the result set, as a string; writable.");

        // TODO: Add wrappers for complex object properties:
        // - List<BulkEnvelope>: bulkEnvelopes
    }

    private final BulkEnvelopeStatus wrapped;

    public DocusignBulkEnvelopeStatus() {
        super(DISPATCHER);
        this.wrapped = new BulkEnvelopeStatus();
    }

    public DocusignBulkEnvelopeStatus(final BulkEnvelopeStatus wrapped) {
        super(DISPATCHER);
        this.wrapped = wrapped;
    }

    /**
     * @return the wrapped DocuSign object
     */
    public BulkEnvelopeStatus getWrapped() {
        return wrapped;
    }

    @Override
    public String toString() {
        return "DocusignBulkEnvelopeStatus [wrapped=" + wrapped + "]";
    }
}
