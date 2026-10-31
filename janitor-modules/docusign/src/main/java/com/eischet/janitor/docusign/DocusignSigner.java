// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.docusign;

import com.docusign.esign.model.Signer;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;

import java.util.Objects;

/** Wrapper for the DocuSign signer. */
public class DocusignSigner extends JanitorComposed<DocusignSigner> {

    public static final DispatchTable<DocusignSigner> DISPATCHER = new DispatchTable<>();

    static {
        // String properties
        DISPATCHER.addStringProperty("email", self -> self.wrapped.getEmail(), (self, value) -> self.wrapped.setEmail(value))
                .setMetaData(Janitor.MetaData.HELP, "email: The signer's email address; writable.");
        DISPATCHER.addStringProperty("name", self -> self.wrapped.getName(), (self, value) -> self.wrapped.setName(value))
                .setMetaData(Janitor.MetaData.HELP, "name: The signer's name; writable.");
        DISPATCHER.addStringProperty("clientUserId", self -> self.wrapped.getClientUserId(), (self, value) -> self.wrapped.setClientUserId(value))
                .setMetaData(Janitor.MetaData.HELP, "clientUserId: Sender-defined ID that makes the signer an embedded recipient; writable.");
        DISPATCHER.addStringProperty("accessCode", self -> self.wrapped.getAccessCode(), (self, value) -> self.wrapped.setAccessCode(value))
                .setMetaData(Janitor.MetaData.HELP, "accessCode: Access code the signer must enter to open the envelope; writable.");
        DISPATCHER.addStringProperty("routingOrder", self -> self.wrapped.getRoutingOrder(), (self, value) -> self.wrapped.setRoutingOrder(value))
                .setMetaData(Janitor.MetaData.HELP, "routingOrder: The signer's position in the signing order, as a string; writable.");
        DISPATCHER.addStringProperty("recipientId", self -> self.wrapped.getRecipientId(), (self, value) -> self.wrapped.setRecipientId(value))
                .setMetaData(Janitor.MetaData.HELP, "recipientId: ID of the recipient within the envelope; writable.");
        DISPATCHER.addStringProperty("recipientType", self -> self.wrapped.getRecipientType(), (self, value) -> self.wrapped.setRecipientType(value))
                .setMetaData(Janitor.MetaData.HELP, "recipientType: The type of the recipient, e.g. signer; writable.");
        DISPATCHER.addStringProperty("addAccessCodeToEmail", self -> self.wrapped.getAddAccessCodeToEmail(), (self, value) -> self.wrapped.setAddAccessCodeToEmail(value))
                .setMetaData(Janitor.MetaData.HELP, "addAccessCodeToEmail: \"true\" to include the access code in the notification email; writable.");
        DISPATCHER.addStringProperty("agentCanEditEmail", self -> self.wrapped.getAgentCanEditEmail(), (self, value) -> self.wrapped.setAgentCanEditEmail(value))
                .setMetaData(Janitor.MetaData.HELP, "agentCanEditEmail: \"true\" if an agent recipient may change the signer's email address; writable.");
        DISPATCHER.addStringProperty("agentCanEditName", self -> self.wrapped.getAgentCanEditName(), (self, value) -> self.wrapped.setAgentCanEditName(value))
                .setMetaData(Janitor.MetaData.HELP, "agentCanEditName: \"true\" if an agent recipient may change the signer's name; writable.");
        DISPATCHER.addStringProperty("allowSystemOverrideForLockedRecipient", self -> self.wrapped.getAllowSystemOverrideForLockedRecipient(), (self, value) -> self.wrapped.setAllowSystemOverrideForLockedRecipient(value));
        DISPATCHER.addStringProperty("autoNavigation", self -> self.wrapped.getAutoNavigation(), (self, value) -> self.wrapped.setAutoNavigation(value));
        DISPATCHER.addStringProperty("autoRespondedReason", self -> self.wrapped.getAutoRespondedReason(), (self, value) -> self.wrapped.setAutoRespondedReason(value));
        DISPATCHER.addStringProperty("bulkRecipientsUri", self -> self.wrapped.getBulkRecipientsUri(), (self, value) -> self.wrapped.setBulkRecipientsUri(value));
        DISPATCHER.addStringProperty("bulkSendV2Recipient", self -> self.wrapped.getBulkSendV2Recipient(), (self, value) -> self.wrapped.setBulkSendV2Recipient(value));
        DISPATCHER.addStringProperty("canSignOffline", self -> self.wrapped.getCanSignOffline(), (self, value) -> self.wrapped.setCanSignOffline(value))
                .setMetaData(Janitor.MetaData.HELP, "canSignOffline: \"true\" if the signer may sign offline; writable.");
        DISPATCHER.addStringProperty("completedCount", self -> self.wrapped.getCompletedCount(), (self, value) -> self.wrapped.setCompletedCount(value));
        DISPATCHER.addStringProperty("creationReason", self -> self.wrapped.getCreationReason(), (self, value) -> self.wrapped.setCreationReason(value));
        DISPATCHER.addStringProperty("declinedDateTime", self -> self.wrapped.getDeclinedDateTime(), (self, value) -> self.wrapped.setDeclinedDateTime(value))
                .setMetaData(Janitor.MetaData.HELP, "declinedDateTime: When the signer declined to sign, as a string; writable.");
        DISPATCHER.addStringProperty("declinedReason", self -> self.wrapped.getDeclinedReason(), (self, value) -> self.wrapped.setDeclinedReason(value))
                .setMetaData(Janitor.MetaData.HELP, "declinedReason: The reason the signer gave for declining; writable.");
        DISPATCHER.addStringProperty("defaultRecipient", self -> self.wrapped.getDefaultRecipient(), (self, value) -> self.wrapped.setDefaultRecipient(value));
        DISPATCHER.addStringProperty("deliveredDateTime", self -> self.wrapped.getDeliveredDateTime(), (self, value) -> self.wrapped.setDeliveredDateTime(value))
                .setMetaData(Janitor.MetaData.HELP, "deliveredDateTime: When the envelope was delivered to the signer, as a string; writable.");
        DISPATCHER.addStringProperty("deliveryMethod", self -> self.wrapped.getDeliveryMethod(), (self, value) -> self.wrapped.setDeliveryMethod(value))
                .setMetaData(Janitor.MetaData.HELP, "deliveryMethod: How the envelope is delivered to the signer, e.g. email; writable.");
        DISPATCHER.addStringProperty("designatorId", self -> self.wrapped.getDesignatorId(), (self, value) -> self.wrapped.setDesignatorId(value));
        DISPATCHER.addStringProperty("designatorIdGuid", self -> self.wrapped.getDesignatorIdGuid(), (self, value) -> self.wrapped.setDesignatorIdGuid(value));
        DISPATCHER.addStringProperty("documentTemplateId", self -> self.wrapped.getDocumentTemplateId(), (self, value) -> self.wrapped.setDocumentTemplateId(value));
        DISPATCHER.addStringProperty("emailRecipientPostSigningURL", self -> self.wrapped.getEmailRecipientPostSigningURL(), (self, value) -> self.wrapped.setEmailRecipientPostSigningURL(value));
        DISPATCHER.addStringProperty("embeddedRecipientStartURL", self -> self.wrapped.getEmbeddedRecipientStartURL(), (self, value) -> self.wrapped.setEmbeddedRecipientStartURL(value));
        DISPATCHER.addStringProperty("faxNumber", self -> self.wrapped.getFaxNumber(), (self, value) -> self.wrapped.setFaxNumber(value))
                .setMetaData(Janitor.MetaData.HELP, "faxNumber: The signer's fax number; writable.");
        DISPATCHER.addStringProperty("firstName", self -> self.wrapped.getFirstName(), (self, value) -> self.wrapped.setFirstName(value))
                .setMetaData(Janitor.MetaData.HELP, "firstName: The signer's first name; writable.");
        DISPATCHER.addStringProperty("fullName", self -> self.wrapped.getFullName(), (self, value) -> self.wrapped.setFullName(value))
                .setMetaData(Janitor.MetaData.HELP, "fullName: The signer's full name; writable.");
        DISPATCHER.addStringProperty("idCheckConfigurationName", self -> self.wrapped.getIdCheckConfigurationName(), (self, value) -> self.wrapped.setIdCheckConfigurationName(value));
        DISPATCHER.addStringProperty("inheritEmailNotificationConfiguration", self -> self.wrapped.getInheritEmailNotificationConfiguration(), (self, value) -> self.wrapped.setInheritEmailNotificationConfiguration(value));
        DISPATCHER.addStringProperty("isBulkRecipient", self -> self.wrapped.getIsBulkRecipient(), (self, value) -> self.wrapped.setIsBulkRecipient(value));
        DISPATCHER.addStringProperty("lastName", self -> self.wrapped.getLastName(), (self, value) -> self.wrapped.setLastName(value))
                .setMetaData(Janitor.MetaData.HELP, "lastName: The signer's last name; writable.");
        DISPATCHER.addStringProperty("lockedRecipientPhoneAuthEditable", self -> self.wrapped.getLockedRecipientPhoneAuthEditable(), (self, value) -> self.wrapped.setLockedRecipientPhoneAuthEditable(value));
        DISPATCHER.addStringProperty("lockedRecipientSmsEditable", self -> self.wrapped.getLockedRecipientSmsEditable(), (self, value) -> self.wrapped.setLockedRecipientSmsEditable(value));
        DISPATCHER.addStringProperty("notaryId", self -> self.wrapped.getNotaryId(), (self, value) -> self.wrapped.setNotaryId(value));
        DISPATCHER.addStringProperty("notarySignerEmailSent", self -> self.wrapped.getNotarySignerEmailSent(), (self, value) -> self.wrapped.setNotarySignerEmailSent(value));
        DISPATCHER.addStringProperty("note", self -> self.wrapped.getNote(), (self, value) -> self.wrapped.setNote(value))
                .setMetaData(Janitor.MetaData.HELP, "note: A note shown only to this signer; writable.");
        DISPATCHER.addStringProperty("recipientIdGuid", self -> self.wrapped.getRecipientIdGuid(), (self, value) -> self.wrapped.setRecipientIdGuid(value))
                .setMetaData(Janitor.MetaData.HELP, "recipientIdGuid: The recipient's ID as a GUID; writable.");
        DISPATCHER.addStringProperty("recipientSuppliesTabs", self -> self.wrapped.getRecipientSuppliesTabs(), (self, value) -> self.wrapped.setRecipientSuppliesTabs(value));
        DISPATCHER.addStringProperty("requireIdLookup", self -> self.wrapped.getRequireIdLookup(), (self, value) -> self.wrapped.setRequireIdLookup(value));
        DISPATCHER.addStringProperty("requireSignerCertificate", self -> self.wrapped.getRequireSignerCertificate(), (self, value) -> self.wrapped.setRequireSignerCertificate(value));
        DISPATCHER.addStringProperty("requireSignOnPaper", self -> self.wrapped.getRequireSignOnPaper(), (self, value) -> self.wrapped.setRequireSignOnPaper(value))
                .setMetaData(Janitor.MetaData.HELP, "requireSignOnPaper: \"true\" if the signer must sign on paper; writable.");
        DISPATCHER.addStringProperty("requireUploadSignature", self -> self.wrapped.getRequireUploadSignature(), (self, value) -> self.wrapped.setRequireUploadSignature(value));
        DISPATCHER.addStringProperty("roleName", self -> self.wrapped.getRoleName(), (self, value) -> self.wrapped.setRoleName(value))
                .setMetaData(Janitor.MetaData.HELP, "roleName: Name of the template role the signer fills; writable.");
        DISPATCHER.addStringProperty("sentDateTime", self -> self.wrapped.getSentDateTime(), (self, value) -> self.wrapped.setSentDateTime(value))
                .setMetaData(Janitor.MetaData.HELP, "sentDateTime: When the envelope was sent to the signer, as a string; writable.");
        DISPATCHER.addStringProperty("signedDateTime", self -> self.wrapped.getSignedDateTime(), (self, value) -> self.wrapped.setSignedDateTime(value))
                .setMetaData(Janitor.MetaData.HELP, "signedDateTime: When the signer signed, as a string; writable.");
        DISPATCHER.addStringProperty("signInEachLocation", self -> self.wrapped.getSignInEachLocation(), (self, value) -> self.wrapped.setSignInEachLocation(value));
        DISPATCHER.addStringProperty("signingGroupId", self -> self.wrapped.getSigningGroupId(), (self, value) -> self.wrapped.setSigningGroupId(value))
                .setMetaData(Janitor.MetaData.HELP, "signingGroupId: ID of the signing group that signs in place of a single person; writable.");
        DISPATCHER.addStringProperty("signingGroupName", self -> self.wrapped.getSigningGroupName(), (self, value) -> self.wrapped.setSigningGroupName(value))
                .setMetaData(Janitor.MetaData.HELP, "signingGroupName: Name of the signing group; writable.");
        DISPATCHER.addStringProperty("status", self -> self.wrapped.getStatus(), (self, value) -> self.wrapped.setStatus(value))
                .setMetaData(Janitor.MetaData.HELP, "status: The signer's status, e.g. sent, delivered, completed or declined; writable.");
        DISPATCHER.addStringProperty("statusCode", self -> self.wrapped.getStatusCode(), (self, value) -> self.wrapped.setStatusCode(value));
        DISPATCHER.addStringProperty("suppressEmails", self -> self.wrapped.getSuppressEmails(), (self, value) -> self.wrapped.setSuppressEmails(value))
                .setMetaData(Janitor.MetaData.HELP, "suppressEmails: \"true\" to send no emails to the signer; writable.");
        DISPATCHER.addStringProperty("templateLocked", self -> self.wrapped.getTemplateLocked(), (self, value) -> self.wrapped.setTemplateLocked(value));
        DISPATCHER.addStringProperty("templateRequired", self -> self.wrapped.getTemplateRequired(), (self, value) -> self.wrapped.setTemplateRequired(value));
        DISPATCHER.addStringProperty("totalTabCount", self -> self.wrapped.getTotalTabCount(), (self, value) -> self.wrapped.setTotalTabCount(value))
                .setMetaData(Janitor.MetaData.HELP, "totalTabCount: The number of tabs assigned to the signer, as a string; writable.");
        DISPATCHER.addStringProperty("userId", self -> self.wrapped.getUserId(), (self, value) -> self.wrapped.setUserId(value))
                .setMetaData(Janitor.MetaData.HELP, "userId: The signer's DocuSign user ID; writable.");
        DISPATCHER.addStringProperty("webFormRecipientViewId", self -> self.wrapped.getWebFormRecipientViewId(), (self, value) -> self.wrapped.setWebFormRecipientViewId(value));

        // Object properties
        DISPATCHER.addObjectProperty("tabs",
                self -> new DocusignTabs(self.wrapped.getTabs()),
                (self, tabs) -> self.wrapped.setTabs(Objects.requireNonNull(tabs).getWrapped()),
                DocusignTabs::new)
                .setMetaData(Janitor.MetaData.HELP, "tabs: The signer's tabs, i.e. the fields to sign or fill in; writable, must not be null.");

        // TODO: Add wrappers for complex object properties:
        // - PropertyMetadata: accessCodeMetadata, deliveryMethodMetadata, emailMetadata, faxNumberMetadata, firstNameMetadata, fullNameMetadata, idCheckConfigurationNameMetadata, isBulkRecipientMetadata, lastNameMetadata, nameMetadata, noteMetadata, recipientTypeMetadata, requireIdLookupMetadata, routingOrderMetadata, signInEachLocationMetadata, signingGroupIdMetadata
        // - List<RecipientAdditionalNotification>: additionalNotifications
        // - List<ConsentDetails>: consentDetailsList
        // - List<String>: customFields, excludedDocuments
        // - DelegationInfo: delegatedBy
        // - List<DelegationInfo>: delegatedTo
        // - List<DocumentVisibility>: documentVisibility
        // - RecipientEmailNotification: emailNotification
        // - ErrorDetails: errorDetails
        // - IdCheckInformationInput: idCheckInformationInput
        // - RecipientIdentityVerification: identityVerification
        // - OfflineAttributes: offlineAttributes
        // - RecipientPhoneAuthentication: phoneAuthentication
        // - RecipientPhoneNumber: phoneNumber
        // - RecipientProofFile: proofFile
        // - List<RecipientAttachment>: recipientAttachments
        // - AuthenticationStatus: recipientAuthenticationStatus
        // - List<FeatureAvailableMetadata>: recipientFeatureMetadata
        // - List<RecipientSignatureProvider>: recipientSignatureProviders
        // - RecipientSignatureInformation: signatureInfo
        // - List<UserInfo>: signingGroupUsers
        // - RecipientSMSAuthentication: smsAuthentication
        // - List<SocialAuthentication>: socialAuthentications
    }

    private final Signer wrapped;

    public DocusignSigner() {
        super(DISPATCHER);
        this.wrapped = new Signer();
    }

    /**
     * @return the wrapped DocuSign object
     */
    public Signer getWrapped() {
        return wrapped;
    }

    @Override
    public String toString() {
        return "DocusignSigner [wrapped=" + wrapped + "]";
    }
}
