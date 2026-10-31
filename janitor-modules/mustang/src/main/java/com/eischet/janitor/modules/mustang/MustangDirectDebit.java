// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.mustang;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.mustangproject.DirectDebit;

/**
 * SEPA direct debit details: the debited IBAN and the mandate reference authorizing the debit.
 */
public class MustangDirectDebit extends JanitorWrapper<DirectDebit> {

    public static final WrapperDispatchTable<DirectDebit> DISPATCH = new WrapperDispatchTable<>(MustangDirectDebit::new);

    static {
        DISPATCH.addBuilderMethod("setIban", (self, process, args) -> self.janitorGetHostValue().setIBAN(args.getRequiredStringValue(0)))
                .setMetaData(Janitor.MetaData.HELP, "setIban(iban): Sets the IBAN of the debited account. Returns this direct debit.");
        DISPATCH.addBuilderMethod("setMandate", (self, process, args) -> self.janitorGetHostValue().setMandate(args.getRequiredStringValue(0)))
                .setMetaData(Janitor.MetaData.HELP, "setMandate(mandate): Sets the reference of the mandate that authorizes the debit. Returns this direct debit.");
        DISPATCH.addBuilderMethod("setPaymentMeansCode", (self, process, args) -> self.janitorGetHostValue().setPaymentMeansCode(args.getRequiredStringValue(0)))
                .setMetaData(Janitor.MetaData.HELP, "setPaymentMeansCode(code): Sets the UNTDID 4461 payment means code, e.g. \"59\" for a SEPA direct debit. Returns this direct debit.");
        DISPATCH.addBuilderMethod("setPaymentMeansInformation", (self, process, args) -> self.janitorGetHostValue().setPaymentMeansInformation(args.getRequiredStringValue(0)))
                .setMetaData(Janitor.MetaData.HELP, "setPaymentMeansInformation(text): Sets a free-text description of the payment means. Returns this direct debit.");

        DISPATCH.addStringProperty("iban", self -> self.janitorGetHostValue().getIBAN(), (self, value) -> self.janitorGetHostValue().setIBAN(value))
                .setMetaData(Janitor.MetaData.HELP, "iban: The IBAN of the debited account; writable.");
        DISPATCH.addStringProperty("mandate", self -> self.janitorGetHostValue().getMandate(), (self, value) -> self.janitorGetHostValue().setMandate(value))
                .setMetaData(Janitor.MetaData.HELP, "mandate: The reference of the mandate that authorizes the debit; writable.");
        DISPATCH.addStringProperty("paymentMeansCode", self -> self.janitorGetHostValue().getPaymentMeansCode(), (self, value) -> self.janitorGetHostValue().setPaymentMeansCode(value))
                .setMetaData(Janitor.MetaData.HELP, "paymentMeansCode: The UNTDID 4461 payment means code, e.g. \"59\" for a SEPA direct debit; writable.");
        DISPATCH.addStringProperty("paymentMeansInformation", self -> self.janitorGetHostValue().getPaymentMeansInformation(), (self, value) -> self.janitorGetHostValue().setPaymentMeansInformation(value))
                .setMetaData(Janitor.MetaData.HELP, "paymentMeansInformation: A free-text description of the payment means; writable.");
    }

    public MustangDirectDebit() {
        super(DISPATCH, new DirectDebit());
    }

    public MustangDirectDebit(final DirectDebit directDebit) {
        super(DISPATCH, directDebit);
    }

}
