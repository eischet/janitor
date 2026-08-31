package com.eischet.janitor.modules.mustang;

import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.mustangproject.CashDiscount;

/**
 * An early-payment discount ("Skonto"): a percentage off if paid within a number of days.
 */
public class MustangCashDiscount extends JanitorWrapper<CashDiscount> {

    public static final WrapperDispatchTable<CashDiscount> DISPATCH = new WrapperDispatchTable<>(MustangCashDiscount::new);

    static {
        DISPATCH.addBuilderMethod("setPercent", (self, process, args) -> self.janitorGetHostValue().setPercent(args.getRequiredJNumber(0).toBigDecimal()));
        DISPATCH.addBuilderMethod("setDays", (self, process, args) -> self.janitorGetHostValue().setDays(args.getRequiredIntValue(0)));
        DISPATCH.addBuilderMethod("setBasisAmount", (self, process, args) -> self.janitorGetHostValue().setBasisAmount(args.getRequiredJNumber(0).toBigDecimal()));

        DISPATCH.addBigDecimalProperty("percent", self -> self.janitorGetHostValue().getPercent(), (self, value) -> self.janitorGetHostValue().setPercent(value));
        DISPATCH.addNullableIntegerProperty("days", self -> self.janitorGetHostValue().getDays(), (self, value) -> self.janitorGetHostValue().setDays(value));
        DISPATCH.addBigDecimalProperty("basisAmount", self -> self.janitorGetHostValue().getBasisAmount(), (self, value) -> self.janitorGetHostValue().setBasisAmount(value));
    }

    public MustangCashDiscount() {
        super(DISPATCH, new CashDiscount());
    }

    public MustangCashDiscount(final CashDiscount cashDiscount) {
        super(DISPATCH, cashDiscount);
    }

}
