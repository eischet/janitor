package com.eischet.janitor.orm.filter;

import com.eischet.janitor.JanitorTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Regression test for FilterOperator.fromCode(): it used to silently fall back to EQ for an
 * unrecognized code instead of raising, unlike the rest of the filter-parsing path (e.g.
 * FilterExpression.fromJson()), which is inconsistent behavior for essentially the same kind of
 * "malformed input" situation, so it now raises MalformedExpression like its siblings.
 */
public class FilterOperatorTestCase extends JanitorTest {

    @Test
    public void fromCodeReturnsTheMatchingOperator() {
        assertEquals(FilterOperator.CONTAINS, FilterOperator.fromCode("contains"));
        assertEquals(FilterOperator.ISNULL, FilterOperator.fromCode("isnull"));
    }

    @Test
    public void fromCodeThrowsForAnUnknownCode() {
        assertThrows(MalformedExpression.class, () -> FilterOperator.fromCode("not-a-real-operator"));
    }

}
