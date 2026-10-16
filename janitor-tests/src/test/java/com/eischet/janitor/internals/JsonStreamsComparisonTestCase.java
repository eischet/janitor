// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.internals;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.json.impl.GsonJsonStreams;
import com.eischet.janitor.json.jackson.JacksonJsonStreams;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonInputStream;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;
import com.eischet.janitor.toolbox.json.api.JsonReadMode;
import com.eischet.janitor.toolbox.json.api.JsonStreamFactory;
import com.eischet.janitor.toolbox.json.api.JsonTokenType;
import com.eischet.janitor.toolbox.json.api.JsonWriteOptions;
import com.eischet.janitor.toolbox.json.api.JsonWriteOptions.NonFinitePolicy;
import com.eischet.janitor.toolbox.json.api.JsonWriter;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Feeds the same input through the Gson and the Jackson JSON streams and compares the results.
 * Where Jackson deliberately differs from Gson, namely where Gson's LENIENT mode does not follow {@link JsonReadMode#LENIENT},
 * the expected Jackson result is given explicitly.
 */
public class JsonStreamsComparisonTestCase extends JanitorTest {

    private static final JsonStreamFactory GSON = JsonStreamFactory.named(GsonJsonStreams.NAME);
    private static final JsonStreamFactory JACKSON = JsonStreamFactory.named(JacksonJsonStreams.NAME);

    private static final String ERROR = "ERROR";
    private static final String BOM = String.valueOf((char) 0xFEFF);
    /** Starts a JSON unicode escape; spelled this way because javac would read the escape itself. */
    private static final String U = "\\" + "u";

    /** Documents that are valid JSON, so both modes must read them the same way. */
    private static final List<String> VALID = List.of(
            "{\"a\":1,\"b\":[true,false,null,\"x\"],\"c\":{\"d\":-1.5e3,\"e\":{}},\"f\":[]}",
            "[]", "{}", "\"s\"", "17", "null", "true", "  [1, 2]\n\t ",
            "\"esc \\\" \\\\ \\/ \\b \\f \\n \\r \\t " + U + "00e9 " + U + "d83d" + U + "de00 " + U + "2028\"",
            "{\"a\":1,\"a\":2}",
            "[-0, 0.5, 1E2, 1e-2, 9007199254740993, -12345678901234567890]",
            "[[[[[]]]],{\"x\":[{}]}]"
    );

    /** Documents that both implementations reject in STRICT mode. */
    private static final List<String> STRICT_ERRORS = List.of(
            "{'a':1}", "{a:1}", "[1,]", "{\"a\":1,}", "// c\n[1]", "/* c */[1]", "[NaN]", "[Infinity]", "[1e400]",
            "[1] [2]", "", "   ", "[1", "{\"a\"}", "{\"a\":}", "[01]", "[1;2]", "[abc]", "[+1]", "[.5]",
            "[\"a" + (char) 1 + "\"]", "[\"\\x\"]"
    );

    /** Documents that both implementations accept in LENIENT mode. */
    private static final List<String> LENIENT_SAME = List.of(
            "{'a':'b'}", "{a:1}", "{$a_1:1}", "/*x*/[1 // y\n]", "'single'", "{'a':\"it's\"}",
            BOM + "{\"a\":1}", "[1e400]"
    );

    private record Difference(String json, JsonReadMode mode, String jackson) {
    }

    /** Where Jackson deliberately reads something other than Gson. */
    private static final List<Difference> DIFFERENCES = List.of(
            // LENIENT accepts these as numbers; Gson reads them as unquoted strings
            new Difference("[NaN,Infinity,-Infinity]", JsonReadMode.LENIENT, "[n(NaN),n(Infinity),n(-Infinity),]"),
            // trailing commas: Gson reads an extra null in arrays and rejects them in objects
            new Difference("[1,]", JsonReadMode.LENIENT, "[n(1.0),]"),
            new Difference("{\"a\":1,}", JsonReadMode.LENIENT, "{a:n(1.0),}"),
            // Gson oddities that LENIENT does not include
            new Difference("[,1]", JsonReadMode.LENIENT, ERROR),
            new Difference("{\"a\"=1}", JsonReadMode.LENIENT, ERROR),
            new Difference("{\"a\"=>1;\"b\":2}", JsonReadMode.LENIENT, ERROR),
            new Difference("[abc]", JsonReadMode.LENIENT, ERROR),
            new Difference("[1;2]", JsonReadMode.LENIENT, ERROR),
            new Difference("# c\n[1]", JsonReadMode.LENIENT, ERROR),
            new Difference(")]}'\n[1]", JsonReadMode.LENIENT, ERROR),
            new Difference("[1] [2]", JsonReadMode.LENIENT, ERROR),
            // Gson always skips a byte order mark; STRICT does not accept one
            new Difference(BOM + "[1]", JsonReadMode.STRICT, ERROR)
    );

    /**
     * Reads a whole document into a string that shows the tokens and values.
     * @return the string, or {@link #ERROR} if the document cannot be read
     */
    private static String dump(final JsonStreamFactory json, final JsonReadMode mode, final String text) {
        try {
            final JsonInputStream in = json.reader(text, mode);
            final StringBuilder sb = new StringBuilder();
            dumpValue(in, sb);
            if (in.peek() != JsonTokenType.END_DOCUMENT) {
                sb.append(" +more");
            }
            in.close();
            return sb.toString();
        } catch (JsonException e) {
            return ERROR;
        }
    }

    private static void dumpValue(final JsonInputStream in, final StringBuilder sb) throws JsonException {
        switch (in.peek()) {
            case BEGIN_ARRAY -> {
                in.beginArray();
                sb.append('[');
                while (in.hasNext()) {
                    dumpValue(in, sb);
                    sb.append(',');
                }
                in.endArray();
                sb.append(']');
            }
            case BEGIN_OBJECT -> {
                in.beginObject();
                sb.append('{');
                while (in.hasNext()) {
                    sb.append(in.nextKey()).append(':');
                    dumpValue(in, sb);
                    sb.append(',');
                }
                in.endObject();
                sb.append('}');
            }
            case STRING -> sb.append("s(").append(in.nextString()).append(')');
            case NUMBER -> sb.append("n(").append(in.nextDouble()).append(')');
            case BOOLEAN -> sb.append("b(").append(in.nextBoolean()).append(')');
            case NULL -> {
                in.nextNull();
                sb.append("null");
            }
            default -> fail("unexpected token " + in.peek() + " at " + in.getPath());
        }
    }

    private static void assertSameResult(final String text, final JsonReadMode mode) {
        assertEquals(dump(GSON, mode, text), dump(JACKSON, mode, text), () -> mode + ": " + text);
    }

    @Test
    public void discovery() {
        final List<String> names = JsonStreamFactory.available().stream().map(JsonStreamFactory::name).toList();
        assertEquals(List.of(GsonJsonStreams.NAME, JacksonJsonStreams.NAME), names); // sorted by priority
        assertInstanceOf(JacksonJsonStreams.class, JACKSON);
    }

    @Test
    public void validDocuments() {
        for (final String text : VALID) {
            for (final JsonReadMode mode : JsonReadMode.values()) {
                assertNotEquals(ERROR, dump(JACKSON, mode, text), () -> mode + ": " + text);
                assertSameResult(text, mode);
            }
        }
    }

    @Test
    public void strictErrors() {
        for (final String text : STRICT_ERRORS) {
            assertEquals(ERROR, dump(JACKSON, JsonReadMode.STRICT, text), text);
            assertSameResult(text, JsonReadMode.STRICT);
        }
    }

    @Test
    public void lenientAdditions() {
        for (final String text : LENIENT_SAME) {
            assertNotEquals(ERROR, dump(JACKSON, JsonReadMode.LENIENT, text), text);
            assertSameResult(text, JsonReadMode.LENIENT);
        }
    }

    @Test
    public void expectedDifferences() {
        for (final Difference difference : DIFFERENCES) {
            final String jackson = dump(JACKSON, difference.mode(), difference.json());
            assertEquals(difference.jackson(), jackson, () -> difference.mode() + ": " + difference.json());
            assertNotEquals(dump(GSON, difference.mode(), difference.json()), jackson,
                    () -> "Gson no longer differs, so move this case: " + difference.mode() + ": " + difference.json());
        }
    }

    @Test
    public void readingFromReader() throws JsonException {
        final String text = "{\"a\":[1,2,3],\"b\":\"x\"}";
        for (final JsonStreamFactory json : List.of(GSON, JACKSON)) {
            final StringBuilder sb = new StringBuilder();
            final JsonInputStream in = json.reader(new StringReader(text), JsonReadMode.STRICT);
            dumpValue(in, sb);
            in.close();
            assertEquals("{a:[n(1.0),n(2.0),n(3.0),],b:s(x),}", sb.toString(), json.name());
        }
    }

    /** Reads something from a stream; may throw a JsonException. */
    @FunctionalInterface
    private interface Conversion {
        Object apply(JsonInputStream in) throws JsonException;
    }

    /** Applies the same conversion with both implementations, writing errors as {@link #ERROR}. */
    private static void assertConversion(final String text, final Conversion conversion) {
        final List<String> results = new ArrayList<>();
        for (final JsonStreamFactory json : List.of(GSON, JACKSON)) {
            try {
                final JsonInputStream in = json.reader(text, JsonReadMode.STRICT);
                results.add(String.valueOf(conversion.apply(in)));
            } catch (JsonException e) {
                results.add(ERROR);
            }
        }
        assertEquals(results.get(0), results.get(1), text);
    }

    @Test
    public void conversions() {
        for (final String text : List.of("1", "1.0", "1e3", "-7", "\"7\"", "\"7.0\"", "1.5", "9999999999", "99999999999999999999",
                "\"x\"", "true", "null", "\"NaN\"", "[1]")) {
            assertConversion(text, JsonInputStream::nextLong);
            assertConversion(text, JsonInputStream::nextInt);
            assertConversion(text, JsonInputStream::nextDouble);
            assertConversion(text, JsonInputStream::nextString);
            assertConversion(text, JsonInputStream::nextBoolean);
            assertConversion(text, JsonInputStream::nextNullObject);
            assertConversion(text, in -> {
                in.skipValue();
                return in.peek();
            });
        }
    }

    @Test
    public void skipping() {
        final String text = "{\"a\":{\"x\":[1,{\"y\":2}]},\"b\":2,\"c\":[3],\"d\":4}";
        assertConversion(text, in -> {
            in.beginObject();
            final StringBuilder sb = new StringBuilder();
            while (in.hasNext()) {
                final String key = in.nextKey();
                if (key.equals("b")) {
                    sb.append(key).append('=').append(in.nextLong()).append(';');
                } else {
                    in.skipValue();
                }
            }
            in.endObject();
            return sb.append(in.peek()).toString();
        });
        // skipping at a name
        assertConversion(text, in -> {
            in.beginObject();
            in.skipValue();
            return in.peek();
        });
        // structural mismatches are errors
        assertConversion(text, in -> {
            in.beginArray();
            return "no error";
        });
        assertConversion(text, in -> {
            in.beginObject();
            return in.nextString();
        });
        assertConversion(text, in -> {
            in.beginObject();
            in.nextKey();
            in.skipValue();
            in.endObject();
            return "no error";
        });
    }

    @Test
    public void paths() throws JsonException {
        final JsonInputStream in = JACKSON.reader("{\"a\":[1,{\"b\":2}]}", JsonReadMode.STRICT);
        assertEquals("$", in.getPath());
        in.beginObject();
        in.nextKey();
        in.beginArray();
        in.nextLong();
        in.beginObject();
        in.nextKey();
        assertEquals("$.a[1].b", in.getPath());
    }

    /** A number type that Gson does not know, so its toString() is checked. */
    private static final class OddNumber extends Number {
        private final String text;

        private OddNumber(final String text) {
            this.text = text;
        }

        @Override
        public int intValue() {
            return 0;
        }

        @Override
        public long longValue() {
            return 0;
        }

        @Override
        public float floatValue() {
            return 0;
        }

        @Override
        public double doubleValue() {
            return 0;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    private static String allCharacters() {
        final StringBuilder sb = new StringBuilder();
        for (char c = 0; c < 0x80; c++) {
            sb.append(c);
        }
        sb.append((char) 0x2028).append((char) 0x2029).append((char) 0xE9).append((char) 0x20AC).appendCodePoint(0x1F600);
        return sb.toString();
    }

    private static final JsonWriter DOCUMENT = out -> {
        out.beginObject();
        out.key("chars").value(allCharacters());
        out.key(allCharacters()).value("key with odd characters");
        out.key("null string").value((String) null);
        out.key("null number").value((Number) null);
        out.key("null date").value((LocalDateTime) null);
        out.key("longs").beginArray().value(0L).value(Long.MIN_VALUE).value(Long.MAX_VALUE).endArray();
        out.key("doubles").beginArray().value(1.0).value(0.1).value(1e20).value(-0.0).value(1e-7).value(Double.MIN_VALUE).value(1e300).endArray();
        out.key("numbers").beginArray()
                .value((Number) 5).value((Number) 1.5f).value((Number) 0.1f).value(new BigDecimal("1E+3"))
                .value(new BigDecimal("0.000001234")).value(new BigInteger("123456789012345678901234567890"))
                .value((Number) (short) 7).value((Number) (byte) -8).value(new AtomicLong(9)).value(new LongAdder())
                .value(new OddNumber("1.5e3")).value((Number) 2.5)
                .endArray();
        out.key("booleans").beginArray().value(true).value(false).endArray();
        out.key("dates").beginArray().value(LocalDateTime.of(2026, 10, 16, 12, 34, 56, 789_000_000)).value(new Date(0L)).endArray();
        out.key("empty array").beginArray().endArray();
        out.key("empty object").beginObject().endObject();
        out.key("nested").beginArray().beginArray().endArray().beginObject().key("a").beginArray().nullValue().endArray().endObject().endArray();
        out.pair("pair", "x").pair("null pair", null).optional("optional", "y").optional("missing", (String) null);
        out.endObject();
    };

    @Test
    public void writing() throws JsonException {
        for (final JsonWriteOptions options : List.of(JsonWriteOptions.COMPACT, JsonWriteOptions.PRETTY, JsonWriteOptions.COMPACT.withIndent("\t"),
                JsonWriteOptions.PRETTY.withEmpty(JsonWriteOptions.EmptyPolicy.OMIT_EMPTY))) {
            final String gson = GSON.write(options, DOCUMENT);
            assertEquals(gson, JACKSON.write(options, DOCUMENT), options::toString);
            // and both read it back in the same way
            assertNotEquals(ERROR, dump(JACKSON, JsonReadMode.STRICT, gson));
            assertSameResult(gson, JsonReadMode.STRICT);
        }
    }

    @Test
    public void writingTopLevelValues() throws JsonException {
        final List<JsonWriter> writers = List.of(
                out -> out.value("x"), out -> out.value(17L), out -> out.value(1.5), JsonOutputStream::nullValue, out -> out.value(true),
                out -> out.beginArray().endArray(), out -> out.beginObject().endObject(),
                out -> out.beginArray().value(1L).beginArray().endArray().endArray());
        for (final JsonWriteOptions options : List.of(JsonWriteOptions.COMPACT, JsonWriteOptions.PRETTY)) {
            for (final JsonWriter writer : writers) {
                assertEquals(GSON.write(options, writer), JACKSON.write(options, writer));
            }
        }
    }

    @Test
    public void writingNonFiniteNumbers() throws JsonException {
        final JsonWriter writer = out -> out.beginArray().value(Double.NaN).value(Double.NEGATIVE_INFINITY)
                .value((Number) Float.POSITIVE_INFINITY).value((Number) Double.NaN).endArray();
        for (final NonFinitePolicy policy : List.of(NonFinitePolicy.NULL, NonFinitePolicy.STRING)) {
            final JsonWriteOptions options = JsonWriteOptions.COMPACT.withNonFinite(policy);
            assertEquals(GSON.write(options, writer), JACKSON.write(options, writer));
        }
        assertThrows(JsonException.class, () -> JACKSON.write(JsonWriteOptions.COMPACT, writer));
    }

    @Test
    public void writingErrors() throws JsonException {
        final List<JsonWriter> bad = List.of(
                out -> out.value(1L).value(2L),
                out -> out.beginArray().endArray().beginArray(),
                out -> out.beginArray().value(new OddNumber("abc")),
                out -> out.beginObject().value(1L),
                out -> out.beginArray().endObject());
        for (final JsonWriter writer : bad) {
            // Gson throws IllegalStateException or IllegalArgumentException for some of these, Jackson always JsonException
            assertThrows(RuntimeException.class, () -> GSON.write(JsonWriteOptions.COMPACT, writer));
            assertThrows(JsonException.class, () -> JACKSON.write(JsonWriteOptions.COMPACT, writer));
        }
        for (final JsonStreamFactory json : List.of(GSON, JACKSON)) {
            final JsonOutputStream incomplete = json.writer(JsonWriteOptions.COMPACT, new StringWriter());
            incomplete.beginArray();
            assertThrows(JsonException.class, incomplete::close, json.name());
        }
    }

    @Test
    public void closingClosesTarget() throws JsonException {
        final int[] closed = {0};
        final StringWriter target = new StringWriter() {
            @Override
            public void close() {
                closed[0]++;
            }
        };
        final JsonOutputStream out = JACKSON.writer(JsonWriteOptions.COMPACT, target);
        out.beginArray().endArray();
        out.close();
        assertEquals(1, closed[0]);
        assertEquals("[]", target.toString());
    }

}
