// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.internals;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.json.impl.GsonOutputStream;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonWriteOptions;
import com.eischet.janitor.toolbox.json.api.JsonWriteOptions.EmptyPolicy;
import com.eischet.janitor.toolbox.json.api.JsonWriteOptions.NonFinitePolicy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Checks that JSON output streams follow their {@link JsonWriteOptions}.
 */
public class JsonWriteOptionsTestCase extends JanitorTest {

    private static String strings(final JsonWriteOptions options) throws JsonException {
        final GsonOutputStream.GsonStringOut out = GsonOutputStream.stringWriter(options);
        out.beginObject()
                .pair("a", "x")
                .pair("b", null)
                .pair("c", "")
                .optional("d", (String) null)
                .optional("e", "")
                .endObject();
        return out.getString();
    }

    @Test
    public void emptyPolicies() throws JsonException {
        assertEquals("{\"a\":\"x\",\"b\":null,\"c\":\"\",\"e\":\"\"}", strings(JsonWriteOptions.COMPACT));
        assertEquals("{\"a\":\"x\",\"c\":\"\",\"e\":\"\"}", strings(JsonWriteOptions.COMPACT.withEmpty(EmptyPolicy.OMIT_NULL)));
        assertEquals("{\"a\":\"x\"}", strings(JsonWriteOptions.COMPACT.withEmpty(EmptyPolicy.OMIT_EMPTY)));
    }

    @Test
    public void indentation() throws JsonException {
        final GsonOutputStream.GsonStringOut out = GsonOutputStream.stringWriter(JsonWriteOptions.PRETTY);
        out.beginObject().key("a").value(1L).endObject();
        assertEquals("{\n  \"a\": 1\n}", out.getString());
    }

    private static String nonFinite(final JsonWriteOptions options) throws JsonException {
        final GsonOutputStream.GsonStringOut out = GsonOutputStream.stringWriter(options);
        out.beginArray().value(Double.NaN).value((Number) Double.POSITIVE_INFINITY).value(1.5).endArray();
        return out.getString();
    }

    @Test
    public void nonFiniteNumbers() throws JsonException {
        assertThrows(JsonException.class, () -> nonFinite(JsonWriteOptions.COMPACT));
        assertEquals("[null,null,1.5]", nonFinite(JsonWriteOptions.COMPACT.withNonFinite(NonFinitePolicy.NULL)));
        assertEquals("[\"NaN\",\"Infinity\",1.5]", nonFinite(JsonWriteOptions.COMPACT.withNonFinite(NonFinitePolicy.STRING)));
    }

}
