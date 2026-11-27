// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.features;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.metadata.ScriptType;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.api.types.functions.Signature;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Native methods take numeric arguments the way numeric properties take values: a whole number may come with a decimal point,
 * a whole number is fine where a float is expected, and what cannot be passed on without changing the value is refused.
 */
public class NumericArgumentConversionTestCase extends JanitorTest {

    private static final List<String> WHOLE_NUMBER_METHODS = List.of("requiredInt", "requiredLong", "coercedInt", "seconds", "namedInteger", "namedSeconds");

    private static final Signature NAMED_INTEGER = Signature.method("namedInteger").param("count", ScriptType.INT).help("Returns the count.");
    private static final Signature NAMED_SECONDS = Signature.method("namedSeconds").param("timeout", ScriptType.DURATION).help("Returns the timeout.");

    private static class Probe extends JanitorComposed<Probe> {
        public static final DispatchTable<Probe> DISPATCH = new DispatchTable<>();

        static {
            DISPATCH.addMethod("requiredInt", (self, process, args) -> Janitor.integer(args.getRequiredIntValue(0)));
            DISPATCH.addMethod("requiredLong", (self, process, args) -> Janitor.integer(args.getRequiredLongValue(0)));
            DISPATCH.addMethod("coercedInt", (self, process, args) -> args.getInt(0));
            DISPATCH.addMethod("coercedFloat", (self, process, args) -> args.getFloat(0));
            DISPATCH.addMethod("seconds", (self, process, args) -> Janitor.integer(args.getSeconds(0)));
            DISPATCH.addMethod(NAMED_INTEGER, (self, process, args) -> Janitor.integer(args.integer("count")));
            DISPATCH.addMethod(NAMED_SECONDS, (self, process, args) -> Janitor.integer(args.seconds("timeout")));
        }

        Probe() {
            super(DISPATCH);
        }
    }

    private Object call(final String method, final String argument) throws Exception {
        return evaluate("probe." + method + "(" + argument + ")", g -> g.bind("probe", new Probe())).janitorGetHostValue();
    }

    @Test
    void wholeNumbersArePassedOn() throws Exception {
        for (final String method : WHOLE_NUMBER_METHODS) {
            assertEquals(5L, call(method, "5"), method);
        }
    }

    @Test
    void wholeNumbersWithDecimalPointArePassedOn() throws Exception {
        for (final String method : WHOLE_NUMBER_METHODS) {
            assertEquals(5L, call(method, "5.0"), method);
            assertEquals(-12L, call(method, "-12.0"), method);
        }
    }

    @Test
    void fractionsAreRefusedNotCutOff() {
        for (final String method : WHOLE_NUMBER_METHODS) {
            assertThrows(Exception.class, () -> call(method, "5.7"), method);
        }
    }

    @Test
    void numbersTooLargeForAnIntAreRefused() {
        assertThrows(Exception.class, () -> call("requiredInt", "3000000000"));
        assertThrows(Exception.class, () -> call("requiredInt", "3000000000.0"));
    }

    @Test
    void floatsTakeWholeNumbers() throws Exception {
        assertEquals(5.0, call("coercedFloat", "5"));
        assertEquals(2.5, call("coercedFloat", "2.5"));
    }

    @Test
    void durationsAreStillSeconds() throws Exception {
        assertEquals(90L, call("seconds", "@90s"));
        assertEquals(90L, call("namedSeconds", "@90s"));
    }

    @Test
    void otherTypesAreStillRefused() {
        for (final String method : List.of("requiredInt", "requiredLong", "coercedInt", "coercedFloat", "seconds", "namedInteger", "namedSeconds")) {
            assertThrows(Exception.class, () -> call(method, "'5'"), method);
            assertThrows(Exception.class, () -> call(method, "true"), method);
        }
    }

}
