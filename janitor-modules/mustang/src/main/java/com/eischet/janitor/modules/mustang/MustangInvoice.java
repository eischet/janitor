// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.mustang;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JDate;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import com.eischet.janitor.toolbox.time.DateTimeUtils;
import org.mustangproject.*;
import org.mustangproject.ZUGFeRD.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** Wrapper for the Mustang invoice, i.e. an electronic invoice in the ZUGFeRD/Factur-X format. */
public class MustangInvoice extends JanitorWrapper<Invoice> {
    public static WrapperDispatchTable<Invoice> DISPATCH = new WrapperDispatchTable<>(MustangInvoice::new);

    static {
        DISPATCH.addBuilderMethod("setNumber", (self, process, args) ->
                self.janitorGetHostValue().setNumber(args.require(1).getRequiredStringValue(0)))
                .setMetaData(Janitor.MetaData.HELP, "setNumber(number): Sets the invoice number. Returns this invoice.");
        DISPATCH.addStringProperty("number", self -> self.janitorGetHostValue().getNumber(),
                (self, value) -> self.janitorGetHostValue().setNumber(value))
                .setMetaData(Janitor.MetaData.HELP, "number: The invoice number; writable.");
        DISPATCH.addBuilderMethod("setCurrency", (self, process, args) ->
                self.janitorGetHostValue().setCurrency(args.require(1).getRequiredStringValue(0)))
                .setMetaData(Janitor.MetaData.HELP, "setCurrency(currency): Sets the invoice currency as an ISO 4217 code, e.g. \"EUR\". Returns this invoice.");
        DISPATCH.addStringProperty("currency", self -> self.janitorGetHostValue().getCurrency(),
                (self, value) -> self.janitorGetHostValue().setCurrency(value))
                .setMetaData(Janitor.MetaData.HELP, "currency: The invoice currency as an ISO 4217 code, e.g. \"EUR\"; writable.");
        DISPATCH.addBuilderMethod("setSender", (self, process, args) -> {
            self.janitorGetHostValue().setSender(args.require(1).getRequired(0, MustangTradeParty.class).janitorGetHostValue());
        }).setMetaData(Janitor.MetaData.HELP, "setSender(tradeParty): Sets the sender (seller) of the invoice. Returns this invoice.");
        DISPATCH.addObjectProperty("sender", self -> self.janitorGetHostValue().getSender() == null ? null : new MustangTradeParty(self.janitorGetHostValue().getSender()),
                (self, value) -> self.janitorGetHostValue().setSender(value == null ? null : value.janitorGetHostValue()), MustangTradeParty::new)
                .setMetaData(Janitor.MetaData.HELP, "sender: The sender (seller) of the invoice, or null; writable.");
        DISPATCH.addBuilderMethod("setIssueDate", (self, process, args) -> {
            self.janitorGetHostValue().setIssueDate(JDate.toLegacyJavaDate(args.require(1).getRequired(0, JDate.class).janitorGetHostValue()));
        }).setMetaData(Janitor.MetaData.HELP, "setIssueDate(date): Sets the invoice date. Returns this invoice.");
        DISPATCH.addDateProperty("issueDate", self -> DateTimeUtils.convertDateToLocalDate(self.janitorGetHostValue().getIssueDate()),
                (self, value) -> self.janitorGetHostValue().setIssueDate(JDate.toLegacyJavaDate(value)))
                .setMetaData(Janitor.MetaData.HELP, "issueDate: The invoice date; writable.");
        DISPATCH.addBuilderMethod("setDueDate", (self, process, args) -> {
            self.janitorGetHostValue().setDueDate(JDate.toLegacyJavaDate(args.require(1).getRequired(0, JDate.class).janitorGetHostValue()));
        }).setMetaData(Janitor.MetaData.HELP, "setDueDate(date): Sets the date by which the invoice must be paid. Returns this invoice.");
        DISPATCH.addDateProperty("dueDate", self -> DateTimeUtils.convertDateToLocalDate(self.janitorGetHostValue().getDueDate()),
                (self, value) -> self.janitorGetHostValue().setDueDate(JDate.toLegacyJavaDate(value)))
                .setMetaData(Janitor.MetaData.HELP, "dueDate: The date by which the invoice must be paid; writable.");
        DISPATCH.addBuilderMethod("setRecipient", (self, process, args) -> {
            self.janitorGetHostValue().setRecipient(args.require(1).getRequired(0, MustangTradeParty.class).janitorGetHostValue());
        }).setMetaData(Janitor.MetaData.HELP, "setRecipient(tradeParty): Sets the recipient (buyer) of the invoice. Returns this invoice.");
        DISPATCH.addObjectProperty("recipient", self -> self.janitorGetHostValue().getRecipient() == null ? null : new MustangTradeParty(self.janitorGetHostValue().getRecipient()),
                (self, value) -> self.janitorGetHostValue().setRecipient(value == null ? null : value.janitorGetHostValue()), MustangTradeParty::new)
                .setMetaData(Janitor.MetaData.HELP, "recipient: The recipient (buyer) of the invoice, or null; writable.");
        DISPATCH.addBuilderMethod("setDetailedDeliveryPeriod", (self, process, args) -> {
            final JDate fromDate = args.getRequired(0, JDate.class);
            final JDate toDate = args.getRequired(1, JDate.class);
            self.janitorGetHostValue().setDetailedDeliveryPeriod(JDate.toLegacyJavaDate(fromDate), JDate.toLegacyJavaDate(toDate));
        }).setMetaData(Janitor.MetaData.HELP, "setDetailedDeliveryPeriod(fromDate, toDate): Sets the delivery (service) period. Returns this invoice.");
        DISPATCH.addBuilderMethod("setDetailedDeliveryPeriodFrom", (self, process, args) -> {
            final JDate fromDate = args.getRequired(0, JDate.class);
            self.janitorGetHostValue().setDetailedDeliveryPeriod(JDate.toLegacyJavaDate(fromDate), null);
        });
        DISPATCH.addBuilderMethod("setDetailedDeliveryPeriodTo", (self, process, args) -> {
            final JDate toDate = args.getRequired(0, JDate.class);
            self.janitorGetHostValue().setDetailedDeliveryPeriodTo(JDate.toLegacyJavaDate(toDate));
        }).setMetaData(Janitor.MetaData.HELP, "setDetailedDeliveryPeriodTo(date): Sets the last day of the delivery (service) period. Returns this invoice.");
        DISPATCH.addDateProperty("detailedDeliveryPeriodFrom", self -> DateTimeUtils.convertDateToLocalDate(self.janitorGetHostValue().getDetailedDeliveryPeriodFrom()),
                (self, date) -> self.janitorGetHostValue().setDetailedDeliveryPeriodFrom(JDate.toLegacyJavaDate(date)))
                .setMetaData(Janitor.MetaData.HELP, "detailedDeliveryPeriodFrom: The first day of the delivery (service) period, or null; writable.");
        DISPATCH.addDateProperty("detailedDeliveryPeriodTo", self -> DateTimeUtils.convertDateToLocalDate(self.janitorGetHostValue().getDetailedDeliveryPeriodTo()),
                (self, date) -> self.janitorGetHostValue().setDetailedDeliveryPeriodTo(JDate.toLegacyJavaDate(date)))
                .setMetaData(Janitor.MetaData.HELP, "detailedDeliveryPeriodTo: The last day of the delivery (service) period, or null; writable.");

        DISPATCH.addBuilderMethod("addItem", (self, process, args) -> {
            final MustangItem item = args.getRequired(0, MustangItem.class);
            self.janitorGetHostValue().addItem(item.janitorGetHostValue());
        }).setMetaData(Janitor.MetaData.HELP, "addItem(item): Adds an item (line) to the invoice. Returns this invoice.");
        DISPATCH.addMethod("getItems", (self, process, args) -> {
            final List<JanitorObject> result = new ArrayList<>();
            final IZUGFeRDExportableItem[] items = self.janitorGetHostValue().getZFItems();
            if (items != null) {
                for (final IZUGFeRDExportableItem item : items) {
                    if (item instanceof Item hostItem) {
                        result.add(new MustangItem(hostItem));
                    }
                }
            }
            return Janitor.list(result);
        }).setMetaData(Janitor.MetaData.HELP, "getItems(): Returns a list of the invoice's items (lines).");

        // -- document identity / delivery dates -----------------------------------

        DISPATCH.addBuilderMethod("setTestIndicator", (self, process, args) -> self.janitorGetHostValue().setTestIndicator())
                .setMetaData(Janitor.MetaData.HELP, "setTestIndicator(): Marks the invoice as a test. Returns this invoice.");
        DISPATCH.addBooleanProperty("testIndicator", self -> self.janitorGetHostValue().getTestIndicator())
                .setMetaData(Janitor.MetaData.HELP, "testIndicator: True if the invoice is marked as a test; read-only.");

        DISPATCH.addBuilderMethod("setDocumentName", (self, process, args) -> self.janitorGetHostValue().setDocumentName(args.getRequiredStringValue(0)))
                .setMetaData(Janitor.MetaData.HELP, "setDocumentName(name): Sets the name (title) of the document. Returns this invoice.");
        DISPATCH.addStringProperty("documentName", self -> self.janitorGetHostValue().getDocumentName(), (self, value) -> self.janitorGetHostValue().setDocumentName(value))
                .setMetaData(Janitor.MetaData.HELP, "documentName: The name (title) of the document; writable.");

        DISPATCH.addStringProperty("documentCode", self -> self.janitorGetHostValue().getDocumentCode())
                .setMetaData(Janitor.MetaData.HELP, "documentCode: The UNTDID 1001 document type code, e.g. \"380\" for an invoice; read-only.");

        DISPATCH.addBuilderMethod("setCorrection", (self, process, args) -> self.janitorGetHostValue().setCorrection(args.getRequiredStringValue(0)))
                .setMetaData(Janitor.MetaData.HELP, "setCorrection(number): Makes this invoice a correction of the invoice with the given number. Returns this invoice.");
        DISPATCH.addBuilderMethod("setCreditNote", (self, process, args) -> {
            if (args.size() == 0) {
                self.janitorGetHostValue().setCreditNote();
            } else {
                self.janitorGetHostValue().setCreditNote(args.getRequiredStringValue(0));
            }
        });

        DISPATCH.addBuilderMethod("setDeliveryDate", (self, process, args) ->
                self.janitorGetHostValue().setDeliveryDate(JDate.toLegacyJavaDate(args.getRequired(0, JDate.class).janitorGetHostValue())))
                .setMetaData(Janitor.MetaData.HELP, "setDeliveryDate(date): Sets the date of delivery. Returns this invoice.");
        DISPATCH.addDateProperty("deliveryDate", self -> DateTimeUtils.convertDateToLocalDate(self.janitorGetHostValue().getDeliveryDate()),
                (self, value) -> self.janitorGetHostValue().setDeliveryDate(JDate.toLegacyJavaDate(value)))
                .setMetaData(Janitor.MetaData.HELP, "deliveryDate: The date of delivery, or null; writable.");

        DISPATCH.addBuilderMethod("setDeliveryTypeCode", (self, process, args) -> self.janitorGetHostValue().setDeliveryTypeCode(args.getRequiredStringValue(0)));
        DISPATCH.addStringProperty("deliveryTypeCode", self -> self.janitorGetHostValue().getDeliveryTypeCode(), (self, value) -> self.janitorGetHostValue().setDeliveryTypeCode(value));

        // -- currency / tax / amounts --------------------------------------------

        DISPATCH.addBuilderMethod("setTaxCurrency", (self, process, args) -> self.janitorGetHostValue().setTaxCurrency(args.getRequiredStringValue(0)))
                .setMetaData(Janitor.MetaData.HELP, "setTaxCurrency(currency): Sets the currency for the VAT amounts if it differs from the invoice currency. Returns this invoice.");
        DISPATCH.addStringProperty("taxCurrency", self -> self.janitorGetHostValue().getTaxCurrency(), (self, value) -> self.janitorGetHostValue().setTaxCurrency(value))
                .setMetaData(Janitor.MetaData.HELP, "taxCurrency: The currency for the VAT amounts if it differs from the invoice currency, or null; writable.");

        DISPATCH.addBuilderMethod("setTaxConversionRate", (self, process, args) -> self.janitorGetHostValue().setTaxConversionRate(args.getRequiredJNumber(0).toBigDecimal()));
        DISPATCH.addBigDecimalProperty("taxConversionRate", self -> self.janitorGetHostValue().getTaxConversionRate(), (self, value) -> self.janitorGetHostValue().setTaxConversionRate(value));

        DISPATCH.addBuilderMethod("setTaxConversionRateDateTime", (self, process, args) ->
                self.janitorGetHostValue().setTaxConversionRateDateTime(JDate.toLegacyJavaDate(args.getRequired(0, JDate.class).janitorGetHostValue())));
        DISPATCH.addDateProperty("taxConversionRateDateTime", self -> DateTimeUtils.convertDateToLocalDate(self.janitorGetHostValue().getTaxConversionRateDateTime()),
                (self, value) -> self.janitorGetHostValue().setTaxConversionRateDateTime(JDate.toLegacyJavaDate(value)));

        DISPATCH.addBuilderMethod("setRoundingAmount", (self, process, args) -> self.janitorGetHostValue().setRoundingAmount(args.getRequiredJNumber(0).toBigDecimal()))
                .setMetaData(Janitor.MetaData.HELP, "setRoundingAmount(amount): Sets the amount by which the total was rounded. Returns this invoice.");
        DISPATCH.addBigDecimalProperty("roundingAmount", self -> self.janitorGetHostValue().getRoundingAmount(), (self, value) -> self.janitorGetHostValue().setRoundingAmount(value))
                .setMetaData(Janitor.MetaData.HELP, "roundingAmount: The amount by which the total was rounded, or null; writable.");

        DISPATCH.addBuilderMethod("setTotalPrepaidAmount", (self, process, args) -> self.janitorGetHostValue().setTotalPrepaidAmount(args.getRequiredJNumber(0).toBigDecimal()))
                .setMetaData(Janitor.MetaData.HELP, "setTotalPrepaidAmount(amount): Sets the amount that has already been paid. Returns this invoice.");
        DISPATCH.addBigDecimalProperty("totalPrepaidAmount", self -> self.janitorGetHostValue().getTotalPrepaidAmount(), (self, value) -> self.janitorGetHostValue().setTotalPrepaidAmount(value))
                .setMetaData(Janitor.MetaData.HELP, "totalPrepaidAmount: The amount that has already been paid, or null; writable.");

        // -- payment ---------------------------------------------------------------

        DISPATCH.addBuilderMethod("setPaymentReference", (self, process, args) -> self.janitorGetHostValue().setPaymentReference(args.getRequiredStringValue(0)))
                .setMetaData(Janitor.MetaData.HELP, "setPaymentReference(reference): Sets the reference the payer should give with the payment. Returns this invoice.");
        DISPATCH.addStringProperty("paymentReference", self -> self.janitorGetHostValue().getPaymentReference(), (self, value) -> self.janitorGetHostValue().setPaymentReference(value))
                .setMetaData(Janitor.MetaData.HELP, "paymentReference: The reference the payer should give with the payment; writable.");

        DISPATCH.addBuilderMethod("setPaymentTermDescription", (self, process, args) -> self.janitorGetHostValue().setPaymentTermDescription(args.getRequiredStringValue(0)))
                .setMetaData(Janitor.MetaData.HELP, "setPaymentTermDescription(text): Sets the payment terms as free text. Returns this invoice.");
        DISPATCH.addStringProperty("paymentTermDescription", self -> self.janitorGetHostValue().getPaymentTermDescription(), (self, value) -> self.janitorGetHostValue().setPaymentTermDescription(value))
                .setMetaData(Janitor.MetaData.HELP, "paymentTermDescription: The payment terms as free text; writable.");

        DISPATCH.addBuilderMethod("setPaymentTerms", (self, process, args) ->
                self.janitorGetHostValue().setPaymentTerms(args.getRequired(0, MustangPaymentTerms.class).janitorGetHostValue()))
                .setMetaData(Janitor.MetaData.HELP, "setPaymentTerms(paymentTerms): Sets the payment terms. Returns this invoice.");
        DISPATCH.addObjectProperty("paymentTerms",
                self -> self.janitorGetHostValue().getPaymentTerms() instanceof PaymentTerms paymentTerms ? new MustangPaymentTerms(paymentTerms) : null,
                (self, value) -> self.janitorGetHostValue().setPaymentTerms(value == null ? null : value.janitorGetHostValue()),
                MustangPaymentTerms::new)
                .setMetaData(Janitor.MetaData.HELP, "paymentTerms: The payment terms, or null; writable.");

        DISPATCH.addBuilderMethod("addPaymentTerms", (self, process, args) ->
                self.janitorGetHostValue().addPaymentTerms(args.getRequired(0, MustangPaymentTerms.class).janitorGetHostValue()));
        DISPATCH.addMethod("getExtendedPaymentTerms", (self, process, args) -> {
            final List<JanitorObject> result = new ArrayList<>();
            final IZUGFeRDPaymentTerms[] terms = self.janitorGetHostValue().getExtendedPaymentTerms();
            if (terms != null) {
                for (final IZUGFeRDPaymentTerms term : terms) {
                    if (term instanceof PaymentTerms paymentTerms) {
                        result.add(new MustangPaymentTerms(paymentTerms));
                    }
                }
            }
            return Janitor.list(result);
        });

        DISPATCH.addBuilderMethod("addCashDiscount", (self, process, args) ->
                self.janitorGetHostValue().addCashDiscount(args.getRequired(0, MustangCashDiscount.class).janitorGetHostValue()))
                .setMetaData(Janitor.MetaData.HELP, "addCashDiscount(cashDiscount): Adds an early-payment discount. Returns this invoice.");
        DISPATCH.addMethod("getCashDiscounts", (self, process, args) -> {
            final List<JanitorObject> result = new ArrayList<>();
            final CashDiscount[] discounts = self.janitorGetHostValue().getCashDiscounts();
            if (discounts != null) {
                for (final CashDiscount discount : discounts) {
                    result.add(new MustangCashDiscount(discount));
                }
            }
            return Janitor.list(result);
        }).setMetaData(Janitor.MetaData.HELP, "getCashDiscounts(): Returns a list of the invoice's early-payment discounts.");

        // -- involved parties --------------------------------------------------------

        DISPATCH.addBuilderMethod("setPayee", (self, process, args) ->
                self.janitorGetHostValue().setPayee(args.getRequired(0, MustangTradeParty.class).janitorGetHostValue()))
                .setMetaData(Janitor.MetaData.HELP, "setPayee(tradeParty): Sets the party that receives the payment, if it is not the seller. Returns this invoice.");
        DISPATCH.addObjectProperty("payee",
                self -> self.janitorGetHostValue().getPayee() == null ? null : new MustangTradeParty(self.janitorGetHostValue().getPayee()),
                (self, value) -> self.janitorGetHostValue().setPayee(value == null ? null : value.janitorGetHostValue()),
                MustangTradeParty::new)
                .setMetaData(Janitor.MetaData.HELP, "payee: The party that receives the payment, if it is not the seller, or null; writable.");

        DISPATCH.addBuilderMethod("setInvoicer", (self, process, args) ->
                self.janitorGetHostValue().setInvoicer(args.getRequired(0, MustangTradeParty.class).janitorGetHostValue()));
        DISPATCH.addObjectProperty("invoicer",
                self -> self.janitorGetHostValue().getInvoicer() == null ? null : new MustangTradeParty(self.janitorGetHostValue().getInvoicer()),
                (self, value) -> self.janitorGetHostValue().setInvoicer(value == null ? null : value.janitorGetHostValue()),
                MustangTradeParty::new);

        DISPATCH.addBuilderMethod("setInvoicee", (self, process, args) ->
                self.janitorGetHostValue().setInvoicee(args.getRequired(0, MustangTradeParty.class).janitorGetHostValue()));
        DISPATCH.addObjectProperty("invoicee",
                self -> self.janitorGetHostValue().getInvoicee() == null ? null : new MustangTradeParty(self.janitorGetHostValue().getInvoicee()),
                (self, value) -> self.janitorGetHostValue().setInvoicee(value == null ? null : value.janitorGetHostValue()),
                MustangTradeParty::new);

        DISPATCH.addBuilderMethod("setTaxRepresentative", (self, process, args) ->
                self.janitorGetHostValue().setTaxRepresentative(args.getRequired(0, MustangTradeParty.class).janitorGetHostValue()))
                .setMetaData(Janitor.MetaData.HELP, "setTaxRepresentative(tradeParty): Sets the seller's tax representative. Returns this invoice.");
        DISPATCH.addObjectProperty("taxRepresentative",
                self -> self.janitorGetHostValue().getTaxRepresentative() == null ? null : new MustangTradeParty(self.janitorGetHostValue().getTaxRepresentative()),
                (self, value) -> self.janitorGetHostValue().setTaxRepresentative(value == null ? null : value.janitorGetHostValue()),
                MustangTradeParty::new)
                .setMetaData(Janitor.MetaData.HELP, "taxRepresentative: The seller's tax representative, or null; writable.");

        DISPATCH.addBuilderMethod("setDeliveryAddress", (self, process, args) ->
                self.janitorGetHostValue().setDeliveryAddress(args.getRequired(0, MustangTradeParty.class).janitorGetHostValue()))
                .setMetaData(Janitor.MetaData.HELP, "setDeliveryAddress(tradeParty): Sets the party and address the goods are delivered to. Returns this invoice.");
        DISPATCH.addObjectProperty("deliveryAddress",
                self -> self.janitorGetHostValue().getDeliveryAddress() == null ? null : new MustangTradeParty(self.janitorGetHostValue().getDeliveryAddress()),
                (self, value) -> self.janitorGetHostValue().setDeliveryAddress(value == null ? null : value.janitorGetHostValue()),
                MustangTradeParty::new)
                .setMetaData(Janitor.MetaData.HELP, "deliveryAddress: The party and address the goods are delivered to, or null; writable.");

        DISPATCH.addBuilderMethod("setEndCustomerDeliveryAddress", (self, process, args) ->
                self.janitorGetHostValue().setEndCustomerDeliveryAddress(args.getRequired(0, MustangTradeParty.class).janitorGetHostValue()));
        DISPATCH.addObjectProperty("endCustomerDeliveryAddress",
                self -> self.janitorGetHostValue().getEndCustomerDeliveryAddress() == null ? null : new MustangTradeParty(self.janitorGetHostValue().getEndCustomerDeliveryAddress()),
                (self, value) -> self.janitorGetHostValue().setEndCustomerDeliveryAddress(value == null ? null : value.janitorGetHostValue()),
                MustangTradeParty::new);

        // -- ship-to (used e.g. for despatch advices) --------------------------------

        DISPATCH.addBuilderMethod("setShipToOrganisationId", (self, process, args) -> self.janitorGetHostValue().setShipToOrganisationID(args.getRequiredStringValue(0)));
        DISPATCH.addStringProperty("shipToOrganisationId", self -> self.janitorGetHostValue().getShipToOrganisationID(), (self, value) -> self.janitorGetHostValue().setShipToOrganisationID(value));
        DISPATCH.addBuilderMethod("setShipToOrganisationName", (self, process, args) -> self.janitorGetHostValue().setShipToOrganisationName(args.getRequiredStringValue(0)))
                .setMetaData(Janitor.MetaData.HELP, "setShipToOrganisationName(name): Sets the name of the organisation the goods are shipped to. Returns this invoice.");
        DISPATCH.addStringProperty("shipToOrganisationName", self -> self.janitorGetHostValue().getShipToOrganisationName(), (self, value) -> self.janitorGetHostValue().setShipToOrganisationName(value))
                .setMetaData(Janitor.MetaData.HELP, "shipToOrganisationName: The name of the organisation the goods are shipped to; writable.");
        DISPATCH.addBuilderMethod("setShipToStreet", (self, process, args) -> self.janitorGetHostValue().setShipToStreet(args.getRequiredStringValue(0)))
                .setMetaData(Janitor.MetaData.HELP, "setShipToStreet(street): Sets the street of the ship-to address. Returns this invoice.");
        DISPATCH.addStringProperty("shipToStreet", self -> self.janitorGetHostValue().getShipToStreet(), (self, value) -> self.janitorGetHostValue().setShipToStreet(value))
                .setMetaData(Janitor.MetaData.HELP, "shipToStreet: The street of the ship-to address; writable.");
        DISPATCH.addBuilderMethod("setShipToZip", (self, process, args) -> self.janitorGetHostValue().setShipToZIP(args.getRequiredStringValue(0)))
                .setMetaData(Janitor.MetaData.HELP, "setShipToZip(zip): Sets the postal code of the ship-to address. Returns this invoice.");
        DISPATCH.addStringProperty("shipToZip", self -> self.janitorGetHostValue().getShipToZIP(), (self, value) -> self.janitorGetHostValue().setShipToZIP(value))
                .setMetaData(Janitor.MetaData.HELP, "shipToZip: The postal code of the ship-to address; writable.");
        DISPATCH.addBuilderMethod("setShipToLocation", (self, process, args) -> self.janitorGetHostValue().setShipToLocation(args.getRequiredStringValue(0)))
                .setMetaData(Janitor.MetaData.HELP, "setShipToLocation(city): Sets the city of the ship-to address. Returns this invoice.");
        DISPATCH.addStringProperty("shipToLocation", self -> self.janitorGetHostValue().getShipToLocation(), (self, value) -> self.janitorGetHostValue().setShipToLocation(value))
                .setMetaData(Janitor.MetaData.HELP, "shipToLocation: The city of the ship-to address; writable.");
        DISPATCH.addBuilderMethod("setShipToCountry", (self, process, args) -> self.janitorGetHostValue().setShipToCountry(args.getRequiredStringValue(0)))
                .setMetaData(Janitor.MetaData.HELP, "setShipToCountry(country): Sets the country of the ship-to address. Returns this invoice.");
        DISPATCH.addStringProperty("shipToCountry", self -> self.janitorGetHostValue().getShipToCountry(), (self, value) -> self.janitorGetHostValue().setShipToCountry(value))
                .setMetaData(Janitor.MetaData.HELP, "shipToCountry: The country of the ship-to address; writable.");

        // -- "own organisation" read-only convenience getters (mirror sender) -------

        DISPATCH.addStringProperty("ownOrganisationFullPlaintextInfo", self -> self.janitorGetHostValue().getOwnOrganisationFullPlaintextInfo(),
                (self, value) -> self.janitorGetHostValue().setOwnOrganisationFullPlaintextInfo(value));
        DISPATCH.addStringProperty("ownTaxID", self -> self.janitorGetHostValue().getOwnTaxID(), (self, value) -> self.janitorGetHostValue().setOwnTaxID(value))
                .setMetaData(Janitor.MetaData.HELP, "ownTaxID: The seller's tax number; writable.");
        DISPATCH.addStringProperty("ownVATID", self -> self.janitorGetHostValue().getOwnVATID(), (self, value) -> self.janitorGetHostValue().setOwnVATID(value))
                .setMetaData(Janitor.MetaData.HELP, "ownVATID: The seller's VAT ID; writable.");
        DISPATCH.addStringProperty("ownForeignOrganisationID", self -> self.janitorGetHostValue().getOwnForeignOrganisationID(),
                (self, value) -> self.janitorGetHostValue().setOwnForeignOrganisationID(value));
        DISPATCH.addStringProperty("ownOrganisationName", self -> self.janitorGetHostValue().getOwnOrganisationName(),
                (self, value) -> self.janitorGetHostValue().setOwnOrganisationName(value))
                .setMetaData(Janitor.MetaData.HELP, "ownOrganisationName: The seller's name; writable.");
        DISPATCH.addStringProperty("ownStreet", self -> self.janitorGetHostValue().getOwnStreet())
                .setMetaData(Janitor.MetaData.HELP, "ownStreet: The seller's street; read-only.");
        DISPATCH.addStringProperty("ownZIP", self -> self.janitorGetHostValue().getOwnZIP())
                .setMetaData(Janitor.MetaData.HELP, "ownZIP: The seller's postal code; read-only.");
        DISPATCH.addStringProperty("ownLocation", self -> self.janitorGetHostValue().getOwnLocation())
                .setMetaData(Janitor.MetaData.HELP, "ownLocation: The seller's city; read-only.");
        DISPATCH.addStringProperty("ownCountry", self -> self.janitorGetHostValue().getOwnCountry())
                .setMetaData(Janitor.MetaData.HELP, "ownCountry: The seller's country; read-only.");

        // -- referenced documents ----------------------------------------------------

        DISPATCH.addBuilderMethod("setBuyerOrderReferencedDocument", (self, process, args) ->
                self.janitorGetHostValue().setBuyerOrderReferencedDocument(args.getRequired(0, MustangReferencedDocument.class).janitorGetHostValue()))
                .setMetaData(Janitor.MetaData.HELP, "setBuyerOrderReferencedDocument(document): Sets the reference to the buyer's order. Returns this invoice.");
        DISPATCH.addObjectProperty("buyerOrderReferencedDocument",
                self -> self.janitorGetHostValue().getBuyerOrderReferencedDocument() == null ? null : new MustangReferencedDocument(self.janitorGetHostValue().getBuyerOrderReferencedDocument()),
                (self, value) -> self.janitorGetHostValue().setBuyerOrderReferencedDocument(value == null ? null : value.janitorGetHostValue()),
                MustangReferencedDocument::new)
                .setMetaData(Janitor.MetaData.HELP, "buyerOrderReferencedDocument: The reference to the buyer's order, or null; writable.");

        DISPATCH.addBuilderMethod("setSellerOrderReferencedDocument", (self, process, args) ->
                self.janitorGetHostValue().setSellerOrderReferencedDocument(args.getRequired(0, MustangReferencedDocument.class).janitorGetHostValue()))
                .setMetaData(Janitor.MetaData.HELP, "setSellerOrderReferencedDocument(document): Sets the reference to the seller's order (order confirmation). Returns this invoice.");
        DISPATCH.addObjectProperty("sellerOrderReferencedDocument",
                self -> self.janitorGetHostValue().getSellerOrderReferencedDocument() == null ? null : new MustangReferencedDocument(self.janitorGetHostValue().getSellerOrderReferencedDocument()),
                (self, value) -> self.janitorGetHostValue().setSellerOrderReferencedDocument(value == null ? null : value.janitorGetHostValue()),
                MustangReferencedDocument::new)
                .setMetaData(Janitor.MetaData.HELP, "sellerOrderReferencedDocument: The reference to the seller's order (order confirmation), or null; writable.");

        DISPATCH.addBuilderMethod("setContractReferencedDocument", (self, process, args) ->
                self.janitorGetHostValue().setContractReferencedDocument(args.getRequired(0, MustangReferencedDocument.class).janitorGetHostValue()))
                .setMetaData(Janitor.MetaData.HELP, "setContractReferencedDocument(document): Sets the reference to the contract. Returns this invoice.");
        DISPATCH.addObjectProperty("contractReferencedDocument",
                self -> self.janitorGetHostValue().getContractReferencedDocument() == null ? null : new MustangReferencedDocument(self.janitorGetHostValue().getContractReferencedDocument()),
                (self, value) -> self.janitorGetHostValue().setContractReferencedDocument(value == null ? null : value.janitorGetHostValue()),
                MustangReferencedDocument::new)
                .setMetaData(Janitor.MetaData.HELP, "contractReferencedDocument: The reference to the contract, or null; writable.");

        DISPATCH.addBuilderMethod("setDespatchAdviceReferencedDocument", (self, process, args) ->
                self.janitorGetHostValue().setDespatchAdviceReferencedDocument(args.getRequired(0, MustangReferencedDocument.class).janitorGetHostValue()))
                .setMetaData(Janitor.MetaData.HELP, "setDespatchAdviceReferencedDocument(document): Sets the reference to the despatch advice. Returns this invoice.");
        DISPATCH.addObjectProperty("despatchAdviceReferencedDocument",
                self -> self.janitorGetHostValue().getDespatchAdviceReferencedDocument() == null ? null : new MustangReferencedDocument(self.janitorGetHostValue().getDespatchAdviceReferencedDocument()),
                (self, value) -> self.janitorGetHostValue().setDespatchAdviceReferencedDocument(value == null ? null : value.janitorGetHostValue()),
                MustangReferencedDocument::new)
                .setMetaData(Janitor.MetaData.HELP, "despatchAdviceReferencedDocument: The reference to the despatch advice, or null; writable.");

        DISPATCH.addBuilderMethod("setDeliveryNoteReferencedDocument", (self, process, args) ->
                self.janitorGetHostValue().setDeliveryNoteReferencedDocument(args.getRequired(0, MustangReferencedDocument.class).janitorGetHostValue()))
                .setMetaData(Janitor.MetaData.HELP, "setDeliveryNoteReferencedDocument(document): Sets the reference to the delivery note. Returns this invoice.");
        DISPATCH.addObjectProperty("deliveryNoteReferencedDocument",
                self -> self.janitorGetHostValue().getDeliveryNoteReferencedDocument() == null ? null : new MustangReferencedDocument(self.janitorGetHostValue().getDeliveryNoteReferencedDocument()),
                (self, value) -> self.janitorGetHostValue().setDeliveryNoteReferencedDocument(value == null ? null : value.janitorGetHostValue()),
                MustangReferencedDocument::new)
                .setMetaData(Janitor.MetaData.HELP, "deliveryNoteReferencedDocument: The reference to the delivery note, or null; writable.");

        DISPATCH.addBuilderMethod("setTenderReferencedDocument", (self, process, args) ->
                self.janitorGetHostValue().setTenderReferencedDocument(args.getRequired(0, MustangReferencedDocument.class).janitorGetHostValue()))
                .setMetaData(Janitor.MetaData.HELP, "setTenderReferencedDocument(document): Sets the reference to the tender or lot. Returns this invoice.");
        DISPATCH.addMethod("getTenderReferencedDocument", (self, process, args) ->
                self.janitorGetHostValue().getTenderReferencedDocument() instanceof ReferencedDocument rd ? new MustangReferencedDocument(rd) : Janitor.NULL)
                .setMetaData(Janitor.MetaData.HELP, "getTenderReferencedDocument(): Returns the reference to the tender or lot, or null.");

        DISPATCH.addBuilderMethod("setObjectIdentifierReferencedDocument", (self, process, args) ->
                self.janitorGetHostValue().setObjectIdentifierReferencedDocument(args.getRequired(0, MustangReferencedDocument.class).janitorGetHostValue()));
        DISPATCH.addMethod("getObjectIdentifierReferencedDocument", (self, process, args) ->
                self.janitorGetHostValue().getObjectIdentifierReferencedDocument() instanceof ReferencedDocument rd ? new MustangReferencedDocument(rd) : Janitor.NULL);

        DISPATCH.addBuilderMethod("setRelatedReferencedDocument", (self, process, args) ->
                self.janitorGetHostValue().setRelatedReferencedDocument(args.getRequired(0, MustangReferencedDocument.class).janitorGetHostValue()));
        DISPATCH.addMethod("getRelatedReferencedDocument", (self, process, args) ->
                self.janitorGetHostValue().getRelatedReferencedDocument() instanceof ReferencedDocument rd ? new MustangReferencedDocument(rd) : Janitor.NULL);

        DISPATCH.addBuilderMethod("addInvoiceReferencedDocument", (self, process, args) ->
                self.janitorGetHostValue().addInvoiceReferencedDocument(args.getRequired(0, MustangReferencedDocument.class).janitorGetHostValue()))
                .setMetaData(Janitor.MetaData.HELP, "addInvoiceReferencedDocument(document): Adds a reference to a preceding invoice, e.g. the one being corrected. Returns this invoice.");
        DISPATCH.addMethod("getInvoiceReferencedDocuments", (self, process, args) -> {
            final List<JanitorObject> result = new ArrayList<>();
            final List<ReferencedDocument> documents = self.janitorGetHostValue().getInvoiceReferencedDocuments();
            if (documents != null) {
                for (final ReferencedDocument document : documents) {
                    result.add(new MustangReferencedDocument(document));
                }
            }
            return Janitor.list(result);
        }).setMetaData(Janitor.MetaData.HELP, "getInvoiceReferencedDocuments(): Returns a list of the references to preceding invoices.");

        DISPATCH.addBuilderMethod("embedFileInXML", (self, process, args) ->
                self.janitorGetHostValue().embedFileInXML(args.getRequired(0, MustangFileAttachment.class).janitorGetHostValue()))
                .setMetaData(Janitor.MetaData.HELP, "embedFileInXML(attachment): Embeds a file into the invoice XML as an additional referenced document. Returns this invoice.");
        DISPATCH.addMethod("getAdditionalReferencedDocuments", (self, process, args) -> {
            final List<JanitorObject> result = new ArrayList<>();
            final FileAttachment[] attachments = self.janitorGetHostValue().getAdditionalReferencedDocuments();
            if (attachments != null) {
                for (final FileAttachment attachment : attachments) {
                    result.add(new MustangFileAttachment(attachment));
                }
            }
            return Janitor.list(result);
        }).setMetaData(Janitor.MetaData.HELP, "getAdditionalReferencedDocuments(): Returns a list of the files embedded into the invoice XML.");

        // -- notes -------------------------------------------------------------------

        // addNote(String) stores a plain-text paragraph (host getNotes(), no subject code), while
        // addIncludedNote/addGeneralNote/... store a subject-coded note (host getNotesWithSubjectCode());
        // these are two independent lists on the host side, so we expose them as two separate properties.
        DISPATCH.addBuilderMethod("addNote", (self, process, args) -> self.janitorGetHostValue().addNote(args.getRequiredStringValue(0)))
                .setMetaData(Janitor.MetaData.HELP, "addNote(text): Adds a plain-text note without subject code; see getPlainNotes(). Returns this invoice.");
        DISPATCH.addMethod("getPlainNotes", (self, process, args) -> {
            final List<JanitorObject> result = new ArrayList<>();
            final String[] notes = self.janitorGetHostValue().getNotes();
            if (notes != null) {
                for (final String note : notes) {
                    result.add(Janitor.string(note));
                }
            }
            return Janitor.list(result);
        }).setMetaData(Janitor.MetaData.HELP, "getPlainNotes(): Returns a list of the notes added with addNote(), as strings.");
        DISPATCH.addBuilderMethod("addIncludedNote", (self, process, args) ->
                self.janitorGetHostValue().addNotes(List.of(args.getRequired(0, MustangIncludedNote.class).janitorGetHostValue())))
                .setMetaData(Janitor.MetaData.HELP, "addIncludedNote(note): Adds a note object with a subject code; see getNotes(). Returns this invoice.");
        DISPATCH.addBuilderMethod("addGeneralNote", (self, process, args) -> self.janitorGetHostValue().addGeneralNote(args.getRequiredStringValue(0)))
                .setMetaData(Janitor.MetaData.HELP, "addGeneralNote(text): Adds a note with the subject code for general information. Returns this invoice.");
        DISPATCH.addBuilderMethod("addRegulatoryNote", (self, process, args) -> self.janitorGetHostValue().addRegulatoryNote(args.getRequiredStringValue(0)))
                .setMetaData(Janitor.MetaData.HELP, "addRegulatoryNote(text): Adds a note with the subject code for regulatory information. Returns this invoice.");
        DISPATCH.addBuilderMethod("addLegalNote", (self, process, args) -> self.janitorGetHostValue().addLegalNote(args.getRequiredStringValue(0)))
                .setMetaData(Janitor.MetaData.HELP, "addLegalNote(text): Adds a note with the subject code for legal information. Returns this invoice.");
        DISPATCH.addBuilderMethod("addCustomsNote", (self, process, args) -> self.janitorGetHostValue().addCustomsNote(args.getRequiredStringValue(0)))
                .setMetaData(Janitor.MetaData.HELP, "addCustomsNote(text): Adds a note with the subject code for customs information. Returns this invoice.");
        DISPATCH.addBuilderMethod("addSellerNote", (self, process, args) -> self.janitorGetHostValue().addSellerNote(args.getRequiredStringValue(0)))
                .setMetaData(Janitor.MetaData.HELP, "addSellerNote(text): Adds a note with the subject code for remarks by the seller. Returns this invoice.");
        DISPATCH.addBuilderMethod("addTaxNote", (self, process, args) -> self.janitorGetHostValue().addTaxNote(args.getRequiredStringValue(0)))
                .setMetaData(Janitor.MetaData.HELP, "addTaxNote(text): Adds a note with the subject code for tax information. Returns this invoice.");
        DISPATCH.addBuilderMethod("addIntroductionNote", (self, process, args) -> self.janitorGetHostValue().addIntroductionNote(args.getRequiredStringValue(0)))
                .setMetaData(Janitor.MetaData.HELP, "addIntroductionNote(text): Adds a note with the subject code for an introductory text. Returns this invoice.");
        DISPATCH.addBuilderMethod("addDiscountBonusNote", (self, process, args) -> self.janitorGetHostValue().addDiscountBonusNote(args.getRequiredStringValue(0)))
                .setMetaData(Janitor.MetaData.HELP, "addDiscountBonusNote(text): Adds a note with the subject code for discounts and bonuses. Returns this invoice.");
        DISPATCH.addMethod("getNotes", (self, process, args) -> {
            final List<JanitorObject> result = new ArrayList<>();
            final List<IncludedNote> notes = self.janitorGetHostValue().getNotesWithSubjectCode();
            if (notes != null) {
                for (final IncludedNote note : notes) {
                    result.add(new MustangIncludedNote(note));
                }
            }
            return Janitor.list(result);
        }).setMetaData(Janitor.MetaData.HELP, "getNotes(): Returns a list of the notes with subject codes as note objects; plain notes are in getPlainNotes().");

        // -- allowances / charges -----------------------------------------------------

        DISPATCH.addBuilderMethod("addAllowance", (self, process, args) ->
                self.janitorGetHostValue().addAllowance(args.getRequired(0, MustangAllowance.class).janitorGetHostValue()))
                .setMetaData(Janitor.MetaData.HELP, "addAllowance(allowance): Adds a document-level allowance (discount). Returns this invoice.");
        DISPATCH.addMethod("getAllowances", (self, process, args) -> Janitor.list(wrapAllowancesAndCharges(self.janitorGetHostValue().getZFAllowances())))
                .setMetaData(Janitor.MetaData.HELP, "getAllowances(): Returns a list of the document-level allowances (discounts).");

        DISPATCH.addBuilderMethod("addCharge", (self, process, args) ->
                self.janitorGetHostValue().addCharge(args.getRequired(0, MustangCharge.class).janitorGetHostValue()))
                .setMetaData(Janitor.MetaData.HELP, "addCharge(charge): Adds a document-level charge (surcharge). Returns this invoice.");
        DISPATCH.addMethod("getCharges", (self, process, args) -> Janitor.list(wrapAllowancesAndCharges(self.janitorGetHostValue().getZFCharges())))
                .setMetaData(Janitor.MetaData.HELP, "getCharges(): Returns a list of the document-level charges (surcharges).");

        // -- misc identifiers ----------------------------------------------------------

        DISPATCH.addBuilderMethod("setReferenceNumber", (self, process, args) -> self.janitorGetHostValue().setReferenceNumber(args.getRequiredStringValue(0)))
                .setMetaData(Janitor.MetaData.HELP, "setReferenceNumber(reference): Sets the buyer reference, e.g. the routing ID required by public buyers. Returns this invoice.");
        DISPATCH.addStringProperty("referenceNumber", self -> self.janitorGetHostValue().getReferenceNumber(), (self, value) -> self.janitorGetHostValue().setReferenceNumber(value))
                .setMetaData(Janitor.MetaData.HELP, "referenceNumber: The buyer reference, e.g. the routing ID required by public buyers; writable.");

        DISPATCH.addBuilderMethod("setSpecifiedProcuringProjectId", (self, process, args) -> self.janitorGetHostValue().setSpecifiedProcuringProjectID(args.getRequiredStringValue(0)))
                .setMetaData(Janitor.MetaData.HELP, "setSpecifiedProcuringProjectId(id): Sets the ID of the project the invoice refers to. Returns this invoice.");
        DISPATCH.addStringProperty("specifiedProcuringProjectId", self -> self.janitorGetHostValue().getSpecifiedProcuringProjectID(), (self, value) -> self.janitorGetHostValue().setSpecifiedProcuringProjectID(value))
                .setMetaData(Janitor.MetaData.HELP, "specifiedProcuringProjectId: The ID of the project the invoice refers to; writable.");
        DISPATCH.addBuilderMethod("setSpecifiedProcuringProjectName", (self, process, args) -> self.janitorGetHostValue().setSpecifiedProcuringProjectName(args.getRequiredStringValue(0)))
                .setMetaData(Janitor.MetaData.HELP, "setSpecifiedProcuringProjectName(name): Sets the name of the project the invoice refers to. Returns this invoice.");
        DISPATCH.addStringProperty("specifiedProcuringProjectName", self -> self.janitorGetHostValue().getSpecifiedProcuringProjectName(), (self, value) -> self.janitorGetHostValue().setSpecifiedProcuringProjectName(value))
                .setMetaData(Janitor.MetaData.HELP, "specifiedProcuringProjectName: The name of the project the invoice refers to; writable.");

        DISPATCH.addBuilderMethod("setCreditorReferenceId", (self, process, args) -> self.janitorGetHostValue().setCreditorReferenceID(args.getRequiredStringValue(0)))
                .setMetaData(Janitor.MetaData.HELP, "setCreditorReferenceId(id): Sets the SEPA creditor identifier for direct debits. Returns this invoice.");
        DISPATCH.addStringProperty("creditorReferenceId", self -> self.janitorGetHostValue().getCreditorReferenceID(), (self, value) -> self.janitorGetHostValue().setCreditorReferenceID(value))
                .setMetaData(Janitor.MetaData.HELP, "creditorReferenceId: The SEPA creditor identifier for direct debits; writable.");

        DISPATCH.addBuilderMethod("setBusinessProcessId", (self, process, args) -> self.janitorGetHostValue().setBusinessProcessId(args.getRequiredStringValue(0)));
        DISPATCH.addStringProperty("businessProcessId", self -> self.janitorGetHostValue().getBusinessProcessId(), (self, value) -> self.janitorGetHostValue().setBusinessProcessId(value));

        DISPATCH.addBuilderMethod("setVatDueDateTypeCode", (self, process, args) -> self.janitorGetHostValue().setVATDueDateTypeCode(args.getRequiredStringValue(0)));
        DISPATCH.addStringProperty("vatDueDateTypeCode", self -> self.janitorGetHostValue().getVATDueDateTypeCode(), (self, value) -> self.janitorGetHostValue().setVATDueDateTypeCode(value));

        // -- validation ------------------------------------------------------------------

        DISPATCH.addBooleanProperty("valid", self -> self.janitorGetHostValue().isValid());


        DISPATCH.addMethod("exportXmlEn16931", (self, process, args) -> {
            ZUGFeRD2PullProvider zf2p = new ZUGFeRD2PullProvider();
            zf2p.setProfile(Profiles.getByName("EN16931"));
            zf2p.generateXML(self.janitorGetHostValue());
            byte[] xml = zf2p.getXML(); // only in case you need the XML separately
            return Janitor.string(new String(xml, StandardCharsets.UTF_8));
        }).setMetaData(Janitor.MetaData.HELP, "exportXmlEn16931(): Returns the invoice as ZUGFeRD/Factur-X XML in the EN 16931 profile, as a string.");


    }

    public MustangInvoice() {
        super(DISPATCH, new Invoice());
    }

    public MustangInvoice(final Invoice invoice) {
        super(DISPATCH, invoice);
    }

    private static List<JanitorObject> wrapAllowancesAndCharges(final IZUGFeRDAllowanceCharge[] entries) {
        final List<JanitorObject> result = new ArrayList<>();
        if (entries != null) {
            for (final IZUGFeRDAllowanceCharge entry : entries) {
                if (entry instanceof Allowance allowance) {
                    result.add(new MustangAllowance(allowance));
                } else if (entry instanceof Charge charge) {
                    result.add(new MustangCharge(charge));
                }
            }
        }
        return result;
    }
}
