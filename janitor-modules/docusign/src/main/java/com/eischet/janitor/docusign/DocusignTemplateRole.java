package com.eischet.janitor.docusign;

import com.docusign.esign.model.*;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;

import java.util.Objects;

public class DocusignTemplateRole extends JanitorComposed<DocusignTemplateRole> {

    public static DispatchTable<DocusignTemplateRole> DISPATCH = new DispatchTable<>();

    static {
        DISPATCH.addStringProperty("accessCode", self -> self.wrapped.getAccessCode(), (self, value) -> self.wrapped.setAccessCode(value));
        DISPATCH.addStringProperty("clientUserId", self -> self.wrapped.getClientUserId(), (self, value) -> self.wrapped.setClientUserId(value));
        DISPATCH.addStringProperty("defaultRecipient", self -> self.wrapped.getDefaultRecipient(), (self, value) -> self.wrapped.setDefaultRecipient(value));
        DISPATCH.addStringProperty("deliveryMethod", self -> self.wrapped.getDeliveryMethod(), (self, value) -> self.wrapped.setDeliveryMethod(value));
        DISPATCH.addStringProperty("email", self ->  self.wrapped.getEmail(), (self, value) -> self.wrapped.setEmail(value));
        DISPATCH.addStringProperty("embeddedRecipientStartURL", self -> self.wrapped.getEmbeddedRecipientStartURL(), (self, value) -> self.wrapped.setEmbeddedRecipientStartURL(value));
        DISPATCH.addStringProperty("inPersonSignerName", self -> self.wrapped.getInPersonSignerName(), (self, value) -> self.wrapped.setInPersonSignerName(value));
        DISPATCH.addStringProperty("name", self -> self.wrapped.getName(), (self, value) -> self.wrapped.setName(value));
        DISPATCH.addStringProperty("signingGroupId", self -> self.wrapped.getSigningGroupId(), (self, value) -> self.wrapped.setSigningGroupId(value));
        DISPATCH.addStringProperty("roleName", self -> self.wrapped.getRoleName(), (self, value) -> self.wrapped.setRoleName(value));
        DISPATCH.addStringProperty("routingOrder", self -> self.wrapped.getRoutingOrder(), (self, value) -> self.wrapped.setRoutingOrder(value));
        DISPATCH.addObjectProperty("tabs",
                self -> new DocusignTabs(self.wrapped.getTabs()),
                (self, tabs) -> self.wrapped.setTabs(Objects.requireNonNull(tabs).getWrapped()),
                DocusignTabs::new);

        // LATER: List<RecipientSignatureProvider> recipientSignatureProviders, List<RecipientAdditionalNotification> additionalNotifications
    }

    protected final TemplateRole wrapped = new TemplateRole();

    public DocusignTemplateRole() {
        super(DISPATCH);
    }

    public TemplateRole getWrapped() {
        return wrapped;
    }
}
