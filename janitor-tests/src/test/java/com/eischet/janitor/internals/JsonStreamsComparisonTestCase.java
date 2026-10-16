// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.internals;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.json.gson.GsonJsonStreams;
import com.eischet.janitor.toolbox.json.impl.NativeInputStream;
import com.eischet.janitor.toolbox.json.impl.NativeJsonStreams;
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

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Feeds the same input through every available JSON stream implementation and compares the results with Gson's,
 * the implementation Janitor has always used.
 * Where the others deliberately differ from Gson, namely where Gson's LENIENT mode does not follow {@link JsonReadMode#LENIENT},
 * the expected result is given explicitly.
 */
public class JsonStreamsComparisonTestCase extends JanitorTest {

    /** The reference implementation. */
    private static final JsonStreamFactory GSON = JsonStreamFactory.named(GsonJsonStreams.NAME);
    /** All implementations, including Gson. */
    private static final List<JsonStreamFactory> ALL = JsonStreamFactory.available();
    /** All implementations except Gson; these must follow {@link JsonReadMode} exactly. */
    private static final List<JsonStreamFactory> OTHERS = ALL.stream().filter(json -> !json.name().equals(GsonJsonStreams.NAME)).toList();

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

    private record Difference(String json, JsonReadMode mode, String expected) {
    }

    /** Where the other implementations deliberately read something other than Gson. */
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

    /** Asserts that every implementation reads the text in the same way as Gson. */
    private static void assertSameResult(final String text, final JsonReadMode mode) {
        final String expected = dump(GSON, mode, text);
        for (final JsonStreamFactory json : OTHERS) {
            assertEquals(expected, dump(json, mode, text), () -> json.name() + ", " + mode + ": " + text);
        }
    }

    /** Asserts that every implementation, including Gson, can or cannot read the text. */
    private static void assertReadable(final String text, final JsonReadMode mode, final boolean readable) {
        for (final JsonStreamFactory json : ALL) {
            assertEquals(readable, !ERROR.equals(dump(json, mode, text)), () -> json.name() + ", " + mode + ": " + text);
        }
    }

    @Test
    public void discovery() {
        final List<String> names = ALL.stream().map(JsonStreamFactory::name).toList();
        assertTrue(names.containsAll(List.of(GsonJsonStreams.NAME, NativeJsonStreams.NAME)), names::toString);
        final List<JsonStreamFactory> sorted = ALL.stream()
                .sorted(Comparator.comparingInt(JsonStreamFactory::priority).reversed().thenComparing(JsonStreamFactory::name))
                .toList();
        assertEquals(sorted, ALL);
        assertInstanceOf(NativeJsonStreams.class, JsonStreamFactory.named(NativeJsonStreams.NAME));
    }

    @Test
    public void validDocuments() {
        for (final String text : VALID) {
            for (final JsonReadMode mode : JsonReadMode.values()) {
                assertReadable(text, mode, true);
                assertSameResult(text, mode);
            }
        }
    }

    @Test
    public void strictErrors() {
        for (final String text : STRICT_ERRORS) {
            assertReadable(text, JsonReadMode.STRICT, false);
        }
    }

    @Test
    public void lenientAdditions() {
        for (final String text : LENIENT_SAME) {
            assertReadable(text, JsonReadMode.LENIENT, true);
            assertSameResult(text, JsonReadMode.LENIENT);
        }
    }

    @Test
    public void expectedDifferences() {
        for (final Difference difference : DIFFERENCES) {
            for (final JsonStreamFactory json : OTHERS) {
                assertEquals(difference.expected(), dump(json, difference.mode(), difference.json()),
                        () -> json.name() + ", " + difference.mode() + ": " + difference.json());
            }
            assertNotEquals(difference.expected(), dump(GSON, difference.mode(), difference.json()),
                    () -> "Gson no longer differs, so move this case: " + difference.mode() + ": " + difference.json());
        }
    }

    @Test
    public void readingFromReader() throws JsonException {
        final String text = "{\"a\":[1,2,3],\"b\":\"x\"}";
        for (final JsonStreamFactory json : ALL) {
            final StringBuilder sb = new StringBuilder();
            final JsonInputStream in = json.reader(new StringReader(text), JsonReadMode.STRICT);
            dumpValue(in, sb);
            in.close();
            assertEquals("{a:[n(1.0),n(2.0),n(3.0),],b:s(x),}", sb.toString(), json.name());
        }
    }

    /** A reader that returns one character at a time, so that every token crosses buffer boundaries. */
    private static final class TrickleReader extends StringReader {
        TrickleReader(final String s) {
            super(s);
        }

        @Override
        public int read(final char[] buffer, final int offset, final int length) throws IOException {
            return super.read(buffer, offset, Math.min(length, 1));
        }
    }

    private static String dumpTrickling(final JsonStreamFactory json, final JsonReadMode mode, final String text) {
        try {
            final JsonInputStream in = json.reader(new TrickleReader(text), mode);
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

    @Test
    public void readingInSmallChunks() throws JsonException {
        final List<String> texts = new ArrayList<>(VALID);
        texts.addAll(STRICT_ERRORS);
        texts.addAll(LENIENT_SAME);
        DIFFERENCES.forEach(difference -> texts.add(difference.json()));
        texts.add(GSON.write(JsonWriteOptions.PRETTY, DOCUMENT));
        texts.add("/* a\n * b */ {'a' : // c\n [1, 'x' , ] , }");
        for (final String text : texts) {
            for (final JsonReadMode mode : JsonReadMode.values()) {
                for (final JsonStreamFactory json : ALL) {
                    assertEquals(dump(json, mode, text), dumpTrickling(json, mode, text), () -> json.name() + ", " + mode + ": " + text);
                }
            }
        }
    }

    @Test
    public void nativeLimits() {
        final JsonStreamFactory json = JsonStreamFactory.named(NativeJsonStreams.NAME);
        final String deep = "[".repeat(NativeInputStream.MAX_DEPTH) + "]".repeat(NativeInputStream.MAX_DEPTH);
        assertNotEquals(ERROR, dump(json, JsonReadMode.STRICT, deep));
        assertEquals(ERROR, dump(json, JsonReadMode.STRICT, "[" + deep + "]"));
        final String digits = "0." + "1".repeat(NativeInputStream.MAX_NUMBER_LENGTH - 2); // a finite double, as dump() reads doubles
        assertNotEquals(ERROR, dump(json, JsonReadMode.STRICT, "[" + digits + "]"));
        assertEquals(ERROR, dump(json, JsonReadMode.STRICT, "[" + digits + "1]"));
    }

    /** Reads something from a stream; may throw a JsonException. */
    @FunctionalInterface
    private interface Conversion {
        Object apply(JsonInputStream in) throws JsonException;
    }

    /** Applies the conversion, writing errors as {@link #ERROR}. */
    private static String convert(final JsonStreamFactory json, final String text, final Conversion conversion) {
        try {
            return String.valueOf(conversion.apply(json.reader(text, JsonReadMode.STRICT)));
        } catch (JsonException e) {
            return ERROR;
        }
    }

    /** Applies the same conversion with every implementation and compares the results with Gson's. */
    private static void assertConversion(final String text, final Conversion conversion) {
        final String expected = convert(GSON, text, conversion);
        for (final JsonStreamFactory json : OTHERS) {
            assertEquals(expected, convert(json, text, conversion), () -> json.name() + ": " + text);
        }
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
        for (final JsonStreamFactory json : ALL) {
            final JsonInputStream in = json.reader("{\"a\":[1,{\"b\":2}]}", JsonReadMode.STRICT);
            assertEquals("$", in.getPath(), json.name());
            in.beginObject();
            in.nextKey();
            in.beginArray();
            in.nextLong();
            in.beginObject();
            in.nextKey();
            assertEquals("$.a[1].b", in.getPath(), json.name());
        }
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

    /** Asserts that every implementation writes the same text as Gson. */
    private static void assertSameOutput(final JsonWriteOptions options, final JsonWriter writer) throws JsonException {
        final String expected = GSON.write(options, writer);
        for (final JsonStreamFactory json : OTHERS) {
            assertEquals(expected, json.write(options, writer), () -> json.name() + ", " + options);
        }
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
            assertSameOutput(options, DOCUMENT);
            // and all of them read it back in the same way
            final String gson = GSON.write(options, DOCUMENT);
            assertReadable(gson, JsonReadMode.STRICT, true);
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
                assertSameOutput(options, writer);
            }
        }
    }

    @Test
    public void writingNonFiniteNumbers() throws JsonException {
        final JsonWriter writer = out -> out.beginArray().value(Double.NaN).value(Double.NEGATIVE_INFINITY)
                .value((Number) Float.POSITIVE_INFINITY).value((Number) Double.NaN).endArray();
        for (final NonFinitePolicy policy : List.of(NonFinitePolicy.NULL, NonFinitePolicy.STRING)) {
            final JsonWriteOptions options = JsonWriteOptions.COMPACT.withNonFinite(policy);
            assertSameOutput(options, writer);
        }
        for (final JsonStreamFactory json : ALL) {
            assertThrows(JsonException.class, () -> json.write(JsonWriteOptions.COMPACT, writer), json.name());
        }
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
            // Gson throws IllegalStateException or IllegalArgumentException for some of these, the others always JsonException
            assertThrows(RuntimeException.class, () -> GSON.write(JsonWriteOptions.COMPACT, writer));
            for (final JsonStreamFactory json : OTHERS) {
                assertThrows(JsonException.class, () -> json.write(JsonWriteOptions.COMPACT, writer), json.name());
            }
        }
        for (final JsonStreamFactory json : ALL) {
            final JsonOutputStream incomplete = json.writer(JsonWriteOptions.COMPACT, new StringWriter());
            incomplete.beginArray();
            assertThrows(JsonException.class, incomplete::close, json.name());
        }
    }

    /**
     * Reads a value into lists, maps, strings, booleans, nulls and numbers; numbers keep their literal text,
     * so that writing them again gives the same text.
     */
    private static Object readValue(final JsonInputStream in) throws JsonException {
        return switch (in.peek()) {
            case BEGIN_ARRAY -> {
                final List<Object> list = new ArrayList<>();
                in.beginArray();
                while (in.hasNext()) {
                    list.add(readValue(in));
                }
                in.endArray();
                yield list;
            }
            case BEGIN_OBJECT -> {
                final Map<String, Object> map = new LinkedHashMap<>();
                in.beginObject();
                while (in.hasNext()) {
                    final String key = in.nextKey();
                    map.put(key, readValue(in));
                }
                in.endObject();
                yield map;
            }
            case STRING -> in.nextString();
            case NUMBER -> new OddNumber(in.nextString());
            case BOOLEAN -> in.nextBoolean();
            case NULL -> in.nextNullObject();
            default -> throw new AssertionError("unexpected token " + in.peek() + " at " + in.getPath());
        };
    }

    /** Writes a value as built by {@link #readValue} or {@link #randomValue}. */
    private static void writeValue(final JsonOutputStream out, final Object value) throws JsonException {
        switch (value) {
            case null -> out.nullValue();
            case String string -> out.value(string);
            case Boolean bool -> out.value(bool.booleanValue());
            case Number number -> out.value(number);
            case List<?> list -> {
                out.beginArray();
                for (final Object item : list) {
                    writeValue(out, item);
                }
                out.endArray();
            }
            case Map<?, ?> map -> {
                out.beginObject();
                for (final Map.Entry<?, ?> entry : map.entrySet()) {
                    out.key((String) entry.getKey());
                    writeValue(out, entry.getValue());
                }
                out.endObject();
            }
            default -> throw new AssertionError("cannot write " + value.getClass());
        }
    }

    /** Characters that are likely to cause trouble, besides plain ASCII. */
    private static final String ODD_CHARACTERS = "\"\\/'<>&=\u007fé€  ﻿";

    private static String randomString(final Random random) {
        final StringBuilder sb = new StringBuilder();
        final int length = random.nextInt(12);
        for (int i = 0; i < length; i++) {
            switch (random.nextInt(5)) {
                case 0 -> sb.append((char) random.nextInt(0x20)); // control characters
                case 1 -> sb.append(ODD_CHARACTERS.charAt(random.nextInt(ODD_CHARACTERS.length())));
                case 2 -> sb.appendCodePoint(0x1F600 + random.nextInt(0x50)); // surrogate pairs
                default -> sb.append((char) (0x20 + random.nextInt(0x5F)));
            }
        }
        return sb.toString();
    }

    private static Number randomNumber(final Random random) {
        return switch (random.nextInt(6)) {
            case 0 -> random.nextInt(2000) - 1000;
            case 1 -> random.nextLong();
            case 2 -> random.nextFloat() * 1e10f;
            case 3 -> new BigDecimal(BigInteger.valueOf(random.nextLong()), random.nextInt(40) - 10);
            case 4 -> new BigInteger(100, random);
            default -> {
                double d;
                do {
                    d = Double.longBitsToDouble(random.nextLong());
                } while (!Double.isFinite(d));
                yield d;
            }
        };
    }

    /** Builds a random value; containers become rarer the deeper they are nested. */
    private static Object randomValue(final Random random, final int depth) {
        return switch (random.nextInt(depth > 4 ? 4 : 6)) {
            case 0 -> random.nextInt(4) == 0 ? null : random.nextBoolean();
            case 1 -> randomString(random);
            case 2, 3 -> randomNumber(random);
            case 4 -> {
                final List<Object> list = new ArrayList<>();
                for (int i = random.nextInt(5); i > 0; i--) {
                    list.add(randomValue(random, depth + 1));
                }
                yield list;
            }
            default -> {
                final Map<String, Object> map = new LinkedHashMap<>();
                for (int i = random.nextInt(5); i > 0; i--) {
                    map.put(randomString(random), randomValue(random, depth + 1));
                }
                yield map;
            }
        };
    }

    /**
     * Writes documents with every implementation, reads them back with every implementation in both modes, and writes what was
     * read with every implementation again; all of these must give the same text.
     */
    @Test
    public void roundTrips() throws JsonException {
        final Random random = new Random(20261016);
        final List<JsonWriter> documents = new ArrayList<>();
        documents.add(DOCUMENT);
        for (int i = 0; i < 300; i++) {
            final Object value = randomValue(random, 0);
            documents.add(out -> writeValue(out, value));
        }
        for (final JsonWriter document : documents) {
            for (final JsonWriteOptions options : List.of(JsonWriteOptions.COMPACT, JsonWriteOptions.PRETTY)) {
                assertSameOutput(options, document);
                final String text = GSON.write(options, document);
                for (final JsonStreamFactory reader : ALL) {
                    for (final JsonReadMode mode : JsonReadMode.values()) {
                        final Object value;
                        try {
                            final JsonInputStream in = reader.reader(text, mode);
                            value = readValue(in);
                            assertEquals(JsonTokenType.END_DOCUMENT, in.peek());
                            in.close();
                        } catch (JsonException e) {
                            throw new AssertionError(reader.name() + ", " + mode + " cannot read: " + text, e);
                        }
                        for (final JsonStreamFactory writer : ALL) {
                            assertEquals(text, writer.write(options, out -> writeValue(out, value)),
                                    () -> "read by " + reader.name() + " in " + mode + ", written by " + writer.name());
                        }
                    }
                }
            }
        }
    }

    @Test
    public void closingClosesTarget() throws JsonException {
        for (final JsonStreamFactory json : ALL) {
            final int[] closed = {0};
            final StringWriter target = new StringWriter() {
                @Override
                public void close() {
                    closed[0]++;
                }
            };
            final JsonOutputStream out = json.writer(JsonWriteOptions.COMPACT, target);
            out.beginArray().endArray();
            out.close();
            assertEquals(1, closed[0], json.name());
            assertEquals("[]", target.toString(), json.name());
        }
    }

}
