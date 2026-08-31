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

public class MustangTradeParty extends JanitorWrapper<TradeParty> {
    public static WrapperDispatchTable<TradeParty> DISPATCH = new WrapperDispatchTable<>(MustangTradeParty::new);

    static {
        DISPATCH.addBuilderMethod("setName", (self, process, args) ->
            self.janitorGetHostValue().setName(args.require(1).getRequiredStringValue(0)));
        DISPATCH.addStringProperty("name", self -> self.janitorGetHostValue().getName(),
            (self, value) -> self.janitorGetHostValue().setName(value));
        DISPATCH.addBuilderMethod("setStreet", (self, process, args) ->
            self.janitorGetHostValue().setStreet(args.require(1).getRequiredStringValue(0)));
        DISPATCH.addStringProperty("street", self -> self.janitorGetHostValue().getStreet(),
            (self, value) -> self.janitorGetHostValue().setStreet(value));
        DISPATCH.addBuilderMethod("setLocation", (self, process, args) ->
            self.janitorGetHostValue().setLocation(args.require(1).getRequiredStringValue(0)));
        DISPATCH.addStringProperty("location", self -> self.janitorGetHostValue().getLocation(),
            (self, value) -> self.janitorGetHostValue().setLocation(value));
        DISPATCH.addBuilderMethod("setCountry", (self, process, args) ->
            self.janitorGetHostValue().setCountry(args.require(1).getRequiredStringValue(0)));
        DISPATCH.addStringProperty("country", self -> self.janitorGetHostValue().getCountry(),
            (self, value) -> self.janitorGetHostValue().setCountry(value));
        DISPATCH.addBuilderMethod("setVatId", (self, process, args) ->
            self.janitorGetHostValue().setVATID(args.require(1).getRequiredStringValue(0)));
        DISPATCH.addStringProperty("vatId", self -> self.janitorGetHostValue().getVATID(),
            (self, value) -> self.janitorGetHostValue().setVATID(value));
        DISPATCH.addBuilderMethod("setEmail", (self, process, args) ->
            self.janitorGetHostValue().setEmail(args.require(1).getRequiredStringValue(0)));
        DISPATCH.addStringProperty("email", self -> self.janitorGetHostValue().getEmail(),
            (self, value) -> self.janitorGetHostValue().setEmail(value));
        DISPATCH.addBuilderMethod("setZip", (self, process, args) ->
            self.janitorGetHostValue().setZIP(args.require(1).getRequiredStringValue(0)));
        DISPATCH.addStringProperty("zip", self -> self.janitorGetHostValue().getZIP(),
            (self, value) -> self.janitorGetHostValue().setZIP(value));

        // -- identification -------------------------------------------------

        DISPATCH.addBuilderMethod("setId", (self, process, args) -> self.janitorGetHostValue().setID(args.getRequiredStringValue(0)));
        DISPATCH.addStringProperty("id", self -> self.janitorGetHostValue().getID(), (self, value) -> self.janitorGetHostValue().setID(value));

        DISPATCH.addBuilderMethod("setGlobalId", (self, process, args) -> self.janitorGetHostValue().setGlobalID(args.getRequiredStringValue(0)));
        DISPATCH.addStringProperty("globalId", self -> self.janitorGetHostValue().getGlobalID(), (self, value) -> self.janitorGetHostValue().setGlobalID(value));
        DISPATCH.addBuilderMethod("setGlobalIdScheme", (self, process, args) -> self.janitorGetHostValue().setGlobalIDScheme(args.getRequiredStringValue(0)));
        DISPATCH.addStringProperty("globalIdScheme", self -> self.janitorGetHostValue().getGlobalIDScheme(), (self, value) -> self.janitorGetHostValue().setGlobalIDScheme(value));
        DISPATCH.addBuilderMethod("addGlobalId", (self, process, args) ->
            self.janitorGetHostValue().addGlobalID(args.getRequired(0, MustangSchemedID.class).janitorGetHostValue()));

        DISPATCH.addStringProperty("uriUniversalCommunicationId", self -> self.janitorGetHostValue().getUriUniversalCommunicationID());
        DISPATCH.addStringProperty("uriUniversalCommunicationIdScheme", self -> self.janitorGetHostValue().getUriUniversalCommunicationIDScheme());
        DISPATCH.addBuilderMethod("addUriUniversalCommunicationId", (self, process, args) ->
            self.janitorGetHostValue().addUriUniversalCommunicationID(args.getRequired(0, MustangSchemedID.class).janitorGetHostValue()));

        DISPATCH.addBuilderMethod("addTaxId", (self, process, args) -> self.janitorGetHostValue().addTaxID(args.getRequiredStringValue(0)));
        DISPATCH.addBuilderMethod("addVatId", (self, process, args) -> self.janitorGetHostValue().addVATID(args.getRequiredStringValue(0)));
        DISPATCH.addBuilderMethod("setTaxId", (self, process, args) -> self.janitorGetHostValue().setTaxID(args.getRequiredStringValue(0)));
        DISPATCH.addStringProperty("taxId", self -> self.janitorGetHostValue().getTaxID(), (self, value) -> self.janitorGetHostValue().setTaxID(value));

        // -- description / additional address --------------------------------

        DISPATCH.addBuilderMethod("setDescription", (self, process, args) -> self.janitorGetHostValue().setDescription(args.getRequiredStringValue(0)));
        DISPATCH.addStringProperty("description", self -> self.janitorGetHostValue().getDescription(), (self, value) -> self.janitorGetHostValue().setDescription(value));
        DISPATCH.addBuilderMethod("setAdditionalAddress", (self, process, args) -> self.janitorGetHostValue().setAdditionalAddress(args.getRequiredStringValue(0)));
        DISPATCH.addStringProperty("additionalAddress", self -> self.janitorGetHostValue().getAdditionalAddress(), (self, value) -> self.janitorGetHostValue().setAdditionalAddress(value));
        DISPATCH.addBuilderMethod("setAdditionalAddressExtension", (self, process, args) -> self.janitorGetHostValue().setAdditionalAddressExtension(args.getRequiredStringValue(0)));
        DISPATCH.addStringProperty("additionalAddressExtension", self -> self.janitorGetHostValue().getAdditionalAddressExtension(), (self, value) -> self.janitorGetHostValue().setAdditionalAddressExtension(value));

        // -- contact / legal organisation -------------------------------------

        DISPATCH.addBuilderMethod("setContact", (self, process, args) ->
            self.janitorGetHostValue().setContact(args.getRequired(0, MustangContact.class).janitorGetHostValue()));
        DISPATCH.addObjectProperty("contact",
            self -> self.janitorGetHostValue().getContact() instanceof org.mustangproject.Contact contact ? new MustangContact(contact) : null,
            (self, value) -> self.janitorGetHostValue().setContact(value == null ? null : value.janitorGetHostValue()),
            MustangContact::new);

        DISPATCH.addBuilderMethod("setLegalOrganisation", (self, process, args) ->
            self.janitorGetHostValue().setLegalOrganisation(args.getRequired(0, MustangLegalOrganisation.class).janitorGetHostValue()));
        DISPATCH.addObjectProperty("legalOrganisation",
            self -> self.janitorGetHostValue().getLegalOrganisation() instanceof LegalOrganisation legalOrg ? new MustangLegalOrganisation(legalOrg) : null,
            (self, value) -> self.janitorGetHostValue().setLegalOrganisation(value == null ? null : value.janitorGetHostValue()),
            MustangLegalOrganisation::new);

        // -- bank details / direct debit --------------------------------------

        DISPATCH.addBuilderMethod("addBankDetails", (self, process, args) ->
            self.janitorGetHostValue().addBankDetails(args.getRequired(0, MustangBankDetails.class).janitorGetHostValue()));
        DISPATCH.addMethod("getBankDetails", (self, process, args) -> {
            final List<JanitorObject> result = new ArrayList<>();
            for (final BankDetails bankDetails : self.janitorGetHostValue().getBankDetails()) {
                result.add(new MustangBankDetails(bankDetails));
            }
            return Janitor.list(result);
        });

        DISPATCH.addBuilderMethod("addDebitDetails", (self, process, args) ->
            self.janitorGetHostValue().addDebitDetails(args.getRequired(0, MustangDirectDebit.class).janitorGetHostValue()));
        DISPATCH.addMethod("getDebitDetails", (self, process, args) -> {
            final List<JanitorObject> result = new ArrayList<>();
            for (final DirectDebit debitDetail : self.janitorGetHostValue().getDebitDetails()) {
                result.add(new MustangDirectDebit(debitDetail));
            }
            return Janitor.list(result);
        });
    }

    public MustangTradeParty() {
        super(DISPATCH, new TradeParty());
    }

    public MustangTradeParty(final TradeParty tradeParty) {
        super(DISPATCH, tradeParty);
    }

}
