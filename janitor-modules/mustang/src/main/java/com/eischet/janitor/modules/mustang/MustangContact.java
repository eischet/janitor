package com.eischet.janitor.modules.mustang;

import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.mustangproject.Contact;

/**
 * A named contact person at a trade party, e.g. the person to address on an invoice.
 * For the trade party (organisation) itself see {@link MustangTradeParty}.
 */
public class MustangContact extends JanitorWrapper<Contact> {

    public static final WrapperDispatchTable<Contact> DISPATCH = new WrapperDispatchTable<>(MustangContact::new);

    static {
        DISPATCH.addBuilderMethod("setName", (self, process, args) -> self.janitorGetHostValue().setName(args.getRequiredStringValue(0)));
        DISPATCH.addBuilderMethod("setPhone", (self, process, args) -> self.janitorGetHostValue().setPhone(args.getRequiredStringValue(0)));
        DISPATCH.addBuilderMethod("setFax", (self, process, args) -> self.janitorGetHostValue().setFax(args.getRequiredStringValue(0)));
        DISPATCH.addBuilderMethod("setEmail", (self, process, args) -> self.janitorGetHostValue().setEMail(args.getRequiredStringValue(0)));
        DISPATCH.addBuilderMethod("setZip", (self, process, args) -> self.janitorGetHostValue().setZIP(args.getRequiredStringValue(0)));
        DISPATCH.addBuilderMethod("setStreet", (self, process, args) -> self.janitorGetHostValue().setStreet(args.getRequiredStringValue(0)));
        DISPATCH.addBuilderMethod("setLocation", (self, process, args) -> self.janitorGetHostValue().setLocation(args.getRequiredStringValue(0)));
        DISPATCH.addBuilderMethod("setCountry", (self, process, args) -> self.janitorGetHostValue().setCountry(args.getRequiredStringValue(0)));

        DISPATCH.addStringProperty("name", self -> self.janitorGetHostValue().getName(), (self, value) -> self.janitorGetHostValue().setName(value));
        DISPATCH.addStringProperty("phone", self -> self.janitorGetHostValue().getPhone(), (self, value) -> self.janitorGetHostValue().setPhone(value));
        DISPATCH.addStringProperty("fax", self -> self.janitorGetHostValue().getFax(), (self, value) -> self.janitorGetHostValue().setFax(value));
        DISPATCH.addStringProperty("email", self -> self.janitorGetHostValue().getEMail(), (self, value) -> self.janitorGetHostValue().setEMail(value));
        DISPATCH.addStringProperty("zip", self -> self.janitorGetHostValue().getZIP(), (self, value) -> self.janitorGetHostValue().setZIP(value));
        DISPATCH.addStringProperty("street", self -> self.janitorGetHostValue().getStreet(), (self, value) -> self.janitorGetHostValue().setStreet(value));
        DISPATCH.addStringProperty("location", self -> self.janitorGetHostValue().getLocation(), (self, value) -> self.janitorGetHostValue().setLocation(value));
        DISPATCH.addStringProperty("country", self -> self.janitorGetHostValue().getCountry(), (self, value) -> self.janitorGetHostValue().setCountry(value));
    }

    public MustangContact() {
        super(DISPATCH, new Contact());
    }

    public MustangContact(final Contact contact) {
        super(DISPATCH, contact);
    }

}
