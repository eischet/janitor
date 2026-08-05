package com.eischet.janitor.docusign;

import com.docusign.esign.model.Envelope;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;

public class DocusignEnvelope extends JanitorComposed<DocusignEnvelope> {
    public static final DispatchTable<DocusignEnvelope> DISPATCH = new DispatchTable<>();

    static {
        DISPATCH.addStringProperty("allowComments", self -> self.wrapped.getAllowComments(), (self, value) -> self.wrapped.setAllowComments(value));
        DISPATCH.addStringProperty("accessControlListBase64", self -> self.wrapped.getAccessControlListBase64(), (self, value) -> self.wrapped.setAccessControlListBase64(value));
        DISPATCH.addStringProperty("allowComments", self -> self.wrapped.getAllowComments(), (self, value) -> self.wrapped.setAllowComments(value));
        DISPATCH.addStringProperty("allowMarkup", self -> self.wrapped.getAllowMarkup(), (self, value) -> self.wrapped.setAllowMarkup(value));
        DISPATCH.addStringProperty("allowReassign", self -> self.wrapped.getAllowReassign(), (self, value) -> self.wrapped.setAllowReassign(value));
        DISPATCH.addStringProperty("allowViewHistory", self -> self.wrapped.getAllowViewHistory(), (self, value) -> self.wrapped.setAllowViewHistory(value));
        DISPATCH.addStringProperty("anySigner", self -> self.wrapped.getAnySigner(), (self, value) -> self.wrapped.setAnySigner(value));
        DISPATCH.addStringProperty("asynchronous", self -> self.wrapped.getAsynchronous(), (self, value) -> self.wrapped.setAsynchronous(value));
        DISPATCH.addStringProperty("attachmentsUri", self -> self.wrapped.getAttachmentsUri(), (self, value) -> self.wrapped.setAttachmentsUri(value));
        DISPATCH.addStringProperty("authoritativeCopy", self -> self.wrapped.getAuthoritativeCopy(), (self, value) -> self.wrapped.setAuthoritativeCopy(value));
        DISPATCH.addStringProperty("authoritativeCopyDefault", self -> self.wrapped.getAuthoritativeCopyDefault(), (self, value) -> self.wrapped.setAuthoritativeCopyDefault(value));
        DISPATCH.addStringProperty("autoNavigation", self -> self.wrapped.getAutoNavigation(), (self, value) -> self.wrapped.setAutoNavigation(value));
        DISPATCH.addStringProperty("brandId", self -> self.wrapped.getBrandId(), (self, value) -> self.wrapped.setBrandId(value));
        DISPATCH.addStringProperty("brandLock", self -> self.wrapped.getBrandLock(), (self, value) -> self.wrapped.setBrandLock(value));
        DISPATCH.addStringProperty("burnDefaultTabData", self -> self.wrapped.getBurnDefaultTabData(), (self, value) -> self.wrapped.setBurnDefaultTabData(value));
        DISPATCH.addStringProperty("certificateUri", self -> self.wrapped.getCertificateUri(), (self, value) -> self.wrapped.setCertificateUri(value));
        DISPATCH.addStringProperty("completedDateTime", self -> self.wrapped.getCompletedDateTime(), (self, value) -> self.wrapped.setCompletedDateTime(value));
        DISPATCH.addStringProperty("copyRecipientData", self -> self.wrapped.getCopyRecipientData(), (self, value) -> self.wrapped.setCopyRecipientData(value));
        DISPATCH.addStringProperty("createdDateTime", self -> self.wrapped.getCreatedDateTime(), (self, value) -> self.wrapped.setCreatedDateTime(value));
        DISPATCH.addStringProperty("customFieldsUri", self -> self.wrapped.getCustomFieldsUri(), (self, value) -> self.wrapped.setCustomFieldsUri(value));
        DISPATCH.addStringProperty("declinedDateTime", self -> self.wrapped.getDeclinedDateTime(), (self, value) -> self.wrapped.setDeclinedDateTime(value));
        DISPATCH.addStringProperty("deletedDateTime", self -> self.wrapped.getDeletedDateTime(), (self, value) -> self.wrapped.setDeletedDateTime(value));
        DISPATCH.addStringProperty("deliveredDateTime", self -> self.wrapped.getDeliveredDateTime(), (self, value) -> self.wrapped.setDeliveredDateTime(value));
        DISPATCH.addStringProperty("disableResponsiveDocument", self -> self.wrapped.getDisableResponsiveDocument(), (self, value) -> self.wrapped.setDisableResponsiveDocument(value));
        DISPATCH.addStringProperty("documentBase64", self -> self.wrapped.getDocumentBase64(), (self, value) -> self.wrapped.setDocumentBase64(value));
        DISPATCH.addStringProperty("documentsCombinedUri", self -> self.wrapped.getDocumentsCombinedUri(), (self, value) -> self.wrapped.setDocumentsCombinedUri(value));
        DISPATCH.addStringProperty("documentsUri", self -> self.wrapped.getDocumentsUri(), (self, value) -> self.wrapped.setDocumentsUri(value));
        DISPATCH.addStringProperty("emailBlurb", self -> self.wrapped.getEmailBlurb(), (self, value) -> self.wrapped.setEmailBlurb(value));
        DISPATCH.addStringProperty("emailSubject", self -> self.wrapped.getEmailSubject(), (self, value) -> self.wrapped.setEmailSubject(value));
        DISPATCH.addStringProperty("enableWetSign", self -> self.wrapped.getEnableWetSign(), (self, value) -> self.wrapped.setEnableWetSign(value));
        DISPATCH.addStringProperty("enforceSignerVisibility", self -> self.wrapped.getEnforceSignerVisibility(), (self, value) -> self.wrapped.setEnforceSignerVisibility(value));
        DISPATCH.addStringProperty("envelopeId", self -> self.wrapped.getEnvelopeId(), (self, value) -> self.wrapped.setEnvelopeId(value));
        DISPATCH.addStringProperty("envelopeIdStamping", self -> self.wrapped.getEnvelopeIdStamping(), (self, value) -> self.wrapped.setEnvelopeIdStamping(value));
        DISPATCH.addStringProperty("envelopeLocation", self -> self.wrapped.getEnvelopeLocation(), (self, value) -> self.wrapped.setEnvelopeLocation(value));
        DISPATCH.addStringProperty("envelopeUri", self -> self.wrapped.getEnvelopeUri(), (self, value) -> self.wrapped.setEnvelopeUri(value));
        DISPATCH.addStringProperty("expireAfter", self -> self.wrapped.getExpireAfter(), (self, value) -> self.wrapped.setExpireAfter(value));
        DISPATCH.addStringProperty("expireDateTime", self -> self.wrapped.getExpireDateTime(), (self, value) -> self.wrapped.setExpireDateTime(value));
        DISPATCH.addStringProperty("expireEnabled", self -> self.wrapped.getExpireEnabled(), (self, value) -> self.wrapped.setExpireEnabled(value));
        DISPATCH.addStringProperty("externalEnvelopeId", self -> self.wrapped.getExternalEnvelopeId(), (self, value) -> self.wrapped.setExternalEnvelopeId(value));
        DISPATCH.addStringProperty("hasComments", self -> self.wrapped.getHasComments(), (self, value) -> self.wrapped.setHasComments(value));
        DISPATCH.addStringProperty("hasFormDataChanged", self -> self.wrapped.getHasFormDataChanged(), (self, value) -> self.wrapped.setHasFormDataChanged(value));
        DISPATCH.addStringProperty("hasWavFile", self -> self.wrapped.getHasWavFile(), (self, value) -> self.wrapped.setHasWavFile(value));
        DISPATCH.addStringProperty("holder", self -> self.wrapped.getHolder(), (self, value) -> self.wrapped.setHolder(value));
        DISPATCH.addStringProperty("initialSentDateTime", self -> self.wrapped.getInitialSentDateTime(), (self, value) -> self.wrapped.setInitialSentDateTime(value));
        DISPATCH.addStringProperty("is21CFRPart11", self -> self.wrapped.getIs21CFRPart11(), (self, value) -> self.wrapped.setIs21CFRPart11(value));
        DISPATCH.addStringProperty("isDynamicEnvelope", self -> self.wrapped.getIsDynamicEnvelope(), (self, value) -> self.wrapped.setIsDynamicEnvelope(value));
        DISPATCH.addStringProperty("isSignatureProviderEnvelope", self -> self.wrapped.getIsSignatureProviderEnvelope(), (self, value) -> self.wrapped.setIsSignatureProviderEnvelope(value));
        DISPATCH.addStringProperty("isTicketRelatedEnvelope", self -> self.wrapped.getIsTicketRelatedEnvelope(), (self, value) -> self.wrapped.setIsTicketRelatedEnvelope(value));
        DISPATCH.addStringProperty("lastModifiedDateTime", self -> self.wrapped.getLastModifiedDateTime(), (self, value) -> self.wrapped.setLastModifiedDateTime(value));
        DISPATCH.addStringProperty("location", self -> self.wrapped.getLocation(), (self, value) -> self.wrapped.setLocation(value));
        DISPATCH.addStringProperty("messageLock", self -> self.wrapped.getMessageLock(), (self, value) -> self.wrapped.setMessageLock(value));
        DISPATCH.addStringProperty("notificationUri", self -> self.wrapped.getNotificationUri(), (self, value) -> self.wrapped.setNotificationUri(value));
        DISPATCH.addStringProperty("purgeCompletedDate", self -> self.wrapped.getPurgeCompletedDate(), (self, value) -> self.wrapped.setPurgeCompletedDate(value));
        DISPATCH.addStringProperty("purgeRequestDate", self -> self.wrapped.getPurgeRequestDate(), (self, value) -> self.wrapped.setPurgeRequestDate(value));
        DISPATCH.addStringProperty("purgeState", self -> self.wrapped.getPurgeState(), (self, value) -> self.wrapped.setPurgeState(value));
        DISPATCH.addStringProperty("recipientsLock", self -> self.wrapped.getRecipientsLock(), (self, value) -> self.wrapped.setRecipientsLock(value));
        DISPATCH.addStringProperty("recipientsUri", self -> self.wrapped.getRecipientsUri(), (self, value) -> self.wrapped.setRecipientsUri(value));
        DISPATCH.addStringProperty("sentDateTime", self -> self.wrapped.getSentDateTime(), (self, value) -> self.wrapped.setSentDateTime(value));
        DISPATCH.addStringProperty("signerCanSignOnMobile", self -> self.wrapped.getSignerCanSignOnMobile(), (self, value) -> self.wrapped.setSignerCanSignOnMobile(value));
        DISPATCH.addStringProperty("signingLocation", self -> self.wrapped.getSigningLocation(), (self, value) -> self.wrapped.setSigningLocation(value));
        DISPATCH.addStringProperty("status", self -> self.wrapped.getStatus(), (self, value) -> self.wrapped.setStatus(value));
        DISPATCH.addStringProperty("statusChangedDateTime", self -> self.wrapped.getStatusChangedDateTime(), (self, value) -> self.wrapped.setStatusChangedDateTime(value));
        DISPATCH.addStringProperty("statusDateTime", self -> self.wrapped.getStatusDateTime(), (self, value) -> self.wrapped.setStatusDateTime(value));
        DISPATCH.addStringProperty("templatesUri", self -> self.wrapped.getTemplatesUri(), (self, value) -> self.wrapped.setTemplatesUri(value));
        DISPATCH.addStringProperty("transactionId", self -> self.wrapped.getTransactionId(), (self, value) -> self.wrapped.setTransactionId(value));
        DISPATCH.addStringProperty("useDisclosure", self -> self.wrapped.getUseDisclosure(), (self, value) -> self.wrapped.setUseDisclosure(value));
        DISPATCH.addStringProperty("uSigState", self -> self.wrapped.getUSigState(), (self, value) -> self.wrapped.setUSigState(value));
        DISPATCH.addStringProperty("voidedDateTime", self -> self.wrapped.getVoidedDateTime(), (self, value) -> self.wrapped.setVoidedDateTime(value));
        DISPATCH.addStringProperty("voidedReason", self -> self.wrapped.getVoidedReason(), (self, value) -> self.wrapped.setVoidedReason(value));

        /* LATER: when needed, map these more complex fields, too...
        private PowerForm powerForm = null;
        private CustomFields customFields = null;
        private EmailSettings emailSettings = null;
        private java.util.List<Attachment> envelopeAttachments = null;
        private EnvelopeCustomMetadata envelopeCustomMetadata = null;
        private java.util.List<EnvelopeDocument> envelopeDocuments = null;
        private EnvelopeMetadata envelopeMetadata = null;
        private java.util.List<Folder> folders = null;
        private LockInformation lockInformation = null;
        private Notification notification = null;
        private Workflow workflow = null;
        private UserInfo sender = null;
        private Recipients recipients = null;
         */
    }

    protected final Envelope wrapped;

    public DocusignEnvelope(final Envelope response) {
        super(DISPATCH);
        this.wrapped = response;
    }
}
