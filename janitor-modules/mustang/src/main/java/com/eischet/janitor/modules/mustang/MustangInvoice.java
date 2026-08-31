package com.eischet.janitor.modules.mustang;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JDate;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import com.eischet.janitor.json.impl.DateTimeUtils;
import org.mustangproject.Allowance;
import org.mustangproject.CashDiscount;
import org.mustangproject.Charge;
import org.mustangproject.FileAttachment;
import org.mustangproject.IncludedNote;
import org.mustangproject.Invoice;
import org.mustangproject.Item;
import org.mustangproject.PaymentTerms;
import org.mustangproject.ReferencedDocument;
import org.mustangproject.ZUGFeRD.IZUGFeRDAllowanceCharge;
import org.mustangproject.ZUGFeRD.IZUGFeRDExportableItem;
import org.mustangproject.ZUGFeRD.IZUGFeRDPaymentTerms;

import java.util.ArrayList;
import java.util.List;

public class MustangInvoice extends JanitorWrapper<Invoice> {
    public static WrapperDispatchTable<Invoice> DISPATCH = new WrapperDispatchTable<>(MustangInvoice::new);

    static {
        DISPATCH.addBuilderMethod("setNumber", (self, process, args) ->
            self.janitorGetHostValue().setNumber(args.require(1).getRequiredStringValue(0)));
        DISPATCH.addStringProperty("number", self -> self.janitorGetHostValue().getNumber(),
            (self, value) -> self.janitorGetHostValue().setNumber(value));
        DISPATCH.addBuilderMethod("setCurrency", (self, process, args) ->
            self.janitorGetHostValue().setCurrency(args.require(1).getRequiredStringValue(0)));
        DISPATCH.addStringProperty("currency", self -> self.janitorGetHostValue().getCurrency(),
            (self, value) -> self.janitorGetHostValue().setCurrency(value));
        DISPATCH.addBuilderMethod("setSender", (self, process, args) -> {
            self.janitorGetHostValue().setSender(args.require(1).getRequired(0, MustangTradeParty.class).janitorGetHostValue());
        });
        DISPATCH.addObjectProperty("sender", self -> new MustangTradeParty(self.janitorGetHostValue().getSender()),
            (self, value) -> self.janitorGetHostValue().setSender(value == null ? null : value.janitorGetHostValue()), MustangTradeParty::new);
        DISPATCH.addBuilderMethod("setIssueDate", (self, process, args) -> {
            self.janitorGetHostValue().setIssueDate(JDate.toLegacyJavaDate(args.require(1).getRequired(0, JDate.class).janitorGetHostValue()));
        });
        DISPATCH.addDateProperty("issueDate", self -> DateTimeUtils.convertDateToLocalDate(self.janitorGetHostValue().getIssueDate()),
            (self, value) -> self.janitorGetHostValue().setIssueDate(JDate.toLegacyJavaDate(value)));
        DISPATCH.addBuilderMethod("setDueDate", (self, process, args) -> {
            self.janitorGetHostValue().setDueDate(JDate.toLegacyJavaDate(args.require(1).getRequired(0, JDate.class).janitorGetHostValue()));
        });
        DISPATCH.addDateProperty("dueDate", self -> DateTimeUtils.convertDateToLocalDate(self.janitorGetHostValue().getDueDate()),
            (self, value) -> self.janitorGetHostValue().setDueDate(JDate.toLegacyJavaDate(value)));
        DISPATCH.addBuilderMethod("setRecipient", (self, process, args) -> {
            self.janitorGetHostValue().setRecipient(args.require(1).getRequired(0, MustangTradeParty.class).janitorGetHostValue());
        });
        DISPATCH.addObjectProperty("recipient", self -> new MustangTradeParty(self.janitorGetHostValue().getRecipient()),
            (self, value) -> self.janitorGetHostValue().setRecipient(value == null ? null : value.janitorGetHostValue()), MustangTradeParty::new);
        DISPATCH.addBuilderMethod("setDetailedDeliveryPeriod", (self, process, args) -> {
            final JDate fromDate = args.getRequired(0, JDate.class);
            final JDate toDate = args.getRequired(1, JDate.class);
            self.janitorGetHostValue().setDetailedDeliveryPeriod(JDate.toLegacyJavaDate(fromDate), JDate.toLegacyJavaDate(toDate));
        });
        DISPATCH.addBuilderMethod("setDetailedDeliveryPeriodFrom", (self, process, args) -> {
            final JDate fromDate = args.getRequired(0, JDate.class);
            self.janitorGetHostValue().setDetailedDeliveryPeriod(JDate.toLegacyJavaDate(fromDate), null);
        });
        DISPATCH.addBuilderMethod("setDetailedDeliveryPeriodTo", (self, process, args) -> {
            final JDate toDate = args.getRequired(0, JDate.class);
            self.janitorGetHostValue().setDetailedDeliveryPeriodTo(JDate.toLegacyJavaDate(toDate));
        });
        DISPATCH.addDateProperty("detailedDeliveryPeriodFrom", self -> DateTimeUtils.convertDateToLocalDate(self.janitorGetHostValue().getDetailedDeliveryPeriodFrom()),
            (self, date) -> self.janitorGetHostValue().setDetailedDeliveryPeriodFrom(JDate.toLegacyJavaDate(date)));
        DISPATCH.addDateProperty("detailedDeliveryPeriodTo", self -> DateTimeUtils.convertDateToLocalDate(self.janitorGetHostValue().getDetailedDeliveryPeriodTo()),
            (self, date) -> self.janitorGetHostValue().setDetailedDeliveryPeriodTo(JDate.toLegacyJavaDate(date)));

        DISPATCH.addBuilderMethod("addItem", (self, process, args) -> {
            final MustangItem item = args.getRequired(0, MustangItem.class);
            self.janitorGetHostValue().addItem(item.janitorGetHostValue());
        });
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
        });

        // -- document identity / delivery dates -----------------------------------

        DISPATCH.addBuilderMethod("setTestIndicator", (self, process, args) -> self.janitorGetHostValue().setTestIndicator());
        DISPATCH.addBooleanProperty("testIndicator", self -> self.janitorGetHostValue().getTestIndicator());

        DISPATCH.addBuilderMethod("setDocumentName", (self, process, args) -> self.janitorGetHostValue().setDocumentName(args.getRequiredStringValue(0)));
        DISPATCH.addStringProperty("documentName", self -> self.janitorGetHostValue().getDocumentName(), (self, value) -> self.janitorGetHostValue().setDocumentName(value));

        DISPATCH.addStringProperty("documentCode", self -> self.janitorGetHostValue().getDocumentCode());

        DISPATCH.addBuilderMethod("setCorrection", (self, process, args) -> self.janitorGetHostValue().setCorrection(args.getRequiredStringValue(0)));
        DISPATCH.addBuilderMethod("setCreditNote", (self, process, args) -> {
            if (args.size() == 0) {
                self.janitorGetHostValue().setCreditNote();
            } else {
                self.janitorGetHostValue().setCreditNote(args.getRequiredStringValue(0));
            }
        });

        DISPATCH.addBuilderMethod("setDeliveryDate", (self, process, args) ->
            self.janitorGetHostValue().setDeliveryDate(JDate.toLegacyJavaDate(args.getRequired(0, JDate.class).janitorGetHostValue())));
        DISPATCH.addDateProperty("deliveryDate", self -> DateTimeUtils.convertDateToLocalDate(self.janitorGetHostValue().getDeliveryDate()),
            (self, value) -> self.janitorGetHostValue().setDeliveryDate(JDate.toLegacyJavaDate(value)));

        DISPATCH.addBuilderMethod("setDeliveryTypeCode", (self, process, args) -> self.janitorGetHostValue().setDeliveryTypeCode(args.getRequiredStringValue(0)));
        DISPATCH.addStringProperty("deliveryTypeCode", self -> self.janitorGetHostValue().getDeliveryTypeCode(), (self, value) -> self.janitorGetHostValue().setDeliveryTypeCode(value));

        // -- currency / tax / amounts --------------------------------------------

        DISPATCH.addBuilderMethod("setTaxCurrency", (self, process, args) -> self.janitorGetHostValue().setTaxCurrency(args.getRequiredStringValue(0)));
        DISPATCH.addStringProperty("taxCurrency", self -> self.janitorGetHostValue().getTaxCurrency(), (self, value) -> self.janitorGetHostValue().setTaxCurrency(value));

        DISPATCH.addBuilderMethod("setTaxConversionRate", (self, process, args) -> self.janitorGetHostValue().setTaxConversionRate(args.getRequiredJNumber(0).toBigDecimal()));
        DISPATCH.addBigDecimalProperty("taxConversionRate", self -> self.janitorGetHostValue().getTaxConversionRate(), (self, value) -> self.janitorGetHostValue().setTaxConversionRate(value));

        DISPATCH.addBuilderMethod("setTaxConversionRateDateTime", (self, process, args) ->
            self.janitorGetHostValue().setTaxConversionRateDateTime(JDate.toLegacyJavaDate(args.getRequired(0, JDate.class).janitorGetHostValue())));
        DISPATCH.addDateProperty("taxConversionRateDateTime", self -> DateTimeUtils.convertDateToLocalDate(self.janitorGetHostValue().getTaxConversionRateDateTime()),
            (self, value) -> self.janitorGetHostValue().setTaxConversionRateDateTime(JDate.toLegacyJavaDate(value)));

        DISPATCH.addBuilderMethod("setRoundingAmount", (self, process, args) -> self.janitorGetHostValue().setRoundingAmount(args.getRequiredJNumber(0).toBigDecimal()));
        DISPATCH.addBigDecimalProperty("roundingAmount", self -> self.janitorGetHostValue().getRoundingAmount(), (self, value) -> self.janitorGetHostValue().setRoundingAmount(value));

        DISPATCH.addBuilderMethod("setTotalPrepaidAmount", (self, process, args) -> self.janitorGetHostValue().setTotalPrepaidAmount(args.getRequiredJNumber(0).toBigDecimal()));
        DISPATCH.addBigDecimalProperty("totalPrepaidAmount", self -> self.janitorGetHostValue().getTotalPrepaidAmount(), (self, value) -> self.janitorGetHostValue().setTotalPrepaidAmount(value));

        // -- payment ---------------------------------------------------------------

        DISPATCH.addBuilderMethod("setPaymentReference", (self, process, args) -> self.janitorGetHostValue().setPaymentReference(args.getRequiredStringValue(0)));
        DISPATCH.addStringProperty("paymentReference", self -> self.janitorGetHostValue().getPaymentReference(), (self, value) -> self.janitorGetHostValue().setPaymentReference(value));

        DISPATCH.addBuilderMethod("setPaymentTermDescription", (self, process, args) -> self.janitorGetHostValue().setPaymentTermDescription(args.getRequiredStringValue(0)));
        DISPATCH.addStringProperty("paymentTermDescription", self -> self.janitorGetHostValue().getPaymentTermDescription(), (self, value) -> self.janitorGetHostValue().setPaymentTermDescription(value));

        DISPATCH.addBuilderMethod("setPaymentTerms", (self, process, args) ->
            self.janitorGetHostValue().setPaymentTerms(args.getRequired(0, MustangPaymentTerms.class).janitorGetHostValue()));
        DISPATCH.addObjectProperty("paymentTerms",
            self -> self.janitorGetHostValue().getPaymentTerms() instanceof PaymentTerms paymentTerms ? new MustangPaymentTerms(paymentTerms) : null,
            (self, value) -> self.janitorGetHostValue().setPaymentTerms(value == null ? null : value.janitorGetHostValue()),
            MustangPaymentTerms::new);

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
            self.janitorGetHostValue().addCashDiscount(args.getRequired(0, MustangCashDiscount.class).janitorGetHostValue()));
        DISPATCH.addMethod("getCashDiscounts", (self, process, args) -> {
            final List<JanitorObject> result = new ArrayList<>();
            final CashDiscount[] discounts = self.janitorGetHostValue().getCashDiscounts();
            if (discounts != null) {
                for (final CashDiscount discount : discounts) {
                    result.add(new MustangCashDiscount(discount));
                }
            }
            return Janitor.list(result);
        });

        // -- involved parties --------------------------------------------------------

        DISPATCH.addBuilderMethod("setPayee", (self, process, args) ->
            self.janitorGetHostValue().setPayee(args.getRequired(0, MustangTradeParty.class).janitorGetHostValue()));
        DISPATCH.addObjectProperty("payee",
            self -> self.janitorGetHostValue().getPayee() == null ? null : new MustangTradeParty(self.janitorGetHostValue().getPayee()),
            (self, value) -> self.janitorGetHostValue().setPayee(value == null ? null : value.janitorGetHostValue()),
            MustangTradeParty::new);

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
            self.janitorGetHostValue().setTaxRepresentative(args.getRequired(0, MustangTradeParty.class).janitorGetHostValue()));
        DISPATCH.addObjectProperty("taxRepresentative",
            self -> self.janitorGetHostValue().getTaxRepresentative() == null ? null : new MustangTradeParty(self.janitorGetHostValue().getTaxRepresentative()),
            (self, value) -> self.janitorGetHostValue().setTaxRepresentative(value == null ? null : value.janitorGetHostValue()),
            MustangTradeParty::new);

        DISPATCH.addBuilderMethod("setDeliveryAddress", (self, process, args) ->
            self.janitorGetHostValue().setDeliveryAddress(args.getRequired(0, MustangTradeParty.class).janitorGetHostValue()));
        DISPATCH.addObjectProperty("deliveryAddress",
            self -> self.janitorGetHostValue().getDeliveryAddress() == null ? null : new MustangTradeParty(self.janitorGetHostValue().getDeliveryAddress()),
            (self, value) -> self.janitorGetHostValue().setDeliveryAddress(value == null ? null : value.janitorGetHostValue()),
            MustangTradeParty::new);

        DISPATCH.addBuilderMethod("setEndCustomerDeliveryAddress", (self, process, args) ->
            self.janitorGetHostValue().setEndCustomerDeliveryAddress(args.getRequired(0, MustangTradeParty.class).janitorGetHostValue()));
        DISPATCH.addObjectProperty("endCustomerDeliveryAddress",
            self -> self.janitorGetHostValue().getEndCustomerDeliveryAddress() == null ? null : new MustangTradeParty(self.janitorGetHostValue().getEndCustomerDeliveryAddress()),
            (self, value) -> self.janitorGetHostValue().setEndCustomerDeliveryAddress(value == null ? null : value.janitorGetHostValue()),
            MustangTradeParty::new);

        // -- ship-to (used e.g. for despatch advices) --------------------------------

        DISPATCH.addBuilderMethod("setShipToOrganisationId", (self, process, args) -> self.janitorGetHostValue().setShipToOrganisationID(args.getRequiredStringValue(0)));
        DISPATCH.addStringProperty("shipToOrganisationId", self -> self.janitorGetHostValue().getShipToOrganisationID(), (self, value) -> self.janitorGetHostValue().setShipToOrganisationID(value));
        DISPATCH.addBuilderMethod("setShipToOrganisationName", (self, process, args) -> self.janitorGetHostValue().setShipToOrganisationName(args.getRequiredStringValue(0)));
        DISPATCH.addStringProperty("shipToOrganisationName", self -> self.janitorGetHostValue().getShipToOrganisationName(), (self, value) -> self.janitorGetHostValue().setShipToOrganisationName(value));
        DISPATCH.addBuilderMethod("setShipToStreet", (self, process, args) -> self.janitorGetHostValue().setShipToStreet(args.getRequiredStringValue(0)));
        DISPATCH.addStringProperty("shipToStreet", self -> self.janitorGetHostValue().getShipToStreet(), (self, value) -> self.janitorGetHostValue().setShipToStreet(value));
        DISPATCH.addBuilderMethod("setShipToZip", (self, process, args) -> self.janitorGetHostValue().setShipToZIP(args.getRequiredStringValue(0)));
        DISPATCH.addStringProperty("shipToZip", self -> self.janitorGetHostValue().getShipToZIP(), (self, value) -> self.janitorGetHostValue().setShipToZIP(value));
        DISPATCH.addBuilderMethod("setShipToLocation", (self, process, args) -> self.janitorGetHostValue().setShipToLocation(args.getRequiredStringValue(0)));
        DISPATCH.addStringProperty("shipToLocation", self -> self.janitorGetHostValue().getShipToLocation(), (self, value) -> self.janitorGetHostValue().setShipToLocation(value));
        DISPATCH.addBuilderMethod("setShipToCountry", (self, process, args) -> self.janitorGetHostValue().setShipToCountry(args.getRequiredStringValue(0)));
        DISPATCH.addStringProperty("shipToCountry", self -> self.janitorGetHostValue().getShipToCountry(), (self, value) -> self.janitorGetHostValue().setShipToCountry(value));

        // -- "own organisation" read-only convenience getters (mirror sender) -------

        DISPATCH.addStringProperty("ownOrganisationFullPlaintextInfo", self -> self.janitorGetHostValue().getOwnOrganisationFullPlaintextInfo(),
            (self, value) -> self.janitorGetHostValue().setOwnOrganisationFullPlaintextInfo(value));
        DISPATCH.addStringProperty("ownTaxID", self -> self.janitorGetHostValue().getOwnTaxID(), (self, value) -> self.janitorGetHostValue().setOwnTaxID(value));
        DISPATCH.addStringProperty("ownVATID", self -> self.janitorGetHostValue().getOwnVATID(), (self, value) -> self.janitorGetHostValue().setOwnVATID(value));
        DISPATCH.addStringProperty("ownForeignOrganisationID", self -> self.janitorGetHostValue().getOwnForeignOrganisationID(),
            (self, value) -> self.janitorGetHostValue().setOwnForeignOrganisationID(value));
        DISPATCH.addStringProperty("ownOrganisationName", self -> self.janitorGetHostValue().getOwnOrganisationName(),
            (self, value) -> self.janitorGetHostValue().setOwnOrganisationName(value));
        DISPATCH.addStringProperty("ownStreet", self -> self.janitorGetHostValue().getOwnStreet());
        DISPATCH.addStringProperty("ownZIP", self -> self.janitorGetHostValue().getOwnZIP());
        DISPATCH.addStringProperty("ownLocation", self -> self.janitorGetHostValue().getOwnLocation());
        DISPATCH.addStringProperty("ownCountry", self -> self.janitorGetHostValue().getOwnCountry());

        // -- referenced documents ----------------------------------------------------

        DISPATCH.addBuilderMethod("setBuyerOrderReferencedDocument", (self, process, args) ->
            self.janitorGetHostValue().setBuyerOrderReferencedDocument(args.getRequired(0, MustangReferencedDocument.class).janitorGetHostValue()));
        DISPATCH.addObjectProperty("buyerOrderReferencedDocument",
            self -> self.janitorGetHostValue().getBuyerOrderReferencedDocument() == null ? null : new MustangReferencedDocument(self.janitorGetHostValue().getBuyerOrderReferencedDocument()),
            (self, value) -> self.janitorGetHostValue().setBuyerOrderReferencedDocument(value == null ? null : value.janitorGetHostValue()),
            MustangReferencedDocument::new);

        DISPATCH.addBuilderMethod("setSellerOrderReferencedDocument", (self, process, args) ->
            self.janitorGetHostValue().setSellerOrderReferencedDocument(args.getRequired(0, MustangReferencedDocument.class).janitorGetHostValue()));
        DISPATCH.addObjectProperty("sellerOrderReferencedDocument",
            self -> self.janitorGetHostValue().getSellerOrderReferencedDocument() == null ? null : new MustangReferencedDocument(self.janitorGetHostValue().getSellerOrderReferencedDocument()),
            (self, value) -> self.janitorGetHostValue().setSellerOrderReferencedDocument(value == null ? null : value.janitorGetHostValue()),
            MustangReferencedDocument::new);

        DISPATCH.addBuilderMethod("setContractReferencedDocument", (self, process, args) ->
            self.janitorGetHostValue().setContractReferencedDocument(args.getRequired(0, MustangReferencedDocument.class).janitorGetHostValue()));
        DISPATCH.addObjectProperty("contractReferencedDocument",
            self -> self.janitorGetHostValue().getContractReferencedDocument() == null ? null : new MustangReferencedDocument(self.janitorGetHostValue().getContractReferencedDocument()),
            (self, value) -> self.janitorGetHostValue().setContractReferencedDocument(value == null ? null : value.janitorGetHostValue()),
            MustangReferencedDocument::new);

        DISPATCH.addBuilderMethod("setDespatchAdviceReferencedDocument", (self, process, args) ->
            self.janitorGetHostValue().setDespatchAdviceReferencedDocument(args.getRequired(0, MustangReferencedDocument.class).janitorGetHostValue()));
        DISPATCH.addObjectProperty("despatchAdviceReferencedDocument",
            self -> self.janitorGetHostValue().getDespatchAdviceReferencedDocument() == null ? null : new MustangReferencedDocument(self.janitorGetHostValue().getDespatchAdviceReferencedDocument()),
            (self, value) -> self.janitorGetHostValue().setDespatchAdviceReferencedDocument(value == null ? null : value.janitorGetHostValue()),
            MustangReferencedDocument::new);

        DISPATCH.addBuilderMethod("setDeliveryNoteReferencedDocument", (self, process, args) ->
            self.janitorGetHostValue().setDeliveryNoteReferencedDocument(args.getRequired(0, MustangReferencedDocument.class).janitorGetHostValue()));
        DISPATCH.addObjectProperty("deliveryNoteReferencedDocument",
            self -> self.janitorGetHostValue().getDeliveryNoteReferencedDocument() == null ? null : new MustangReferencedDocument(self.janitorGetHostValue().getDeliveryNoteReferencedDocument()),
            (self, value) -> self.janitorGetHostValue().setDeliveryNoteReferencedDocument(value == null ? null : value.janitorGetHostValue()),
            MustangReferencedDocument::new);

        DISPATCH.addBuilderMethod("setTenderReferencedDocument", (self, process, args) ->
            self.janitorGetHostValue().setTenderReferencedDocument(args.getRequired(0, MustangReferencedDocument.class).janitorGetHostValue()));
        DISPATCH.addMethod("getTenderReferencedDocument", (self, process, args) ->
            self.janitorGetHostValue().getTenderReferencedDocument() instanceof ReferencedDocument rd ? new MustangReferencedDocument(rd) : Janitor.NULL);

        DISPATCH.addBuilderMethod("setObjectIdentifierReferencedDocument", (self, process, args) ->
            self.janitorGetHostValue().setObjectIdentifierReferencedDocument(args.getRequired(0, MustangReferencedDocument.class).janitorGetHostValue()));
        DISPATCH.addMethod("getObjectIdentifierReferencedDocument", (self, process, args) ->
            self.janitorGetHostValue().getObjectIdentifierReferencedDocument() instanceof ReferencedDocument rd ? new MustangReferencedDocument(rd) : Janitor.NULL);

        DISPATCH.addBuilderMethod("setRelatedReferencedDocument", (self, process, args) ->
            self.janitorGetHostValue().setRelatedReferencedDocument(args.getRequired(0, MustangReferencedDocument.class).janitorGetHostValue()));
        DISPATCH.addMethod("getRelatedReferencedDocument", (self, process, args) ->
            self.janitorGetHostValue().getRelatedReferencedDocument() instanceof ReferencedDocument rd ? new MustangReferencedDocument(rd) : Janitor.NULL);

        DISPATCH.addBuilderMethod("addInvoiceReferencedDocument", (self, process, args) ->
            self.janitorGetHostValue().addInvoiceReferencedDocument(args.getRequired(0, MustangReferencedDocument.class).janitorGetHostValue()));
        DISPATCH.addMethod("getInvoiceReferencedDocuments", (self, process, args) -> {
            final List<JanitorObject> result = new ArrayList<>();
            final List<ReferencedDocument> documents = self.janitorGetHostValue().getInvoiceReferencedDocuments();
            if (documents != null) {
                for (final ReferencedDocument document : documents) {
                    result.add(new MustangReferencedDocument(document));
                }
            }
            return Janitor.list(result);
        });

        DISPATCH.addBuilderMethod("embedFileInXML", (self, process, args) ->
            self.janitorGetHostValue().embedFileInXML(args.getRequired(0, MustangFileAttachment.class).janitorGetHostValue()));
        DISPATCH.addMethod("getAdditionalReferencedDocuments", (self, process, args) -> {
            final List<JanitorObject> result = new ArrayList<>();
            final FileAttachment[] attachments = self.janitorGetHostValue().getAdditionalReferencedDocuments();
            if (attachments != null) {
                for (final FileAttachment attachment : attachments) {
                    result.add(new MustangFileAttachment(attachment));
                }
            }
            return Janitor.list(result);
        });

        // -- notes -------------------------------------------------------------------

        // addNote(String) stores a plain-text paragraph (host getNotes(), no subject code), while
        // addIncludedNote/addGeneralNote/... store a subject-coded note (host getNotesWithSubjectCode());
        // these are two independent lists on the host side, so we expose them as two separate properties.
        DISPATCH.addBuilderMethod("addNote", (self, process, args) -> self.janitorGetHostValue().addNote(args.getRequiredStringValue(0)));
        DISPATCH.addMethod("getPlainNotes", (self, process, args) -> {
            final List<JanitorObject> result = new ArrayList<>();
            final String[] notes = self.janitorGetHostValue().getNotes();
            if (notes != null) {
                for (final String note : notes) {
                    result.add(Janitor.string(note));
                }
            }
            return Janitor.list(result);
        });
        DISPATCH.addBuilderMethod("addIncludedNote", (self, process, args) ->
            self.janitorGetHostValue().addNotes(List.of(args.getRequired(0, MustangIncludedNote.class).janitorGetHostValue())));
        DISPATCH.addBuilderMethod("addGeneralNote", (self, process, args) -> self.janitorGetHostValue().addGeneralNote(args.getRequiredStringValue(0)));
        DISPATCH.addBuilderMethod("addRegulatoryNote", (self, process, args) -> self.janitorGetHostValue().addRegulatoryNote(args.getRequiredStringValue(0)));
        DISPATCH.addBuilderMethod("addLegalNote", (self, process, args) -> self.janitorGetHostValue().addLegalNote(args.getRequiredStringValue(0)));
        DISPATCH.addBuilderMethod("addCustomsNote", (self, process, args) -> self.janitorGetHostValue().addCustomsNote(args.getRequiredStringValue(0)));
        DISPATCH.addBuilderMethod("addSellerNote", (self, process, args) -> self.janitorGetHostValue().addSellerNote(args.getRequiredStringValue(0)));
        DISPATCH.addBuilderMethod("addTaxNote", (self, process, args) -> self.janitorGetHostValue().addTaxNote(args.getRequiredStringValue(0)));
        DISPATCH.addBuilderMethod("addIntroductionNote", (self, process, args) -> self.janitorGetHostValue().addIntroductionNote(args.getRequiredStringValue(0)));
        DISPATCH.addBuilderMethod("addDiscountBonusNote", (self, process, args) -> self.janitorGetHostValue().addDiscountBonusNote(args.getRequiredStringValue(0)));
        DISPATCH.addMethod("getNotes", (self, process, args) -> {
            final List<JanitorObject> result = new ArrayList<>();
            final List<IncludedNote> notes = self.janitorGetHostValue().getNotesWithSubjectCode();
            if (notes != null) {
                for (final IncludedNote note : notes) {
                    result.add(new MustangIncludedNote(note));
                }
            }
            return Janitor.list(result);
        });

        // -- allowances / charges -----------------------------------------------------

        DISPATCH.addBuilderMethod("addAllowance", (self, process, args) ->
            self.janitorGetHostValue().addAllowance(args.getRequired(0, MustangAllowance.class).janitorGetHostValue()));
        DISPATCH.addMethod("getAllowances", (self, process, args) -> Janitor.list(wrapAllowancesAndCharges(self.janitorGetHostValue().getZFAllowances())));

        DISPATCH.addBuilderMethod("addCharge", (self, process, args) ->
            self.janitorGetHostValue().addCharge(args.getRequired(0, MustangCharge.class).janitorGetHostValue()));
        DISPATCH.addMethod("getCharges", (self, process, args) -> Janitor.list(wrapAllowancesAndCharges(self.janitorGetHostValue().getZFCharges())));

        // -- misc identifiers ----------------------------------------------------------

        DISPATCH.addBuilderMethod("setReferenceNumber", (self, process, args) -> self.janitorGetHostValue().setReferenceNumber(args.getRequiredStringValue(0)));
        DISPATCH.addStringProperty("referenceNumber", self -> self.janitorGetHostValue().getReferenceNumber(), (self, value) -> self.janitorGetHostValue().setReferenceNumber(value));

        DISPATCH.addBuilderMethod("setSpecifiedProcuringProjectId", (self, process, args) -> self.janitorGetHostValue().setSpecifiedProcuringProjectID(args.getRequiredStringValue(0)));
        DISPATCH.addStringProperty("specifiedProcuringProjectId", self -> self.janitorGetHostValue().getSpecifiedProcuringProjectID(), (self, value) -> self.janitorGetHostValue().setSpecifiedProcuringProjectID(value));
        DISPATCH.addBuilderMethod("setSpecifiedProcuringProjectName", (self, process, args) -> self.janitorGetHostValue().setSpecifiedProcuringProjectName(args.getRequiredStringValue(0)));
        DISPATCH.addStringProperty("specifiedProcuringProjectName", self -> self.janitorGetHostValue().getSpecifiedProcuringProjectName(), (self, value) -> self.janitorGetHostValue().setSpecifiedProcuringProjectName(value));

        DISPATCH.addBuilderMethod("setCreditorReferenceId", (self, process, args) -> self.janitorGetHostValue().setCreditorReferenceID(args.getRequiredStringValue(0)));
        DISPATCH.addStringProperty("creditorReferenceId", self -> self.janitorGetHostValue().getCreditorReferenceID(), (self, value) -> self.janitorGetHostValue().setCreditorReferenceID(value));

        DISPATCH.addBuilderMethod("setBusinessProcessId", (self, process, args) -> self.janitorGetHostValue().setBusinessProcessId(args.getRequiredStringValue(0)));
        DISPATCH.addStringProperty("businessProcessId", self -> self.janitorGetHostValue().getBusinessProcessId(), (self, value) -> self.janitorGetHostValue().setBusinessProcessId(value));

        DISPATCH.addBuilderMethod("setVatDueDateTypeCode", (self, process, args) -> self.janitorGetHostValue().setVATDueDateTypeCode(args.getRequiredStringValue(0)));
        DISPATCH.addStringProperty("vatDueDateTypeCode", self -> self.janitorGetHostValue().getVATDueDateTypeCode(), (self, value) -> self.janitorGetHostValue().setVATDueDateTypeCode(value));

        // -- validation ------------------------------------------------------------------

        DISPATCH.addBooleanProperty("valid", self -> self.janitorGetHostValue().isValid());
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

    public MustangInvoice() {
        super(DISPATCH, new Invoice());
    }

    public MustangInvoice(final Invoice invoice) {
        super(DISPATCH, invoice);
    }
}
