// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.features;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A decimal property holds amounts of money: JSON must hand over the number as it is written, in both directions, and not a
 * binary approximation of it.
 */
public class BigDecimalJsonTestCase extends JanitorTest {

    private static class Invoice extends JanitorComposed<Invoice> {
        public static final DispatchTable<Invoice> DISPATCH = new DispatchTable<>(Invoice::new);

        static {
            DISPATCH.addBigDecimalProperty("amount", self -> self.amount, (self, value) -> self.amount = value);
        }

        private BigDecimal amount;

        Invoice() {
            super(DISPATCH);
        }
    }

    private Invoice read(final String number) throws Exception {
        return Invoice.DISPATCH.readFromJson(Invoice::new, "{\"amount\":" + number + "}");
    }

    private String written(final String number) throws Exception {
        final Invoice invoice = new Invoice();
        invoice.amount = new BigDecimal(number);
        return Invoice.DISPATCH.writeToJson(invoice);
    }

    @Test
    void amountsAreReadAsWritten() throws Exception {
        assertEquals(new BigDecimal("5.7"), read("5.7").amount);
        assertEquals(new BigDecimal("0.1"), read("0.1").amount);
        assertEquals(new BigDecimal("5"), read("5").amount);
        assertEquals(new BigDecimal("1234567890123456.78"), read("1234567890123456.78").amount);
        assertEquals(new BigDecimal("9007199254740993"), read("9007199254740993").amount);
    }

    @Test
    void amountsAreWrittenExactly() throws Exception {
        assertEquals("{\"amount\":5.7}", written("5.7"));
        assertEquals("{\"amount\":1234567890123456.78}", written("1234567890123456.78"));
        assertEquals("{\"amount\":9007199254740993}", written("9007199254740993"));
    }

    @Test
    void amountsSurviveWritingAndReading() throws Exception {
        for (final String number : new String[]{"19.99", "0.07", "1234567890123456.78", "-0.005"}) {
            assertEquals(new BigDecimal(number), read(written(number).replaceAll("[^0-9.-]", "")).amount, number);
        }
    }

}
