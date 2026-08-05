package com.eischet.janitor.docusign;

import com.docusign.esign.api.EnvelopesApi;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;

public class DocusignGetDocumentOptions extends JanitorComposed<DocusignGetDocumentOptions> {
    public static final DispatchTable<DocusignGetDocumentOptions> DISPATCH = new DispatchTable<>();

    static {
        DISPATCH.addStringProperty("certificate", self -> self.options.getCertificate(), (self, value) -> self.options.setCertificate(value));
        DISPATCH.addStringProperty("documentsByUserid", self -> self.options.getDocumentsByUserid(), (self, value) -> self.options.setDocumentsByUserid(value));
        DISPATCH.addStringProperty("encoding", self -> self.options.getEncoding(), (self, value) -> self.options.setEncoding(value));
        DISPATCH.addStringProperty("encrypt", self -> self.options.getEncrypt(), (self, value) -> self.options.setEncrypt(value));
        DISPATCH.addStringProperty("language", self -> self.options.getLanguage(), (self, value) -> self.options.setLanguage(value));
        DISPATCH.addStringProperty("recipientId", self -> self.options.getRecipientId(), (self, value) -> self.options.setRecipientId(value));
        DISPATCH.addStringProperty("sharedUserId", self -> self.options.getSharedUserId(), (self, value) -> self.options.setSharedUserId(value));
        DISPATCH.addStringProperty("showChanges", self -> self.options.getShowChanges(), (self, value) -> self.options.setShowChanges(value));
        DISPATCH.addStringProperty("watermark", self -> self.options.getWatermark(), (self, value) -> self.options.setWatermark(value));
    }

    private final EnvelopesApi.GetDocumentOptions options;

    public DocusignGetDocumentOptions(final EnvelopesApi.GetDocumentOptions options) {
        super(DISPATCH);
        this.options = options;
    }

    public EnvelopesApi.GetDocumentOptions getOptions() {
        return options;
    }
}
