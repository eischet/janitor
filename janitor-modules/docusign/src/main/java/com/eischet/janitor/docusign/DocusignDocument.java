// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.docusign;

import com.docusign.esign.model.Document;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;

/** Wrapper for the DocuSign document. */
public class DocusignDocument extends JanitorComposed<DocusignDocument> {

    public static final DispatchTable<DocusignDocument> DISPATCHER = new DispatchTable<>();

    static {
        // String properties
        DISPATCHER.addStringProperty("documentBase64", self -> self.wrapped.getDocumentBase64(), (self, value) -> self.wrapped.setDocumentBase64(value))
                .setMetaData(Janitor.MetaData.HELP, "documentBase64: The content of the document, encoded as base64; writable.");
        DISPATCHER.addStringProperty("name", self -> self.wrapped.getName(), (self, value) -> self.wrapped.setName(value))
                .setMetaData(Janitor.MetaData.HELP, "name: The name of the document; writable.");
        DISPATCHER.addStringProperty("fileExtension", self -> self.wrapped.getFileExtension(), (self, value) -> self.wrapped.setFileExtension(value))
                .setMetaData(Janitor.MetaData.HELP, "fileExtension: The file extension of the document, e.g. \"pdf\" or \"docx\"; documents that are no PDFs are converted to PDF; writable.");
        DISPATCHER.addStringProperty("documentId", self -> self.wrapped.getDocumentId(), (self, value) -> self.wrapped.setDocumentId(value))
                .setMetaData(Janitor.MetaData.HELP, "documentId: The ID of the document within the envelope, which tabs use to refer to it; writable.");
        DISPATCHER.addStringProperty("order", self -> self.wrapped.getOrder(), (self, value) -> self.wrapped.setOrder(value))
                .setMetaData(Janitor.MetaData.HELP, "order: The position of the document within the envelope, as a string; writable.");
        DISPATCHER.addStringProperty("transformPdfFields", self -> self.wrapped.getTransformPdfFields(), (self, value) -> self.wrapped.setTransformPdfFields(value))
                .setMetaData(Janitor.MetaData.HELP, "transformPdfFields: \"true\" to turn the PDF form fields of the document into DocuSign tabs; writable.");
        DISPATCHER.addStringProperty("applyAnchorTabs", self -> self.wrapped.getApplyAnchorTabs(), (self, value) -> self.wrapped.setApplyAnchorTabs(value));
        DISPATCHER.addStringProperty("assignTabsToRecipientId", self -> self.wrapped.getAssignTabsToRecipientId(), (self, value) -> self.wrapped.setAssignTabsToRecipientId(value));
        DISPATCHER.addStringProperty("display", self -> self.wrapped.getDisplay(), (self, value) -> self.wrapped.setDisplay(value));
        DISPATCHER.addStringProperty("docGenDocumentStatus", self -> self.wrapped.getDocGenDocumentStatus(), (self, value) -> self.wrapped.setDocGenDocumentStatus(value));
        DISPATCHER.addStringProperty("documentTemplateId", self -> self.wrapped.getDocumentTemplateId(), (self, value) -> self.wrapped.setDocumentTemplateId(value));
        DISPATCHER.addStringProperty("encryptedWithKeyManager", self -> self.wrapped.getEncryptedWithKeyManager(), (self, value) -> self.wrapped.setEncryptedWithKeyManager(value))
                .setMetaData(Janitor.MetaData.HELP, "encryptedWithKeyManager: \"true\" if the sender has already encrypted the document for the DocuSign Key Manager; writable.");
        DISPATCHER.addStringProperty("fileFormatHint", self -> self.wrapped.getFileFormatHint(), (self, value) -> self.wrapped.setFileFormatHint(value));
        DISPATCHER.addStringProperty("includeInDownload", self -> self.wrapped.getIncludeInDownload(), (self, value) -> self.wrapped.setIncludeInDownload(value))
                .setMetaData(Janitor.MetaData.HELP, "includeInDownload: \"true\" if the document is included when the envelope's documents are downloaded; writable.");
        DISPATCHER.addStringProperty("isDocGenDocument", self -> self.wrapped.getIsDocGenDocument(), (self, value) -> self.wrapped.setIsDocGenDocument(value));
        DISPATCHER.addStringProperty("pages", self -> self.wrapped.getPages(), (self, value) -> self.wrapped.setPages(value))
                .setMetaData(Janitor.MetaData.HELP, "pages: The number of pages of the document, as a string; writable.");
        DISPATCHER.addStringProperty("password", self -> self.wrapped.getPassword(), (self, value) -> self.wrapped.setPassword(value))
                .setMetaData(Janitor.MetaData.HELP, "password: The password that opens the document, if it is password-protected; writable.");
        DISPATCHER.addStringProperty("pdfFormFieldOption", self -> self.wrapped.getPdfFormFieldOption(), (self, value) -> self.wrapped.setPdfFormFieldOption(value));
        DISPATCHER.addStringProperty("remoteUrl", self -> self.wrapped.getRemoteUrl(), (self, value) -> self.wrapped.setRemoteUrl(value))
                .setMetaData(Janitor.MetaData.HELP, "remoteUrl: The file ID of the document in the cloud storage service it is taken from; writable.");
        DISPATCHER.addStringProperty("signerMustAcknowledge", self -> self.wrapped.getSignerMustAcknowledge(), (self, value) -> self.wrapped.setSignerMustAcknowledge(value));
        DISPATCHER.addStringProperty("templateLocked", self -> self.wrapped.getTemplateLocked(), (self, value) -> self.wrapped.setTemplateLocked(value))
                .setMetaData(Janitor.MetaData.HELP, "templateLocked: \"true\" if the sender may not change the document taken from a template; writable.");
        DISPATCHER.addStringProperty("templateRequired", self -> self.wrapped.getTemplateRequired(), (self, value) -> self.wrapped.setTemplateRequired(value))
                .setMetaData(Janitor.MetaData.HELP, "templateRequired: \"true\" if the sender may not remove the document taken from a template; writable.");
        DISPATCHER.addStringProperty("uri", self -> self.wrapped.getUri(), (self, value) -> self.wrapped.setUri(value))
                .setMetaData(Janitor.MetaData.HELP, "uri: The URI of the document in the DocuSign API; writable.");

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

    /**
     * @return the wrapped DocuSign object
     */
    public Document getWrapped() {
        return wrapped;
    }

    @Override
    public String toString() {
        return "DocusignDocument [wrapped=" + wrapped + "]";
    }
}
