// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

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

/** Wrapper for the Mustang product of an invoice item. */
public class MustangProduct extends JanitorWrapper<Product> {
    public static WrapperDispatchTable<Product> DISPATCH = new WrapperDispatchTable<>(MustangProduct::new);

    static {
        DISPATCH.addBuilderMethod("setName", (self, process, args) -> self.janitorGetHostValue().setName(args.getRequiredStringValue(0)))
            .setMetaData(Janitor.MetaData.HELP, "setName(name): Sets the name of the product; returns the product.");
        DISPATCH.addStringProperty("name", self -> self.janitorGetHostValue().getName(), (self, value) -> self.janitorGetHostValue().setName(value))
            .setMetaData(Janitor.MetaData.HELP, "name: The name of the product; writable.");

        DISPATCH.addBuilderMethod("setDescription", (self, process, args) -> self.janitorGetHostValue().setDescription(args.getRequiredStringValue(0)))
            .setMetaData(Janitor.MetaData.HELP, "setDescription(description): Sets the description of the product; returns the product.");
        DISPATCH.addStringProperty("description", self -> self.janitorGetHostValue().getDescription(), (self, value) -> self.janitorGetHostValue().setDescription(value))
            .setMetaData(Janitor.MetaData.HELP, "description: The description of the product; writable.");

        DISPATCH.addBuilderMethod("setUnit", (self, process, args) -> self.janitorGetHostValue().setUnit(args.getRequiredStringValue(0)))
            .setMetaData(Janitor.MetaData.HELP, "setUnit(unit): Sets the unit code of the product's quantity; returns the product.");
        DISPATCH.addStringProperty("unit", self -> self.janitorGetHostValue().getUnit(), (self, value) -> self.janitorGetHostValue().setUnit(value))
            .setMetaData(Janitor.MetaData.HELP, "unit: The unit code of the product's quantity; writable.");

        DISPATCH.addBuilderMethod("setVatPercent", (self, process, args) -> self.janitorGetHostValue().setVATPercent(args.getRequiredJNumber(0).toBigDecimal()))
            .setMetaData(Janitor.MetaData.HELP, "setVatPercent(percent): Sets the VAT rate of the product in percent; returns the product.");
        DISPATCH.addBigDecimalProperty("vatPercent", self -> self.janitorGetHostValue().getVATPercent(), (self, value) -> self.janitorGetHostValue().setVATPercent(value))
            .setMetaData(Janitor.MetaData.HELP, "vatPercent: The VAT rate of the product in percent; writable.");

        DISPATCH.addBuilderMethod("setSellerAssignedId", (self, process, args) -> self.janitorGetHostValue().setSellerAssignedID(args.getRequiredStringValue(0)))
            .setMetaData(Janitor.MetaData.HELP, "setSellerAssignedId(id): Sets the seller's ID for the product; returns the product.");
        DISPATCH.addStringProperty("sellerAssignedId", self -> self.janitorGetHostValue().getSellerAssignedID(), (self, value) -> self.janitorGetHostValue().setSellerAssignedID(value))
            .setMetaData(Janitor.MetaData.HELP, "sellerAssignedId: The seller's ID for the product; writable.");

        DISPATCH.addBuilderMethod("setBuyerAssignedId", (self, process, args) -> self.janitorGetHostValue().setBuyerAssignedID(args.getRequiredStringValue(0)))
            .setMetaData(Janitor.MetaData.HELP, "setBuyerAssignedId(id): Sets the buyer's ID for the product; returns the product.");
        DISPATCH.addStringProperty("buyerAssignedId", self -> self.janitorGetHostValue().getBuyerAssignedID(), (self, value) -> self.janitorGetHostValue().setBuyerAssignedID(value))
            .setMetaData(Janitor.MetaData.HELP, "buyerAssignedId: The buyer's ID for the product; writable.");

        DISPATCH.addBuilderMethod("setTaxExemptionReason", (self, process, args) -> self.janitorGetHostValue().setTaxExemptionReason(args.getRequiredStringValue(0)))
            .setMetaData(Janitor.MetaData.HELP, "setTaxExemptionReason(reason): Sets the free-text reason for a VAT exemption; returns the product.");
        DISPATCH.addStringProperty("taxExemptionReason", self -> self.janitorGetHostValue().getTaxExemptionReason(), (self, value) -> self.janitorGetHostValue().setTaxExemptionReason(value))
            .setMetaData(Janitor.MetaData.HELP, "taxExemptionReason: The free-text reason for a VAT exemption; writable.");

        DISPATCH.addBuilderMethod("setTaxExemptionReasonCode", (self, process, args) -> self.janitorGetHostValue().setTaxExemptionReasonCode(args.getRequiredStringValue(0)))
            .setMetaData(Janitor.MetaData.HELP, "setTaxExemptionReasonCode(code): Sets the code of the reason for a VAT exemption; returns the product.");
        DISPATCH.addStringProperty("taxExemptionReasonCode", self -> self.janitorGetHostValue().getTaxExemptionReasonCode(), (self, value) -> self.janitorGetHostValue().setTaxExemptionReasonCode(value))
            .setMetaData(Janitor.MetaData.HELP, "taxExemptionReasonCode: The code of the reason for a VAT exemption; writable.");

        DISPATCH.addBuilderMethod("setTaxCategoryCode", (self, process, args) -> self.janitorGetHostValue().setTaxCategoryCode(args.getRequiredStringValue(0)))
            .setMetaData(Janitor.MetaData.HELP, "setTaxCategoryCode(code): Sets the VAT category code of the product; returns the product.");
        DISPATCH.addStringProperty("taxCategoryCode", self -> self.janitorGetHostValue().getTaxCategoryCode(), (self, value) -> self.janitorGetHostValue().setTaxCategoryCode(value))
            .setMetaData(Janitor.MetaData.HELP, "taxCategoryCode: The VAT category code of the product; writable.");

        DISPATCH.addBuilderMethod("setCountryOfOrigin", (self, process, args) -> self.janitorGetHostValue().setCountryOfOrigin(args.getRequiredStringValue(0)))
            .setMetaData(Janitor.MetaData.HELP, "setCountryOfOrigin(country): Sets the country of origin of the product; returns the product.");
        DISPATCH.addStringProperty("countryOfOrigin", self -> self.janitorGetHostValue().getCountryOfOrigin(), (self, value) -> self.janitorGetHostValue().setCountryOfOrigin(value))
            .setMetaData(Janitor.MetaData.HELP, "countryOfOrigin: The country of origin of the product; writable.");

        // reverse charge (delivery outside the EU) and intra-community supply (delivery inside the EU,
        // outside the seller's country) are mutually exclusive VAT exemption flags; the host only
        // offers no-arg "trigger" setters, so we mirror that as builder methods plus read-only flags.
        DISPATCH.addBuilderMethod("setReverseCharge", (self, process, args) -> self.janitorGetHostValue().setReverseCharge())
            .setMetaData(Janitor.MetaData.HELP, "setReverseCharge(): Marks the product as reverse charge (VAT exemption for deliveries outside the EU); returns the product.");
        DISPATCH.addBooleanProperty("reverseCharge", self -> self.janitorGetHostValue().isReverseCharge())
            .setMetaData(Janitor.MetaData.HELP, "reverseCharge: True if the product is marked as reverse charge; read-only, see setReverseCharge().");
        DISPATCH.addBuilderMethod("setIntraCommunitySupply", (self, process, args) -> self.janitorGetHostValue().setIntraCommunitySupply())
            .setMetaData(Janitor.MetaData.HELP, "setIntraCommunitySupply(): Marks the product as intra-community supply (VAT exemption for deliveries inside the EU, outside the seller's country); returns the product.");
        DISPATCH.addBooleanProperty("intraCommunitySupply", self -> self.janitorGetHostValue().isIntraCommunitySupply())
            .setMetaData(Janitor.MetaData.HELP, "intraCommunitySupply: True if the product is marked as intra-community supply; read-only, see setIntraCommunitySupply().");

        DISPATCH.addBuilderMethod("addGlobalId", (self, process, args) ->
            self.janitorGetHostValue().addGlobalID(args.getRequired(0, MustangSchemedID.class).janitorGetHostValue()))
            .setMetaData(Janitor.MetaData.HELP, "addGlobalId(schemedID): Adds a global ID (a SchemedID, e.g. a GTIN) to the product; returns the product.");
        DISPATCH.addStringProperty("globalId", self -> self.janitorGetHostValue().getGlobalID())
            .setMetaData(Janitor.MetaData.HELP, "globalId: The global ID of the product, or null; read-only, see addGlobalId().");
        DISPATCH.addStringProperty("globalIdScheme", self -> self.janitorGetHostValue().getGlobalIDScheme())
            .setMetaData(Janitor.MetaData.HELP, "globalIdScheme: The scheme of the product's global ID, or null; read-only.");

        DISPATCH.addBuilderMethod("addCharge", (self, process, args) ->
            self.janitorGetHostValue().addCharge(args.getRequired(0, MustangCharge.class).janitorGetHostValue()))
            .setMetaData(Janitor.MetaData.HELP, "addCharge(charge): Adds a Charge (surcharge) to the product; returns the product.");
        DISPATCH.addMethod("getCharges", (self, process, args) -> {
            final Charge[] charges = self.janitorGetHostValue().getCharges();
            final List<JanitorObject> result = new ArrayList<>();
            if (charges != null) {
                for (final Charge charge : charges) {
                    result.add(new MustangCharge(charge));
                }
            }
            return Janitor.list(result);
        }).setMetaData(Janitor.MetaData.HELP, "getCharges(): Returns the charges (surcharges) of the product as a list, empty if there are none.");

        DISPATCH.addBuilderMethod("addAllowance", (self, process, args) ->
            self.janitorGetHostValue().addAllowance(args.getRequired(0, MustangAllowance.class).janitorGetHostValue()))
            .setMetaData(Janitor.MetaData.HELP, "addAllowance(allowance): Adds an Allowance (discount) to the product; returns the product.");
        DISPATCH.addMethod("getAllowances", (self, process, args) -> {
            final Allowance[] allowances = self.janitorGetHostValue().getAllowances();
            final List<JanitorObject> result = new ArrayList<>();
            if (allowances != null) {
                for (final Allowance allowance : allowances) {
                    result.add(new MustangAllowance(allowance));
                }
            }
            return Janitor.list(result);
        }).setMetaData(Janitor.MetaData.HELP, "getAllowances(): Returns the allowances (discounts) of the product as a list, empty if there are none.");
    }

    public MustangProduct() {
        super(DISPATCH, new Product());
    }

    public MustangProduct(final Product product) {
        super(DISPATCH, product);
    }

}
