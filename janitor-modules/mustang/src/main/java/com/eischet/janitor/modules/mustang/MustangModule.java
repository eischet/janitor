// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.mustang;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.modules.JanitorModule;
import com.eischet.janitor.api.modules.JanitorModuleRegistration;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;

/** The script module "mustang", which creates electronic invoices with the Mustang library. */
public class MustangModule extends JanitorComposed<MustangModule> implements JanitorModule {

    public static final JanitorModuleRegistration REGISTRATION = new JanitorModuleRegistration("mustang", MustangModule::new);

    public static final DispatchTable<MustangModule> DISPATCH = new DispatchTable<>(MustangModule::new, false);

    static {
        DISPATCH.addConstructor("BankDetails", MustangBankDetails::new)
            .setMetaData(Janitor.MetaData.HELP, "BankDetails(): Creates new, empty bank details of a trade party.");
        DISPATCH.addConstructor("Invoice", MustangInvoice::new)
            .setMetaData(Janitor.MetaData.HELP, "Invoice(): Creates a new, empty electronic invoice (ZUGFeRD/Factur-X).");
        DISPATCH.addConstructor("Item", MustangItem::new)
            .setMetaData(Janitor.MetaData.HELP, "Item(): Creates a new, empty invoice item (line).");
        DISPATCH.addConstructor("Product", MustangProduct::new)
            .setMetaData(Janitor.MetaData.HELP, "Product(): Creates a new, empty product for an invoice item.");
        DISPATCH.addConstructor("TradeParty", MustangTradeParty::new)
            .setMetaData(Janitor.MetaData.HELP, "TradeParty(): Creates a new, empty trade party, i.e. the sender or recipient of an invoice.");
        DISPATCH.addConstructor("Exporter", MustangExporter::new)
            .setMetaData(Janitor.MetaData.HELP, "Exporter(): Creates a new exporter, which embeds invoice data into PDF files.");
        DISPATCH.addConstructor("Contact", MustangContact::new)
            .setMetaData(Janitor.MetaData.HELP, "Contact(): Creates a new, empty contact person of a trade party.");
        DISPATCH.addConstructor("LegalOrganisation", MustangLegalOrganisation::new)
            .setMetaData(Janitor.MetaData.HELP, "LegalOrganisation(): Creates a new, empty legal registration of a trade party.");
        DISPATCH.addConstructor("SchemedID", MustangSchemedID::new)
            .setMetaData(Janitor.MetaData.HELP, "SchemedID(): Creates a new, empty scheme-qualified identifier.");
        DISPATCH.addConstructor("DirectDebit", MustangDirectDebit::new)
            .setMetaData(Janitor.MetaData.HELP, "DirectDebit(): Creates new, empty SEPA direct debit details.");
        DISPATCH.addConstructor("CashDiscount", MustangCashDiscount::new)
            .setMetaData(Janitor.MetaData.HELP, "CashDiscount(): Creates a new, empty early-payment discount.");
        DISPATCH.addConstructor("PaymentTerms", MustangPaymentTerms::new)
            .setMetaData(Janitor.MetaData.HELP, "PaymentTerms(): Creates new, empty payment terms.");
        DISPATCH.addConstructor("IncludedNote", MustangIncludedNote::new)
            .setMetaData(Janitor.MetaData.HELP, "IncludedNote(): Creates a new, empty free-text note for an invoice or item.");
        DISPATCH.addConstructor("ReferencedDocument", MustangReferencedDocument::new)
            .setMetaData(Janitor.MetaData.HELP, "ReferencedDocument(): Creates a new, empty reference to another document.");
        DISPATCH.addConstructor("FileAttachment", MustangFileAttachment::new)
            .setMetaData(Janitor.MetaData.HELP, "FileAttachment(): Creates a new, empty file attachment for an invoice.");
        DISPATCH.addConstructor("Charge", MustangCharge::new)
            .setMetaData(Janitor.MetaData.HELP, "Charge(): Creates a new, empty charge (surcharge) on item or document level.");
        DISPATCH.addConstructor("Allowance", MustangAllowance::new)
            .setMetaData(Janitor.MetaData.HELP, "Allowance(): Creates a new, empty allowance (discount) on item or document level.");
    }

    public MustangModule() {
        super(DISPATCH);
    }


}
