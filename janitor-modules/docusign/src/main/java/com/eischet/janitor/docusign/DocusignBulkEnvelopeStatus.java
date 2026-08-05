package com.eischet.janitor.docusign;

import com.docusign.esign.model.BulkEnvelopeStatus;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;

public class DocusignBulkEnvelopeStatus extends JanitorComposed<DocusignBulkEnvelopeStatus> {

    public static final DispatchTable<DocusignBulkEnvelopeStatus> DISPATCHER = new DispatchTable<>();

    static {
        // String properties
        DISPATCHER.addStringProperty("batchId", self -> self.wrapped.getBatchId(), (self, value) -> self.wrapped.setBatchId(value));
        DISPATCHER.addStringProperty("batchSize", self -> self.wrapped.getBatchSize(), (self, value) -> self.wrapped.setBatchSize(value));
        DISPATCHER.addStringProperty("bulkEnvelopesBatchUri", self -> self.wrapped.getBulkEnvelopesBatchUri(), (self, value) -> self.wrapped.setBulkEnvelopesBatchUri(value));
        DISPATCHER.addStringProperty("endPosition", self -> self.wrapped.getEndPosition(), (self, value) -> self.wrapped.setEndPosition(value));
        DISPATCHER.addStringProperty("failed", self -> self.wrapped.getFailed(), (self, value) -> self.wrapped.setFailed(value));
        DISPATCHER.addStringProperty("nextUri", self -> self.wrapped.getNextUri(), (self, value) -> self.wrapped.setNextUri(value));
        DISPATCHER.addStringProperty("previousUri", self -> self.wrapped.getPreviousUri(), (self, value) -> self.wrapped.setPreviousUri(value));
        DISPATCHER.addStringProperty("queued", self -> self.wrapped.getQueued(), (self, value) -> self.wrapped.setQueued(value));
        DISPATCHER.addStringProperty("resultSetSize", self -> self.wrapped.getResultSetSize(), (self, value) -> self.wrapped.setResultSetSize(value));
        DISPATCHER.addStringProperty("sent", self -> self.wrapped.getSent(), (self, value) -> self.wrapped.setSent(value));
        DISPATCHER.addStringProperty("startPosition", self -> self.wrapped.getStartPosition(), (self, value) -> self.wrapped.setStartPosition(value));
        DISPATCHER.addStringProperty("submittedDate", self -> self.wrapped.getSubmittedDate(), (self, value) -> self.wrapped.setSubmittedDate(value));
        DISPATCHER.addStringProperty("totalSetSize", self -> self.wrapped.getTotalSetSize(), (self, value) -> self.wrapped.setTotalSetSize(value));

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

    public BulkEnvelopeStatus getWrapped() {
        return wrapped;
    }

    @Override
    public String toString() {
        return "DocusignBulkEnvelopeStatus [wrapped=" + wrapped + "]";
    }
}
