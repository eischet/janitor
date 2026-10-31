// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.mustang;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.builtin.JDate;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import com.eischet.janitor.toolbox.time.DateTimeUtils;
import org.mustangproject.PaymentTerms;

/**
 * A free-text description of the payment terms, optionally with an explicit due date, e.g. a
 * Skonto term followed by a net-payment term.
 */
public class MustangPaymentTerms extends JanitorWrapper<PaymentTerms> {

    public static final WrapperDispatchTable<PaymentTerms> DISPATCH = new WrapperDispatchTable<>(MustangPaymentTerms::new);

    static {
        DISPATCH.addBuilderMethod("setDescription", (self, process, args) -> self.janitorGetHostValue().setDescription(args.getRequiredStringValue(0)))
            .setMetaData(Janitor.MetaData.HELP, "setDescription(description): Sets the free-text description of the payment terms; returns the payment terms.");
        DISPATCH.addBuilderMethod("setDueDate", (self, process, args) ->
            self.janitorGetHostValue().setDueDate(JDate.toLegacyJavaDate(args.getRequired(0, JDate.class).janitorGetHostValue())))
            .setMetaData(Janitor.MetaData.HELP, "setDueDate(date): Sets the due date of the payment; returns the payment terms.");

        DISPATCH.addStringProperty("description", self -> self.janitorGetHostValue().getDescription(), (self, value) -> self.janitorGetHostValue().setDescription(value))
            .setMetaData(Janitor.MetaData.HELP, "description: The free-text description of the payment terms; writable.");
        DISPATCH.addDateProperty("dueDate", self -> DateTimeUtils.convertDateToLocalDate(self.janitorGetHostValue().getDueDate()),
            (self, value) -> self.janitorGetHostValue().setDueDate(JDate.toLegacyJavaDate(value)))
            .setMetaData(Janitor.MetaData.HELP, "dueDate: The due date of the payment, or null; writable.");
    }

    public MustangPaymentTerms() {
        super(DISPATCH, new PaymentTerms());
    }

    public MustangPaymentTerms(final PaymentTerms paymentTerms) {
        super(DISPATCH, paymentTerms);
    }

}
