// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.internals;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonStreamFactory;
import com.eischet.janitor.toolbox.json.api.JsonWriteOptions;
import com.eischet.janitor.toolbox.json.api.JsonWriteOptions.EmptyPolicy;
import com.eischet.janitor.toolbox.json.api.JsonWriteOptions.NonFinitePolicy;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Checks that JSON output streams follow their {@link JsonWriteOptions}, for every available implementation.
 */
public class JsonWriteOptionsTestCase extends JanitorTest {

    static List<JsonStreamFactory> factories() {
        return JsonStreamFactory.available();
    }

    private static String strings(final JsonStreamFactory json, final JsonWriteOptions options) throws JsonException {
        return json.write(options, out -> out.beginObject()
                .pair("a", "x")
                .pair("b", null)
                .pair("c", "")
                .optional("d", (String) null)
                .optional("e", "")
                .endObject());
    }

    @ParameterizedTest
    @MethodSource("factories")
    public void emptyPolicies(final JsonStreamFactory json) throws JsonException {
        assertEquals("{\"a\":\"x\",\"b\":null,\"c\":\"\",\"e\":\"\"}", strings(json, JsonWriteOptions.COMPACT));
        assertEquals("{\"a\":\"x\",\"c\":\"\",\"e\":\"\"}", strings(json, JsonWriteOptions.COMPACT.withEmpty(EmptyPolicy.OMIT_NULL)));
        assertEquals("{\"a\":\"x\"}", strings(json, JsonWriteOptions.COMPACT.withEmpty(EmptyPolicy.OMIT_EMPTY)));
    }

    @ParameterizedTest
    @MethodSource("factories")
    public void indentation(final JsonStreamFactory json) throws JsonException {
        assertEquals("{\n  \"a\": 1\n}", json.write(JsonWriteOptions.PRETTY, out -> out.beginObject().key("a").value(1L).endObject()));
    }

    private static String nonFinite(final JsonStreamFactory json, final JsonWriteOptions options) throws JsonException {
        return json.write(options, out -> out.beginArray().value(Double.NaN).value((Number) Double.POSITIVE_INFINITY).value(1.5).endArray());
    }

    @ParameterizedTest
    @MethodSource("factories")
    public void nonFiniteNumbers(final JsonStreamFactory json) throws JsonException {
        assertThrows(JsonException.class, () -> nonFinite(json, JsonWriteOptions.COMPACT));
        assertEquals("[null,null,1.5]", nonFinite(json, JsonWriteOptions.COMPACT.withNonFinite(NonFinitePolicy.NULL)));
        assertEquals("[\"NaN\",\"Infinity\",1.5]", nonFinite(json, JsonWriteOptions.COMPACT.withNonFinite(NonFinitePolicy.STRING)));
    }

}
