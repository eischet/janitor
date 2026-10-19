// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.features;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.toolbox.json.api.JsonException;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Reading JSON into objects with dispatch tables: a field that is null is read like a missing field, for every kind of property; the
 * property keeps its value.
 */
public class JsonNullFieldsTestCase extends JanitorTest {

    @Language("JSON")
    private static final String ALL_NULL = """
            {"text": null, "integer": null, "nullableInteger": null, "longValue": null, "nullableLong": null, "decimal": null,
             "doubleValue": null, "nullableDouble": null, "bool": null, "nullableBool": null}""";

    @Language("JSON")
    private static final String ALL_SET = """
            {"text": "neu", "integer": 1, "nullableInteger": 2, "longValue": 3, "nullableLong": 4, "decimal": 5.5,
             "doubleValue": 6.5, "nullableDouble": 7.5, "bool": false, "nullableBool": false}""";

    @Test
    public void nullIsReadLikeAMissingField() throws JsonException {
        final Thing missing = Thing.DISPATCH.readFromJson(Thing::new, "{}");
        final Thing nulls = Thing.DISPATCH.readFromJson(Thing::new, ALL_NULL);
        assertEquals(missing.values(), nulls.values());
        assertEquals(new Thing().values(), nulls.values(), "the values of the constructor stay");
    }

    @Test
    public void valuesAreStillRead() throws JsonException {
        final Thing read = Thing.DISPATCH.readFromJson(Thing::new, ALL_SET);
        assertEquals(List.of("neu", 1, 2, 3L, 4L, new BigDecimal("5.5"), 6.5, 7.5, false, false), read.values());
    }

    @Test
    public void writtenObjectsReadBack() throws JsonException {
        final Thing read = Thing.DISPATCH.readFromJson(Thing::new, ALL_NULL);
        final Thing again = Thing.DISPATCH.readFromJson(Thing::new, Thing.DISPATCH.writeToJson(read));
        assertEquals(read.values(), again.values());
    }

    private static class Thing extends JanitorComposed<Thing> {
        private static final DispatchTable<Thing> DISPATCH = new DispatchTable<>(null);

        static {
            DISPATCH.addStringProperty("text", self -> self.text, (self, value) -> self.text = value);
            DISPATCH.addIntegerProperty("integer", self -> self.integer, (self, value) -> self.integer = value);
            DISPATCH.addNullableIntegerProperty("nullableInteger", self -> self.nullableInteger, (self, value) -> self.nullableInteger = value);
            DISPATCH.addLongProperty("longValue", self -> self.longValue, (self, value) -> self.longValue = value);
            DISPATCH.addNullableLongProperty("nullableLong", self -> self.nullableLong, (self, value) -> self.nullableLong = value);
            DISPATCH.addBigDecimalProperty("decimal", self -> self.decimal, (self, value) -> self.decimal = value);
            DISPATCH.addDoubleProperty("doubleValue", self -> self.doubleValue, (self, value) -> self.doubleValue = value);
            DISPATCH.addNullableDoubleProperty("nullableDouble", self -> self.nullableDouble, (self, value) -> self.nullableDouble = value);
            DISPATCH.addBooleanProperty("bool", self -> self.bool, (self, value) -> self.bool = value);
            DISPATCH.addNullableBooleanProperty("nullableBool", self -> self.nullableBool, (self, value) -> self.nullableBool = value);
        }

        private String text = "vorgabe";
        private int integer = 10;
        private Integer nullableInteger = 20;
        private long longValue = 30;
        private Long nullableLong = 40L;
        private BigDecimal decimal = new BigDecimal("50.5");
        private double doubleValue = 60.5;
        private Double nullableDouble = 70.5;
        private boolean bool = true;
        private Boolean nullableBool = true;

        Thing() {
            super(DISPATCH);
        }

        List<Object> values() {
            return List.of(text, integer, nullableInteger, longValue, nullableLong, decimal, doubleValue, nullableDouble, bool, nullableBool);
        }
    }

}
