package com.eischet.janitor.runtime;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.builtin.JBool;

/**
 * Possible Results of comparison operations.
 * <p>
 * Most programming languages represent these as -1, 0, +1 in a number of places, but I find
 * code involving comparisons easier to read with explicitly defined ComparisonResult enum constants.
 * </p>
 * @see java.util.Comparator
 */
public enum ComparisonResult {

    /** The left argument is less than the right argument    */ LESS,
    /** The left argument is equal to the right argument     */ EQUALS,
    /** The left argument is greater than the right argument */ GREATER;

    /**
     * Adapts a Java comparison result to a {@link ComparisonResult}.
     * @param javaComparisonScore the result of a comparison operation
     * @return the adapted result
     * @see java.util.Comparator
     */
    public static ComparisonResult adaptJava(final int javaComparisonScore) {
        if (javaComparisonScore < 0) {
            return LESS;
        } else if (javaComparisonScore == 0) {
            return EQUALS;
        } else {
            return GREATER;
        }
    }

    /**
     * Maps this result to a boolean.
     * @return TRUE when appropriate, FALSE otherwise
     */
    public JBool isGreaterThan() {
        return Janitor.toBool(this == GREATER);
    }

    /**
     * Maps this result to a boolean.
     * @return TRUE when appropriate, FALSE otherwise
     */
    public JBool isLessThan() {
        return Janitor.toBool(this == LESS);
    }

    /**
     * Maps this result to a boolean.
     * @return TRUE when appropriate, FALSE otherwise
     */
    public JBool isEquals() {
        return Janitor.toBool(this == EQUALS);
    }

    /**
     * Maps this result to a boolean.
     * @return TRUE when appropriate, FALSE otherwise
     */
    public JBool isLessThanOrEquals() {
        return Janitor.toBool(this != GREATER);
    }

    /**
     * Maps this result to a boolean.
     * @return TRUE when appropriate, FALSE otherwise
     */
    public JBool isGreaterThanOrEquals() {
        return Janitor.toBool(this != LESS);
    }

}
