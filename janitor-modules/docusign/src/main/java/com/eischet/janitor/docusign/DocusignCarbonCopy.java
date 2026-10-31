// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.docusign;

import com.docusign.esign.model.CarbonCopy;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;

/** Wrapper for the DocuSign carbon copy. */
public class DocusignCarbonCopy extends JanitorComposed<DocusignCarbonCopy> {

    public static final DispatchTable<DocusignCarbonCopy> DISPATCHER = new DispatchTable<>();

    static {
        // String properties
        DISPATCHER.addStringProperty("email", self -> self.wrapped.getEmail(), (self, value) -> self.wrapped.setEmail(value))
                .setMetaData(Janitor.MetaData.HELP, "email: The email address of the recipient; writable.");
        DISPATCHER.addStringProperty("name", self -> self.wrapped.getName(), (self, value) -> self.wrapped.setName(value))
                .setMetaData(Janitor.MetaData.HELP, "name: The full legal name of the recipient; writable.");
        DISPATCHER.addStringProperty("recipientId", self -> self.wrapped.getRecipientId(), (self, value) -> self.wrapped.setRecipientId(value))
                .setMetaData(Janitor.MetaData.HELP, "recipientId: The ID of the recipient within the envelope, which tabs use to refer to it; writable.");
        DISPATCHER.addStringProperty("routingOrder", self -> self.wrapped.getRoutingOrder(), (self, value) -> self.wrapped.setRoutingOrder(value))
                .setMetaData(Janitor.MetaData.HELP, "routingOrder: The position of the recipient in the envelope's routing order, as a string, e.g. \"1\"; writable.");
        DISPATCHER.addStringProperty("accessCode", self -> self.wrapped.getAccessCode(), (self, value) -> self.wrapped.setAccessCode(value))
                .setMetaData(Janitor.MetaData.HELP, "accessCode: The access code that the recipient must enter to view the envelope; writable.");
        DISPATCHER.addStringProperty("addAccessCodeToEmail", self -> self.wrapped.getAddAccessCodeToEmail(), (self, value) -> self.wrapped.setAddAccessCodeToEmail(value))
                .setMetaData(Janitor.MetaData.HELP, "addAccessCodeToEmail: \"true\" to include the access code in the email sent to the recipient; writable.");
        DISPATCHER.addStringProperty("agentCanEditEmail", self -> self.wrapped.getAgentCanEditEmail(), (self, value) -> self.wrapped.setAgentCanEditEmail(value))
                .setMetaData(Janitor.MetaData.HELP, "agentCanEditEmail: \"true\" if an agent recipient may change the email address of this recipient; writable.");
        DISPATCHER.addStringProperty("agentCanEditName", self -> self.wrapped.getAgentCanEditName(), (self, value) -> self.wrapped.setAgentCanEditName(value))
                .setMetaData(Janitor.MetaData.HELP, "agentCanEditName: \"true\" if an agent recipient may change the name of this recipient; writable.");
        DISPATCHER.addStringProperty("allowSystemOverrideForLockedRecipient", self -> self.wrapped.getAllowSystemOverrideForLockedRecipient(), (self, value) -> self.wrapped.setAllowSystemOverrideForLockedRecipient(value));
        DISPATCHER.addStringProperty("autoRespondedReason", self -> self.wrapped.getAutoRespondedReason(), (self, value) -> self.wrapped.setAutoRespondedReason(value));
        DISPATCHER.addStringProperty("bulkSendV2Recipient", self -> self.wrapped.getBulkSendV2Recipient(), (self, value) -> self.wrapped.setBulkSendV2Recipient(value));
        DISPATCHER.addStringProperty("clientUserId", self -> self.wrapped.getClientUserId(), (self, value) -> self.wrapped.setClientUserId(value))
                .setMetaData(Janitor.MetaData.HELP, "clientUserId: The sender's own ID for the recipient; if it is set, the recipient is embedded instead of remote; writable.");
        DISPATCHER.addStringProperty("completedCount", self -> self.wrapped.getCompletedCount(), (self, value) -> self.wrapped.setCompletedCount(value));
        DISPATCHER.addStringProperty("declinedDateTime", self -> self.wrapped.getDeclinedDateTime(), (self, value) -> self.wrapped.setDeclinedDateTime(value))
                .setMetaData(Janitor.MetaData.HELP, "declinedDateTime: The date and time the recipient declined, as a string; writable.");
        DISPATCHER.addStringProperty("declinedReason", self -> self.wrapped.getDeclinedReason(), (self, value) -> self.wrapped.setDeclinedReason(value))
                .setMetaData(Janitor.MetaData.HELP, "declinedReason: The reason the recipient gave for declining; writable.");
        DISPATCHER.addStringProperty("deliveredDateTime", self -> self.wrapped.getDeliveredDateTime(), (self, value) -> self.wrapped.setDeliveredDateTime(value))
                .setMetaData(Janitor.MetaData.HELP, "deliveredDateTime: The date and time the envelope was delivered to the recipient, as a string; writable.");
        DISPATCHER.addStringProperty("deliveryMethod", self -> self.wrapped.getDeliveryMethod(), (self, value) -> self.wrapped.setDeliveryMethod(value));
        DISPATCHER.addStringProperty("designatorId", self -> self.wrapped.getDesignatorId(), (self, value) -> self.wrapped.setDesignatorId(value));
        DISPATCHER.addStringProperty("designatorIdGuid", self -> self.wrapped.getDesignatorIdGuid(), (self, value) -> self.wrapped.setDesignatorIdGuid(value));
        DISPATCHER.addStringProperty("documentTemplateId", self -> self.wrapped.getDocumentTemplateId(), (self, value) -> self.wrapped.setDocumentTemplateId(value));
        DISPATCHER.addStringProperty("emailRecipientPostSigningURL", self -> self.wrapped.getEmailRecipientPostSigningURL(), (self, value) -> self.wrapped.setEmailRecipientPostSigningURL(value));
        DISPATCHER.addStringProperty("embeddedRecipientStartURL", self -> self.wrapped.getEmbeddedRecipientStartURL(), (self, value) -> self.wrapped.setEmbeddedRecipientStartURL(value))
                .setMetaData(Janitor.MetaData.HELP, "embeddedRecipientStartURL: The URL that an embedded recipient is sent to from the link in the DocuSign email, or \"SIGN_AT_DOCUSIGN\" to sign directly at DocuSign; writable.");
        DISPATCHER.addStringProperty("faxNumber", self -> self.wrapped.getFaxNumber(), (self, value) -> self.wrapped.setFaxNumber(value))
                .setMetaData(Janitor.MetaData.HELP, "faxNumber: The fax number of the recipient; writable.");
        DISPATCHER.addStringProperty("firstName", self -> self.wrapped.getFirstName(), (self, value) -> self.wrapped.setFirstName(value))
                .setMetaData(Janitor.MetaData.HELP, "firstName: The first name of the recipient; writable.");
        DISPATCHER.addStringProperty("fullName", self -> self.wrapped.getFullName(), (self, value) -> self.wrapped.setFullName(value))
                .setMetaData(Janitor.MetaData.HELP, "fullName: The full name of the recipient; writable.");
        DISPATCHER.addStringProperty("idCheckConfigurationName", self -> self.wrapped.getIdCheckConfigurationName(), (self, value) -> self.wrapped.setIdCheckConfigurationName(value))
                .setMetaData(Janitor.MetaData.HELP, "idCheckConfigurationName: The name of the authentication check that the recipient must pass, as configured in the account; writable.");
        DISPATCHER.addStringProperty("inheritEmailNotificationConfiguration", self -> self.wrapped.getInheritEmailNotificationConfiguration(), (self, value) -> self.wrapped.setInheritEmailNotificationConfiguration(value));
        DISPATCHER.addStringProperty("lastName", self -> self.wrapped.getLastName(), (self, value) -> self.wrapped.setLastName(value))
                .setMetaData(Janitor.MetaData.HELP, "lastName: The last name of the recipient; writable.");
        DISPATCHER.addStringProperty("linkedAccountConfigurationId", self -> self.wrapped.getLinkedAccountConfigurationId(), (self, value) -> self.wrapped.setLinkedAccountConfigurationId(value));
        DISPATCHER.addStringProperty("lockedRecipientPhoneAuthEditable", self -> self.wrapped.getLockedRecipientPhoneAuthEditable(), (self, value) -> self.wrapped.setLockedRecipientPhoneAuthEditable(value));
        DISPATCHER.addStringProperty("lockedRecipientSmsEditable", self -> self.wrapped.getLockedRecipientSmsEditable(), (self, value) -> self.wrapped.setLockedRecipientSmsEditable(value));
        DISPATCHER.addStringProperty("note", self -> self.wrapped.getNote(), (self, value) -> self.wrapped.setNote(value))
                .setMetaData(Janitor.MetaData.HELP, "note: A note for this recipient only, which is shown in the email and while signing; writable.");
        DISPATCHER.addStringProperty("recipientIdGuid", self -> self.wrapped.getRecipientIdGuid(), (self, value) -> self.wrapped.setRecipientIdGuid(value))
                .setMetaData(Janitor.MetaData.HELP, "recipientIdGuid: The globally unique ID of the recipient; writable.");
        DISPATCHER.addStringProperty("recipientType", self -> self.wrapped.getRecipientType(), (self, value) -> self.wrapped.setRecipientType(value))
                .setMetaData(Janitor.MetaData.HELP, "recipientType: The type of the recipient; writable.");
        DISPATCHER.addStringProperty("requireIdLookup", self -> self.wrapped.getRequireIdLookup(), (self, value) -> self.wrapped.setRequireIdLookup(value))
                .setMetaData(Janitor.MetaData.HELP, "requireIdLookup: \"true\" if the recipient must verify their identity with the configured ID check; writable.");
        DISPATCHER.addStringProperty("roleName", self -> self.wrapped.getRoleName(), (self, value) -> self.wrapped.setRoleName(value))
                .setMetaData(Janitor.MetaData.HELP, "roleName: The role name of the recipient, which is required for template recipients; writable.");
        DISPATCHER.addStringProperty("sentDateTime", self -> self.wrapped.getSentDateTime(), (self, value) -> self.wrapped.setSentDateTime(value))
                .setMetaData(Janitor.MetaData.HELP, "sentDateTime: The date and time the envelope was sent to the recipient, as a string; writable.");
        DISPATCHER.addStringProperty("signedDateTime", self -> self.wrapped.getSignedDateTime(), (self, value) -> self.wrapped.setSignedDateTime(value));
        DISPATCHER.addStringProperty("signingGroupId", self -> self.wrapped.getSigningGroupId(), (self, value) -> self.wrapped.setSigningGroupId(value))
                .setMetaData(Janitor.MetaData.HELP, "signingGroupId: The ID of the signing group that receives the copy; writable.");
        DISPATCHER.addStringProperty("signingGroupName", self -> self.wrapped.getSigningGroupName(), (self, value) -> self.wrapped.setSigningGroupName(value))
                .setMetaData(Janitor.MetaData.HELP, "signingGroupName: The display name of the signing group; writable.");
        DISPATCHER.addStringProperty("status", self -> self.wrapped.getStatus(), (self, value) -> self.wrapped.setStatus(value))
                .setMetaData(Janitor.MetaData.HELP, "status: The status of the recipient, e.g. \"sent\" or \"completed\"; writable.");
        DISPATCHER.addStringProperty("statusCode", self -> self.wrapped.getStatusCode(), (self, value) -> self.wrapped.setStatusCode(value));
        DISPATCHER.addStringProperty("suppressEmails", self -> self.wrapped.getSuppressEmails(), (self, value) -> self.wrapped.setSuppressEmails(value))
                .setMetaData(Janitor.MetaData.HELP, "suppressEmails: \"true\" to send no email notifications to the recipient; writable.");
        DISPATCHER.addStringProperty("templateLocked", self -> self.wrapped.getTemplateLocked(), (self, value) -> self.wrapped.setTemplateLocked(value))
                .setMetaData(Janitor.MetaData.HELP, "templateLocked: \"true\" if the sender may not change the recipient taken from a template; writable.");
        DISPATCHER.addStringProperty("templateRequired", self -> self.wrapped.getTemplateRequired(), (self, value) -> self.wrapped.setTemplateRequired(value))
                .setMetaData(Janitor.MetaData.HELP, "templateRequired: \"true\" if the sender may not remove the recipient taken from a template; writable.");
        DISPATCHER.addStringProperty("totalTabCount", self -> self.wrapped.getTotalTabCount(), (self, value) -> self.wrapped.setTotalTabCount(value));
        DISPATCHER.addStringProperty("userId", self -> self.wrapped.getUserId(), (self, value) -> self.wrapped.setUserId(value));
        DISPATCHER.addStringProperty("webFormRecipientViewId", self -> self.wrapped.getWebFormRecipientViewId(), (self, value) -> self.wrapped.setWebFormRecipientViewId(value));

        // TODO: Add wrappers for complex object properties:
        // - PropertyMetadata: accessCodeMetadata, deliveryMethodMetadata, emailMetadata, faxNumberMetadata, firstNameMetadata, fullNameMetadata, idCheckConfigurationNameMetadata, lastNameMetadata, nameMetadata, noteMetadata, recipientTypeMetadata, requireIdLookupMetadata, routingOrderMetadata, signingGroupIdMetadata
        // - List<RecipientAdditionalNotification>: additionalNotifications
        // - List<ConsentDetails>: consentDetailsList
        // - List<String>: customFields, excludedDocuments
        // - List<DocumentVisibility>: documentVisibility
        // - List<RecipientAttachment>: recipientAttachments
        // - List<FeatureAvailableMetadata>: recipientFeatureMetadata
        // - List<UserInfo>: signingGroupUsers
        // - List<SocialAuthentication>: socialAuthentications
    }

    private final CarbonCopy wrapped;

    public DocusignCarbonCopy() {
        super(DISPATCHER);
        this.wrapped = new CarbonCopy();
    }

    /**
     * @return the wrapped DocuSign object
     */
    public CarbonCopy getWrapped() {
        return wrapped;
    }

    @Override
    public String toString() {
        return "DocusignCarbonCopy [wrapped=" + wrapped + "]";
    }

}
