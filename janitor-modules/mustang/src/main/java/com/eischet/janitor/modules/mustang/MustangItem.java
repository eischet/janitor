package com.eischet.janitor.modules.mustang;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JDate;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import com.eischet.janitor.json.impl.DateTimeUtils;
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

public class MustangItem extends JanitorWrapper<Item> {
    public static WrapperDispatchTable<Item> DISPATCH = new WrapperDispatchTable<>(MustangItem::new);

    static {
        DISPATCH.addBuilderMethod("setQuantity", (self, process, args) ->
            self.janitorGetHostValue().setQuantity(args.getRequiredJNumber(0).toBigDecimal()));
        DISPATCH.addBigDecimalProperty("quantity", self -> self.janitorGetHostValue().getQuantity(), (self, value) -> self.janitorGetHostValue().setQuantity(value));

        DISPATCH.addBuilderMethod("setPrice",(self, process, args) ->
            self.janitorGetHostValue().setPrice(args.getRequiredJNumber(0).toBigDecimal()));
        DISPATCH.addBigDecimalProperty("price", self -> self.janitorGetHostValue().getPrice(), (self, value) -> self.janitorGetHostValue().setPrice(value));

        DISPATCH.addBuilderMethod("setTax", (self, process, args) ->
            self.janitorGetHostValue().setTax(args.getRequiredJNumber(0).toBigDecimal()));
        DISPATCH.addBigDecimalProperty("tax", self -> self.janitorGetHostValue().getTax(), (self, value) -> self.janitorGetHostValue().setTax(value));

        DISPATCH.addBuilderMethod("setGrossPrice", (self, process, args) ->
            self.janitorGetHostValue().setGrossPrice(args.getRequiredJNumber(0).toBigDecimal()));
        DISPATCH.addBigDecimalProperty("grossPrice", self -> self.janitorGetHostValue().getGrossPrice(), (self, value) -> self.janitorGetHostValue().setGrossPrice(value));

        DISPATCH.addBuilderMethod("setLineTotalAmount", (self, process, args) ->
            self.janitorGetHostValue().setLineTotalAmount(args.getRequiredJNumber(0).toBigDecimal()));
        DISPATCH.addBigDecimalProperty("lineTotalAmount", self -> self.janitorGetHostValue().getLineTotalAmount(), (self, value) -> self.janitorGetHostValue().setLineTotalAmount(value));

        DISPATCH.addBuilderMethod("setBasisQuantity", (self, process, args) ->
            self.janitorGetHostValue().setBasisQuantity(args.getRequiredJNumber(0).toBigDecimal()));
        DISPATCH.addBigDecimalProperty("basisQuantity", self -> self.janitorGetHostValue().getBasisQuantity(), (self, value) -> self.janitorGetHostValue().setBasisQuantity(value));

        DISPATCH.addBuilderMethod("setId", (self, process, args) -> self.janitorGetHostValue().setId(args.getRequiredStringValue(0)));
        DISPATCH.addStringProperty("id", self -> self.janitorGetHostValue().getId(), (self, value) -> self.janitorGetHostValue().setId(value));

        DISPATCH.addBuilderMethod("setAccountingReference", (self, process, args) -> self.janitorGetHostValue().setAccountingReference(args.getRequiredStringValue(0)));
        DISPATCH.addStringProperty("accountingReference", self -> self.janitorGetHostValue().getAccountingReference(), (self, value) -> self.janitorGetHostValue().setAccountingReference(value));

        DISPATCH.addBuilderMethod("setParentLineId", (self, process, args) -> self.janitorGetHostValue().setParentLineID(args.getRequiredStringValue(0)));
        DISPATCH.addStringProperty("parentLineId", self -> self.janitorGetHostValue().getParentLineID(), (self, value) -> self.janitorGetHostValue().setParentLineID(value));

        DISPATCH.addBuilderMethod("setLineStatusReasonCode", (self, process, args) -> self.janitorGetHostValue().setLineStatusReasonCode(args.getRequiredStringValue(0)));
        DISPATCH.addStringProperty("lineStatusReasonCode", self -> self.janitorGetHostValue().getLineStatusReasonCode(), (self, value) -> self.janitorGetHostValue().setLineStatusReasonCode(value));

        // -- delivery period ---------------------------------------------------

        DISPATCH.addBuilderMethod("setDetailedDeliveryPeriod", (self, process, args) -> {
            final JDate fromDate = args.getRequired(0, JDate.class);
            final JDate toDate = args.getRequired(1, JDate.class);
            self.janitorGetHostValue().setDetailedDeliveryPeriod(JDate.toLegacyJavaDate(fromDate), JDate.toLegacyJavaDate(toDate));
        });
        DISPATCH.addDateProperty("detailedDeliveryPeriodFrom", self -> DateTimeUtils.convertDateToLocalDate(self.janitorGetHostValue().getDetailedDeliveryPeriodFrom()));
        DISPATCH.addDateProperty("detailedDeliveryPeriodTo", self -> DateTimeUtils.convertDateToLocalDate(self.janitorGetHostValue().getDetailedDeliveryPeriodTo()));

        // -- product / seller ----------------------------------------------------

        DISPATCH.addBuilderMethod("setProduct", (self, process, args) ->
            self.janitorGetHostValue().setProduct(args.getRequired(0, MustangProduct.class).janitorGetHostValue()));
        DISPATCH.addObjectProperty("product",
            self -> self.janitorGetHostValue().getProduct() instanceof Product product ? new MustangProduct(product) : null,
            (self, value) -> self.janitorGetHostValue().setProduct(value == null ? null : value.janitorGetHostValue()),
            MustangProduct::new);

        DISPATCH.addBuilderMethod("setLineSeller", (self, process, args) ->
            self.janitorGetHostValue().setLineSeller(args.getRequired(0, MustangTradeParty.class).janitorGetHostValue()));
        DISPATCH.addObjectProperty("lineSeller",
            self -> self.janitorGetHostValue().getLineSeller() instanceof TradeParty tradeParty ? new MustangTradeParty(tradeParty) : null,
            (self, value) -> self.janitorGetHostValue().setLineSeller(value == null ? null : value.janitorGetHostValue()),
            MustangTradeParty::new);

        // -- referenced documents --------------------------------------------

        DISPATCH.addBuilderMethod("setSellerOrderReferencedDocument", (self, process, args) ->
            self.janitorGetHostValue().setSellerOrderReferencedDocument(args.getRequired(0, MustangReferencedDocument.class).janitorGetHostValue()));
        DISPATCH.addObjectProperty("sellerOrderReferencedDocument",
            self -> self.janitorGetHostValue().getSellerOrderReferencedDocument() == null ? null : new MustangReferencedDocument(self.janitorGetHostValue().getSellerOrderReferencedDocument()),
            (self, value) -> self.janitorGetHostValue().setSellerOrderReferencedDocument(value == null ? null : value.janitorGetHostValue()),
            MustangReferencedDocument::new);

        DISPATCH.addBuilderMethod("setBuyerOrderReferencedDocument", (self, process, args) ->
            self.janitorGetHostValue().setBuyerOrderReferencedDocument(args.getRequired(0, MustangReferencedDocument.class).janitorGetHostValue()));
        DISPATCH.addObjectProperty("buyerOrderReferencedDocument",
            self -> self.janitorGetHostValue().getBuyerOrderReferencedDocument() == null ? null : new MustangReferencedDocument(self.janitorGetHostValue().getBuyerOrderReferencedDocument()),
            (self, value) -> self.janitorGetHostValue().setBuyerOrderReferencedDocument(value == null ? null : value.janitorGetHostValue()),
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

        DISPATCH.addBuilderMethod("addReferencedDocument", (self, process, args) ->
            self.janitorGetHostValue().addReferencedDocument(args.getRequired(0, MustangReferencedDocument.class).janitorGetHostValue()));
        DISPATCH.addMethod("getReferencedDocuments", (self, process, args) -> Janitor.list(wrapReferencedDocuments(self.janitorGetHostValue().getReferencedDocuments())));

        DISPATCH.addBuilderMethod("addAdditionalReference", (self, process, args) ->
            self.janitorGetHostValue().addAdditionalReference(args.getRequired(0, MustangReferencedDocument.class).janitorGetHostValue()));
        DISPATCH.addMethod("getAdditionalReferences", (self, process, args) -> Janitor.list(wrapReferencedDocuments(self.janitorGetHostValue().getAdditionalReferences())));

        // -- notes -------------------------------------------------------------

        DISPATCH.addBuilderMethod("addNote", (self, process, args) -> self.janitorGetHostValue().addNote(args.getRequiredStringValue(0)));
        DISPATCH.addBuilderMethod("addIncludedNote", (self, process, args) ->
            self.janitorGetHostValue().addNote(args.getRequired(0, MustangIncludedNote.class).janitorGetHostValue()));
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

        // -- allowances / charges -----------------------------------------------

        DISPATCH.addBuilderMethod("addAllowance", (self, process, args) ->
            self.janitorGetHostValue().addAllowance(args.getRequired(0, MustangAllowance.class).janitorGetHostValue()));
        DISPATCH.addMethod("getAllowances", (self, process, args) -> Janitor.list(wrapAllowancesAndCharges(self.janitorGetHostValue().getItemAllowances())));

        DISPATCH.addBuilderMethod("addCharge", (self, process, args) ->
            self.janitorGetHostValue().addCharge(args.getRequired(0, MustangCharge.class).janitorGetHostValue()));
        DISPATCH.addMethod("getCharges", (self, process, args) -> Janitor.list(wrapAllowancesAndCharges(self.janitorGetHostValue().getItemCharges())));
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
