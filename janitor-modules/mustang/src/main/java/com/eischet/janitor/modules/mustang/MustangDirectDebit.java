package com.eischet.janitor.modules.mustang;

import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.mustangproject.DirectDebit;

/**
 * SEPA direct debit details: the debited IBAN and the mandate reference authorizing the debit.
 */
public class MustangDirectDebit extends JanitorWrapper<DirectDebit> {

    public static final WrapperDispatchTable<DirectDebit> DISPATCH = new WrapperDispatchTable<>(MustangDirectDebit::new);

    static {
        DISPATCH.addBuilderMethod("setIban", (self, process, args) -> self.janitorGetHostValue().setIBAN(args.getRequiredStringValue(0)));
        DISPATCH.addBuilderMethod("setMandate", (self, process, args) -> self.janitorGetHostValue().setMandate(args.getRequiredStringValue(0)));
        DISPATCH.addBuilderMethod("setPaymentMeansCode", (self, process, args) -> self.janitorGetHostValue().setPaymentMeansCode(args.getRequiredStringValue(0)));
        DISPATCH.addBuilderMethod("setPaymentMeansInformation", (self, process, args) -> self.janitorGetHostValue().setPaymentMeansInformation(args.getRequiredStringValue(0)));

        DISPATCH.addStringProperty("iban", self -> self.janitorGetHostValue().getIBAN(), (self, value) -> self.janitorGetHostValue().setIBAN(value));
        DISPATCH.addStringProperty("mandate", self -> self.janitorGetHostValue().getMandate(), (self, value) -> self.janitorGetHostValue().setMandate(value));
        DISPATCH.addStringProperty("paymentMeansCode", self -> self.janitorGetHostValue().getPaymentMeansCode(), (self, value) -> self.janitorGetHostValue().setPaymentMeansCode(value));
        DISPATCH.addStringProperty("paymentMeansInformation", self -> self.janitorGetHostValue().getPaymentMeansInformation(), (self, value) -> self.janitorGetHostValue().setPaymentMeansInformation(value));
    }

    public MustangDirectDebit() {
        super(DISPATCH, new DirectDebit());
    }

    public MustangDirectDebit(final DirectDebit directDebit) {
        super(DISPATCH, directDebit);
    }

}
