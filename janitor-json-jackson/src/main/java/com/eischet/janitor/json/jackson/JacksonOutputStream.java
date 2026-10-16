// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.json.jackson;

import com.eischet.janitor.json.impl.DateTimeUtils;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;
import com.eischet.janitor.toolbox.json.api.JsonWriteOptions;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.io.CharacterEscapes;
import com.fasterxml.jackson.core.io.SerializedString;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

/**
 * JsonOutputStream implementation using Jackson's {@link JsonGenerator}.
 * <p>
 * The output is the same as Gson's, character for character: the same indentation, separators and string escapes.
 * </p>
 */
public class JacksonOutputStream implements JsonOutputStream {

    /** Number types whose toString() is known to be valid JSON, as long as the value is finite. */
    private static final Set<Class<?>> TRUSTED_NUMBERS = Set.of(Integer.class, Long.class, Double.class, Float.class,
            Byte.class, Short.class, BigDecimal.class, BigInteger.class, AtomicInteger.class, AtomicLong.class);

    private static final Pattern VALID_JSON_NUMBER = Pattern.compile("-?(?:0|[1-9][0-9]*)(?:\\.[0-9]+)?(?:[eE][-+]?[0-9]+)?");

    private final JsonWriteOptions options;
    private final JsonGenerator generator;

    /**
     * Constructor.
     * @param options controls how JSON is written; null means {@link JsonWriteOptions#COMPACT}
     * @param generator a Jackson generator, which this stream configures
     */
    JacksonOutputStream(final JsonWriteOptions options, final JsonGenerator generator) {
        this.options = options != null ? options : JsonWriteOptions.COMPACT;
        this.generator = generator;
        generator.setCharacterEscapes(GsonEscapes.INSTANCE);
        if (this.options.isPretty()) {
            generator.setPrettyPrinter(new IndentingPrinter(this.options.indent()));
        }
    }

    @Override
    public @NotNull JsonWriteOptions options() {
        return options;
    }

    private static JsonException fail(final IOException e) {
        return new JsonException("error generating JSON", e);
    }

    /** JSON has exactly one top-level value, while Jackson would write a sequence of documents. */
    private void checkRoot() throws JsonException {
        if (generator.getOutputContext().inRoot() && generator.getOutputContext().getEntryCount() > 0) {
            throw new JsonException("JSON must have only one top-level value");
        }
    }

    /**
     * Writes a NaN or infinite number as the options say.
     * @param value the number
     * @return this
     * @throws JsonException if the options say that such numbers are not allowed
     */
    private JsonOutputStream nonFinite(final Number value) throws JsonException {
        return switch (options.nonFinite()) {
            case FAIL -> throw new JsonException("JSON cannot represent the number " + value);
            case NULL -> nullValue();
            case STRING -> value(value.toString());
        };
    }

    private static boolean isNonFinite(final Number value) {
        return (value instanceof Double d && !Double.isFinite(d)) || (value instanceof Float f && !Float.isFinite(f));
    }

    @Override
    public void close() throws JsonException {
        final boolean complete = generator.getOutputContext().inRoot();
        try {
            generator.close(); // also closes the target
        } catch (IOException e) {
            throw new JsonException("error closing JSON writer", e);
        }
        if (!complete) {
            throw new JsonException("incomplete JSON document");
        }
    }

    @Override
    public void flush() throws JsonException {
        try {
            generator.flush();
        } catch (IOException e) {
            throw fail(e);
        }
    }

    @Override
    public JsonOutputStream beginObject() throws JsonException {
        checkRoot();
        try {
            generator.writeStartObject();
            return this;
        } catch (IOException e) {
            throw fail(e);
        }
    }

    @Override
    public JsonOutputStream endObject() throws JsonException {
        try {
            generator.writeEndObject();
            return this;
        } catch (IOException e) {
            throw fail(e);
        }
    }

    @Override
    public JsonOutputStream beginArray() throws JsonException {
        checkRoot();
        try {
            generator.writeStartArray();
            return this;
        } catch (IOException e) {
            throw fail(e);
        }
    }

    @Override
    public JsonOutputStream endArray() throws JsonException {
        try {
            generator.writeEndArray();
            return this;
        } catch (IOException e) {
            throw fail(e);
        }
    }

    @Override
    public JsonOutputStream key(final String key) throws JsonException {
        try {
            generator.writeFieldName(key);
            return this;
        } catch (IOException e) {
            throw fail(e);
        }
    }

    @Override
    public JsonOutputStream nullValue() throws JsonException {
        checkRoot();
        try {
            generator.writeNull();
            return this;
        } catch (IOException e) {
            throw fail(e);
        }
    }

    @Override
    public JsonOutputStream value(final String value) throws JsonException {
        checkRoot();
        try {
            generator.writeString(value); // writes null for null
            return this;
        } catch (IOException e) {
            throw fail(e);
        }
    }

    @Override
    public JsonOutputStream value(final long value) throws JsonException {
        checkRoot();
        try {
            generator.writeNumber(value);
            return this;
        } catch (IOException e) {
            throw fail(e);
        }
    }

    @Override
    public JsonOutputStream value(final double value) throws JsonException {
        if (!Double.isFinite(value)) {
            return nonFinite(value);
        }
        checkRoot();
        try {
            generator.writeNumber(value);
            return this;
        } catch (IOException e) {
            throw fail(e);
        }
    }

    @Override
    public JsonOutputStream value(final Number value) throws JsonException {
        if (value == null) {
            return nullValue();
        }
        if (isNonFinite(value)) {
            return nonFinite(value);
        }
        // like Gson, write what toString() says, e.g. "1.5" for a Float instead of its value as a double
        final String text = value.toString();
        if (!TRUSTED_NUMBERS.contains(value.getClass()) && !VALID_JSON_NUMBER.matcher(text).matches()) {
            throw new JsonException("not a valid JSON number: " + text + " (" + value.getClass().getName() + ")");
        }
        checkRoot();
        try {
            generator.writeNumber(text);
            return this;
        } catch (IOException e) {
            throw fail(e);
        }
    }

    @Override
    public JsonOutputStream value(final boolean value) throws JsonException {
        checkRoot();
        try {
            generator.writeBoolean(value);
            return this;
        } catch (IOException e) {
            throw fail(e);
        }
    }

    @Override
    public JsonOutputStream value(final LocalDateTime value) throws JsonException {
        return value(DateTimeUtils.toJsonZulu(value));
    }

    @Override
    public JsonOutputStream value(final Date value) throws JsonException {
        return value(DateTimeUtils.toJsonZulu(DateTimeUtils.convert(value)));
    }

    /**
     * Escapes strings like Gson: control characters other than \b, \t, \n, \f and \r as lower case
     * <code>&#92;u00xx</code>, and U+2028 and U+2029, which JavaScript does not allow unescaped in string literals.
     */
    private static final class GsonEscapes extends CharacterEscapes {

        static final GsonEscapes INSTANCE = new GsonEscapes();

        private static final SerializableString LINE_SEPARATOR = new SerializedString("\\u2028");
        private static final SerializableString PARAGRAPH_SEPARATOR = new SerializedString("\\u2029");

        private final int[] asciiEscapes;
        private final SerializableString[] controlEscapes = new SerializableString[32];

        private GsonEscapes() {
            asciiEscapes = standardAsciiEscapesForJSON();
            for (int c = 0; c < 32; c++) {
                if (asciiEscapes[c] == ESCAPE_STANDARD) { // Jackson's generic escape uses upper case hex digits
                    asciiEscapes[c] = ESCAPE_CUSTOM;
                    controlEscapes[c] = new SerializedString(String.format("\\u%04x", c));
                }
            }
        }

        @Override
        public int[] getEscapeCodesForAscii() {
            return asciiEscapes;
        }

        @Override
        public SerializableString getEscapeSequence(final int ch) {
            if (ch < 32) {
                return controlEscapes[ch];
            }
            return switch (ch) {
                case 0x2028 -> LINE_SEPARATOR;
                case 0x2029 -> PARAGRAPH_SEPARATOR;
                default -> null;
            };
        }
    }

    /**
     * Indents like Gson: every value of an array and every property of an object on its own line, ": " between names
     * and values, and nothing between the brackets of empty arrays and objects.
     */
    private static final class IndentingPrinter implements PrettyPrinter {

        private final String indent;
        private int depth = 0;

        IndentingPrinter(final String indent) {
            this.indent = indent;
        }

        private void newline(final JsonGenerator g) throws IOException {
            g.writeRaw('\n');
            for (int i = 0; i < depth; i++) {
                g.writeRaw(indent);
            }
        }

        @Override
        public void writeRootValueSeparator(final JsonGenerator g) {
            // there is only one top-level value
        }

        @Override
        public void writeStartObject(final JsonGenerator g) throws IOException {
            g.writeRaw('{');
            depth++;
        }

        @Override
        public void beforeObjectEntries(final JsonGenerator g) throws IOException {
            newline(g);
        }

        @Override
        public void writeObjectFieldValueSeparator(final JsonGenerator g) throws IOException {
            g.writeRaw(": ");
        }

        @Override
        public void writeObjectEntrySeparator(final JsonGenerator g) throws IOException {
            g.writeRaw(',');
            newline(g);
        }

        @Override
        public void writeEndObject(final JsonGenerator g, final int nrOfEntries) throws IOException {
            depth--;
            if (nrOfEntries > 0) {
                newline(g);
            }
            g.writeRaw('}');
        }

        @Override
        public void writeStartArray(final JsonGenerator g) throws IOException {
            g.writeRaw('[');
            depth++;
        }

        @Override
        public void beforeArrayValues(final JsonGenerator g) throws IOException {
            newline(g);
        }

        @Override
        public void writeArrayValueSeparator(final JsonGenerator g) throws IOException {
            g.writeRaw(',');
            newline(g);
        }

        @Override
        public void writeEndArray(final JsonGenerator g, final int nrOfValues) throws IOException {
            depth--;
            if (nrOfValues > 0) {
                newline(g);
            }
            g.writeRaw(']');
        }
    }

}
