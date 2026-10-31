// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.mustang;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.mustangproject.CashDiscount;

/**
 * An early-payment discount ("Skonto"): a percentage off if paid within a number of days.
 */
public class MustangCashDiscount extends JanitorWrapper<CashDiscount> {

    public static final WrapperDispatchTable<CashDiscount> DISPATCH = new WrapperDispatchTable<>(MustangCashDiscount::new);

    static {
        DISPATCH.addBuilderMethod("setPercent", (self, process, args) -> self.janitorGetHostValue().setPercent(args.getRequiredJNumber(0).toBigDecimal()))
                .setMetaData(Janitor.MetaData.HELP, "setPercent(percent): Sets the discount in percent. Returns this cash discount.");
        DISPATCH.addBuilderMethod("setDays", (self, process, args) -> self.janitorGetHostValue().setDays(args.getRequiredIntValue(0)))
                .setMetaData(Janitor.MetaData.HELP, "setDays(days): Sets the number of days within which the payment must be made to get the discount. Returns this cash discount.");
        DISPATCH.addBuilderMethod("setBasisAmount", (self, process, args) -> self.janitorGetHostValue().setBasisAmount(args.getRequiredJNumber(0).toBigDecimal()))
                .setMetaData(Janitor.MetaData.HELP, "setBasisAmount(amount): Sets the amount the discount percentage applies to. Returns this cash discount.");

        DISPATCH.addBigDecimalProperty("percent", self -> self.janitorGetHostValue().getPercent(), (self, value) -> self.janitorGetHostValue().setPercent(value))
                .setMetaData(Janitor.MetaData.HELP, "percent: The discount in percent; writable.");
        DISPATCH.addNullableIntegerProperty("days", self -> self.janitorGetHostValue().getDays(), (self, value) -> self.janitorGetHostValue().setDays(value))
                .setMetaData(Janitor.MetaData.HELP, "days: The number of days within which the payment must be made to get the discount, or null; writable.");
        DISPATCH.addBigDecimalProperty("basisAmount", self -> self.janitorGetHostValue().getBasisAmount(), (self, value) -> self.janitorGetHostValue().setBasisAmount(value))
                .setMetaData(Janitor.MetaData.HELP, "basisAmount: The amount the discount percentage applies to; writable.");
    }

    public MustangCashDiscount() {
        super(DISPATCH, new CashDiscount());
    }

    public MustangCashDiscount(final CashDiscount cashDiscount) {
        super(DISPATCH, cashDiscount);
    }

}
