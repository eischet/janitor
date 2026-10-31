// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.docusign;

import com.docusign.esign.model.*;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;

import java.util.Objects;

/** Wrapper for the DocuSign template role. */
public class DocusignTemplateRole extends JanitorComposed<DocusignTemplateRole> {

    public static DispatchTable<DocusignTemplateRole> DISPATCH = new DispatchTable<>();

    static {
        DISPATCH.addStringProperty("accessCode", self -> self.wrapped.getAccessCode(), (self, value) -> self.wrapped.setAccessCode(value))
                .setMetaData(Janitor.MetaData.HELP, "accessCode: Access code the recipient must enter to open the envelope; writable.");
        DISPATCH.addStringProperty("clientUserId", self -> self.wrapped.getClientUserId(), (self, value) -> self.wrapped.setClientUserId(value))
                .setMetaData(Janitor.MetaData.HELP, "clientUserId: Sender-defined ID that makes the recipient an embedded recipient; writable.");
        DISPATCH.addStringProperty("defaultRecipient", self -> self.wrapped.getDefaultRecipient(), (self, value) -> self.wrapped.setDefaultRecipient(value));
        DISPATCH.addStringProperty("deliveryMethod", self -> self.wrapped.getDeliveryMethod(), (self, value) -> self.wrapped.setDeliveryMethod(value))
                .setMetaData(Janitor.MetaData.HELP, "deliveryMethod: How the envelope is delivered to the recipient, e.g. email; writable.");
        DISPATCH.addStringProperty("email", self ->  self.wrapped.getEmail(), (self, value) -> self.wrapped.setEmail(value))
                .setMetaData(Janitor.MetaData.HELP, "email: The recipient's email address; writable.");
        DISPATCH.addStringProperty("embeddedRecipientStartURL", self -> self.wrapped.getEmbeddedRecipientStartURL(), (self, value) -> self.wrapped.setEmbeddedRecipientStartURL(value));
        DISPATCH.addStringProperty("inPersonSignerName", self -> self.wrapped.getInPersonSignerName(), (self, value) -> self.wrapped.setInPersonSignerName(value))
                .setMetaData(Janitor.MetaData.HELP, "inPersonSignerName: Name of the person who signs in person; writable.");
        DISPATCH.addStringProperty("name", self -> self.wrapped.getName(), (self, value) -> self.wrapped.setName(value))
                .setMetaData(Janitor.MetaData.HELP, "name: The recipient's name; writable.");
        DISPATCH.addStringProperty("signingGroupId", self -> self.wrapped.getSigningGroupId(), (self, value) -> self.wrapped.setSigningGroupId(value))
                .setMetaData(Janitor.MetaData.HELP, "signingGroupId: ID of the signing group that fills the role in place of a single person; writable.");
        DISPATCH.addStringProperty("roleName", self -> self.wrapped.getRoleName(), (self, value) -> self.wrapped.setRoleName(value))
                .setMetaData(Janitor.MetaData.HELP, "roleName: Name of the template role this recipient fills; writable.");
        DISPATCH.addStringProperty("routingOrder", self -> self.wrapped.getRoutingOrder(), (self, value) -> self.wrapped.setRoutingOrder(value))
                .setMetaData(Janitor.MetaData.HELP, "routingOrder: The recipient's position in the signing order, as a string; writable.");
        DISPATCH.addObjectProperty("tabs",
                self -> new DocusignTabs(self.wrapped.getTabs()),
                (self, tabs) -> self.wrapped.setTabs(Objects.requireNonNull(tabs).getWrapped()),
                DocusignTabs::new)
                .setMetaData(Janitor.MetaData.HELP, "tabs: The recipient's tabs, i.e. the fields to sign or fill in; writable, must not be null.");

        // LATER: List<RecipientSignatureProvider> recipientSignatureProviders, List<RecipientAdditionalNotification> additionalNotifications
    }

    protected final TemplateRole wrapped = new TemplateRole();

    public DocusignTemplateRole() {
        super(DISPATCH);
    }

    /**
     * @return the wrapped DocuSign object
     */
    public TemplateRole getWrapped() {
        return wrapped;
    }
}
