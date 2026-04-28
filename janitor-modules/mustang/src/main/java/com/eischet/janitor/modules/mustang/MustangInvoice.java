package com.eischet.janitor.modules.mustang;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.builtin.JDate;
import com.eischet.janitor.api.types.builtin.JDateTime;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import com.eischet.janitor.json.impl.DateTimeUtils;
import com.eischet.janitor.runtime.DateTimeUtilities;
import org.mustangproject.Invoice;

public class MustangInvoice extends JanitorWrapper<Invoice> {
    public static WrapperDispatchTable<Invoice> DISPATCH = new WrapperDispatchTable<>(MustangInvoice::new);

    static {
        DISPATCH.addBuilderMethod("setNumber", (self, process, args) ->
            self.janitorGetHostValue().setNumber(args.require(1).getRequiredStringValue(0)));
        DISPATCH.addStringProperty("number", self -> self.janitorGetHostValue().getNumber(),
            (self, value) -> self.janitorGetHostValue().setNumber(value));
        DISPATCH.addBuilderMethod("setCurrency", (self, process, args) ->
            self.janitorGetHostValue().setCurrency(args.require(1).getRequiredStringValue(0)));
        DISPATCH.addStringProperty("currency", self -> self.janitorGetHostValue().getCurrency(),
            (self, value) -> self.janitorGetHostValue().setCurrency(value));
        DISPATCH.addBuilderMethod("setSender", (self, process, args) -> {
            self.janitorGetHostValue().setSender(args.require(1).getRequired(0, MustangTradeParty.class).janitorGetHostValue());
        });
        DISPATCH.addObjectProperty("sender", self -> new MustangTradeParty(self.janitorGetHostValue().getSender()),
            (self, value) -> self.janitorGetHostValue().setSender(value == null ? null : value.janitorGetHostValue()), MustangTradeParty::new);
        DISPATCH.addBuilderMethod("setIssueDate", (self, process, args) -> {
            self.janitorGetHostValue().setIssueDate(JDate.toLegacyJavaDate(args.require(1).getRequired(0, JDate.class).janitorGetHostValue()));
        });
        DISPATCH.addDateProperty("issueDate", self -> DateTimeUtils.convertDateToLocalDate(self.janitorGetHostValue().getIssueDate()),
            (self, value) -> self.janitorGetHostValue().setIssueDate(JDate.toLegacyJavaDate(value)));
        DISPATCH.addBuilderMethod("setDueDate", (self, process, args) -> {
            self.janitorGetHostValue().setDueDate(JDate.toLegacyJavaDate(args.require(1).getRequired(0, JDate.class).janitorGetHostValue()));
        });
        DISPATCH.addDateProperty("dueDate", self -> DateTimeUtils.convertDateToLocalDate(self.janitorGetHostValue().getDueDate()),
            (self, value) -> self.janitorGetHostValue().setDueDate(JDate.toLegacyJavaDate(value)));
        DISPATCH.addBuilderMethod("setRecipient", (self, process, args) -> {
            self.janitorGetHostValue().setRecipient(args.require(1).getRequired(0, MustangTradeParty.class).janitorGetHostValue());
        });
        DISPATCH.addObjectProperty("recipient", self -> new MustangTradeParty(self.janitorGetHostValue().getRecipient()),
            (self, value) -> self.janitorGetHostValue().setRecipient(value == null ? null : value.janitorGetHostValue()), MustangTradeParty::new);
        DISPATCH.addBuilderMethod("setDetailedDeliveryPeriod", (self, process, args) -> {
            final JDate fromDate = args.getRequired(0, JDate.class);
            final JDate toDate = args.getRequired(1, JDate.class);
            self.janitorGetHostValue().setDetailedDeliveryPeriod(JDate.toLegacyJavaDate(fromDate), JDate.toLegacyJavaDate(toDate));
        });
        DISPATCH.addBuilderMethod("setDetailedDeliveryPeriodFrom", (self, process, args) -> {
            final JDate fromDate = args.getRequired(0, JDate.class);
            self.janitorGetHostValue().setDetailedDeliveryPeriod(JDate.toLegacyJavaDate(fromDate), null);
        });
        DISPATCH.addBuilderMethod("setDetailedDeliveryPeriodTo", (self, process, args) -> {
            final JDate toDate = args.getRequired(0, JDate.class);
            self.janitorGetHostValue().setDetailedDeliveryPeriodTo(JDate.toLegacyJavaDate(toDate));
        });
        DISPATCH.addDateProperty("detailedDeliveryPeriodFrom", self -> DateTimeUtils.convertDateToLocalDate(self.janitorGetHostValue().getDetailedDeliveryPeriodFrom()),
            (self, date) -> self.janitorGetHostValue().setDetailedDeliveryPeriodFrom(JDate.toLegacyJavaDate(date)));
        DISPATCH.addDateProperty("detailedDeliveryPeriodTo", self -> DateTimeUtils.convertDateToLocalDate(self.janitorGetHostValue().getDetailedDeliveryPeriodTo()),
            (self, date) -> self.janitorGetHostValue().setDetailedDeliveryPeriodTo(JDate.toLegacyJavaDate(date)));

        DISPATCH.addBuilderMethod("addItem", (self, process, args) -> {
            final MustangItem item = args.getRequired(0, MustangItem.class);
            self.janitorGetHostValue().addItem(item.janitorGetHostValue());
        });

    }

    public MustangInvoice() {
        super(DISPATCH, new Invoice());
    }
}
