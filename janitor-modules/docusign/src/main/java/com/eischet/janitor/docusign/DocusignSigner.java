package com.eischet.janitor.docusign;

import com.docusign.esign.model.Signer;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;

import java.util.Objects;

public class DocusignSigner extends JanitorComposed<DocusignSigner> {

    public static final DispatchTable<DocusignSigner> DISPATCHER = new DispatchTable<>();

    static {
        // String properties
        DISPATCHER.addStringProperty("email", self -> self.wrapped.getEmail(), (self, value) -> self.wrapped.setEmail(value));
        DISPATCHER.addStringProperty("name", self -> self.wrapped.getName(), (self, value) -> self.wrapped.setName(value));
        DISPATCHER.addStringProperty("clientUserId", self -> self.wrapped.getClientUserId(), (self, value) -> self.wrapped.setClientUserId(value));
        DISPATCHER.addStringProperty("accessCode", self -> self.wrapped.getAccessCode(), (self, value) -> self.wrapped.setAccessCode(value));
        DISPATCHER.addStringProperty("routingOrder", self -> self.wrapped.getRoutingOrder(), (self, value) -> self.wrapped.setRoutingOrder(value));
        DISPATCHER.addStringProperty("recipientId", self -> self.wrapped.getRecipientId(), (self, value) -> self.wrapped.setRecipientId(value));
        DISPATCHER.addStringProperty("recipientType", self -> self.wrapped.getRecipientType(), (self, value) -> self.wrapped.setRecipientType(value));
        DISPATCHER.addStringProperty("addAccessCodeToEmail", self -> self.wrapped.getAddAccessCodeToEmail(), (self, value) -> self.wrapped.setAddAccessCodeToEmail(value));
        DISPATCHER.addStringProperty("agentCanEditEmail", self -> self.wrapped.getAgentCanEditEmail(), (self, value) -> self.wrapped.setAgentCanEditEmail(value));
        DISPATCHER.addStringProperty("agentCanEditName", self -> self.wrapped.getAgentCanEditName(), (self, value) -> self.wrapped.setAgentCanEditName(value));
        DISPATCHER.addStringProperty("allowSystemOverrideForLockedRecipient", self -> self.wrapped.getAllowSystemOverrideForLockedRecipient(), (self, value) -> self.wrapped.setAllowSystemOverrideForLockedRecipient(value));
        DISPATCHER.addStringProperty("autoNavigation", self -> self.wrapped.getAutoNavigation(), (self, value) -> self.wrapped.setAutoNavigation(value));
        DISPATCHER.addStringProperty("autoRespondedReason", self -> self.wrapped.getAutoRespondedReason(), (self, value) -> self.wrapped.setAutoRespondedReason(value));
        DISPATCHER.addStringProperty("bulkRecipientsUri", self -> self.wrapped.getBulkRecipientsUri(), (self, value) -> self.wrapped.setBulkRecipientsUri(value));
        DISPATCHER.addStringProperty("bulkSendV2Recipient", self -> self.wrapped.getBulkSendV2Recipient(), (self, value) -> self.wrapped.setBulkSendV2Recipient(value));
        DISPATCHER.addStringProperty("canSignOffline", self -> self.wrapped.getCanSignOffline(), (self, value) -> self.wrapped.setCanSignOffline(value));
        DISPATCHER.addStringProperty("completedCount", self -> self.wrapped.getCompletedCount(), (self, value) -> self.wrapped.setCompletedCount(value));
        DISPATCHER.addStringProperty("creationReason", self -> self.wrapped.getCreationReason(), (self, value) -> self.wrapped.setCreationReason(value));
        DISPATCHER.addStringProperty("declinedDateTime", self -> self.wrapped.getDeclinedDateTime(), (self, value) -> self.wrapped.setDeclinedDateTime(value));
        DISPATCHER.addStringProperty("declinedReason", self -> self.wrapped.getDeclinedReason(), (self, value) -> self.wrapped.setDeclinedReason(value));
        DISPATCHER.addStringProperty("defaultRecipient", self -> self.wrapped.getDefaultRecipient(), (self, value) -> self.wrapped.setDefaultRecipient(value));
        DISPATCHER.addStringProperty("deliveredDateTime", self -> self.wrapped.getDeliveredDateTime(), (self, value) -> self.wrapped.setDeliveredDateTime(value));
        DISPATCHER.addStringProperty("deliveryMethod", self -> self.wrapped.getDeliveryMethod(), (self, value) -> self.wrapped.setDeliveryMethod(value));
        DISPATCHER.addStringProperty("designatorId", self -> self.wrapped.getDesignatorId(), (self, value) -> self.wrapped.setDesignatorId(value));
        DISPATCHER.addStringProperty("designatorIdGuid", self -> self.wrapped.getDesignatorIdGuid(), (self, value) -> self.wrapped.setDesignatorIdGuid(value));
        DISPATCHER.addStringProperty("documentTemplateId", self -> self.wrapped.getDocumentTemplateId(), (self, value) -> self.wrapped.setDocumentTemplateId(value));
        DISPATCHER.addStringProperty("emailRecipientPostSigningURL", self -> self.wrapped.getEmailRecipientPostSigningURL(), (self, value) -> self.wrapped.setEmailRecipientPostSigningURL(value));
        DISPATCHER.addStringProperty("embeddedRecipientStartURL", self -> self.wrapped.getEmbeddedRecipientStartURL(), (self, value) -> self.wrapped.setEmbeddedRecipientStartURL(value));
        DISPATCHER.addStringProperty("faxNumber", self -> self.wrapped.getFaxNumber(), (self, value) -> self.wrapped.setFaxNumber(value));
        DISPATCHER.addStringProperty("firstName", self -> self.wrapped.getFirstName(), (self, value) -> self.wrapped.setFirstName(value));
        DISPATCHER.addStringProperty("fullName", self -> self.wrapped.getFullName(), (self, value) -> self.wrapped.setFullName(value));
        DISPATCHER.addStringProperty("idCheckConfigurationName", self -> self.wrapped.getIdCheckConfigurationName(), (self, value) -> self.wrapped.setIdCheckConfigurationName(value));
        DISPATCHER.addStringProperty("inheritEmailNotificationConfiguration", self -> self.wrapped.getInheritEmailNotificationConfiguration(), (self, value) -> self.wrapped.setInheritEmailNotificationConfiguration(value));
        DISPATCHER.addStringProperty("isBulkRecipient", self -> self.wrapped.getIsBulkRecipient(), (self, value) -> self.wrapped.setIsBulkRecipient(value));
        DISPATCHER.addStringProperty("lastName", self -> self.wrapped.getLastName(), (self, value) -> self.wrapped.setLastName(value));
        DISPATCHER.addStringProperty("lockedRecipientPhoneAuthEditable", self -> self.wrapped.getLockedRecipientPhoneAuthEditable(), (self, value) -> self.wrapped.setLockedRecipientPhoneAuthEditable(value));
        DISPATCHER.addStringProperty("lockedRecipientSmsEditable", self -> self.wrapped.getLockedRecipientSmsEditable(), (self, value) -> self.wrapped.setLockedRecipientSmsEditable(value));
        DISPATCHER.addStringProperty("notaryId", self -> self.wrapped.getNotaryId(), (self, value) -> self.wrapped.setNotaryId(value));
        DISPATCHER.addStringProperty("notarySignerEmailSent", self -> self.wrapped.getNotarySignerEmailSent(), (self, value) -> self.wrapped.setNotarySignerEmailSent(value));
        DISPATCHER.addStringProperty("note", self -> self.wrapped.getNote(), (self, value) -> self.wrapped.setNote(value));
        DISPATCHER.addStringProperty("recipientIdGuid", self -> self.wrapped.getRecipientIdGuid(), (self, value) -> self.wrapped.setRecipientIdGuid(value));
        DISPATCHER.addStringProperty("recipientSuppliesTabs", self -> self.wrapped.getRecipientSuppliesTabs(), (self, value) -> self.wrapped.setRecipientSuppliesTabs(value));
        DISPATCHER.addStringProperty("requireIdLookup", self -> self.wrapped.getRequireIdLookup(), (self, value) -> self.wrapped.setRequireIdLookup(value));
        DISPATCHER.addStringProperty("requireSignerCertificate", self -> self.wrapped.getRequireSignerCertificate(), (self, value) -> self.wrapped.setRequireSignerCertificate(value));
        DISPATCHER.addStringProperty("requireSignOnPaper", self -> self.wrapped.getRequireSignOnPaper(), (self, value) -> self.wrapped.setRequireSignOnPaper(value));
        DISPATCHER.addStringProperty("requireUploadSignature", self -> self.wrapped.getRequireUploadSignature(), (self, value) -> self.wrapped.setRequireUploadSignature(value));
        DISPATCHER.addStringProperty("roleName", self -> self.wrapped.getRoleName(), (self, value) -> self.wrapped.setRoleName(value));
        DISPATCHER.addStringProperty("sentDateTime", self -> self.wrapped.getSentDateTime(), (self, value) -> self.wrapped.setSentDateTime(value));
        DISPATCHER.addStringProperty("signedDateTime", self -> self.wrapped.getSignedDateTime(), (self, value) -> self.wrapped.setSignedDateTime(value));
        DISPATCHER.addStringProperty("signInEachLocation", self -> self.wrapped.getSignInEachLocation(), (self, value) -> self.wrapped.setSignInEachLocation(value));
        DISPATCHER.addStringProperty("signingGroupId", self -> self.wrapped.getSigningGroupId(), (self, value) -> self.wrapped.setSigningGroupId(value));
        DISPATCHER.addStringProperty("signingGroupName", self -> self.wrapped.getSigningGroupName(), (self, value) -> self.wrapped.setSigningGroupName(value));
        DISPATCHER.addStringProperty("status", self -> self.wrapped.getStatus(), (self, value) -> self.wrapped.setStatus(value));
        DISPATCHER.addStringProperty("statusCode", self -> self.wrapped.getStatusCode(), (self, value) -> self.wrapped.setStatusCode(value));
        DISPATCHER.addStringProperty("suppressEmails", self -> self.wrapped.getSuppressEmails(), (self, value) -> self.wrapped.setSuppressEmails(value));
        DISPATCHER.addStringProperty("templateLocked", self -> self.wrapped.getTemplateLocked(), (self, value) -> self.wrapped.setTemplateLocked(value));
        DISPATCHER.addStringProperty("templateRequired", self -> self.wrapped.getTemplateRequired(), (self, value) -> self.wrapped.setTemplateRequired(value));
        DISPATCHER.addStringProperty("totalTabCount", self -> self.wrapped.getTotalTabCount(), (self, value) -> self.wrapped.setTotalTabCount(value));
        DISPATCHER.addStringProperty("userId", self -> self.wrapped.getUserId(), (self, value) -> self.wrapped.setUserId(value));
        DISPATCHER.addStringProperty("webFormRecipientViewId", self -> self.wrapped.getWebFormRecipientViewId(), (self, value) -> self.wrapped.setWebFormRecipientViewId(value));

        // Object properties
        DISPATCHER.addObjectProperty("tabs",
                self -> new DocusignTabs(self.wrapped.getTabs()),
                (self, tabs) -> self.wrapped.setTabs(Objects.requireNonNull(tabs).getWrapped()),
                DocusignTabs::new);

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

    public Signer getWrapped() {
        return wrapped;
    }

    @Override
    public String toString() {
        return "DocusignSigner [wrapped=" + wrapped + "]";
    }
}
