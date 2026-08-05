package com.eischet.janitor.docusign;

import com.docusign.esign.model.Document;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;

public class DocusignDocument extends JanitorComposed<DocusignDocument> {

    public static final DispatchTable<DocusignDocument> DISPATCHER = new DispatchTable<>();

    static {
        // String properties
        DISPATCHER.addStringProperty("documentBase64", self -> self.wrapped.getDocumentBase64(), (self, value) -> self.wrapped.setDocumentBase64(value));
        DISPATCHER.addStringProperty("name", self -> self.wrapped.getName(), (self, value) -> self.wrapped.setName(value));
        DISPATCHER.addStringProperty("fileExtension", self -> self.wrapped.getFileExtension(), (self, value) -> self.wrapped.setFileExtension(value));
        DISPATCHER.addStringProperty("documentId", self -> self.wrapped.getDocumentId(), (self, value) -> self.wrapped.setDocumentId(value));
        DISPATCHER.addStringProperty("order", self -> self.wrapped.getOrder(), (self, value) -> self.wrapped.setOrder(value));
        DISPATCHER.addStringProperty("transformPdfFields", self -> self.wrapped.getTransformPdfFields(), (self, value) -> self.wrapped.setTransformPdfFields(value));
        DISPATCHER.addStringProperty("applyAnchorTabs", self -> self.wrapped.getApplyAnchorTabs(), (self, value) -> self.wrapped.setApplyAnchorTabs(value));
        DISPATCHER.addStringProperty("assignTabsToRecipientId", self -> self.wrapped.getAssignTabsToRecipientId(), (self, value) -> self.wrapped.setAssignTabsToRecipientId(value));
        DISPATCHER.addStringProperty("display", self -> self.wrapped.getDisplay(), (self, value) -> self.wrapped.setDisplay(value));
        DISPATCHER.addStringProperty("docGenDocumentStatus", self -> self.wrapped.getDocGenDocumentStatus(), (self, value) -> self.wrapped.setDocGenDocumentStatus(value));
        DISPATCHER.addStringProperty("documentTemplateId", self -> self.wrapped.getDocumentTemplateId(), (self, value) -> self.wrapped.setDocumentTemplateId(value));
        DISPATCHER.addStringProperty("encryptedWithKeyManager", self -> self.wrapped.getEncryptedWithKeyManager(), (self, value) -> self.wrapped.setEncryptedWithKeyManager(value));
        DISPATCHER.addStringProperty("fileFormatHint", self -> self.wrapped.getFileFormatHint(), (self, value) -> self.wrapped.setFileFormatHint(value));
        DISPATCHER.addStringProperty("includeInDownload", self -> self.wrapped.getIncludeInDownload(), (self, value) -> self.wrapped.setIncludeInDownload(value));
        DISPATCHER.addStringProperty("isDocGenDocument", self -> self.wrapped.getIsDocGenDocument(), (self, value) -> self.wrapped.setIsDocGenDocument(value));
        DISPATCHER.addStringProperty("pages", self -> self.wrapped.getPages(), (self, value) -> self.wrapped.setPages(value));
        DISPATCHER.addStringProperty("password", self -> self.wrapped.getPassword(), (self, value) -> self.wrapped.setPassword(value));
        DISPATCHER.addStringProperty("pdfFormFieldOption", self -> self.wrapped.getPdfFormFieldOption(), (self, value) -> self.wrapped.setPdfFormFieldOption(value));
        DISPATCHER.addStringProperty("remoteUrl", self -> self.wrapped.getRemoteUrl(), (self, value) -> self.wrapped.setRemoteUrl(value));
        DISPATCHER.addStringProperty("signerMustAcknowledge", self -> self.wrapped.getSignerMustAcknowledge(), (self, value) -> self.wrapped.setSignerMustAcknowledge(value));
        DISPATCHER.addStringProperty("templateLocked", self -> self.wrapped.getTemplateLocked(), (self, value) -> self.wrapped.setTemplateLocked(value));
        DISPATCHER.addStringProperty("templateRequired", self -> self.wrapped.getTemplateRequired(), (self, value) -> self.wrapped.setTemplateRequired(value));
        DISPATCHER.addStringProperty("uri", self -> self.wrapped.getUri(), (self, value) -> self.wrapped.setUri(value));

        // TODO: Add wrappers for complex object properties:
        // - PropertyMetadata: displayMetadata, documentIdMetadata, fileExtensionMetadata, includeInDownloadMetadata, nameMetadata, orderMetadata, pagesMetadata, signerMustAcknowledgeMetadata, templateRequiredMetadata, typeMetadata
        // - List<DocumentHtmlDefinition>: htmlDefinitions
        // - List<MatchBox>: matchBoxes
    }

    private final Document wrapped;

    public DocusignDocument() {
        super(DISPATCHER);
        this.wrapped = new Document();
    }

    public Document getWrapped() {
        return wrapped;
    }

    @Override
    public String toString() {
        return "DocusignDocument [wrapped=" + wrapped + "]";
    }
}
