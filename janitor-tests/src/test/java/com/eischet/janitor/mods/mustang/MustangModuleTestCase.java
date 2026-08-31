package com.eischet.janitor.mods.mustang;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JList;
import com.eischet.janitor.modules.mustang.MustangModule;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Exercises the mustang module's wrapper classes: builds a small but fairly complete invoice
 * from script code, touching most of the object graph (trade parties, legal organisation, bank
 * details, product, item, allowances/charges, referenced documents, notes) and reads the values
 * back to make sure the round trip through {@code JanitorWrapper} works correctly.
 */
public class MustangModuleTestCase extends JanitorTest {

    @Test
    void buildInvoiceAndReadItBack() throws JanitorCompilerException, JanitorRuntimeException {
        final @Language("Janitor") String script = """
            import mustang;

            seller = mustang.TradeParty();
            seller.name = "Seller GmbH";
            seller.street = "Hauptstr. 1";
            seller.zip = "12345";
            seller.location = "Berlin";
            seller.country = "DE";
            seller.vatId = "DE123456789";

            legalOrg = mustang.LegalOrganisation();
            legalOrg.tradingBusinessName = "Seller GmbH & Co KG";
            seller.legalOrganisation = legalOrg;

            bank = mustang.BankDetails();
            bank.iban = "DE1234567890";
            seller.addBankDetails(bank);

            seller.addGlobalId(mustang.SchemedID().setScheme("0088").setId("4012345000009"));

            buyer = mustang.TradeParty();
            buyer.name = "Buyer AG";
            buyer.country = "FR";

            product = mustang.Product();
            product.name = "Widget";
            product.vatPercent = 19;

            item = mustang.Item();
            item.product = product;
            item.quantity = 3;
            item.price = 10;

            charge = mustang.Charge();
            charge.reason = "Shipping";
            charge.totalAmount = 5;
            item.addCharge(charge);

            allowance = mustang.Allowance();
            allowance.reason = "Rebate";
            allowance.totalAmount = 2;
            item.addAllowance(allowance);

            invoice = mustang.Invoice();
            invoice.number = "INV-001";
            invoice.currency = "EUR";
            invoice.sender = seller;
            invoice.recipient = buyer;
            invoice.addItem(item);
            invoice.addNote("General note");
            invoice.addRegulatoryNote("Reg note");
            invoice.setBuyerOrderReferencedDocument(mustang.ReferencedDocument().setIssuerAssignedID("PO-1"));

            note = mustang.IncludedNote();
            note.content = "Coded note";
            note.subjectCode = "AAI";
            invoice.addIncludedNote(note);

            results = [];
            results.add(seller.country);
            results.add(seller.legalOrganisation.tradingBusinessName);
            results.add(seller.getBankDetails().size());
            results.add(seller.getBankDetails()[0].iban);
            results.add(seller.globalId);
            results.add(seller.globalIdScheme);
            results.add(invoice.number);
            results.add(invoice.sender.name);
            results.add(invoice.recipient.country);
            results.add(invoice.getItems().size());
            results.add(invoice.getItems()[0].quantity);
            results.add(invoice.getItems()[0].price);
            results.add(invoice.getItems()[0].product.name);
            results.add(invoice.getItems()[0].getCharges().size());
            results.add(invoice.getItems()[0].getCharges()[0].isCharge);
            results.add(invoice.getItems()[0].getCharges()[0].reason);
            results.add(invoice.getItems()[0].getAllowances().size());
            results.add(invoice.getItems()[0].getAllowances()[0].isCharge);
            results.add(invoice.getItems()[0].getAllowances()[0].reason);
            results.add(invoice.getPlainNotes().size());
            results.add(invoice.getPlainNotes()[0]);
            results.add(invoice.getNotes().size());
            results.add(invoice.getNotes()[0].content);
            results.add(invoice.getNotes()[0].subjectCode);
            results.add(invoice.getNotes()[1].content);
            results.add(invoice.getNotes()[1].subjectCode);
            results.add(invoice.buyerOrderReferencedDocument.issuerAssignedID);

            return results;
            """;

        final JanitorObject result = evaluate(script, env -> env.addModule(MustangModule.REGISTRATION), null);
        final List<String> actual = new ArrayList<>();
        for (final JanitorObject element : ((JList) result).janitorGetHostValue()) {
            actual.add(element.janitorToString());
        }

        assertEquals(List.of(
            "DE",                   // seller.country -- regression test for a copy-paste bug that read getLocation() instead
            "Seller GmbH & Co KG",  // seller.legalOrganisation.tradingBusinessName
            "1",                    // seller.getBankDetails().size()
            "DE1234567890",         // seller.getBankDetails()[0].iban
            "4012345000009",        // seller.globalId
            "0088",                 // seller.globalIdScheme
            "INV-001",               // invoice.number
            "Seller GmbH",          // invoice.sender.name
            "FR",                   // invoice.recipient.country
            "1",                    // invoice.getItems().size()
            "3",                    // invoice.getItems()[0].quantity
            "10",                   // invoice.getItems()[0].price
            "Widget",               // invoice.getItems()[0].product.name
            "1",                    // invoice.getItems()[0].getCharges().size()
            "true",                 // invoice.getItems()[0].getCharges()[0].isCharge
            "Shipping",             // invoice.getItems()[0].getCharges()[0].reason
            "1",                    // invoice.getItems()[0].getAllowances().size()
            "false",                // invoice.getItems()[0].getAllowances()[0].isCharge
            "Rebate",               // invoice.getItems()[0].getAllowances()[0].reason
            "1",                    // invoice.getPlainNotes().size()
            "General note",         // invoice.getPlainNotes()[0]
            "2",                    // invoice.getNotes().size()
            "Reg note",             // invoice.getNotes()[0].content
            "REG",                  // invoice.getNotes()[0].subjectCode
            "Coded note",           // invoice.getNotes()[1].content
            "AAI",                  // invoice.getNotes()[1].subjectCode
            "PO-1"                  // invoice.buyerOrderReferencedDocument.issuerAssignedID
        ), actual);
    }

    @Test
    void buildInvoiceAndReadItBackV2() throws JanitorCompilerException, JanitorRuntimeException {
        final @Language("Janitor") String script = """
            import mustang;

            legalOrg = mustang.LegalOrganisation({
                tradingBusinessName: "Seller GmbH & Co KG",
            });

            bank = mustang.BankDetails({
                iban: "DE1234567890",
            });

            seller = mustang.TradeParty({
                name: "Seller GmbH",
                street: "Hauptstr. 1",
                zip: "12345",
                location: "Berlin",
                country: "DE",
                vatId: "DE123456789",
                legalOrganisation: legalOrg,
            });

            seller.addBankDetails(bank);

            seller.addGlobalId(mustang.SchemedID().setScheme("0088").setId("4012345000009"));

            buyer = mustang.TradeParty();
            buyer.name = "Buyer AG";
            buyer.country = "FR";

            product = mustang.Product();
            product.name = "Widget";
            product.vatPercent = 19;

            item = mustang.Item();
            item.product = product;
            item.quantity = 3;
            item.price = 10;

            charge = mustang.Charge();
            charge.reason = "Shipping";
            charge.totalAmount = 5;
            item.addCharge(charge);

            allowance = mustang.Allowance();
            allowance.reason = "Rebate";
            allowance.totalAmount = 2;
            item.addAllowance(allowance);

            invoice = mustang.Invoice({
                number: "INV-001",
                currency: "EUR",
                sender: seller,
                recipient: buyer,
            });
            invoice.addItem(item);
            invoice.addNote("General note");
            invoice.addRegulatoryNote("Reg note");
            invoice.setBuyerOrderReferencedDocument(mustang.ReferencedDocument().setIssuerAssignedID("PO-1"));

            note = mustang.IncludedNote();
            note.content = "Coded note";
            note.subjectCode = "AAI";
            invoice.addIncludedNote(note);

            results = [
                seller.country,
                seller.legalOrganisation.tradingBusinessName,
                seller.getBankDetails().size(),
                seller.getBankDetails()[0].iban,
                seller.globalId,
                seller.globalIdScheme,
                invoice.number,
                invoice.sender.name,
                invoice.recipient.country,
                invoice.getItems().size(),
                invoice.getItems()[0].quantity,
                invoice.getItems()[0].price,
                invoice.getItems()[0].product.name,
                invoice.getItems()[0].getCharges().size(),
                invoice.getItems()[0].getCharges()[0].isCharge,
                invoice.getItems()[0].getCharges()[0].reason,
                invoice.getItems()[0].getAllowances().size(),
                invoice.getItems()[0].getAllowances()[0].isCharge,
                invoice.getItems()[0].getAllowances()[0].reason,
                invoice.getPlainNotes().size(),
                invoice.getPlainNotes()[0],
                invoice.getNotes().size(),
                invoice.getNotes()[0].content,
                invoice.getNotes()[0].subjectCode,
                invoice.getNotes()[1].content,
                invoice.getNotes()[1].subjectCode,
                invoice.buyerOrderReferencedDocument.issuerAssignedID,
            ];

            return results;
            """;

        final JanitorObject result = evaluate(script, env -> env.addModule(MustangModule.REGISTRATION), null);
        final List<String> actual = new ArrayList<>();
        for (final JanitorObject element : ((JList) result).janitorGetHostValue()) {
            actual.add(element.janitorToString());
        }

        assertEquals(List.of(
            "DE",                   // seller.country -- regression test for a copy-paste bug that read getLocation() instead
            "Seller GmbH & Co KG",  // seller.legalOrganisation.tradingBusinessName
            "1",                    // seller.getBankDetails().size()
            "DE1234567890",         // seller.getBankDetails()[0].iban
            "4012345000009",        // seller.globalId
            "0088",                 // seller.globalIdScheme
            "INV-001",               // invoice.number
            "Seller GmbH",          // invoice.sender.name
            "FR",                   // invoice.recipient.country
            "1",                    // invoice.getItems().size()
            "3",                    // invoice.getItems()[0].quantity
            "10",                   // invoice.getItems()[0].price
            "Widget",               // invoice.getItems()[0].product.name
            "1",                    // invoice.getItems()[0].getCharges().size()
            "true",                 // invoice.getItems()[0].getCharges()[0].isCharge
            "Shipping",             // invoice.getItems()[0].getCharges()[0].reason
            "1",                    // invoice.getItems()[0].getAllowances().size()
            "false",                // invoice.getItems()[0].getAllowances()[0].isCharge
            "Rebate",               // invoice.getItems()[0].getAllowances()[0].reason
            "1",                    // invoice.getPlainNotes().size()
            "General note",         // invoice.getPlainNotes()[0]
            "2",                    // invoice.getNotes().size()
            "Reg note",             // invoice.getNotes()[0].content
            "REG",                  // invoice.getNotes()[0].subjectCode
            "Coded note",           // invoice.getNotes()[1].content
            "AAI",                  // invoice.getNotes()[1].subjectCode
            "PO-1"                  // invoice.buyerOrderReferencedDocument.issuerAssignedID
        ), actual);
    }


    @Test
    void exporterBuilderMethodsChainWithoutThrowing() throws JanitorCompilerException, JanitorRuntimeException {
        final @Language("Janitor") String script = """
            import mustang;

            exporter = mustang.Exporter()
                .setProducer("Janitor Test")
                .setCreator("Janitor Test")
                .setProfile("BASIC")
                .disableAutoClose(true)
                .disableFacturX();

            return "ok";
            """;
        final JanitorObject result = evaluate(script, env -> env.addModule(MustangModule.REGISTRATION), null);
        assertEquals("ok", result.janitorToString());
    }

}
