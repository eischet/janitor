// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.features;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Numeric properties take numbers the way script authors write them: a whole number may come with a decimal point, and every kind
 * of property, nullable or not, follows the same rules. What cannot be stored without changing the value is refused, never
 * silently cut off.
 */
public class NumericPropertyConversionTestCase extends JanitorTest {

    private static final List<String> WHOLE_NUMBER_PROPERTIES = List.of("integer", "nullableInteger", "seconds", "longValue", "nullableLong");

    private static class Thing extends JanitorComposed<Thing> {
        public static final DispatchTable<Thing> DISPATCH = new DispatchTable<>(Thing::new);

        static {
            DISPATCH.addIntegerProperty("integer", self -> self.integer, (self, value) -> self.integer = value);
            DISPATCH.addNullableIntegerProperty("nullableInteger", self -> self.nullableInteger, (self, value) -> self.nullableInteger = value);
            DISPATCH.addSecondsProperty("seconds", self -> self.seconds, (self, value) -> self.seconds = value);
            DISPATCH.addLongProperty("longValue", self -> self.longValue, (self, value) -> self.longValue = value);
            DISPATCH.addNullableLongProperty("nullableLong", self -> self.nullableLong, (self, value) -> self.nullableLong = value);
            DISPATCH.addBigDecimalProperty("decimal", self -> self.decimal, (self, value) -> self.decimal = value);
        }

        private int integer = -1;
        private Integer nullableInteger = -1;
        private int seconds = -1;
        private long longValue = -1;
        private Long nullableLong = -1L;
        private BigDecimal decimal = new BigDecimal("-1");

        Thing() {
            super(DISPATCH);
        }

        long get(final String name) {
            return switch (name) {
                case "integer" -> integer;
                case "nullableInteger" -> nullableInteger;
                case "seconds" -> seconds;
                case "longValue" -> longValue;
                default -> nullableLong;
            };
        }
    }

    private Thing assign(final String property, final String value) throws Exception {
        final Thing thing = new Thing();
        evaluate("thing." + property + " = " + value + ";", g -> g.bind("thing", thing));
        return thing;
    }

    @Test
    void wholeNumbersAreAssigned() throws Exception {
        for (final String property : WHOLE_NUMBER_PROPERTIES) {
            assertEquals(5, assign(property, "5").get(property), property);
        }
    }

    @Test
    void wholeNumbersWithDecimalPointAreAssigned() throws Exception {
        for (final String property : WHOLE_NUMBER_PROPERTIES) {
            assertEquals(5, assign(property, "5.0").get(property), property);
            assertEquals(-12, assign(property, "-12.0").get(property), property);
        }
    }

    @Test
    void fractionsAreRefusedNotCutOff() {
        for (final String property : WHOLE_NUMBER_PROPERTIES) {
            assertThrows(Exception.class, () -> assign(property, "5.7"), property);
            assertThrows(Exception.class, () -> assign(property, "-5.7"), property);
        }
    }

    @Test
    void numbersTooLargeForAnIntegerAreRefusedNotWrapped() {
        for (final String property : List.of("integer", "nullableInteger", "seconds")) {
            assertThrows(Exception.class, () -> assign(property, "3000000000"), property);
            assertThrows(Exception.class, () -> assign(property, "-3000000000.0"), property);
        }
    }

    @Test
    void largeNumbersFitIntoLongProperties() throws Exception {
        for (final String property : List.of("longValue", "nullableLong")) {
            assertEquals(3000000000L, assign(property, "3000000000").get(property), property);
            assertEquals(9007199254740993L, assign(property, "9007199254740993").get(property), property);
        }
    }

    @Test
    void decimalsKeepTheValueAsWritten() throws Exception {
        assertEquals(new BigDecimal("5.7"), assign("decimal", "5.7").decimal);
        assertEquals(new BigDecimal("5"), assign("decimal", "5").decimal);
        assertEquals(new BigDecimal("9007199254740993"), assign("decimal", "9007199254740993").decimal);
    }

    @Test
    void otherTypesAreStillRefused() {
        for (final String property : WHOLE_NUMBER_PROPERTIES) {
            assertThrows(Exception.class, () -> assign(property, "'5'"), property);
            assertThrows(Exception.class, () -> assign(property, "true"), property);
        }
    }

    @Test
    void nullOnlyGoesIntoNullableProperties() throws Exception {
        assertEquals(null, assign("nullableInteger", "null").nullableInteger);
        assertEquals(null, assign("nullableLong", "null").nullableLong);
        assertThrows(Exception.class, () -> assign("integer", "null"));
        assertThrows(Exception.class, () -> assign("longValue", "null"));
    }

}
