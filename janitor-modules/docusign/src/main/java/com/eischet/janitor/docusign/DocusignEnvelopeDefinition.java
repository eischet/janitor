// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.docusign;

import com.docusign.esign.model.EnvelopeDefinition;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.errors.runtime.JanitorError;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;

import static com.eischet.janitor.api.util.ObjectUtilities.simpleClassNameOf;

/** Wrapper for the DocuSign envelope definition. */
public class DocusignEnvelopeDefinition extends JanitorComposed<DocusignEnvelopeDefinition> {

    public static final DispatchTable<DocusignEnvelopeDefinition> DISPATCHER = new DispatchTable<>();

    static {
        // String properties
        DISPATCHER.addStringProperty("emailSubject", self -> self.wrapped.getEmailSubject(), (self, value) -> self.wrapped.setEmailSubject(value))
                .setMetaData(Janitor.MetaData.HELP, "emailSubject: The subject of the email sent to the recipients; writable.");
        DISPATCHER.addStringProperty("emailBlurb", self -> self.wrapped.getEmailBlurb(), (self, value) -> self.wrapped.setEmailBlurb(value))
                .setMetaData(Janitor.MetaData.HELP, "emailBlurb: The message text of the email sent to the recipients; writable.");
        DISPATCHER.addStringProperty("status", self -> self.wrapped.getStatus(), (self, value) -> self.wrapped.setStatus(value))
                .setMetaData(Janitor.MetaData.HELP, "status: The status of the new envelope, \"sent\" to send it right away or \"created\" to save it as a draft; writable.");
        DISPATCHER.addStringProperty("envelopeId", self -> self.wrapped.getEnvelopeId(), (self, value) -> self.wrapped.setEnvelopeId(value))
                .setMetaData(Janitor.MetaData.HELP, "envelopeId: The ID of the envelope; writable.");
        DISPATCHER.addStringProperty("brandId", self -> self.wrapped.getBrandId(), (self, value) -> self.wrapped.setBrandId(value))
                .setMetaData(Janitor.MetaData.HELP, "brandId: The ID of the brand used for the envelope; writable.");
        DISPATCHER.addStringProperty("templateId", self -> self.wrapped.getTemplateId(), (self, value) -> self.wrapped.setTemplateId(value))
                .setMetaData(Janitor.MetaData.HELP, "templateId: The ID of the template that the envelope is created from; writable.");
        DISPATCHER.addStringProperty("accessControlListBase64", self -> self.wrapped.getAccessControlListBase64(), (self, value) -> self.wrapped.setAccessControlListBase64(value));
        DISPATCHER.addStringProperty("accessibility", self -> self.wrapped.getAccessibility(), (self, value) -> self.wrapped.setAccessibility(value));
        DISPATCHER.addStringProperty("allowComments", self -> self.wrapped.getAllowComments(), (self, value) -> self.wrapped.setAllowComments(value))
                .setMetaData(Janitor.MetaData.HELP, "allowComments: \"true\" if recipients may add comments to the envelope; writable.");
        DISPATCHER.addStringProperty("allowMarkup", self -> self.wrapped.getAllowMarkup(), (self, value) -> self.wrapped.setAllowMarkup(value))
                .setMetaData(Janitor.MetaData.HELP, "allowMarkup: \"true\" if recipients may change the documents through markup; writable.");
        DISPATCHER.addStringProperty("allowReassign", self -> self.wrapped.getAllowReassign(), (self, value) -> self.wrapped.setAllowReassign(value))
                .setMetaData(Janitor.MetaData.HELP, "allowReassign: \"true\" if recipients may reassign the envelope to someone else; writable.");
        DISPATCHER.addStringProperty("allowRecipientRecursion", self -> self.wrapped.getAllowRecipientRecursion(), (self, value) -> self.wrapped.setAllowRecipientRecursion(value));
        DISPATCHER.addStringProperty("allowViewHistory", self -> self.wrapped.getAllowViewHistory(), (self, value) -> self.wrapped.setAllowViewHistory(value))
                .setMetaData(Janitor.MetaData.HELP, "allowViewHistory: \"true\" if recipients may view the history of the envelope; writable.");
        DISPATCHER.addStringProperty("anySigner", self -> self.wrapped.getAnySigner(), (self, value) -> self.wrapped.setAnySigner(value));
        DISPATCHER.addStringProperty("asynchronous", self -> self.wrapped.getAsynchronous(), (self, value) -> self.wrapped.setAsynchronous(value));
        DISPATCHER.addStringProperty("attachmentsUri", self -> self.wrapped.getAttachmentsUri(), (self, value) -> self.wrapped.setAttachmentsUri(value))
                .setMetaData(Janitor.MetaData.HELP, "attachmentsUri: The API URI of the envelope's attachments; writable.");
        DISPATCHER.addStringProperty("authoritativeCopy", self -> self.wrapped.getAuthoritativeCopy(), (self, value) -> self.wrapped.setAuthoritativeCopy(value));
        DISPATCHER.addStringProperty("authoritativeCopyDefault", self -> self.wrapped.getAuthoritativeCopyDefault(), (self, value) -> self.wrapped.setAuthoritativeCopyDefault(value));
        DISPATCHER.addStringProperty("autoNavigation", self -> self.wrapped.getAutoNavigation(), (self, value) -> self.wrapped.setAutoNavigation(value));
        DISPATCHER.addStringProperty("brandLock", self -> self.wrapped.getBrandLock(), (self, value) -> self.wrapped.setBrandLock(value));
        DISPATCHER.addStringProperty("burnDefaultTabData", self -> self.wrapped.getBurnDefaultTabData(), (self, value) -> self.wrapped.setBurnDefaultTabData(value));
        DISPATCHER.addStringProperty("certificateUri", self -> self.wrapped.getCertificateUri(), (self, value) -> self.wrapped.setCertificateUri(value))
                .setMetaData(Janitor.MetaData.HELP, "certificateUri: The API URI of the envelope's certificate of completion; writable.");
        DISPATCHER.addStringProperty("completedDateTime", self -> self.wrapped.getCompletedDateTime(), (self, value) -> self.wrapped.setCompletedDateTime(value))
                .setMetaData(Janitor.MetaData.HELP, "completedDateTime: The date and time the envelope was completed, as a string; writable.");
        DISPATCHER.addStringProperty("copyRecipientData", self -> self.wrapped.getCopyRecipientData(), (self, value) -> self.wrapped.setCopyRecipientData(value));
        DISPATCHER.addStringProperty("createdDateTime", self -> self.wrapped.getCreatedDateTime(), (self, value) -> self.wrapped.setCreatedDateTime(value))
                .setMetaData(Janitor.MetaData.HELP, "createdDateTime: The date and time the envelope was created, as a string; writable.");
        DISPATCHER.addStringProperty("customFieldsUri", self -> self.wrapped.getCustomFieldsUri(), (self, value) -> self.wrapped.setCustomFieldsUri(value))
                .setMetaData(Janitor.MetaData.HELP, "customFieldsUri: The API URI of the envelope's custom fields; writable.");
        DISPATCHER.addStringProperty("declinedDateTime", self -> self.wrapped.getDeclinedDateTime(), (self, value) -> self.wrapped.setDeclinedDateTime(value))
                .setMetaData(Janitor.MetaData.HELP, "declinedDateTime: The date and time the envelope was declined, as a string; writable.");
        DISPATCHER.addStringProperty("deletedDateTime", self -> self.wrapped.getDeletedDateTime(), (self, value) -> self.wrapped.setDeletedDateTime(value))
                .setMetaData(Janitor.MetaData.HELP, "deletedDateTime: The date and time the envelope was deleted, as a string; writable.");
        DISPATCHER.addStringProperty("deliveredDateTime", self -> self.wrapped.getDeliveredDateTime(), (self, value) -> self.wrapped.setDeliveredDateTime(value))
                .setMetaData(Janitor.MetaData.HELP, "deliveredDateTime: The date and time the envelope was delivered to the recipients, as a string; writable.");
        DISPATCHER.addStringProperty("disableResponsiveDocument", self -> self.wrapped.getDisableResponsiveDocument(), (self, value) -> self.wrapped.setDisableResponsiveDocument(value))
                .setMetaData(Janitor.MetaData.HELP, "disableResponsiveDocument: \"true\" to show the documents without responsive signing, i.e. not adapted to the screen size; writable.");
        DISPATCHER.addStringProperty("documentBase64", self -> self.wrapped.getDocumentBase64(), (self, value) -> self.wrapped.setDocumentBase64(value));
        DISPATCHER.addStringProperty("documentsCombinedUri", self -> self.wrapped.getDocumentsCombinedUri(), (self, value) -> self.wrapped.setDocumentsCombinedUri(value))
                .setMetaData(Janitor.MetaData.HELP, "documentsCombinedUri: The API URI for downloading all documents of the envelope combined; writable.");
        DISPATCHER.addStringProperty("documentsUri", self -> self.wrapped.getDocumentsUri(), (self, value) -> self.wrapped.setDocumentsUri(value))
                .setMetaData(Janitor.MetaData.HELP, "documentsUri: The API URI of the envelope's documents; writable.");
        DISPATCHER.addStringProperty("enableWetSign", self -> self.wrapped.getEnableWetSign(), (self, value) -> self.wrapped.setEnableWetSign(value))
                .setMetaData(Janitor.MetaData.HELP, "enableWetSign: \"true\" if signers may print the documents and sign them on paper; writable.");
        DISPATCHER.addStringProperty("enforceSignerVisibility", self -> self.wrapped.getEnforceSignerVisibility(), (self, value) -> self.wrapped.setEnforceSignerVisibility(value))
                .setMetaData(Janitor.MetaData.HELP, "enforceSignerVisibility: \"true\" if signers only see the documents that they have tabs on; writable.");
        DISPATCHER.addStringProperty("envelopeIdStamping", self -> self.wrapped.getEnvelopeIdStamping(), (self, value) -> self.wrapped.setEnvelopeIdStamping(value))
                .setMetaData(Janitor.MetaData.HELP, "envelopeIdStamping: \"true\" if the envelope ID is stamped onto the documents; writable.");
        DISPATCHER.addStringProperty("envelopeLocation", self -> self.wrapped.getEnvelopeLocation(), (self, value) -> self.wrapped.setEnvelopeLocation(value));
        DISPATCHER.addStringProperty("envelopeUri", self -> self.wrapped.getEnvelopeUri(), (self, value) -> self.wrapped.setEnvelopeUri(value))
                .setMetaData(Janitor.MetaData.HELP, "envelopeUri: The API URI of the envelope; writable.");
        DISPATCHER.addStringProperty("expireAfter", self -> self.wrapped.getExpireAfter(), (self, value) -> self.wrapped.setExpireAfter(value))
                .setMetaData(Janitor.MetaData.HELP, "expireAfter: The number of days after sending after which the envelope expires, as a string; writable.");
        DISPATCHER.addStringProperty("expireDateTime", self -> self.wrapped.getExpireDateTime(), (self, value) -> self.wrapped.setExpireDateTime(value))
                .setMetaData(Janitor.MetaData.HELP, "expireDateTime: The date and time the envelope expires, as a string; writable.");
        DISPATCHER.addStringProperty("expireEnabled", self -> self.wrapped.getExpireEnabled(), (self, value) -> self.wrapped.setExpireEnabled(value))
                .setMetaData(Janitor.MetaData.HELP, "expireEnabled: \"true\" if the envelope expires; writable.");
        DISPATCHER.addStringProperty("externalEnvelopeId", self -> self.wrapped.getExternalEnvelopeId(), (self, value) -> self.wrapped.setExternalEnvelopeId(value));
        DISPATCHER.addStringProperty("hasComments", self -> self.wrapped.getHasComments(), (self, value) -> self.wrapped.setHasComments(value))
                .setMetaData(Janitor.MetaData.HELP, "hasComments: \"true\" if the envelope has comments; writable.");
        DISPATCHER.addStringProperty("hasFormDataChanged", self -> self.wrapped.getHasFormDataChanged(), (self, value) -> self.wrapped.setHasFormDataChanged(value))
                .setMetaData(Janitor.MetaData.HELP, "hasFormDataChanged: \"true\" if the form data of the envelope has changed; writable.");
        DISPATCHER.addStringProperty("hasWavFile", self -> self.wrapped.getHasWavFile(), (self, value) -> self.wrapped.setHasWavFile(value));
        DISPATCHER.addStringProperty("holder", self -> self.wrapped.getHolder(), (self, value) -> self.wrapped.setHolder(value));
        DISPATCHER.addStringProperty("initialSentDateTime", self -> self.wrapped.getInitialSentDateTime(), (self, value) -> self.wrapped.setInitialSentDateTime(value))
                .setMetaData(Janitor.MetaData.HELP, "initialSentDateTime: The date and time the envelope was first sent, as a string; writable.");
        DISPATCHER.addStringProperty("is21CFRPart11", self -> self.wrapped.getIs21CFRPart11(), (self, value) -> self.wrapped.setIs21CFRPart11(value))
                .setMetaData(Janitor.MetaData.HELP, "is21CFRPart11: \"true\" if the envelope follows the signing rules of 21 CFR Part 11; writable.");
        DISPATCHER.addStringProperty("isDynamicEnvelope", self -> self.wrapped.getIsDynamicEnvelope(), (self, value) -> self.wrapped.setIsDynamicEnvelope(value));
        DISPATCHER.addStringProperty("isSignatureProviderEnvelope", self -> self.wrapped.getIsSignatureProviderEnvelope(), (self, value) -> self.wrapped.setIsSignatureProviderEnvelope(value));
        DISPATCHER.addStringProperty("lastModifiedDateTime", self -> self.wrapped.getLastModifiedDateTime(), (self, value) -> self.wrapped.setLastModifiedDateTime(value))
                .setMetaData(Janitor.MetaData.HELP, "lastModifiedDateTime: The date and time the envelope was last changed, as a string; writable.");
        DISPATCHER.addStringProperty("location", self -> self.wrapped.getLocation(), (self, value) -> self.wrapped.setLocation(value));
        DISPATCHER.addStringProperty("messageLock", self -> self.wrapped.getMessageLock(), (self, value) -> self.wrapped.setMessageLock(value))
                .setMetaData(Janitor.MetaData.HELP, "messageLock: \"true\" if the email subject and message of the envelope may not be changed; writable.");
        DISPATCHER.addStringProperty("notificationUri", self -> self.wrapped.getNotificationUri(), (self, value) -> self.wrapped.setNotificationUri(value))
                .setMetaData(Janitor.MetaData.HELP, "notificationUri: The API URI of the envelope's notification settings; writable.");
        DISPATCHER.addStringProperty("password", self -> self.wrapped.getPassword(), (self, value) -> self.wrapped.setPassword(value));
        DISPATCHER.addStringProperty("purgeCompletedDate", self -> self.wrapped.getPurgeCompletedDate(), (self, value) -> self.wrapped.setPurgeCompletedDate(value))
                .setMetaData(Janitor.MetaData.HELP, "purgeCompletedDate: The date the documents of the envelope were purged, as a string; writable.");
        DISPATCHER.addStringProperty("purgeRequestDate", self -> self.wrapped.getPurgeRequestDate(), (self, value) -> self.wrapped.setPurgeRequestDate(value))
                .setMetaData(Janitor.MetaData.HELP, "purgeRequestDate: The date the purge of the envelope's documents was requested, as a string; writable.");
        DISPATCHER.addStringProperty("purgeState", self -> self.wrapped.getPurgeState(), (self, value) -> self.wrapped.setPurgeState(value));
        DISPATCHER.addStringProperty("recipientsLock", self -> self.wrapped.getRecipientsLock(), (self, value) -> self.wrapped.setRecipientsLock(value))
                .setMetaData(Janitor.MetaData.HELP, "recipientsLock: \"true\" if the recipients of the envelope may not be changed; writable.");
        DISPATCHER.addStringProperty("recipientsUri", self -> self.wrapped.getRecipientsUri(), (self, value) -> self.wrapped.setRecipientsUri(value))
                .setMetaData(Janitor.MetaData.HELP, "recipientsUri: The API URI of the envelope's recipients; writable.");
        DISPATCHER.addStringProperty("sentDateTime", self -> self.wrapped.getSentDateTime(), (self, value) -> self.wrapped.setSentDateTime(value))
                .setMetaData(Janitor.MetaData.HELP, "sentDateTime: The date and time the envelope was sent, as a string; writable.");
        DISPATCHER.addStringProperty("signerCanSignOnMobile", self -> self.wrapped.getSignerCanSignOnMobile(), (self, value) -> self.wrapped.setSignerCanSignOnMobile(value))
                .setMetaData(Janitor.MetaData.HELP, "signerCanSignOnMobile: \"true\" if signers may sign on a mobile device; writable.");
        DISPATCHER.addStringProperty("signingLocation", self -> self.wrapped.getSigningLocation(), (self, value) -> self.wrapped.setSigningLocation(value))
                .setMetaData(Janitor.MetaData.HELP, "signingLocation: Where the envelope is signed, \"Online\" or \"InPerson\"; writable.");
        DISPATCHER.addStringProperty("statusChangedDateTime", self -> self.wrapped.getStatusChangedDateTime(), (self, value) -> self.wrapped.setStatusChangedDateTime(value))
                .setMetaData(Janitor.MetaData.HELP, "statusChangedDateTime: The date and time the status of the envelope last changed, as a string; writable.");
        DISPATCHER.addStringProperty("templatesUri", self -> self.wrapped.getTemplatesUri(), (self, value) -> self.wrapped.setTemplatesUri(value))
                .setMetaData(Janitor.MetaData.HELP, "templatesUri: The API URI of the templates used by the envelope; writable.");
        DISPATCHER.addStringProperty("transactionId", self -> self.wrapped.getTransactionId(), (self, value) -> self.wrapped.setTransactionId(value));
        DISPATCHER.addStringProperty("useDisclosure", self -> self.wrapped.getUseDisclosure(), (self, value) -> self.wrapped.setUseDisclosure(value))
                .setMetaData(Janitor.MetaData.HELP, "useDisclosure: \"true\" if recipients must accept the electronic record and signature disclosure; writable.");
        DISPATCHER.addStringProperty("uSigState", self -> self.wrapped.getUSigState(), (self, value) -> self.wrapped.setUSigState(value));
        DISPATCHER.addStringProperty("voidedDateTime", self -> self.wrapped.getVoidedDateTime(), (self, value) -> self.wrapped.setVoidedDateTime(value))
                .setMetaData(Janitor.MetaData.HELP, "voidedDateTime: The date and time the envelope was voided, as a string; writable.");
        DISPATCHER.addStringProperty("voidedReason", self -> self.wrapped.getVoidedReason(), (self, value) -> self.wrapped.setVoidedReason(value))
                .setMetaData(Janitor.MetaData.HELP, "voidedReason: The reason the envelope was voided; writable.");

        // Object properties
        DISPATCHER.addObjectProperty("recipients",
            self -> new DocusignRecipients(self.getWrapped().getRecipients()),
            (self, value) -> {
                if (value instanceof DocusignRecipients recipients) {
                    self.wrapped.setRecipients(recipients.getWrapped());
                } else {
                    throw new JanitorError("invalid recipients type: " + value + " (expected "+simpleClassNameOf(value)+")");
                }
            }, DocusignRecipients::new)
                .setMetaData(Janitor.MetaData.HELP, "recipients: The recipients of the envelope; writable, only a Recipients object can be assigned.");

        DISPATCHER.addMethod("addDocument", (self, process, args) -> {
            DocusignDocument document = args.getRequired(0, DocusignDocument.class);
            self.wrapped.addDocumentsItem(document.getWrapped());
            return self;
        })
                .setMetaData(Janitor.MetaData.HELP, "addDocument(document): Adds the document to the envelope and returns the envelope definition.");

        DISPATCHER.addMethod("addTemplateRole", (self, process, args) -> {
            DocusignTemplateRole role = args.getRequired(0, DocusignTemplateRole.class);
            self.wrapped.addTemplateRolesItem(role.getWrapped());
            return self;
        })
                .setMetaData(Janitor.MetaData.HELP, "addTemplateRole(role): Adds the template role to the envelope and returns the envelope definition.");

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

    /**
     * @return the wrapped DocuSign object
     */
    public EnvelopeDefinition getWrapped() {
        return wrapped;
    }

    @Override
    public String toString() {
        return "DocusignEnvelopeDefinition [wrapped=" + wrapped + "]";
    }

}
