// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.mustang;

import com.eischet.janitor.api.Janitor;
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
        DISPATCH.addBuilderMethod("setName", (self, process, args) -> self.janitorGetHostValue().setName(args.getRequiredStringValue(0)))
                .setMetaData(Janitor.MetaData.HELP, "setName(name): Sets the contact person's name. Returns this contact.");
        DISPATCH.addBuilderMethod("setPhone", (self, process, args) -> self.janitorGetHostValue().setPhone(args.getRequiredStringValue(0)))
                .setMetaData(Janitor.MetaData.HELP, "setPhone(phone): Sets the phone number. Returns this contact.");
        DISPATCH.addBuilderMethod("setFax", (self, process, args) -> self.janitorGetHostValue().setFax(args.getRequiredStringValue(0)))
                .setMetaData(Janitor.MetaData.HELP, "setFax(fax): Sets the fax number. Returns this contact.");
        DISPATCH.addBuilderMethod("setEmail", (self, process, args) -> self.janitorGetHostValue().setEMail(args.getRequiredStringValue(0)))
                .setMetaData(Janitor.MetaData.HELP, "setEmail(email): Sets the email address. Returns this contact.");
        DISPATCH.addBuilderMethod("setZip", (self, process, args) -> self.janitorGetHostValue().setZIP(args.getRequiredStringValue(0)))
                .setMetaData(Janitor.MetaData.HELP, "setZip(zip): Sets the postal code. Returns this contact.");
        DISPATCH.addBuilderMethod("setStreet", (self, process, args) -> self.janitorGetHostValue().setStreet(args.getRequiredStringValue(0)))
                .setMetaData(Janitor.MetaData.HELP, "setStreet(street): Sets the street. Returns this contact.");
        DISPATCH.addBuilderMethod("setLocation", (self, process, args) -> self.janitorGetHostValue().setLocation(args.getRequiredStringValue(0)))
                .setMetaData(Janitor.MetaData.HELP, "setLocation(city): Sets the city. Returns this contact.");
        DISPATCH.addBuilderMethod("setCountry", (self, process, args) -> self.janitorGetHostValue().setCountry(args.getRequiredStringValue(0)))
                .setMetaData(Janitor.MetaData.HELP, "setCountry(country): Sets the country code, e.g. \"DE\". Returns this contact.");

        DISPATCH.addStringProperty("name", self -> self.janitorGetHostValue().getName(), (self, value) -> self.janitorGetHostValue().setName(value))
                .setMetaData(Janitor.MetaData.HELP, "name: The contact person's name; writable.");
        DISPATCH.addStringProperty("phone", self -> self.janitorGetHostValue().getPhone(), (self, value) -> self.janitorGetHostValue().setPhone(value))
                .setMetaData(Janitor.MetaData.HELP, "phone: The phone number; writable.");
        DISPATCH.addStringProperty("fax", self -> self.janitorGetHostValue().getFax(), (self, value) -> self.janitorGetHostValue().setFax(value))
                .setMetaData(Janitor.MetaData.HELP, "fax: The fax number; writable.");
        DISPATCH.addStringProperty("email", self -> self.janitorGetHostValue().getEMail(), (self, value) -> self.janitorGetHostValue().setEMail(value))
                .setMetaData(Janitor.MetaData.HELP, "email: The email address; writable.");
        DISPATCH.addStringProperty("zip", self -> self.janitorGetHostValue().getZIP(), (self, value) -> self.janitorGetHostValue().setZIP(value))
                .setMetaData(Janitor.MetaData.HELP, "zip: The postal code; writable.");
        DISPATCH.addStringProperty("street", self -> self.janitorGetHostValue().getStreet(), (self, value) -> self.janitorGetHostValue().setStreet(value))
                .setMetaData(Janitor.MetaData.HELP, "street: The street; writable.");
        DISPATCH.addStringProperty("location", self -> self.janitorGetHostValue().getLocation(), (self, value) -> self.janitorGetHostValue().setLocation(value))
                .setMetaData(Janitor.MetaData.HELP, "location: The city; writable.");
        DISPATCH.addStringProperty("country", self -> self.janitorGetHostValue().getCountry(), (self, value) -> self.janitorGetHostValue().setCountry(value))
                .setMetaData(Janitor.MetaData.HELP, "country: The country code, e.g. \"DE\"; writable.");
    }

    public MustangContact() {
        super(DISPATCH, new Contact());
    }

    public MustangContact(final Contact contact) {
        super(DISPATCH, contact);
    }

}
