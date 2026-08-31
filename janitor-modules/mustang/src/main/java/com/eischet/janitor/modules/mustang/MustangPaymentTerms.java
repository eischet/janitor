package com.eischet.janitor.modules.mustang;

import com.eischet.janitor.api.types.builtin.JDate;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import com.eischet.janitor.json.impl.DateTimeUtils;
import org.mustangproject.PaymentTerms;

/**
 * A free-text description of the payment terms, optionally with an explicit due date, e.g. a
 * Skonto term followed by a net-payment term.
 */
public class MustangPaymentTerms extends JanitorWrapper<PaymentTerms> {

    public static final WrapperDispatchTable<PaymentTerms> DISPATCH = new WrapperDispatchTable<>(MustangPaymentTerms::new);

    static {
        DISPATCH.addBuilderMethod("setDescription", (self, process, args) -> self.janitorGetHostValue().setDescription(args.getRequiredStringValue(0)));
        DISPATCH.addBuilderMethod("setDueDate", (self, process, args) ->
            self.janitorGetHostValue().setDueDate(JDate.toLegacyJavaDate(args.getRequired(0, JDate.class).janitorGetHostValue())));

        DISPATCH.addStringProperty("description", self -> self.janitorGetHostValue().getDescription(), (self, value) -> self.janitorGetHostValue().setDescription(value));
        DISPATCH.addDateProperty("dueDate", self -> DateTimeUtils.convertDateToLocalDate(self.janitorGetHostValue().getDueDate()),
            (self, value) -> self.janitorGetHostValue().setDueDate(JDate.toLegacyJavaDate(value)));
    }

    public MustangPaymentTerms() {
        super(DISPATCH, new PaymentTerms());
    }

    public MustangPaymentTerms(final PaymentTerms paymentTerms) {
        super(DISPATCH, paymentTerms);
    }

}
