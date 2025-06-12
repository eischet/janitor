package com.eischet.janitor.api.types.builtin;

import org.jetbrains.annotations.NotNull;

import com.eischet.janitor.api.types.JConstant;
import com.eischet.janitor.toolbox.json.api.JsonExportablePrimitive;

public interface JNumber extends JConstant, JsonExportablePrimitive, Comparable<JNumber> {

    /**
     * Return the inner/host value of this JNumber.
     * This override is here to constrain subclasses to Number subclasses, enabling
     * users to safely expect Number.
     * @return the numeric value
     */
    @Override
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

    @Override
    default int compareTo(@NotNull JNumber o) {
        return Double.compare(toDouble(), o.toDouble());
    }
}
