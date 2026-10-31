// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.mustang;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.mustangproject.BankDetails;
import org.mustangproject.DirectDebit;
import org.mustangproject.LegalOrganisation;
import org.mustangproject.TradeParty;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** Wrapper for a Mustang trade party, i.e. the sender or recipient of an invoice. */
public class MustangTradeParty extends JanitorWrapper<TradeParty> {
    public static WrapperDispatchTable<TradeParty> DISPATCH = new WrapperDispatchTable<>(MustangTradeParty::new);

    static {
        DISPATCH.addBuilderMethod("setName", (self, process, args) ->
            self.janitorGetHostValue().setName(args.require(1).getRequiredStringValue(0)))
            .setMetaData(Janitor.MetaData.HELP, "setName(name): Sets the name of the trade party; returns the trade party.");
        DISPATCH.addStringProperty("name", self -> self.janitorGetHostValue().getName(),
            (self, value) -> self.janitorGetHostValue().setName(value))
            .setMetaData(Janitor.MetaData.HELP, "name: The name of the trade party; writable.");
        DISPATCH.addBuilderMethod("setStreet", (self, process, args) ->
            self.janitorGetHostValue().setStreet(args.require(1).getRequiredStringValue(0)))
            .setMetaData(Janitor.MetaData.HELP, "setStreet(street): Sets the street of the address; returns the trade party.");
        DISPATCH.addStringProperty("street", self -> self.janitorGetHostValue().getStreet(),
            (self, value) -> self.janitorGetHostValue().setStreet(value))
            .setMetaData(Janitor.MetaData.HELP, "street: The street of the address; writable.");
        DISPATCH.addBuilderMethod("setLocation", (self, process, args) ->
            self.janitorGetHostValue().setLocation(args.require(1).getRequiredStringValue(0)))
            .setMetaData(Janitor.MetaData.HELP, "setLocation(city): Sets the city of the address; returns the trade party.");
        DISPATCH.addStringProperty("location", self -> self.janitorGetHostValue().getLocation(),
            (self, value) -> self.janitorGetHostValue().setLocation(value))
            .setMetaData(Janitor.MetaData.HELP, "location: The city of the address; writable.");
        DISPATCH.addBuilderMethod("setCountry", (self, process, args) ->
            self.janitorGetHostValue().setCountry(args.require(1).getRequiredStringValue(0)))
            .setMetaData(Janitor.MetaData.HELP, "setCountry(country): Sets the country code of the address; returns the trade party.");
        DISPATCH.addStringProperty("country", self -> self.janitorGetHostValue().getCountry(),
            (self, value) -> self.janitorGetHostValue().setCountry(value))
            .setMetaData(Janitor.MetaData.HELP, "country: The country code of the address; writable.");
        DISPATCH.addBuilderMethod("setVatId", (self, process, args) ->
            self.janitorGetHostValue().setVATID(args.require(1).getRequiredStringValue(0)))
            .setMetaData(Janitor.MetaData.HELP, "setVatId(vatId): Sets the VAT ID of the trade party; returns the trade party.");
        DISPATCH.addStringProperty("vatId", self -> self.janitorGetHostValue().getVATID(),
            (self, value) -> self.janitorGetHostValue().setVATID(value))
            .setMetaData(Janitor.MetaData.HELP, "vatId: The VAT ID of the trade party; writable.");
        DISPATCH.addBuilderMethod("setEmail", (self, process, args) ->
            self.janitorGetHostValue().setEmail(args.require(1).getRequiredStringValue(0)))
            .setMetaData(Janitor.MetaData.HELP, "setEmail(email): Sets the email address of the trade party; returns the trade party.");
        DISPATCH.addStringProperty("email", self -> self.janitorGetHostValue().getEmail(),
            (self, value) -> self.janitorGetHostValue().setEmail(value))
            .setMetaData(Janitor.MetaData.HELP, "email: The email address of the trade party; writable.");
        DISPATCH.addBuilderMethod("setZip", (self, process, args) ->
            self.janitorGetHostValue().setZIP(args.require(1).getRequiredStringValue(0)))
            .setMetaData(Janitor.MetaData.HELP, "setZip(zip): Sets the postal code of the address; returns the trade party.");
        DISPATCH.addStringProperty("zip", self -> self.janitorGetHostValue().getZIP(),
            (self, value) -> self.janitorGetHostValue().setZIP(value))
            .setMetaData(Janitor.MetaData.HELP, "zip: The postal code of the address; writable.");

        // -- identification -------------------------------------------------

        DISPATCH.addBuilderMethod("setId", (self, process, args) -> self.janitorGetHostValue().setID(args.getRequiredStringValue(0)))
            .setMetaData(Janitor.MetaData.HELP, "setId(id): Sets the ID of the trade party; returns the trade party.");
        DISPATCH.addStringProperty("id", self -> self.janitorGetHostValue().getID(), (self, value) -> self.janitorGetHostValue().setID(value))
            .setMetaData(Janitor.MetaData.HELP, "id: The ID of the trade party; writable.");

        DISPATCH.addBuilderMethod("setGlobalId", (self, process, args) -> self.janitorGetHostValue().setGlobalID(args.getRequiredStringValue(0)))
            .setMetaData(Janitor.MetaData.HELP, "setGlobalId(globalId): Sets the global ID (e.g. a GLN) of the trade party; returns the trade party.");
        DISPATCH.addStringProperty("globalId", self -> self.janitorGetHostValue().getGlobalID(), (self, value) -> self.janitorGetHostValue().setGlobalID(value))
            .setMetaData(Janitor.MetaData.HELP, "globalId: The global ID (e.g. a GLN) of the trade party; writable.");
        DISPATCH.addBuilderMethod("setGlobalIdScheme", (self, process, args) -> self.janitorGetHostValue().setGlobalIDScheme(args.getRequiredStringValue(0)))
            .setMetaData(Janitor.MetaData.HELP, "setGlobalIdScheme(scheme): Sets the scheme of the global ID; returns the trade party.");
        DISPATCH.addStringProperty("globalIdScheme", self -> self.janitorGetHostValue().getGlobalIDScheme(), (self, value) -> self.janitorGetHostValue().setGlobalIDScheme(value))
            .setMetaData(Janitor.MetaData.HELP, "globalIdScheme: The scheme of the global ID; writable.");
        DISPATCH.addBuilderMethod("addGlobalId", (self, process, args) ->
            self.janitorGetHostValue().addGlobalID(args.getRequired(0, MustangSchemedID.class).janitorGetHostValue()))
            .setMetaData(Janitor.MetaData.HELP, "addGlobalId(schemedID): Adds a global ID given as a SchemedID; returns the trade party.");

        DISPATCH.addStringProperty("uriUniversalCommunicationId", self -> self.janitorGetHostValue().getUriUniversalCommunicationID())
            .setMetaData(Janitor.MetaData.HELP, "uriUniversalCommunicationId: The electronic address of the trade party, or null; read-only, see addUriUniversalCommunicationId().");
        DISPATCH.addStringProperty("uriUniversalCommunicationIdScheme", self -> self.janitorGetHostValue().getUriUniversalCommunicationIDScheme())
            .setMetaData(Janitor.MetaData.HELP, "uriUniversalCommunicationIdScheme: The scheme of the electronic address, or null; read-only.");
        DISPATCH.addBuilderMethod("addUriUniversalCommunicationId", (self, process, args) ->
            self.janitorGetHostValue().addUriUniversalCommunicationID(args.getRequired(0, MustangSchemedID.class).janitorGetHostValue()))
            .setMetaData(Janitor.MetaData.HELP, "addUriUniversalCommunicationId(schemedID): Adds an electronic address (e.g. an email address with its scheme) given as a SchemedID; returns the trade party.");

        DISPATCH.addBuilderMethod("addTaxId", (self, process, args) -> self.janitorGetHostValue().addTaxID(args.getRequiredStringValue(0)))
            .setMetaData(Janitor.MetaData.HELP, "addTaxId(taxId): Adds a tax number of the trade party; returns the trade party.");
        DISPATCH.addBuilderMethod("addVatId", (self, process, args) -> self.janitorGetHostValue().addVATID(args.getRequiredStringValue(0)))
            .setMetaData(Janitor.MetaData.HELP, "addVatId(vatId): Adds a VAT ID of the trade party; returns the trade party.");
        DISPATCH.addBuilderMethod("setTaxId", (self, process, args) -> self.janitorGetHostValue().setTaxID(args.getRequiredStringValue(0)))
            .setMetaData(Janitor.MetaData.HELP, "setTaxId(taxId): Sets the tax number of the trade party; returns the trade party.");
        DISPATCH.addStringProperty("taxId", self -> self.janitorGetHostValue().getTaxID(), (self, value) -> self.janitorGetHostValue().setTaxID(value))
            .setMetaData(Janitor.MetaData.HELP, "taxId: The tax number of the trade party; writable.");

        // -- description / additional address --------------------------------

        DISPATCH.addBuilderMethod("setDescription", (self, process, args) -> self.janitorGetHostValue().setDescription(args.getRequiredStringValue(0)))
            .setMetaData(Janitor.MetaData.HELP, "setDescription(description): Sets a free-text description of the trade party; returns the trade party.");
        DISPATCH.addStringProperty("description", self -> self.janitorGetHostValue().getDescription(), (self, value) -> self.janitorGetHostValue().setDescription(value))
            .setMetaData(Janitor.MetaData.HELP, "description: A free-text description of the trade party; writable.");
        DISPATCH.addBuilderMethod("setAdditionalAddress", (self, process, args) -> self.janitorGetHostValue().setAdditionalAddress(args.getRequiredStringValue(0)))
            .setMetaData(Janitor.MetaData.HELP, "setAdditionalAddress(line): Sets an additional address line; returns the trade party.");
        DISPATCH.addStringProperty("additionalAddress", self -> self.janitorGetHostValue().getAdditionalAddress(), (self, value) -> self.janitorGetHostValue().setAdditionalAddress(value))
            .setMetaData(Janitor.MetaData.HELP, "additionalAddress: An additional address line; writable.");
        DISPATCH.addBuilderMethod("setAdditionalAddressExtension", (self, process, args) -> self.janitorGetHostValue().setAdditionalAddressExtension(args.getRequiredStringValue(0)))
            .setMetaData(Janitor.MetaData.HELP, "setAdditionalAddressExtension(line): Sets a further address line after additionalAddress; returns the trade party.");
        DISPATCH.addStringProperty("additionalAddressExtension", self -> self.janitorGetHostValue().getAdditionalAddressExtension(), (self, value) -> self.janitorGetHostValue().setAdditionalAddressExtension(value))
            .setMetaData(Janitor.MetaData.HELP, "additionalAddressExtension: A further address line after additionalAddress; writable.");

        // -- contact / legal organisation -------------------------------------

        DISPATCH.addBuilderMethod("setContact", (self, process, args) ->
            self.janitorGetHostValue().setContact(args.getRequired(0, MustangContact.class).janitorGetHostValue()))
            .setMetaData(Janitor.MetaData.HELP, "setContact(contact): Sets the Contact person of the trade party; returns the trade party.");
        DISPATCH.addObjectProperty("contact",
            self -> self.janitorGetHostValue().getContact() instanceof org.mustangproject.Contact contact ? new MustangContact(contact) : null,
            (self, value) -> self.janitorGetHostValue().setContact(value == null ? null : value.janitorGetHostValue()),
            MustangContact::new)
            .setMetaData(Janitor.MetaData.HELP, "contact: The Contact person of the trade party, or null; writable.");

        DISPATCH.addBuilderMethod("setLegalOrganisation", (self, process, args) ->
            self.janitorGetHostValue().setLegalOrganisation(args.getRequired(0, MustangLegalOrganisation.class).janitorGetHostValue()))
            .setMetaData(Janitor.MetaData.HELP, "setLegalOrganisation(legalOrganisation): Sets the LegalOrganisation (legal registration) of the trade party; returns the trade party.");
        DISPATCH.addObjectProperty("legalOrganisation",
            self -> self.janitorGetHostValue().getLegalOrganisation() instanceof LegalOrganisation legalOrg ? new MustangLegalOrganisation(legalOrg) : null,
            (self, value) -> self.janitorGetHostValue().setLegalOrganisation(value == null ? null : value.janitorGetHostValue()),
            MustangLegalOrganisation::new)
            .setMetaData(Janitor.MetaData.HELP, "legalOrganisation: The LegalOrganisation (legal registration) of the trade party, or null; writable.");

        // -- bank details / direct debit --------------------------------------

        DISPATCH.addBuilderMethod("addBankDetails", (self, process, args) ->
            self.janitorGetHostValue().addBankDetails(args.getRequired(0, MustangBankDetails.class).janitorGetHostValue()))
            .setMetaData(Janitor.MetaData.HELP, "addBankDetails(bankDetails): Adds BankDetails of the trade party; returns the trade party.");
        DISPATCH.addMethod("getBankDetails", (self, process, args) -> {
            final List<JanitorObject> result = new ArrayList<>();
            for (final BankDetails bankDetails : self.janitorGetHostValue().getBankDetails()) {
                result.add(new MustangBankDetails(bankDetails));
            }
            return Janitor.list(result);
        }).setMetaData(Janitor.MetaData.HELP, "getBankDetails(): Returns the bank details of the trade party as a list.");

        DISPATCH.addBuilderMethod("addDebitDetails", (self, process, args) ->
            self.janitorGetHostValue().addDebitDetails(args.getRequired(0, MustangDirectDebit.class).janitorGetHostValue()))
            .setMetaData(Janitor.MetaData.HELP, "addDebitDetails(directDebit): Adds DirectDebit details of the trade party; returns the trade party.");
        DISPATCH.addMethod("getDebitDetails", (self, process, args) -> {
            final List<JanitorObject> result = new ArrayList<>();
            for (final DirectDebit debitDetail : self.janitorGetHostValue().getDebitDetails()) {
                result.add(new MustangDirectDebit(debitDetail));
            }
            return Janitor.list(result);
        }).setMetaData(Janitor.MetaData.HELP, "getDebitDetails(): Returns the direct debit details of the trade party as a list.");
    }

    public MustangTradeParty() {
        super(DISPATCH, new TradeParty());
    }

    public MustangTradeParty(final TradeParty tradeParty) {
        super(DISPATCH, tradeParty);
    }

}
