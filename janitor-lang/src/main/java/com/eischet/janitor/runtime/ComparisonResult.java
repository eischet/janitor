package com.eischet.janitor.runtime;

import com.eischet.janitor.api.types.builtin.JBool;

/**
 * Possible Results of comparison operations.
 */
public enum ComparisonResult {

    /** The left argument is less than the right argument    */ LESS,
    /** The left argument is equal to the right argument     */ EQUALS,
    /** The left argument is greater than the right argument */ GREATER;

    /**
     * Adapts a Java comparison result to a {@link ComparisonResult}.
     * @param javaComparisonScore the result of a comparison operation
     * @return the adapted result
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
        return JBool.map(this == GREATER);
    }

    /**
     * Maps this result to a boolean.
     * @return TRUE when appropriate, FALSE otherwise
     */
    public JBool isLessThan() {
        return JBool.map(this == LESS);
    }

    /**
     * Maps this result to a boolean.
     * @return TRUE when appropriate, FALSE otherwise
     */
    public JBool isEquals() {
        return JBool.map(this == EQUALS);
    }

    /**
     * Maps this result to a boolean.
     * @return TRUE when appropriate, FALSE otherwise
     */
    public JBool isLessThanOrEquals() {
        return JBool.map(this != GREATER);
    }

    /**
     * Maps this result to a boolean.
     * @return TRUE when appropriate, FALSE otherwise
     */
    public JBool isGreaterThanOrEquals() {
        return JBool.map(this != LESS);
    }

}
