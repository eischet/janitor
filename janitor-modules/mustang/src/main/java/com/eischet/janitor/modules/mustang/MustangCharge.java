package com.eischet.janitor.modules.mustang;

import com.eischet.janitor.api.types.builtin.JDate;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.wrapped.WrapperDispatchTable;
import com.eischet.janitor.json.impl.DateTimeUtils;
import org.mustangproject.Charge;

/**
 * An absolute or percentage-based charge (a surcharge, e.g. for packaging or freight) on item
 * or document level. See {@link MustangAllowance} for the "negative" counterpart (a discount).
 */
public class MustangCharge extends JanitorWrapper<Charge> {

    public static final WrapperDispatchTable<Charge> DISPATCH = new WrapperDispatchTable<>(MustangCharge::new);

    static {
        addChargeBuilderMethods(DISPATCH);
        addChargeProperties(DISPATCH);
    }

    /**
     * Adds the "setXxx" fluent builder methods shared by {@link Charge} and its subclass
     * {@link org.mustangproject.Allowance}, so {@link MustangAllowance} does not need to
     * duplicate them.
     *
     * @param table the dispatch table of a wrapper around a {@link Charge} or subclass
     * @param <C>   the wrapped host type
     */
    static <C extends Charge> void addChargeBuilderMethods(final WrapperDispatchTable<C> table) {
        table.addBuilderMethod("setSequenceNumeric", (self, process, args) -> self.janitorGetHostValue().setSequenceNumeric(args.getRequiredIntValue(0)));
        table.addBuilderMethod("setPercent", (self, process, args) -> self.janitorGetHostValue().setPercent(args.getRequiredJNumber(0).toBigDecimal()));
        table.addBuilderMethod("setBasisAmount", (self, process, args) -> self.janitorGetHostValue().setBasisAmount(args.getRequiredJNumber(0).toBigDecimal()));
        table.addBuilderMethod("setBasisQuantity", (self, process, args) -> self.janitorGetHostValue().setBasisQuantity(args.getRequiredJNumber(0).toBigDecimal()));
        table.addBuilderMethod("setTotalAmount", (self, process, args) -> self.janitorGetHostValue().setTotalAmount(args.getRequiredJNumber(0).toBigDecimal()));
        table.addBuilderMethod("setReason", (self, process, args) -> self.janitorGetHostValue().setReason(args.getRequiredStringValue(0)));
        table.addBuilderMethod("setReasonCode", (self, process, args) -> self.janitorGetHostValue().setReasonCode(args.getRequiredStringValue(0)));
        table.addBuilderMethod("setTaxCalculatedAmount", (self, process, args) -> self.janitorGetHostValue().setTaxCalculatedAmount(args.getRequiredJNumber(0).toBigDecimal()));
        table.addBuilderMethod("setTaxExemptionReason", (self, process, args) -> self.janitorGetHostValue().setTaxExemptionReason(args.getRequiredStringValue(0)));
        table.addBuilderMethod("setTaxExemptionReasonCode", (self, process, args) -> self.janitorGetHostValue().setTaxExemptionReasonCode(args.getRequiredStringValue(0)));
        table.addBuilderMethod("setTaxBasisAmount", (self, process, args) -> self.janitorGetHostValue().setTaxBasisAmount(args.getRequiredJNumber(0).toBigDecimal()));
        table.addBuilderMethod("setTaxLineTotalBasisAmount", (self, process, args) -> self.janitorGetHostValue().setTaxLineTotalBasisAmount(args.getRequiredJNumber(0).toBigDecimal()));
        table.addBuilderMethod("setTaxAllowanceChargeBasisAmount", (self, process, args) -> self.janitorGetHostValue().setTaxAllowanceChargeBasisAmount(args.getRequiredJNumber(0).toBigDecimal()));
        table.addBuilderMethod("setTaxCategoryCode", (self, process, args) -> self.janitorGetHostValue().setTaxCategoryCode(args.getRequiredStringValue(0)));
        table.addBuilderMethod("setTaxPointDate", (self, process, args) ->
            self.janitorGetHostValue().setTaxPointDate(JDate.toLegacyJavaDate(args.getRequired(0, JDate.class).janitorGetHostValue())));
        table.addBuilderMethod("setTaxDueDateTypeCode", (self, process, args) -> self.janitorGetHostValue().setTaxDueDateTypeCode(args.getRequiredStringValue(0)));
        table.addBuilderMethod("setTaxRateApplicablePercent", (self, process, args) -> self.janitorGetHostValue().setTaxRateApplicablePercent(args.getRequiredJNumber(0).toBigDecimal()));
    }

    /**
     * Adds the properties shared by {@link Charge} and its subclass {@link org.mustangproject.Allowance}.
     *
     * @param table the dispatch table of a wrapper around a {@link Charge} or subclass
     * @param <C>   the wrapped host type
     */
    static <C extends Charge> void addChargeProperties(final WrapperDispatchTable<C> table) {
        table.addBooleanProperty("isCharge", self -> self.janitorGetHostValue().isCharge());
        table.addNullableIntegerProperty("sequenceNumeric", self -> self.janitorGetHostValue().getSequenceNumeric(), (self, value) -> self.janitorGetHostValue().setSequenceNumeric(value));
        table.addBigDecimalProperty("percent", self -> self.janitorGetHostValue().getPercent(), (self, value) -> self.janitorGetHostValue().setPercent(value));
        table.addBigDecimalProperty("basisAmount", self -> self.janitorGetHostValue().getBasisAmount(), (self, value) -> self.janitorGetHostValue().setBasisAmount(value));
        table.addBigDecimalProperty("basisQuantity", self -> self.janitorGetHostValue().getBasisQuantity(), (self, value) -> self.janitorGetHostValue().setBasisQuantity(value));
        table.addBigDecimalProperty("totalAmount", self -> {
            try {
                return self.janitorGetHostValue().getTotalAmount();
            } catch (RuntimeException e) {
                // thrown by the host when neither a totalAmount nor a percent has been set yet
                return null;
            }
        }, (self, value) -> self.janitorGetHostValue().setTotalAmount(value));
        table.addStringProperty("reason", self -> self.janitorGetHostValue().getReason(), (self, value) -> self.janitorGetHostValue().setReason(value));
        table.addStringProperty("reasonCode", self -> self.janitorGetHostValue().getReasonCode(), (self, value) -> self.janitorGetHostValue().setReasonCode(value));
        table.addBigDecimalProperty("taxCalculatedAmount", self -> self.janitorGetHostValue().getTaxCalculatedAmount(), (self, value) -> self.janitorGetHostValue().setTaxCalculatedAmount(value));
        table.addStringProperty("taxExemptionReason", self -> self.janitorGetHostValue().getTaxExemptionReason(), (self, value) -> self.janitorGetHostValue().setTaxExemptionReason(value));
        table.addStringProperty("taxExemptionReasonCode", self -> self.janitorGetHostValue().getTaxExemptionReasonCode(), (self, value) -> self.janitorGetHostValue().setTaxExemptionReasonCode(value));
        table.addBigDecimalProperty("taxBasisAmount", self -> self.janitorGetHostValue().getTaxBasisAmount(), (self, value) -> self.janitorGetHostValue().setTaxBasisAmount(value));
        table.addBigDecimalProperty("taxLineTotalBasisAmount", self -> self.janitorGetHostValue().getTaxLineTotalBasisAmount(), (self, value) -> self.janitorGetHostValue().setTaxLineTotalBasisAmount(value));
        table.addBigDecimalProperty("taxAllowanceChargeBasisAmount", self -> self.janitorGetHostValue().getTaxAllowanceChargeBasisAmount(), (self, value) -> self.janitorGetHostValue().setTaxAllowanceChargeBasisAmount(value));
        table.addStringProperty("taxCategoryCode", self -> self.janitorGetHostValue().getTaxCategoryCode(), (self, value) -> self.janitorGetHostValue().setTaxCategoryCode(value));
        table.addDateProperty("taxPointDate", self -> DateTimeUtils.convertDateToLocalDate(self.janitorGetHostValue().getTaxPointDate()),
            (self, value) -> self.janitorGetHostValue().setTaxPointDate(JDate.toLegacyJavaDate(value)));
        table.addStringProperty("taxDueDateTypeCode", self -> self.janitorGetHostValue().getTaxDueDateTypeCode(), (self, value) -> self.janitorGetHostValue().setTaxDueDateTypeCode(value));
        table.addBigDecimalProperty("taxRateApplicablePercent", self -> self.janitorGetHostValue().getTaxRateApplicablePercent(), (self, value) -> self.janitorGetHostValue().setTaxRateApplicablePercent(value));
    }

    public MustangCharge() {
        super(DISPATCH, new Charge());
    }

    public MustangCharge(final Charge charge) {
        super(DISPATCH, charge);
    }

}
