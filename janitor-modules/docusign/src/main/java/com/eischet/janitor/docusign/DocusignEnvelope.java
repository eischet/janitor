// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.docusign;

import com.docusign.esign.model.Envelope;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;

/** Wrapper for a DocuSign envelope. */
public class DocusignEnvelope extends JanitorComposed<DocusignEnvelope> {
    public static final DispatchTable<DocusignEnvelope> DISPATCH = new DispatchTable<>();

    static {
        DISPATCH.addStringProperty("allowComments", self -> self.wrapped.getAllowComments(), (self, value) -> self.wrapped.setAllowComments(value))
                .setMetaData(Janitor.MetaData.HELP, "allowComments: \"true\" if recipients may add comments to the envelope; writable.");
        DISPATCH.addStringProperty("accessControlListBase64", self -> self.wrapped.getAccessControlListBase64(), (self, value) -> self.wrapped.setAccessControlListBase64(value));
        DISPATCH.addStringProperty("allowComments", self -> self.wrapped.getAllowComments(), (self, value) -> self.wrapped.setAllowComments(value))
                .setMetaData(Janitor.MetaData.HELP, "allowComments: \"true\" if recipients may add comments to the envelope; writable.");
        DISPATCH.addStringProperty("allowMarkup", self -> self.wrapped.getAllowMarkup(), (self, value) -> self.wrapped.setAllowMarkup(value))
                .setMetaData(Janitor.MetaData.HELP, "allowMarkup: \"true\" if recipients may change the documents through markup; writable.");
        DISPATCH.addStringProperty("allowReassign", self -> self.wrapped.getAllowReassign(), (self, value) -> self.wrapped.setAllowReassign(value))
                .setMetaData(Janitor.MetaData.HELP, "allowReassign: \"true\" if recipients may reassign the envelope to someone else; writable.");
        DISPATCH.addStringProperty("allowViewHistory", self -> self.wrapped.getAllowViewHistory(), (self, value) -> self.wrapped.setAllowViewHistory(value))
                .setMetaData(Janitor.MetaData.HELP, "allowViewHistory: \"true\" if recipients may view the history of the envelope; writable.");
        DISPATCH.addStringProperty("anySigner", self -> self.wrapped.getAnySigner(), (self, value) -> self.wrapped.setAnySigner(value));
        DISPATCH.addStringProperty("asynchronous", self -> self.wrapped.getAsynchronous(), (self, value) -> self.wrapped.setAsynchronous(value));
        DISPATCH.addStringProperty("attachmentsUri", self -> self.wrapped.getAttachmentsUri(), (self, value) -> self.wrapped.setAttachmentsUri(value))
                .setMetaData(Janitor.MetaData.HELP, "attachmentsUri: The API URI of the envelope's attachments; writable.");
        DISPATCH.addStringProperty("authoritativeCopy", self -> self.wrapped.getAuthoritativeCopy(), (self, value) -> self.wrapped.setAuthoritativeCopy(value));
        DISPATCH.addStringProperty("authoritativeCopyDefault", self -> self.wrapped.getAuthoritativeCopyDefault(), (self, value) -> self.wrapped.setAuthoritativeCopyDefault(value));
        DISPATCH.addStringProperty("autoNavigation", self -> self.wrapped.getAutoNavigation(), (self, value) -> self.wrapped.setAutoNavigation(value));
        DISPATCH.addStringProperty("brandId", self -> self.wrapped.getBrandId(), (self, value) -> self.wrapped.setBrandId(value))
                .setMetaData(Janitor.MetaData.HELP, "brandId: The ID of the brand used for the envelope; writable.");
        DISPATCH.addStringProperty("brandLock", self -> self.wrapped.getBrandLock(), (self, value) -> self.wrapped.setBrandLock(value));
        DISPATCH.addStringProperty("burnDefaultTabData", self -> self.wrapped.getBurnDefaultTabData(), (self, value) -> self.wrapped.setBurnDefaultTabData(value));
        DISPATCH.addStringProperty("certificateUri", self -> self.wrapped.getCertificateUri(), (self, value) -> self.wrapped.setCertificateUri(value))
                .setMetaData(Janitor.MetaData.HELP, "certificateUri: The API URI of the envelope's certificate of completion; writable.");
        DISPATCH.addStringProperty("completedDateTime", self -> self.wrapped.getCompletedDateTime(), (self, value) -> self.wrapped.setCompletedDateTime(value))
                .setMetaData(Janitor.MetaData.HELP, "completedDateTime: The date and time the envelope was completed, as a string; writable.");
        DISPATCH.addStringProperty("copyRecipientData", self -> self.wrapped.getCopyRecipientData(), (self, value) -> self.wrapped.setCopyRecipientData(value));
        DISPATCH.addStringProperty("createdDateTime", self -> self.wrapped.getCreatedDateTime(), (self, value) -> self.wrapped.setCreatedDateTime(value))
                .setMetaData(Janitor.MetaData.HELP, "createdDateTime: The date and time the envelope was created, as a string; writable.");
        DISPATCH.addStringProperty("customFieldsUri", self -> self.wrapped.getCustomFieldsUri(), (self, value) -> self.wrapped.setCustomFieldsUri(value))
                .setMetaData(Janitor.MetaData.HELP, "customFieldsUri: The API URI of the envelope's custom fields; writable.");
        DISPATCH.addStringProperty("declinedDateTime", self -> self.wrapped.getDeclinedDateTime(), (self, value) -> self.wrapped.setDeclinedDateTime(value))
                .setMetaData(Janitor.MetaData.HELP, "declinedDateTime: The date and time the envelope was declined, as a string; writable.");
        DISPATCH.addStringProperty("deletedDateTime", self -> self.wrapped.getDeletedDateTime(), (self, value) -> self.wrapped.setDeletedDateTime(value))
                .setMetaData(Janitor.MetaData.HELP, "deletedDateTime: The date and time the envelope was deleted, as a string; writable.");
        DISPATCH.addStringProperty("deliveredDateTime", self -> self.wrapped.getDeliveredDateTime(), (self, value) -> self.wrapped.setDeliveredDateTime(value))
                .setMetaData(Janitor.MetaData.HELP, "deliveredDateTime: The date and time the envelope was delivered to the recipients, as a string; writable.");
        DISPATCH.addStringProperty("disableResponsiveDocument", self -> self.wrapped.getDisableResponsiveDocument(), (self, value) -> self.wrapped.setDisableResponsiveDocument(value))
                .setMetaData(Janitor.MetaData.HELP, "disableResponsiveDocument: \"true\" to show the documents without responsive signing, i.e. not adapted to the screen size; writable.");
        DISPATCH.addStringProperty("documentBase64", self -> self.wrapped.getDocumentBase64(), (self, value) -> self.wrapped.setDocumentBase64(value));
        DISPATCH.addStringProperty("documentsCombinedUri", self -> self.wrapped.getDocumentsCombinedUri(), (self, value) -> self.wrapped.setDocumentsCombinedUri(value))
                .setMetaData(Janitor.MetaData.HELP, "documentsCombinedUri: The API URI for downloading all documents of the envelope combined; writable.");
        DISPATCH.addStringProperty("documentsUri", self -> self.wrapped.getDocumentsUri(), (self, value) -> self.wrapped.setDocumentsUri(value))
                .setMetaData(Janitor.MetaData.HELP, "documentsUri: The API URI of the envelope's documents; writable.");
        DISPATCH.addStringProperty("emailBlurb", self -> self.wrapped.getEmailBlurb(), (self, value) -> self.wrapped.setEmailBlurb(value))
                .setMetaData(Janitor.MetaData.HELP, "emailBlurb: The message text of the email sent to the recipients; writable.");
        DISPATCH.addStringProperty("emailSubject", self -> self.wrapped.getEmailSubject(), (self, value) -> self.wrapped.setEmailSubject(value))
                .setMetaData(Janitor.MetaData.HELP, "emailSubject: The subject of the email sent to the recipients; writable.");
        DISPATCH.addStringProperty("enableWetSign", self -> self.wrapped.getEnableWetSign(), (self, value) -> self.wrapped.setEnableWetSign(value))
                .setMetaData(Janitor.MetaData.HELP, "enableWetSign: \"true\" if signers may print the documents and sign them on paper; writable.");
        DISPATCH.addStringProperty("enforceSignerVisibility", self -> self.wrapped.getEnforceSignerVisibility(), (self, value) -> self.wrapped.setEnforceSignerVisibility(value))
                .setMetaData(Janitor.MetaData.HELP, "enforceSignerVisibility: \"true\" if signers only see the documents that they have tabs on; writable.");
        DISPATCH.addStringProperty("envelopeId", self -> self.wrapped.getEnvelopeId(), (self, value) -> self.wrapped.setEnvelopeId(value))
                .setMetaData(Janitor.MetaData.HELP, "envelopeId: The ID of the envelope; writable.");
        DISPATCH.addStringProperty("envelopeIdStamping", self -> self.wrapped.getEnvelopeIdStamping(), (self, value) -> self.wrapped.setEnvelopeIdStamping(value))
                .setMetaData(Janitor.MetaData.HELP, "envelopeIdStamping: \"true\" if the envelope ID is stamped onto the documents; writable.");
        DISPATCH.addStringProperty("envelopeLocation", self -> self.wrapped.getEnvelopeLocation(), (self, value) -> self.wrapped.setEnvelopeLocation(value));
        DISPATCH.addStringProperty("envelopeUri", self -> self.wrapped.getEnvelopeUri(), (self, value) -> self.wrapped.setEnvelopeUri(value))
                .setMetaData(Janitor.MetaData.HELP, "envelopeUri: The API URI of the envelope; writable.");
        DISPATCH.addStringProperty("expireAfter", self -> self.wrapped.getExpireAfter(), (self, value) -> self.wrapped.setExpireAfter(value))
                .setMetaData(Janitor.MetaData.HELP, "expireAfter: The number of days after sending after which the envelope expires, as a string; writable.");
        DISPATCH.addStringProperty("expireDateTime", self -> self.wrapped.getExpireDateTime(), (self, value) -> self.wrapped.setExpireDateTime(value))
                .setMetaData(Janitor.MetaData.HELP, "expireDateTime: The date and time the envelope expires, as a string; writable.");
        DISPATCH.addStringProperty("expireEnabled", self -> self.wrapped.getExpireEnabled(), (self, value) -> self.wrapped.setExpireEnabled(value))
                .setMetaData(Janitor.MetaData.HELP, "expireEnabled: \"true\" if the envelope expires; writable.");
        DISPATCH.addStringProperty("externalEnvelopeId", self -> self.wrapped.getExternalEnvelopeId(), (self, value) -> self.wrapped.setExternalEnvelopeId(value));
        DISPATCH.addStringProperty("hasComments", self -> self.wrapped.getHasComments(), (self, value) -> self.wrapped.setHasComments(value))
                .setMetaData(Janitor.MetaData.HELP, "hasComments: \"true\" if the envelope has comments; writable.");
        DISPATCH.addStringProperty("hasFormDataChanged", self -> self.wrapped.getHasFormDataChanged(), (self, value) -> self.wrapped.setHasFormDataChanged(value))
                .setMetaData(Janitor.MetaData.HELP, "hasFormDataChanged: \"true\" if the form data of the envelope has changed; writable.");
        DISPATCH.addStringProperty("hasWavFile", self -> self.wrapped.getHasWavFile(), (self, value) -> self.wrapped.setHasWavFile(value));
        DISPATCH.addStringProperty("holder", self -> self.wrapped.getHolder(), (self, value) -> self.wrapped.setHolder(value));
        DISPATCH.addStringProperty("initialSentDateTime", self -> self.wrapped.getInitialSentDateTime(), (self, value) -> self.wrapped.setInitialSentDateTime(value))
                .setMetaData(Janitor.MetaData.HELP, "initialSentDateTime: The date and time the envelope was first sent, as a string; writable.");
        DISPATCH.addStringProperty("is21CFRPart11", self -> self.wrapped.getIs21CFRPart11(), (self, value) -> self.wrapped.setIs21CFRPart11(value))
                .setMetaData(Janitor.MetaData.HELP, "is21CFRPart11: \"true\" if the envelope follows the signing rules of 21 CFR Part 11; writable.");
        DISPATCH.addStringProperty("isDynamicEnvelope", self -> self.wrapped.getIsDynamicEnvelope(), (self, value) -> self.wrapped.setIsDynamicEnvelope(value));
        DISPATCH.addStringProperty("isSignatureProviderEnvelope", self -> self.wrapped.getIsSignatureProviderEnvelope(), (self, value) -> self.wrapped.setIsSignatureProviderEnvelope(value));
        DISPATCH.addStringProperty("isTicketRelatedEnvelope", self -> self.wrapped.getIsTicketRelatedEnvelope(), (self, value) -> self.wrapped.setIsTicketRelatedEnvelope(value));
        DISPATCH.addStringProperty("lastModifiedDateTime", self -> self.wrapped.getLastModifiedDateTime(), (self, value) -> self.wrapped.setLastModifiedDateTime(value))
                .setMetaData(Janitor.MetaData.HELP, "lastModifiedDateTime: The date and time the envelope was last changed, as a string; writable.");
        DISPATCH.addStringProperty("location", self -> self.wrapped.getLocation(), (self, value) -> self.wrapped.setLocation(value));
        DISPATCH.addStringProperty("messageLock", self -> self.wrapped.getMessageLock(), (self, value) -> self.wrapped.setMessageLock(value))
                .setMetaData(Janitor.MetaData.HELP, "messageLock: \"true\" if the email subject and message of the envelope may not be changed; writable.");
        DISPATCH.addStringProperty("notificationUri", self -> self.wrapped.getNotificationUri(), (self, value) -> self.wrapped.setNotificationUri(value))
                .setMetaData(Janitor.MetaData.HELP, "notificationUri: The API URI of the envelope's notification settings; writable.");
        DISPATCH.addStringProperty("purgeCompletedDate", self -> self.wrapped.getPurgeCompletedDate(), (self, value) -> self.wrapped.setPurgeCompletedDate(value))
                .setMetaData(Janitor.MetaData.HELP, "purgeCompletedDate: The date the documents of the envelope were purged, as a string; writable.");
        DISPATCH.addStringProperty("purgeRequestDate", self -> self.wrapped.getPurgeRequestDate(), (self, value) -> self.wrapped.setPurgeRequestDate(value))
                .setMetaData(Janitor.MetaData.HELP, "purgeRequestDate: The date the purge of the envelope's documents was requested, as a string; writable.");
        DISPATCH.addStringProperty("purgeState", self -> self.wrapped.getPurgeState(), (self, value) -> self.wrapped.setPurgeState(value));
        DISPATCH.addStringProperty("recipientsLock", self -> self.wrapped.getRecipientsLock(), (self, value) -> self.wrapped.setRecipientsLock(value))
                .setMetaData(Janitor.MetaData.HELP, "recipientsLock: \"true\" if the recipients of the envelope may not be changed; writable.");
        DISPATCH.addStringProperty("recipientsUri", self -> self.wrapped.getRecipientsUri(), (self, value) -> self.wrapped.setRecipientsUri(value))
                .setMetaData(Janitor.MetaData.HELP, "recipientsUri: The API URI of the envelope's recipients; writable.");
        DISPATCH.addStringProperty("sentDateTime", self -> self.wrapped.getSentDateTime(), (self, value) -> self.wrapped.setSentDateTime(value))
                .setMetaData(Janitor.MetaData.HELP, "sentDateTime: The date and time the envelope was sent, as a string; writable.");
        DISPATCH.addStringProperty("signerCanSignOnMobile", self -> self.wrapped.getSignerCanSignOnMobile(), (self, value) -> self.wrapped.setSignerCanSignOnMobile(value))
                .setMetaData(Janitor.MetaData.HELP, "signerCanSignOnMobile: \"true\" if signers may sign on a mobile device; writable.");
        DISPATCH.addStringProperty("signingLocation", self -> self.wrapped.getSigningLocation(), (self, value) -> self.wrapped.setSigningLocation(value))
                .setMetaData(Janitor.MetaData.HELP, "signingLocation: Where the envelope is signed, \"Online\" or \"InPerson\"; writable.");
        DISPATCH.addStringProperty("status", self -> self.wrapped.getStatus(), (self, value) -> self.wrapped.setStatus(value))
                .setMetaData(Janitor.MetaData.HELP, "status: The status of the envelope, e.g. \"created\", \"sent\", \"delivered\", \"completed\", \"declined\" or \"voided\"; writable.");
        DISPATCH.addStringProperty("statusChangedDateTime", self -> self.wrapped.getStatusChangedDateTime(), (self, value) -> self.wrapped.setStatusChangedDateTime(value))
                .setMetaData(Janitor.MetaData.HELP, "statusChangedDateTime: The date and time the status of the envelope last changed, as a string; writable.");
        DISPATCH.addStringProperty("templatesUri", self -> self.wrapped.getTemplatesUri(), (self, value) -> self.wrapped.setTemplatesUri(value))
                .setMetaData(Janitor.MetaData.HELP, "templatesUri: The API URI of the templates used by the envelope; writable.");
        DISPATCH.addStringProperty("transactionId", self -> self.wrapped.getTransactionId(), (self, value) -> self.wrapped.setTransactionId(value));
        DISPATCH.addStringProperty("useDisclosure", self -> self.wrapped.getUseDisclosure(), (self, value) -> self.wrapped.setUseDisclosure(value))
                .setMetaData(Janitor.MetaData.HELP, "useDisclosure: \"true\" if recipients must accept the electronic record and signature disclosure; writable.");
        DISPATCH.addStringProperty("uSigState", self -> self.wrapped.getUSigState(), (self, value) -> self.wrapped.setUSigState(value));
        DISPATCH.addStringProperty("voidedDateTime", self -> self.wrapped.getVoidedDateTime(), (self, value) -> self.wrapped.setVoidedDateTime(value))
                .setMetaData(Janitor.MetaData.HELP, "voidedDateTime: The date and time the envelope was voided, as a string; writable.");
        DISPATCH.addStringProperty("voidedReason", self -> self.wrapped.getVoidedReason(), (self, value) -> self.wrapped.setVoidedReason(value))
                .setMetaData(Janitor.MetaData.HELP, "voidedReason: The reason the envelope was voided; writable.");

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
