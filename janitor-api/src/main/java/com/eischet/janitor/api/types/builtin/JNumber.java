package com.eischet.janitor.api.types.builtin;

import org.jetbrains.annotations.NotNull;

import com.eischet.janitor.api.types.JConstant;
import com.eischet.janitor.toolbox.json.api.JsonExportablePrimitive;

import java.math.BigDecimal;

public interface JNumber extends JConstant, JsonExportablePrimitive, Comparable<JNumber> {

    /**
     * Return the inner/host value of this JNumber.
     * This override is here to constrain subclasses to Number subclasses, enabling
     * users to safely expect Number.
     * @return the numeric value
     */
    @Override
    @NotNull
    Number janitorGetHostValue();

    /**
     * Retrieve the double value or convert this value to a double.
     * @return the inner/host value, as a double
     */
    double toDouble();

    /**
     * Retrieve the long value or convert this value to a long.
     * @return the inner/host value, as a long
     */
    long toLong();

    BigDecimal toBigDecimal();

    @Override
    default int compareTo(@NotNull JNumber o) {
        // Comparing via double loses precision for long values beyond 2^53, so two distinct
        // JInt values could wrongly compare as equal. Compare as longs when both sides are
        // integral; mixed int/float comparisons still go through double, which is unavoidable
        // since a double can't exactly represent every long anyway.
        if (janitorGetHostValue() instanceof Long left && o.janitorGetHostValue() instanceof Long right) {
            return Long.compare(left, right);
        }
        return Double.compare(toDouble(), o.toDouble());
    }
}
