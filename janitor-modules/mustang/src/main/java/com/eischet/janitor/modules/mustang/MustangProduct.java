package com.eischet.janitor.modules.mustang;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.mustangproject.Allowance;
import org.mustangproject.Charge;
import org.mustangproject.Product;

import java.util.ArrayList;
import java.util.List;

public class MustangProduct extends JanitorWrapper<Product> {
    public static WrapperDispatchTable<Product> DISPATCH = new WrapperDispatchTable<>(MustangProduct::new);

    static {
        DISPATCH.addBuilderMethod("setName", (self, process, args) -> self.janitorGetHostValue().setName(args.getRequiredStringValue(0)));
        DISPATCH.addStringProperty("name", self -> self.janitorGetHostValue().getName(), (self, value) -> self.janitorGetHostValue().setName(value));

        DISPATCH.addBuilderMethod("setDescription", (self, process, args) -> self.janitorGetHostValue().setDescription(args.getRequiredStringValue(0)));
        DISPATCH.addStringProperty("description", self -> self.janitorGetHostValue().getDescription(), (self, value) -> self.janitorGetHostValue().setDescription(value));

        DISPATCH.addBuilderMethod("setUnit", (self, process, args) -> self.janitorGetHostValue().setUnit(args.getRequiredStringValue(0)));
        DISPATCH.addStringProperty("unit", self -> self.janitorGetHostValue().getUnit(), (self, value) -> self.janitorGetHostValue().setUnit(value));

        DISPATCH.addBuilderMethod("setVatPercent", (self, process, args) -> self.janitorGetHostValue().setVATPercent(args.getRequiredJNumber(0).toBigDecimal()));
        DISPATCH.addBigDecimalProperty("vatPercent", self -> self.janitorGetHostValue().getVATPercent(), (self, value) -> self.janitorGetHostValue().setVATPercent(value));

        DISPATCH.addBuilderMethod("setSellerAssignedId", (self, process, args) -> self.janitorGetHostValue().setSellerAssignedID(args.getRequiredStringValue(0)));
        DISPATCH.addStringProperty("sellerAssignedId", self -> self.janitorGetHostValue().getSellerAssignedID(), (self, value) -> self.janitorGetHostValue().setSellerAssignedID(value));

        DISPATCH.addBuilderMethod("setBuyerAssignedId", (self, process, args) -> self.janitorGetHostValue().setBuyerAssignedID(args.getRequiredStringValue(0)));
        DISPATCH.addStringProperty("buyerAssignedId", self -> self.janitorGetHostValue().getBuyerAssignedID(), (self, value) -> self.janitorGetHostValue().setBuyerAssignedID(value));

        DISPATCH.addBuilderMethod("setTaxExemptionReason", (self, process, args) -> self.janitorGetHostValue().setTaxExemptionReason(args.getRequiredStringValue(0)));
        DISPATCH.addStringProperty("taxExemptionReason", self -> self.janitorGetHostValue().getTaxExemptionReason(), (self, value) -> self.janitorGetHostValue().setTaxExemptionReason(value));

        DISPATCH.addBuilderMethod("setTaxExemptionReasonCode", (self, process, args) -> self.janitorGetHostValue().setTaxExemptionReasonCode(args.getRequiredStringValue(0)));
        DISPATCH.addStringProperty("taxExemptionReasonCode", self -> self.janitorGetHostValue().getTaxExemptionReasonCode(), (self, value) -> self.janitorGetHostValue().setTaxExemptionReasonCode(value));

        DISPATCH.addBuilderMethod("setTaxCategoryCode", (self, process, args) -> self.janitorGetHostValue().setTaxCategoryCode(args.getRequiredStringValue(0)));
        DISPATCH.addStringProperty("taxCategoryCode", self -> self.janitorGetHostValue().getTaxCategoryCode(), (self, value) -> self.janitorGetHostValue().setTaxCategoryCode(value));

        DISPATCH.addBuilderMethod("setCountryOfOrigin", (self, process, args) -> self.janitorGetHostValue().setCountryOfOrigin(args.getRequiredStringValue(0)));
        DISPATCH.addStringProperty("countryOfOrigin", self -> self.janitorGetHostValue().getCountryOfOrigin(), (self, value) -> self.janitorGetHostValue().setCountryOfOrigin(value));

        // reverse charge (delivery outside the EU) and intra-community supply (delivery inside the EU,
        // outside the seller's country) are mutually exclusive VAT exemption flags; the host only
        // offers no-arg "trigger" setters, so we mirror that as builder methods plus read-only flags.
        DISPATCH.addBuilderMethod("setReverseCharge", (self, process, args) -> self.janitorGetHostValue().setReverseCharge());
        DISPATCH.addBooleanProperty("reverseCharge", self -> self.janitorGetHostValue().isReverseCharge());
        DISPATCH.addBuilderMethod("setIntraCommunitySupply", (self, process, args) -> self.janitorGetHostValue().setIntraCommunitySupply());
        DISPATCH.addBooleanProperty("intraCommunitySupply", self -> self.janitorGetHostValue().isIntraCommunitySupply());

        DISPATCH.addBuilderMethod("addGlobalId", (self, process, args) ->
            self.janitorGetHostValue().addGlobalID(args.getRequired(0, MustangSchemedID.class).janitorGetHostValue()));
        DISPATCH.addStringProperty("globalId", self -> self.janitorGetHostValue().getGlobalID());
        DISPATCH.addStringProperty("globalIdScheme", self -> self.janitorGetHostValue().getGlobalIDScheme());

        DISPATCH.addBuilderMethod("addCharge", (self, process, args) ->
            self.janitorGetHostValue().addCharge(args.getRequired(0, MustangCharge.class).janitorGetHostValue()));
        DISPATCH.addMethod("getCharges", (self, process, args) -> {
            final Charge[] charges = self.janitorGetHostValue().getCharges();
            final List<JanitorObject> result = new ArrayList<>();
            if (charges != null) {
                for (final Charge charge : charges) {
                    result.add(new MustangCharge(charge));
                }
            }
            return Janitor.list(result);
        });

        DISPATCH.addBuilderMethod("addAllowance", (self, process, args) ->
            self.janitorGetHostValue().addAllowance(args.getRequired(0, MustangAllowance.class).janitorGetHostValue()));
        DISPATCH.addMethod("getAllowances", (self, process, args) -> {
            final Allowance[] allowances = self.janitorGetHostValue().getAllowances();
            final List<JanitorObject> result = new ArrayList<>();
            if (allowances != null) {
                for (final Allowance allowance : allowances) {
                    result.add(new MustangAllowance(allowance));
                }
            }
            return Janitor.list(result);
        });
    }

    public MustangProduct() {
        super(DISPATCH, new Product());
    }

    public MustangProduct(final Product product) {
        super(DISPATCH, product);
    }

}
