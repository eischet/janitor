package com.eischet.janitor.docusign;

import com.docusign.esign.model.EnvelopeDefinition;
import com.eischet.janitor.api.errors.runtime.JanitorError;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;

import static com.eischet.janitor.api.util.ObjectUtilities.simpleClassNameOf;

public class DocusignEnvelopeDefinition extends JanitorComposed<DocusignEnvelopeDefinition> {

    public static final DispatchTable<DocusignEnvelopeDefinition> DISPATCHER = new DispatchTable<>();

    static {
        // String properties
        DISPATCHER.addStringProperty("emailSubject", self -> self.wrapped.getEmailSubject(), (self, value) -> self.wrapped.setEmailSubject(value));
        DISPATCHER.addStringProperty("emailBlurb", self -> self.wrapped.getEmailBlurb(), (self, value) -> self.wrapped.setEmailBlurb(value));
        DISPATCHER.addStringProperty("status", self -> self.wrapped.getStatus(), (self, value) -> self.wrapped.setStatus(value));
        DISPATCHER.addStringProperty("envelopeId", self -> self.wrapped.getEnvelopeId(), (self, value) -> self.wrapped.setEnvelopeId(value));
        DISPATCHER.addStringProperty("brandId", self -> self.wrapped.getBrandId(), (self, value) -> self.wrapped.setBrandId(value));
        DISPATCHER.addStringProperty("templateId", self -> self.wrapped.getTemplateId(), (self, value) -> self.wrapped.setTemplateId(value));
        DISPATCHER.addStringProperty("accessControlListBase64", self -> self.wrapped.getAccessControlListBase64(), (self, value) -> self.wrapped.setAccessControlListBase64(value));
        DISPATCHER.addStringProperty("accessibility", self -> self.wrapped.getAccessibility(), (self, value) -> self.wrapped.setAccessibility(value));
        DISPATCHER.addStringProperty("allowComments", self -> self.wrapped.getAllowComments(), (self, value) -> self.wrapped.setAllowComments(value));
        DISPATCHER.addStringProperty("allowMarkup", self -> self.wrapped.getAllowMarkup(), (self, value) -> self.wrapped.setAllowMarkup(value));
        DISPATCHER.addStringProperty("allowReassign", self -> self.wrapped.getAllowReassign(), (self, value) -> self.wrapped.setAllowReassign(value));
        DISPATCHER.addStringProperty("allowRecipientRecursion", self -> self.wrapped.getAllowRecipientRecursion(), (self, value) -> self.wrapped.setAllowRecipientRecursion(value));
        DISPATCHER.addStringProperty("allowViewHistory", self -> self.wrapped.getAllowViewHistory(), (self, value) -> self.wrapped.setAllowViewHistory(value));
        DISPATCHER.addStringProperty("anySigner", self -> self.wrapped.getAnySigner(), (self, value) -> self.wrapped.setAnySigner(value));
        DISPATCHER.addStringProperty("asynchronous", self -> self.wrapped.getAsynchronous(), (self, value) -> self.wrapped.setAsynchronous(value));
        DISPATCHER.addStringProperty("attachmentsUri", self -> self.wrapped.getAttachmentsUri(), (self, value) -> self.wrapped.setAttachmentsUri(value));
        DISPATCHER.addStringProperty("authoritativeCopy", self -> self.wrapped.getAuthoritativeCopy(), (self, value) -> self.wrapped.setAuthoritativeCopy(value));
        DISPATCHER.addStringProperty("authoritativeCopyDefault", self -> self.wrapped.getAuthoritativeCopyDefault(), (self, value) -> self.wrapped.setAuthoritativeCopyDefault(value));
        DISPATCHER.addStringProperty("autoNavigation", self -> self.wrapped.getAutoNavigation(), (self, value) -> self.wrapped.setAutoNavigation(value));
        DISPATCHER.addStringProperty("brandLock", self -> self.wrapped.getBrandLock(), (self, value) -> self.wrapped.setBrandLock(value));
        DISPATCHER.addStringProperty("burnDefaultTabData", self -> self.wrapped.getBurnDefaultTabData(), (self, value) -> self.wrapped.setBurnDefaultTabData(value));
        DISPATCHER.addStringProperty("certificateUri", self -> self.wrapped.getCertificateUri(), (self, value) -> self.wrapped.setCertificateUri(value));
        DISPATCHER.addStringProperty("completedDateTime", self -> self.wrapped.getCompletedDateTime(), (self, value) -> self.wrapped.setCompletedDateTime(value));
        DISPATCHER.addStringProperty("copyRecipientData", self -> self.wrapped.getCopyRecipientData(), (self, value) -> self.wrapped.setCopyRecipientData(value));
        DISPATCHER.addStringProperty("createdDateTime", self -> self.wrapped.getCreatedDateTime(), (self, value) -> self.wrapped.setCreatedDateTime(value));
        DISPATCHER.addStringProperty("customFieldsUri", self -> self.wrapped.getCustomFieldsUri(), (self, value) -> self.wrapped.setCustomFieldsUri(value));
        DISPATCHER.addStringProperty("declinedDateTime", self -> self.wrapped.getDeclinedDateTime(), (self, value) -> self.wrapped.setDeclinedDateTime(value));
        DISPATCHER.addStringProperty("deletedDateTime", self -> self.wrapped.getDeletedDateTime(), (self, value) -> self.wrapped.setDeletedDateTime(value));
        DISPATCHER.addStringProperty("deliveredDateTime", self -> self.wrapped.getDeliveredDateTime(), (self, value) -> self.wrapped.setDeliveredDateTime(value));
        DISPATCHER.addStringProperty("disableResponsiveDocument", self -> self.wrapped.getDisableResponsiveDocument(), (self, value) -> self.wrapped.setDisableResponsiveDocument(value));
        DISPATCHER.addStringProperty("documentBase64", self -> self.wrapped.getDocumentBase64(), (self, value) -> self.wrapped.setDocumentBase64(value));
        DISPATCHER.addStringProperty("documentsCombinedUri", self -> self.wrapped.getDocumentsCombinedUri(), (self, value) -> self.wrapped.setDocumentsCombinedUri(value));
        DISPATCHER.addStringProperty("documentsUri", self -> self.wrapped.getDocumentsUri(), (self, value) -> self.wrapped.setDocumentsUri(value));
        DISPATCHER.addStringProperty("enableWetSign", self -> self.wrapped.getEnableWetSign(), (self, value) -> self.wrapped.setEnableWetSign(value));
        DISPATCHER.addStringProperty("enforceSignerVisibility", self -> self.wrapped.getEnforceSignerVisibility(), (self, value) -> self.wrapped.setEnforceSignerVisibility(value));
        DISPATCHER.addStringProperty("envelopeIdStamping", self -> self.wrapped.getEnvelopeIdStamping(), (self, value) -> self.wrapped.setEnvelopeIdStamping(value));
        DISPATCHER.addStringProperty("envelopeLocation", self -> self.wrapped.getEnvelopeLocation(), (self, value) -> self.wrapped.setEnvelopeLocation(value));
        DISPATCHER.addStringProperty("envelopeUri", self -> self.wrapped.getEnvelopeUri(), (self, value) -> self.wrapped.setEnvelopeUri(value));
        DISPATCHER.addStringProperty("expireAfter", self -> self.wrapped.getExpireAfter(), (self, value) -> self.wrapped.setExpireAfter(value));
        DISPATCHER.addStringProperty("expireDateTime", self -> self.wrapped.getExpireDateTime(), (self, value) -> self.wrapped.setExpireDateTime(value));
        DISPATCHER.addStringProperty("expireEnabled", self -> self.wrapped.getExpireEnabled(), (self, value) -> self.wrapped.setExpireEnabled(value));
        DISPATCHER.addStringProperty("externalEnvelopeId", self -> self.wrapped.getExternalEnvelopeId(), (self, value) -> self.wrapped.setExternalEnvelopeId(value));
        DISPATCHER.addStringProperty("hasComments", self -> self.wrapped.getHasComments(), (self, value) -> self.wrapped.setHasComments(value));
        DISPATCHER.addStringProperty("hasFormDataChanged", self -> self.wrapped.getHasFormDataChanged(), (self, value) -> self.wrapped.setHasFormDataChanged(value));
        DISPATCHER.addStringProperty("hasWavFile", self -> self.wrapped.getHasWavFile(), (self, value) -> self.wrapped.setHasWavFile(value));
        DISPATCHER.addStringProperty("holder", self -> self.wrapped.getHolder(), (self, value) -> self.wrapped.setHolder(value));
        DISPATCHER.addStringProperty("initialSentDateTime", self -> self.wrapped.getInitialSentDateTime(), (self, value) -> self.wrapped.setInitialSentDateTime(value));
        DISPATCHER.addStringProperty("is21CFRPart11", self -> self.wrapped.getIs21CFRPart11(), (self, value) -> self.wrapped.setIs21CFRPart11(value));
        DISPATCHER.addStringProperty("isDynamicEnvelope", self -> self.wrapped.getIsDynamicEnvelope(), (self, value) -> self.wrapped.setIsDynamicEnvelope(value));
        DISPATCHER.addStringProperty("isSignatureProviderEnvelope", self -> self.wrapped.getIsSignatureProviderEnvelope(), (self, value) -> self.wrapped.setIsSignatureProviderEnvelope(value));
        DISPATCHER.addStringProperty("lastModifiedDateTime", self -> self.wrapped.getLastModifiedDateTime(), (self, value) -> self.wrapped.setLastModifiedDateTime(value));
        DISPATCHER.addStringProperty("location", self -> self.wrapped.getLocation(), (self, value) -> self.wrapped.setLocation(value));
        DISPATCHER.addStringProperty("messageLock", self -> self.wrapped.getMessageLock(), (self, value) -> self.wrapped.setMessageLock(value));
        DISPATCHER.addStringProperty("notificationUri", self -> self.wrapped.getNotificationUri(), (self, value) -> self.wrapped.setNotificationUri(value));
        DISPATCHER.addStringProperty("password", self -> self.wrapped.getPassword(), (self, value) -> self.wrapped.setPassword(value));
        DISPATCHER.addStringProperty("purgeCompletedDate", self -> self.wrapped.getPurgeCompletedDate(), (self, value) -> self.wrapped.setPurgeCompletedDate(value));
        DISPATCHER.addStringProperty("purgeRequestDate", self -> self.wrapped.getPurgeRequestDate(), (self, value) -> self.wrapped.setPurgeRequestDate(value));
        DISPATCHER.addStringProperty("purgeState", self -> self.wrapped.getPurgeState(), (self, value) -> self.wrapped.setPurgeState(value));
        DISPATCHER.addStringProperty("recipientsLock", self -> self.wrapped.getRecipientsLock(), (self, value) -> self.wrapped.setRecipientsLock(value));
        DISPATCHER.addStringProperty("recipientsUri", self -> self.wrapped.getRecipientsUri(), (self, value) -> self.wrapped.setRecipientsUri(value));
        DISPATCHER.addStringProperty("sentDateTime", self -> self.wrapped.getSentDateTime(), (self, value) -> self.wrapped.setSentDateTime(value));
        DISPATCHER.addStringProperty("signerCanSignOnMobile", self -> self.wrapped.getSignerCanSignOnMobile(), (self, value) -> self.wrapped.setSignerCanSignOnMobile(value));
        DISPATCHER.addStringProperty("signingLocation", self -> self.wrapped.getSigningLocation(), (self, value) -> self.wrapped.setSigningLocation(value));
        DISPATCHER.addStringProperty("statusChangedDateTime", self -> self.wrapped.getStatusChangedDateTime(), (self, value) -> self.wrapped.setStatusChangedDateTime(value));
        DISPATCHER.addStringProperty("statusDateTime", self -> self.wrapped.getStatusDateTime(), (self, value) -> self.wrapped.setStatusDateTime(value));
        DISPATCHER.addStringProperty("templatesUri", self -> self.wrapped.getTemplatesUri(), (self, value) -> self.wrapped.setTemplatesUri(value));
        DISPATCHER.addStringProperty("transactionId", self -> self.wrapped.getTransactionId(), (self, value) -> self.wrapped.setTransactionId(value));
        DISPATCHER.addStringProperty("useDisclosure", self -> self.wrapped.getUseDisclosure(), (self, value) -> self.wrapped.setUseDisclosure(value));
        DISPATCHER.addStringProperty("uSigState", self -> self.wrapped.getUSigState(), (self, value) -> self.wrapped.setUSigState(value));
        DISPATCHER.addStringProperty("voidedDateTime", self -> self.wrapped.getVoidedDateTime(), (self, value) -> self.wrapped.setVoidedDateTime(value));
        DISPATCHER.addStringProperty("voidedReason", self -> self.wrapped.getVoidedReason(), (self, value) -> self.wrapped.setVoidedReason(value));

        // Object properties
        DISPATCHER.addObjectProperty("recipients",
            self -> new DocusignRecipients(self.getWrapped().getRecipients()),
            (self, value) -> {
                if (value instanceof DocusignRecipients recipients) {
                    self.wrapped.setRecipients(recipients.getWrapped());
                } else {
                    throw new JanitorError("invalid recipients type: " + value + " (expected "+simpleClassNameOf(value)+")");
                }
            }, DocusignRecipients::new);

        DISPATCHER.addMethod("addDocument", (self, process, args) -> {
            DocusignDocument document = args.getRequired(0, DocusignDocument.class);
            self.wrapped.addDocumentsItem(document.getWrapped());
            return self;
        });

        DISPATCHER.addMethod("addTemplateRole", (self, process, args) -> {
            DocusignTemplateRole role = args.getRequired(0, DocusignTemplateRole.class);
            self.wrapped.addTemplateRolesItem(role.getWrapped());
            return self;
        });

        // TODO: Add wrappers for complex object properties:
        // - PropertyMetadata: accessibilityMetadata, allowCommentsMetadata, allowMarkupMetadata, allowReassignMetadata, allowRecipientRecursionMetadata, allowViewHistoryMetadata, anySignerMetadata, asynchronousMetadata, authoritativeCopyMetadata, autoNavigationMetadata, brandIdMetadata, burnDefaultTabDataMetadata, certificateUriMetadata, completedDateTimeMetadata, copyRecipientDataMetadata, createdDateTimeMetadata, customFieldsUriMetadata, declinedDateTimeMetadata, deletedDateTimeMetadata, deliveredDateTimeMetadata, disableResponsiveDocumentMetadata, documentBase64Metadata, documentsCombinedUriMetadata, documentsUriMetadata, emailBlurbMetadata, emailSubjectMetadata, enableWetSignMetadata, enforceSignerVisibilityMetadata, envelopeIdMetadata, envelopeIdStampingMetadata, envelopeLocationMetadata, envelopeUriMetadata, expireAfterMetadata, expireDateTimeMetadata, expireEnabledMetadata, externalEnvelopeIdMetadata, hasCommentsMetadata, hasFormDataChangedMetadata, hasWavFileMetadata, holderMetadata, initialSentDateTimeMetadata, is21CFRPart11Metadata, isDynamicEnvelopeMetadata, isSignatureProviderEnvelopeMetadata, lastModifiedDateTimeMetadata, locationMetadata, messageLockMetadata, notificationUriMetadata, passwordMetadata, purgeCompletedDateMetadata, purgeRequestDateMetadata, purgeStateMetadata, recipientsLockMetadata, recipientsUriMetadata, sentDateTimeMetadata, signerCanSignOnMobileMetadata, signingLocationMetadata, statusChangedDateTimeMetadata, statusDateTimeMetadata, statusMetadata, templateIdMetadata, templatesUriMetadata, transactionIdMetadata, useDisclosureMetadata, uSigStateMetadata, voidedDateTimeMetadata, voidedReasonMetadata
        // - List<Attachment>: attachments
        // - List<CompositeTemplate>: compositeTemplates
        // - List<Document>: documents
        // - List<EnvelopeCustomMetadata>: customFields
        // - List<EmailSettings>: emailSettings
        // - EnvelopeMetadata: envelopeMetadata
        // - List<EventNotification>: eventNotifications
        // - List<Folder>: folders
        // - List<InlineTemplate>: inlineTemplates
        // - LockInformation: lockInformation
        // - Notification: notification
        // - PowerForm: powerForm
        // - PurgeState: purgeStateObj
        // - Recipients: recipients (already wrapped)
        // - List<ServerTemplate>: serverTemplates
        // - SignerAttachment: signerAttachment
        // - List<TemplateSummary>: templates
        // - TransactionState: transactionState
        // - Workflow: workflow
    }

    private final EnvelopeDefinition wrapped;

    public DocusignEnvelopeDefinition() {
        super(DISPATCHER);
        this.wrapped = new EnvelopeDefinition();
    }

    public EnvelopeDefinition getWrapped() {
        return wrapped;
    }

    @Override
    public String toString() {
        return "DocusignEnvelopeDefinition [wrapped=" + wrapped + "]";
    }

}
