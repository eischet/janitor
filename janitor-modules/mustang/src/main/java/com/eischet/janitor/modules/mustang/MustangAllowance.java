package com.eischet.janitor.modules.mustang;

import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import org.mustangproject.Allowance;

/**
 * An absolute or percentage-based allowance (a discount, e.g. a rebate) on item or document
 * level. See {@link MustangCharge} for the "positive" counterpart (a surcharge); an allowance
 * is technically a {@link org.mustangproject.Charge} whose {@code isCharge()} reports {@code false}.
 */
public class MustangAllowance extends JanitorWrapper<Allowance> {

    public static final WrapperDispatchTable<Allowance> DISPATCH = new WrapperDispatchTable<>(MustangAllowance::new);

    static {
        MustangCharge.addChargeBuilderMethods(DISPATCH);
        MustangCharge.addChargeProperties(DISPATCH);
    }

    public MustangAllowance() {
        super(DISPATCH, new Allowance());
    }

    public MustangAllowance(final Allowance allowance) {
        super(DISPATCH, allowance);
    }

}
