// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.filter;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.functions.JCallable;
import com.eischet.janitor.api.types.functions.tree.JInspectableFunction;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Filter expressions written as code by {@link FilterExpressionCode}, and read back by {@link LambdaFilter}. */
public class FilterExpressionCodeTestCase extends JanitorTest {

    /** Writes the expression as code, compiles and reads it back. */
    private FilterExpression roundTrip(final String code) throws Exception {
        final List<FilterExpression> result = new ArrayList<>();
        evaluate("read(" + code + ")", g -> g.bindF("read", (JCallable) (process, arguments) -> {
            result.add(LambdaFilter.toFilter(process, (JInspectableFunction) arguments.get(0), field -> true));
            return Janitor.NULL;
        }));
        return result.getFirst();
    }

    private void assertRoundTrip(final FilterExpression expression, final String expectedCode) throws Exception {
        final String code = FilterExpressionCode.toLambda(expression, "item");
        assertEquals(expectedCode, code);
        final FilterExpression back = roundTrip(code);
        assertEquals(FilterExpressionCode.normalize(expression).toJson(), FilterExpressionCode.normalize(back).toJson(), code);
        assertEquals(code, FilterExpressionCode.toLambda(back, "item"), "the code of the result is the same");
    }

    private static FilterExpression withIgnoreCase(final FilterExpression expression, final Boolean ignoreCase) {
        expression.setIgnoreCase(ignoreCase);
        return expression;
    }

    @Test
    void comparisons() throws Exception {
        assertRoundTrip(FilterExpression.from("shortCode", FilterOperator.EQ, "FOO"), "item -> item.shortCode == \"FOO\"");
        assertRoundTrip(FilterExpression.from("shortCode", FilterOperator.NEQ, "FOO"), "item -> item.shortCode != \"FOO\"");
        assertRoundTrip(withIgnoreCase(FilterExpression.from("name", FilterOperator.EQ, "foo"), true), "item -> item.name ~= \"foo\"");
        assertRoundTrip(withIgnoreCase(FilterExpression.from("name", FilterOperator.NEQ, "foo"), true), "item -> not (item.name ~= \"foo\")");
        assertRoundTrip(FilterExpression.from("stock", FilterOperator.LT, 5), "item -> item.stock < 5");
        assertRoundTrip(FilterExpression.from("stock", FilterOperator.LTE, -5), "item -> item.stock <= -5");
        assertRoundTrip(FilterExpression.from("price", FilterOperator.GT, 2.5), "item -> item.price > 2.5");
        assertRoundTrip(FilterExpression.from("price", FilterOperator.GTE, 3.0), "item -> item.price >= 3.0");
        assertRoundTrip(new FilterExpression("active", FilterOperator.EQ).withValueBoolean(true), "item -> item.active == true");
        assertRoundTrip(FilterExpression.from("created", FilterOperator.GTE, LocalDate.of(2026, 10, 31)), "item -> item.created >= @2026-10-31");
        assertRoundTrip(FilterExpression.from("changed", FilterOperator.LT, LocalDateTime.of(2026, 10, 31, 14, 30, 5)), "item -> item.changed < @2026-10-31-14:30:05");
        assertRoundTrip(FilterExpression.from("product", FilterOperator.EQ, 42), "item -> item.product == 42");
    }

    @Test
    void textSearchesIgnoreCaseUnlessSaidOtherwise() throws Exception {
        assertRoundTrip(FilterExpression.from("name", FilterOperator.STARTSWITH, "Red"), "item -> item.name.startsWithIgnoreCase(\"Red\")");
        assertRoundTrip(withIgnoreCase(FilterExpression.from("name", FilterOperator.STARTSWITH, "Red"), false), "item -> item.name.startsWith(\"Red\")");
        assertRoundTrip(FilterExpression.from("name", FilterOperator.ENDSWITH, "x"), "item -> item.name.endsWithIgnoreCase(\"x\")");
        assertRoundTrip(withIgnoreCase(FilterExpression.from("name", FilterOperator.ENDSWITH, "x"), false), "item -> item.name.endsWith(\"x\")");
        assertRoundTrip(FilterExpression.from("name", FilterOperator.CONTAINS, "dget"), "item -> item.name.containsIgnoreCase(\"dget\")");
        assertRoundTrip(withIgnoreCase(FilterExpression.from("name", FilterOperator.CONTAINS, "dget"), false), "item -> item.name.contains(\"dget\")");
        assertRoundTrip(FilterExpression.from("name", FilterOperator.DOESNOTCONTAIN, "dget"), "item -> not item.name.containsIgnoreCase(\"dget\")");
    }

    @Test
    void nullAndEmpty() throws Exception {
        assertRoundTrip(new FilterExpression("description", FilterOperator.ISNULL), "item -> item.description == null");
        assertRoundTrip(new FilterExpression("description", FilterOperator.ISNOTNULL), "item -> item.description != null");
        assertRoundTrip(new FilterExpression("description", FilterOperator.ISEMPTY), "item -> item.description == null or item.description == \"\"");
        assertRoundTrip(new FilterExpression("description", FilterOperator.ISNOTEMPTY), "item -> item.description != null and item.description != \"\"");
    }

    @Test
    void groupsAndPaths() throws Exception {
        assertRoundTrip(FilterExpression.and(List.of(
                        FilterExpression.from("product.vendor.name", FilterOperator.EQ, "Acme"),
                        FilterExpression.or(List.of(
                                new FilterExpression("description", FilterOperator.ISEMPTY),
                                FilterExpression.from("stock", FilterOperator.GT, 0))))),
                "item -> (item.product.vendor.name == \"Acme\") and ((item.description == null or item.description == \"\") or (item.stock > 0))");
    }

    @Test
    void stringsAreEscaped() throws Exception {
        assertRoundTrip(FilterExpression.from("name", FilterOperator.EQ, "say \"hi\"\\\n\tend"), "item -> item.name == \"say \\\"hi\\\"\\\\\\n\\tend\"");
    }

    @Test
    void incompleteConditionsAreLeftOut() throws Exception {
        assertNull(FilterExpressionCode.toLambda(FilterExpression.and(List.of()), "item"));
        assertRoundTrip(FilterExpression.and(List.of(new FilterExpression("name", FilterOperator.EQ), FilterExpression.from("stock", FilterOperator.GT, 1))),
                "item -> item.stock > 1");
    }

}
