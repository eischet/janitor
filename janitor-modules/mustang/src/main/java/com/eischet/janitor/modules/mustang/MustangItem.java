// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.mustang;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JDate;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import com.eischet.janitor.toolbox.time.DateTimeUtils;
import org.mustangproject.Allowance;
import org.mustangproject.Charge;
import org.mustangproject.IncludedNote;
import org.mustangproject.Item;
import org.mustangproject.Product;
import org.mustangproject.ReferencedDocument;
import org.mustangproject.TradeParty;
import org.mustangproject.ZUGFeRD.IReferencedDocument;
import org.mustangproject.ZUGFeRD.IZUGFeRDAllowanceCharge;

import java.util.ArrayList;
import java.util.List;

/** Wrapper for an item (line) of a Mustang invoice. */
public class MustangItem extends JanitorWrapper<Item> {
    public static WrapperDispatchTable<Item> DISPATCH = new WrapperDispatchTable<>(MustangItem::new);

    static {
        DISPATCH.addBuilderMethod("setQuantity", (self, process, args) ->
            self.janitorGetHostValue().setQuantity(args.getRequiredJNumber(0).toBigDecimal()))
                .setMetaData(Janitor.MetaData.HELP, "setQuantity(quantity): Sets the invoiced quantity. Returns this item.");
        DISPATCH.addBigDecimalProperty("quantity", self -> self.janitorGetHostValue().getQuantity(), (self, value) -> self.janitorGetHostValue().setQuantity(value))
                .setMetaData(Janitor.MetaData.HELP, "quantity: The invoiced quantity; writable.");

        DISPATCH.addBuilderMethod("setPrice",(self, process, args) ->
            self.janitorGetHostValue().setPrice(args.getRequiredJNumber(0).toBigDecimal()))
                .setMetaData(Janitor.MetaData.HELP, "setPrice(price): Sets the net unit price. Returns this item.");
        DISPATCH.addBigDecimalProperty("price", self -> self.janitorGetHostValue().getPrice(), (self, value) -> self.janitorGetHostValue().setPrice(value))
                .setMetaData(Janitor.MetaData.HELP, "price: The net unit price; writable.");

        DISPATCH.addBuilderMethod("setTax", (self, process, args) ->
            self.janitorGetHostValue().setTax(args.getRequiredJNumber(0).toBigDecimal()));
        DISPATCH.addBigDecimalProperty("tax", self -> self.janitorGetHostValue().getTax(), (self, value) -> self.janitorGetHostValue().setTax(value));

        DISPATCH.addBuilderMethod("setGrossPrice", (self, process, args) ->
            self.janitorGetHostValue().setGrossPrice(args.getRequiredJNumber(0).toBigDecimal()))
                .setMetaData(Janitor.MetaData.HELP, "setGrossPrice(price): Sets the unit price before item allowances and charges. Returns this item.");
        DISPATCH.addBigDecimalProperty("grossPrice", self -> self.janitorGetHostValue().getGrossPrice(), (self, value) -> self.janitorGetHostValue().setGrossPrice(value))
                .setMetaData(Janitor.MetaData.HELP, "grossPrice: The unit price before item allowances and charges; writable.");

        DISPATCH.addBuilderMethod("setLineTotalAmount", (self, process, args) ->
            self.janitorGetHostValue().setLineTotalAmount(args.getRequiredJNumber(0).toBigDecimal()))
                .setMetaData(Janitor.MetaData.HELP, "setLineTotalAmount(amount): Sets the net total amount of the line. Returns this item.");
        DISPATCH.addBigDecimalProperty("lineTotalAmount", self -> self.janitorGetHostValue().getLineTotalAmount(), (self, value) -> self.janitorGetHostValue().setLineTotalAmount(value))
                .setMetaData(Janitor.MetaData.HELP, "lineTotalAmount: The net total amount of the line; writable.");

        DISPATCH.addBuilderMethod("setBasisQuantity", (self, process, args) ->
            self.janitorGetHostValue().setBasisQuantity(args.getRequiredJNumber(0).toBigDecimal()))
                .setMetaData(Janitor.MetaData.HELP, "setBasisQuantity(quantity): Sets the quantity the price refers to, e.g. 100 for a price per hundred. Returns this item.");
        DISPATCH.addBigDecimalProperty("basisQuantity", self -> self.janitorGetHostValue().getBasisQuantity(), (self, value) -> self.janitorGetHostValue().setBasisQuantity(value))
                .setMetaData(Janitor.MetaData.HELP, "basisQuantity: The quantity the price refers to, e.g. 100 for a price per hundred; writable.");

        DISPATCH.addBuilderMethod("setId", (self, process, args) -> self.janitorGetHostValue().setId(args.getRequiredStringValue(0)))
                .setMetaData(Janitor.MetaData.HELP, "setId(id): Sets the ID of the line. Returns this item.");
        DISPATCH.addStringProperty("id", self -> self.janitorGetHostValue().getId(), (self, value) -> self.janitorGetHostValue().setId(value))
                .setMetaData(Janitor.MetaData.HELP, "id: The ID of the line; writable.");

        DISPATCH.addBuilderMethod("setAccountingReference", (self, process, args) -> self.janitorGetHostValue().setAccountingReference(args.getRequiredStringValue(0)))
                .setMetaData(Janitor.MetaData.HELP, "setAccountingReference(reference): Sets the buyer's accounting reference for the line. Returns this item.");
        DISPATCH.addStringProperty("accountingReference", self -> self.janitorGetHostValue().getAccountingReference(), (self, value) -> self.janitorGetHostValue().setAccountingReference(value))
                .setMetaData(Janitor.MetaData.HELP, "accountingReference: The buyer's accounting reference for the line; writable.");

        DISPATCH.addBuilderMethod("setParentLineId", (self, process, args) -> self.janitorGetHostValue().setParentLineID(args.getRequiredStringValue(0)))
                .setMetaData(Janitor.MetaData.HELP, "setParentLineId(id): Sets the ID of the line this line belongs to, making it a sub-line. Returns this item.");
        DISPATCH.addStringProperty("parentLineId", self -> self.janitorGetHostValue().getParentLineID(), (self, value) -> self.janitorGetHostValue().setParentLineID(value))
                .setMetaData(Janitor.MetaData.HELP, "parentLineId: The ID of the line this line belongs to, or null; writable.");

        DISPATCH.addBuilderMethod("setLineStatusReasonCode", (self, process, args) -> self.janitorGetHostValue().setLineStatusReasonCode(args.getRequiredStringValue(0)));
        DISPATCH.addStringProperty("lineStatusReasonCode", self -> self.janitorGetHostValue().getLineStatusReasonCode(), (self, value) -> self.janitorGetHostValue().setLineStatusReasonCode(value));

        // -- delivery period ---------------------------------------------------

        DISPATCH.addBuilderMethod("setDetailedDeliveryPeriod", (self, process, args) -> {
            final JDate fromDate = args.getRequired(0, JDate.class);
            final JDate toDate = args.getRequired(1, JDate.class);
            self.janitorGetHostValue().setDetailedDeliveryPeriod(JDate.toLegacyJavaDate(fromDate), JDate.toLegacyJavaDate(toDate));
        }).setMetaData(Janitor.MetaData.HELP, "setDetailedDeliveryPeriod(fromDate, toDate): Sets the delivery period of the line. Returns this item.");
        DISPATCH.addDateProperty("detailedDeliveryPeriodFrom", self -> DateTimeUtils.convertDateToLocalDate(self.janitorGetHostValue().getDetailedDeliveryPeriodFrom()))
                .setMetaData(Janitor.MetaData.HELP, "detailedDeliveryPeriodFrom: The first day of the line's delivery period, or null; read-only.");
        DISPATCH.addDateProperty("detailedDeliveryPeriodTo", self -> DateTimeUtils.convertDateToLocalDate(self.janitorGetHostValue().getDetailedDeliveryPeriodTo()))
                .setMetaData(Janitor.MetaData.HELP, "detailedDeliveryPeriodTo: The last day of the line's delivery period, or null; read-only.");

        // -- product / seller ----------------------------------------------------

        DISPATCH.addBuilderMethod("setProduct", (self, process, args) ->
            self.janitorGetHostValue().setProduct(args.getRequired(0, MustangProduct.class).janitorGetHostValue()))
                .setMetaData(Janitor.MetaData.HELP, "setProduct(product): Sets the product of the line. Returns this item.");
        DISPATCH.addObjectProperty("product",
            self -> self.janitorGetHostValue().getProduct() instanceof Product product ? new MustangProduct(product) : null,
            (self, value) -> self.janitorGetHostValue().setProduct(value == null ? null : value.janitorGetHostValue()),
            MustangProduct::new)
                .setMetaData(Janitor.MetaData.HELP, "product: The product of the line, or null; writable.");

        DISPATCH.addBuilderMethod("setLineSeller", (self, process, args) ->
            self.janitorGetHostValue().setLineSeller(args.getRequired(0, MustangTradeParty.class).janitorGetHostValue()))
                .setMetaData(Janitor.MetaData.HELP, "setLineSeller(tradeParty): Sets a seller for this line that differs from the invoice's seller. Returns this item.");
        DISPATCH.addObjectProperty("lineSeller",
            self -> self.janitorGetHostValue().getLineSeller() instanceof TradeParty tradeParty ? new MustangTradeParty(tradeParty) : null,
            (self, value) -> self.janitorGetHostValue().setLineSeller(value == null ? null : value.janitorGetHostValue()),
            MustangTradeParty::new)
                .setMetaData(Janitor.MetaData.HELP, "lineSeller: The seller of this line, if it differs from the invoice's seller, or null; writable.");

        // -- referenced documents --------------------------------------------

        DISPATCH.addBuilderMethod("setSellerOrderReferencedDocument", (self, process, args) ->
            self.janitorGetHostValue().setSellerOrderReferencedDocument(args.getRequired(0, MustangReferencedDocument.class).janitorGetHostValue()))
                .setMetaData(Janitor.MetaData.HELP, "setSellerOrderReferencedDocument(document): Sets the reference to the seller's order (order confirmation) for this line. Returns this item.");
        DISPATCH.addObjectProperty("sellerOrderReferencedDocument",
            self -> self.janitorGetHostValue().getSellerOrderReferencedDocument() == null ? null : new MustangReferencedDocument(self.janitorGetHostValue().getSellerOrderReferencedDocument()),
            (self, value) -> self.janitorGetHostValue().setSellerOrderReferencedDocument(value == null ? null : value.janitorGetHostValue()),
            MustangReferencedDocument::new)
                .setMetaData(Janitor.MetaData.HELP, "sellerOrderReferencedDocument: The reference to the seller's order (order confirmation) for this line, or null; writable.");

        DISPATCH.addBuilderMethod("setBuyerOrderReferencedDocument", (self, process, args) ->
            self.janitorGetHostValue().setBuyerOrderReferencedDocument(args.getRequired(0, MustangReferencedDocument.class).janitorGetHostValue()))
                .setMetaData(Janitor.MetaData.HELP, "setBuyerOrderReferencedDocument(document): Sets the reference to the buyer's order for this line. Returns this item.");
        DISPATCH.addObjectProperty("buyerOrderReferencedDocument",
            self -> self.janitorGetHostValue().getBuyerOrderReferencedDocument() == null ? null : new MustangReferencedDocument(self.janitorGetHostValue().getBuyerOrderReferencedDocument()),
            (self, value) -> self.janitorGetHostValue().setBuyerOrderReferencedDocument(value == null ? null : value.janitorGetHostValue()),
            MustangReferencedDocument::new)
                .setMetaData(Janitor.MetaData.HELP, "buyerOrderReferencedDocument: The reference to the buyer's order for this line, or null; writable.");

        DISPATCH.addBuilderMethod("setContractReferencedDocument", (self, process, args) ->
            self.janitorGetHostValue().setContractReferencedDocument(args.getRequired(0, MustangReferencedDocument.class).janitorGetHostValue()))
                .setMetaData(Janitor.MetaData.HELP, "setContractReferencedDocument(document): Sets the reference to the contract for this line. Returns this item.");
        DISPATCH.addObjectProperty("contractReferencedDocument",
            self -> self.janitorGetHostValue().getContractReferencedDocument() == null ? null : new MustangReferencedDocument(self.janitorGetHostValue().getContractReferencedDocument()),
            (self, value) -> self.janitorGetHostValue().setContractReferencedDocument(value == null ? null : value.janitorGetHostValue()),
            MustangReferencedDocument::new)
                .setMetaData(Janitor.MetaData.HELP, "contractReferencedDocument: The reference to the contract for this line, or null; writable.");

        DISPATCH.addBuilderMethod("setDespatchAdviceReferencedDocument", (self, process, args) ->
            self.janitorGetHostValue().setDespatchAdviceReferencedDocument(args.getRequired(0, MustangReferencedDocument.class).janitorGetHostValue()))
                .setMetaData(Janitor.MetaData.HELP, "setDespatchAdviceReferencedDocument(document): Sets the reference to the despatch advice for this line. Returns this item.");
        DISPATCH.addObjectProperty("despatchAdviceReferencedDocument",
            self -> self.janitorGetHostValue().getDespatchAdviceReferencedDocument() == null ? null : new MustangReferencedDocument(self.janitorGetHostValue().getDespatchAdviceReferencedDocument()),
            (self, value) -> self.janitorGetHostValue().setDespatchAdviceReferencedDocument(value == null ? null : value.janitorGetHostValue()),
            MustangReferencedDocument::new)
                .setMetaData(Janitor.MetaData.HELP, "despatchAdviceReferencedDocument: The reference to the despatch advice for this line, or null; writable.");

        DISPATCH.addBuilderMethod("setDeliveryNoteReferencedDocument", (self, process, args) ->
            self.janitorGetHostValue().setDeliveryNoteReferencedDocument(args.getRequired(0, MustangReferencedDocument.class).janitorGetHostValue()))
                .setMetaData(Janitor.MetaData.HELP, "setDeliveryNoteReferencedDocument(document): Sets the reference to the delivery note for this line. Returns this item.");
        DISPATCH.addObjectProperty("deliveryNoteReferencedDocument",
            self -> self.janitorGetHostValue().getDeliveryNoteReferencedDocument() == null ? null : new MustangReferencedDocument(self.janitorGetHostValue().getDeliveryNoteReferencedDocument()),
            (self, value) -> self.janitorGetHostValue().setDeliveryNoteReferencedDocument(value == null ? null : value.janitorGetHostValue()),
            MustangReferencedDocument::new)
                .setMetaData(Janitor.MetaData.HELP, "deliveryNoteReferencedDocument: The reference to the delivery note for this line, or null; writable.");

        DISPATCH.addBuilderMethod("addReferencedDocument", (self, process, args) ->
            self.janitorGetHostValue().addReferencedDocument(args.getRequired(0, MustangReferencedDocument.class).janitorGetHostValue()));
        DISPATCH.addMethod("getReferencedDocuments", (self, process, args) -> Janitor.list(wrapReferencedDocuments(self.janitorGetHostValue().getReferencedDocuments())));

        DISPATCH.addBuilderMethod("addAdditionalReference", (self, process, args) ->
            self.janitorGetHostValue().addAdditionalReference(args.getRequired(0, MustangReferencedDocument.class).janitorGetHostValue()));
        DISPATCH.addMethod("getAdditionalReferences", (self, process, args) -> Janitor.list(wrapReferencedDocuments(self.janitorGetHostValue().getAdditionalReferences())));

        // -- notes -------------------------------------------------------------

        DISPATCH.addBuilderMethod("addNote", (self, process, args) -> self.janitorGetHostValue().addNote(args.getRequiredStringValue(0)))
                .setMetaData(Janitor.MetaData.HELP, "addNote(text): Adds a free-text note to the line. Returns this item.");
        DISPATCH.addBuilderMethod("addIncludedNote", (self, process, args) ->
            self.janitorGetHostValue().addNote(args.getRequired(0, MustangIncludedNote.class).janitorGetHostValue()))
                .setMetaData(Janitor.MetaData.HELP, "addIncludedNote(note): Adds a note, possibly with a subject code, to the line. Returns this item.");
        DISPATCH.addMethod("getNotes", (self, process, args) -> {
            final List<JanitorObject> result = new ArrayList<>();
            final List<IncludedNote> notes = self.janitorGetHostValue().getNotesWithSubjectCode();
            if (notes != null) {
                for (final IncludedNote note : notes) {
                    result.add(new MustangIncludedNote(note));
                }
            }
            return Janitor.list(result);
        }).setMetaData(Janitor.MetaData.HELP, "getNotes(): Returns a list of the line's notes as note objects with content and subject code.");
        DISPATCH.addMethod("getPlainNotes", (self, process, args) -> {
            final List<JanitorObject> result = new ArrayList<>();
            final String[] notes = self.janitorGetHostValue().getNotes();
            if (notes != null) {
                for (final String note : notes) {
                    result.add(Janitor.string(note));
                }
            }
            return Janitor.list(result);
        }).setMetaData(Janitor.MetaData.HELP, "getPlainNotes(): Returns a list of the texts of the line's notes as strings.");

        // -- allowances / charges -----------------------------------------------

        DISPATCH.addBuilderMethod("addAllowance", (self, process, args) ->
            self.janitorGetHostValue().addAllowance(args.getRequired(0, MustangAllowance.class).janitorGetHostValue()))
                .setMetaData(Janitor.MetaData.HELP, "addAllowance(allowance): Adds an allowance (discount) to the line. Returns this item.");
        DISPATCH.addMethod("getAllowances", (self, process, args) -> Janitor.list(wrapAllowancesAndCharges(self.janitorGetHostValue().getItemAllowances())))
                .setMetaData(Janitor.MetaData.HELP, "getAllowances(): Returns a list of the line's allowances (discounts).");

        DISPATCH.addBuilderMethod("addCharge", (self, process, args) ->
            self.janitorGetHostValue().addCharge(args.getRequired(0, MustangCharge.class).janitorGetHostValue()))
                .setMetaData(Janitor.MetaData.HELP, "addCharge(charge): Adds a charge (surcharge) to the line. Returns this item.");
        DISPATCH.addMethod("getCharges", (self, process, args) -> Janitor.list(wrapAllowancesAndCharges(self.janitorGetHostValue().getItemCharges())))
                .setMetaData(Janitor.MetaData.HELP, "getCharges(): Returns a list of the line's charges (surcharges).");
    }

    private static List<JanitorObject> wrapReferencedDocuments(final IReferencedDocument[] documents) {
        final List<JanitorObject> result = new ArrayList<>();
        if (documents != null) {
            for (final IReferencedDocument document : documents) {
                if (document instanceof ReferencedDocument referencedDocument) {
                    result.add(new MustangReferencedDocument(referencedDocument));
                }
            }
        }
        return result;
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

    public MustangItem() {
        super(DISPATCH, new Item());
    }

    public MustangItem(final Item item) {
        super(DISPATCH, item);
    }
}
