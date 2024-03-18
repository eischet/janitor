package com.eischet.janitor.runtime;

import com.eischet.janitor.api.types.JBool;

public enum ComparisonResult {

    LESS, EQUALS, GREATER;

    public static ComparisonResult adaptJava(final int javaComparisonScore) {
        if (javaComparisonScore < 0) {
            return LESS;
        } else if (javaComparisonScore == 0) {
            return EQUALS;
        } else {
            return GREATER;
        }
    }

    public JBool isGreaterThan() {
        return JBool.map(this == GREATER);
    }

    public JBool isLessThan() {
        return JBool.map(this == LESS);
    }

    public JBool isEquals() {
        return JBool.map(this == EQUALS);
    }

    public JBool isLessThanOrEquals() {
        return JBool.map(this != GREATER);
    }

    public JBool isGreaterThanOrEquals() {
        return JBool.map(this != LESS);
    }

}
