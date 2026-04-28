package com.eischet.janitor.modules.mustang;

import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.mustangproject.BankDetails;

public class MustangBankDetails extends JanitorWrapper<BankDetails> {

    public static WrapperDispatchTable<BankDetails> DISPATCH = new WrapperDispatchTable<>(MustangBankDetails::new);

    static {
        DISPATCH.addBuilderMethod("setIban", (self, process, args) -> self.janitorGetHostValue().setIBAN(args.getRequiredStringValue(0)));
        DISPATCH.addBuilderMethod("setBic", (self, process, args) -> self.janitorGetHostValue().setBIC(args.getRequiredStringValue(0)));
        DISPATCH.addBuilderMethod("setAccountName", (self, process, args) -> self.janitorGetHostValue().setAccountName(args.getRequiredStringValue(0)));
        DISPATCH.addBuilderMethod("setPaymentMeansCode", (self, process, args) -> self.janitorGetHostValue().setPaymentMeansCode(args.getRequiredStringValue(0)));
        DISPATCH.addBuilderMethod("setPaymentMeansInformation", (self, process, args) -> self.janitorGetHostValue().setPaymentMeansInformation(args.getRequiredStringValue(0)));

        DISPATCH.addStringProperty("iban", self -> self.janitorGetHostValue().getIBAN(), (self, value) -> self.janitorGetHostValue().setIBAN(value));
        DISPATCH.addStringProperty("bic", self -> self.janitorGetHostValue().getBIC(), (self, value) -> self.janitorGetHostValue().setBIC(value));
        DISPATCH.addStringProperty("accountName", self -> self.janitorGetHostValue().getAccountName(), (self, value) -> self.janitorGetHostValue().setAccountName(value));
        DISPATCH.addStringProperty("paymentMeansCode", self -> self.janitorGetHostValue().getPaymentMeansCode(), (self, value) -> self.janitorGetHostValue().setPaymentMeansCode(value));
        DISPATCH.addStringProperty("paymentMeansInformation", self -> self.janitorGetHostValue().getPaymentMeansInformation(), (self, value) -> self.janitorGetHostValue().setPaymentMeansInformation(value));
    }

    public MustangBankDetails() {
        super(DISPATCH, new BankDetails());
    }

}
