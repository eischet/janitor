// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.filter;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.toolbox.json.api.JsonException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FilterExpressionTestCase extends JanitorTest {

    @Test
    void completeness() throws Exception {
        assertFalse(new FilterExpression(FilterLogic.AND, List.of()).isComplete());
        assertFalse(new FilterExpression(FilterLogic.OR, List.of()).isComplete());
        assertTrue(new FilterExpression(FilterLogic.OR, List.of(FilterExpression.from("foo", FilterOperator.CONTAINS, "bar"))).isComplete());
    }


    @Test
    void expressionTests() throws JsonException {
        final String source = """
                {"logic":"and","filters":[{"field":"name","operator":"contains","valueString":"Olympia"}]}""";

        final FilterExpression fex = FilterExpression.fromJson(source);
        assertEquals(FilterLogic.AND, fex.getLogic());
        assertEquals(1, fex.getFilters().size());

        final FilterExpression inner = fex.getFilters().get(0);
        assertEquals("name", inner.getField());
        assertEquals(FilterOperator.CONTAINS, inner.getOperator());
        assertEquals("Olympia", inner.getValueString());

        // we can do this because *at the time of this writing* the order of fields is stable - and I don't see why that should change.
        // If that ever changes, special tooling for comparing JSON strings would be needed.
        assertEquals("{\"field\":\"name\",\"operator\":\"contains\",\"valueString\":\"Olympia\"}", inner.toJson());
        assertEquals(source, fex.toJson());

        final String sourceWithJunk = """
                    {"logic":"and","filters":[{"field":"name","operator":"contains","valueString":"Olympia"}, {}, {}]}""";

        {
            final FilterExpression fexWithoutJunk = FilterExpression.fromJson(sourceWithJunk).compress(); // compress removes the junk here
            assertEquals(FilterLogic.AND, fexWithoutJunk.getLogic());
            assertEquals(1, fexWithoutJunk.getFilters().size());

            final FilterExpression inner2 = fexWithoutJunk.getFilters().get(0);
            assertEquals("name", inner2.getField());
            assertEquals(FilterOperator.CONTAINS, inner2.getOperator());
            assertEquals("Olympia", inner2.getValueString());
        }

        {
            final FilterExpression fexWithJunk = FilterExpression.fromJson(sourceWithJunk); // not compressing this time
            assertEquals(FilterLogic.AND, fexWithJunk.getLogic());
            assertEquals(3, fexWithJunk.getFilters().size());

            final FilterExpression inner2 = fexWithJunk.getFilters().get(0);
            assertEquals("name", inner2.getField());
            assertEquals(FilterOperator.CONTAINS, inner2.getOperator());
            assertEquals("Olympia", inner2.getValueString());

            assertTrue(fexWithJunk.getFilters().get(1).isIncomplete());
            assertTrue(fexWithJunk.getFilters().get(2).isIncomplete());

        }

    }





    @Test
    void negatesComparisonsAndGroups() {
        final FilterExpression filter = FilterExpression.or(List.of(
                FilterExpression.from("status", FilterOperator.EQ, "o"),
                FilterExpression.and(List.of(
                        new FilterExpression("closed", FilterOperator.ISNULL),
                        FilterExpression.from("stock", FilterOperator.GT, 3L)))));
        final FilterExpression negated = filter.negated();
        assertEquals(FilterLogic.AND, negated.getLogic());
        assertEquals(FilterOperator.NEQ, negated.getFilters().get(0).getOperator());
        final FilterExpression inner = negated.getFilters().get(1);
        assertEquals(FilterLogic.OR, inner.getLogic());
        assertEquals(FilterOperator.ISNOTNULL, inner.getFilters().get(0).getOperator());
        assertEquals(FilterOperator.LTE, inner.getFilters().get(1).getOperator());
        assertEquals(3L, inner.getFilters().get(1).getValueLong());
        assertEquals(FilterOperator.EQ, filter.getFilters().get(0).getOperator(), "the original stays as it was");
        assertThrows(MalformedExpression.class, () -> FilterExpression.from("name", FilterOperator.STARTSWITH, "A").negated());
    }

    @Test
    void knowsWhatABooleanConditionTestsFor() {
        assertTrue(FilterExpression.from("open", FilterOperator.EQ, "").withValueBoolean(true).testsForTrue());
        assertTrue(new FilterExpression("open", FilterOperator.NEQ).withValueBoolean(false).testsForTrue());
        assertFalse(new FilterExpression("open", FilterOperator.EQ).withValueBoolean(false).testsForTrue());
        assertThrows(MalformedExpression.class, () -> FilterExpression.from("open", FilterOperator.EQ, "x").testsForTrue());
    }

    @Test
    void rewritesShorthandsAliasesAndDates() {
        final FilterRewrite open = FilterRewrite.flag(FilterExpression.from("status", FilterOperator.EQ, "o"));
        assertEquals(FilterOperator.EQ, open.rewrite(new FilterExpression("open", FilterOperator.EQ).withValueBoolean(true)).getOperator());
        assertEquals(FilterOperator.NEQ, open.rewrite(new FilterExpression("open", FilterOperator.EQ).withValueBoolean(false)).getOperator());

        final FilterExpression section = FilterRewrite.alias("department.section").rewrite(FilterExpression.from("section", FilterOperator.EQ, 7L));
        assertEquals("department.section", section.getField());
        assertEquals(7L, section.getValueLong());

        final FilterRewrite date = FilterRewrite.dateOf("createdDate");
        final LocalDate day = LocalDate.of(2026, 11, 9);
        final FilterExpression sameDay = date.rewrite(FilterExpression.from("createdDate@date", FilterOperator.EQ, day));
        assertEquals(FilterLogic.AND, sameDay.getLogic());
        assertEquals(day.atStartOfDay(), sameDay.getFilters().get(0).getValueDateTime());
        assertEquals(FilterOperator.GTE, sameDay.getFilters().get(0).getOperator());
        assertEquals(day.plusDays(1).atStartOfDay(), sameDay.getFilters().get(1).getValueDateTime());
        assertEquals(FilterOperator.LT, sameDay.getFilters().get(1).getOperator());
        final FilterExpression after = date.rewrite(FilterExpression.from("createdDate@date", FilterOperator.GT, day));
        assertEquals(FilterOperator.GTE, after.getOperator());
        assertEquals(day.plusDays(1).atStartOfDay(), after.getValueDateTime());
        assertEquals(FilterOperator.ISNULL, date.rewrite(new FilterExpression("createdDate@date", FilterOperator.ISEMPTY)).getOperator());
        assertThrows(MalformedExpression.class, () -> date.rewrite(FilterExpression.from("createdDate@date", FilterOperator.CONTAINS, "2026")));
    }

}
