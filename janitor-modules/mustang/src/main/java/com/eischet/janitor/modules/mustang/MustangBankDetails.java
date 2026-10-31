// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.mustang;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.mustangproject.BankDetails;

/** Wrapper for the Mustang bank details of a trade party. */
public class MustangBankDetails extends JanitorWrapper<BankDetails> {

    public static WrapperDispatchTable<BankDetails> DISPATCH = new WrapperDispatchTable<>(MustangBankDetails::new);

    static {
        DISPATCH.addBuilderMethod("setIban", (self, process, args) -> self.janitorGetHostValue().setIBAN(args.getRequiredStringValue(0)))
                .setMetaData(Janitor.MetaData.HELP, "setIban(iban): Sets the IBAN of the account. Returns these bank details.");
        DISPATCH.addBuilderMethod("setBic", (self, process, args) -> self.janitorGetHostValue().setBIC(args.getRequiredStringValue(0)))
                .setMetaData(Janitor.MetaData.HELP, "setBic(bic): Sets the BIC of the bank. Returns these bank details.");
        DISPATCH.addBuilderMethod("setAccountName", (self, process, args) -> self.janitorGetHostValue().setAccountName(args.getRequiredStringValue(0)))
                .setMetaData(Janitor.MetaData.HELP, "setAccountName(name): Sets the name of the account holder. Returns these bank details.");
        DISPATCH.addBuilderMethod("setPaymentMeansCode", (self, process, args) -> self.janitorGetHostValue().setPaymentMeansCode(args.getRequiredStringValue(0)))
                .setMetaData(Janitor.MetaData.HELP, "setPaymentMeansCode(code): Sets the UNTDID 4461 payment means code, e.g. \"58\" for a SEPA credit transfer. Returns these bank details.");
        DISPATCH.addBuilderMethod("setPaymentMeansInformation", (self, process, args) -> self.janitorGetHostValue().setPaymentMeansInformation(args.getRequiredStringValue(0)))
                .setMetaData(Janitor.MetaData.HELP, "setPaymentMeansInformation(text): Sets a free-text description of the payment means. Returns these bank details.");

        DISPATCH.addStringProperty("iban", self -> self.janitorGetHostValue().getIBAN(), (self, value) -> self.janitorGetHostValue().setIBAN(value))
                .setMetaData(Janitor.MetaData.HELP, "iban: The IBAN of the account; writable.");
        DISPATCH.addStringProperty("bic", self -> self.janitorGetHostValue().getBIC(), (self, value) -> self.janitorGetHostValue().setBIC(value))
                .setMetaData(Janitor.MetaData.HELP, "bic: The BIC of the bank; writable.");
        DISPATCH.addStringProperty("accountName", self -> self.janitorGetHostValue().getAccountName(), (self, value) -> self.janitorGetHostValue().setAccountName(value))
                .setMetaData(Janitor.MetaData.HELP, "accountName: The name of the account holder; writable.");
        DISPATCH.addStringProperty("paymentMeansCode", self -> self.janitorGetHostValue().getPaymentMeansCode(), (self, value) -> self.janitorGetHostValue().setPaymentMeansCode(value))
                .setMetaData(Janitor.MetaData.HELP, "paymentMeansCode: The UNTDID 4461 payment means code, e.g. \"58\" for a SEPA credit transfer; writable.");
        DISPATCH.addStringProperty("paymentMeansInformation", self -> self.janitorGetHostValue().getPaymentMeansInformation(), (self, value) -> self.janitorGetHostValue().setPaymentMeansInformation(value))
                .setMetaData(Janitor.MetaData.HELP, "paymentMeansInformation: A free-text description of the payment means; writable.");
    }

    public MustangBankDetails() {
        super(DISPATCH, new BankDetails());
    }

    public MustangBankDetails(final BankDetails bankDetails) {
        super(DISPATCH, bankDetails);
    }

}
