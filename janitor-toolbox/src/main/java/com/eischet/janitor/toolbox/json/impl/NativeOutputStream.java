// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.toolbox.json.impl;

import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.time.DateTimeUtils;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;
import com.eischet.janitor.toolbox.json.api.JsonWriteOptions;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

/**
 * JsonOutputStream implementation without any library.
 * <p>
 * The output is the same as that of Gson, which Janitor used before, character for character: the same indentation,
 * separators and string escapes.
 * Output is buffered, so it reaches the target only on {@link #flush()} or {@link #close()}.
 * </p>
 */
public class NativeOutputStream implements JsonOutputStream {

    /** Number types whose toString() is known to be valid JSON, as long as the value is finite. */
    private static final Set<Class<?>> TRUSTED_NUMBERS = Set.of(Integer.class, Long.class, Double.class, Float.class,
            Byte.class, Short.class, BigDecimal.class, BigInteger.class, AtomicInteger.class, AtomicLong.class);

    private static final Pattern VALID_JSON_NUMBER = Pattern.compile("-?(?:0|[1-9][0-9]*)(?:\\.[0-9]+)?(?:[eE][-+]?[0-9]+)?");

    /** How to write the characters below 0x80: null if they need no escaping. */
    private static final String[] ASCII_ESCAPES = new String[0x80];

    static {
        for (int c = 0; c < 0x20; c++) {
            ASCII_ESCAPES[c] = String.format("\\u%04x", c);
        }
        ASCII_ESCAPES['"'] = "\\\"";
        ASCII_ESCAPES['\\'] = "\\\\";
        ASCII_ESCAPES['\t'] = "\\t";
        ASCII_ESCAPES['\b'] = "\\b";
        ASCII_ESCAPES['\n'] = "\\n";
        ASCII_ESCAPES['\r'] = "\\r";
        ASCII_ESCAPES['\f'] = "\\f";
    }

    // what the stream expects next, per nesting level
    private static final int EMPTY_DOCUMENT = 0;
    private static final int NONEMPTY_DOCUMENT = 1;
    private static final int EMPTY_ARRAY = 2;
    private static final int NONEMPTY_ARRAY = 3;
    private static final int EMPTY_OBJECT = 4;
    private static final int NONEMPTY_OBJECT = 5;
    /** In an object, after a name, before its value. */
    private static final int DANGLING_NAME = 6;

    private final JsonWriteOptions options;
    private final Writer out;
    /** Collects the output, so that the target gets few large writes instead of many tiny ones. */
    private final char[] buffer = new char[8192];
    private int count = 0;
    private int[] stack = new int[32];
    private int size = 1;
    private boolean closed = false;

    /**
     * Constructor.
     * @param options controls how JSON is written; null means {@link JsonWriteOptions#COMPACT}
     * @param out the target; closing this stream closes it
     */
    public NativeOutputStream(final JsonWriteOptions options, final Writer out) {
        this.options = options != null ? options : JsonWriteOptions.COMPACT;
        this.out = out;
        stack[0] = EMPTY_DOCUMENT;
    }

    @Override
    public @NotNull JsonWriteOptions options() {
        return options;
    }

    private static JsonException fail(final IOException e) {
        return new JsonException("error writing JSON", e);
    }

    private void put(final char c) throws IOException {
        if (count == buffer.length) {
            drain();
        }
        buffer[count++] = c;
    }

    private void put(final String s) throws IOException {
        put(s, 0, s.length());
    }

    private void put(final String s, final int start, final int length) throws IOException {
        if (length > buffer.length - count) {
            drain();
            if (length > buffer.length) {
                out.write(s, start, length);
                return;
            }
        }
        s.getChars(start, start + length, buffer, count);
        count += length;
    }

    /** Passes the buffered output on to the target. */
    private void drain() throws IOException {
        if (count > 0) {
            out.write(buffer, 0, count);
            count = 0;
        }
    }

    private void push(final int scope) {
        if (size == stack.length) {
            stack = Arrays.copyOf(stack, size * 2);
        }
        stack[size++] = scope;
    }

    /** Starts a new line in pretty output, indented for the current nesting level. */
    private void newline() throws IOException {
        if (options.isPretty()) {
            put('\n');
            for (int i = 1; i < size; i++) {
                put(options.indent());
            }
        }
    }

    /** Writes what goes before a value, and checks that a value is allowed here. */
    private void beforeValue() throws IOException {
        switch (stack[size - 1]) {
            case EMPTY_DOCUMENT -> stack[size - 1] = NONEMPTY_DOCUMENT;
            case NONEMPTY_DOCUMENT -> throw new JsonException("JSON must have only one top-level value");
            case EMPTY_ARRAY -> {
                stack[size - 1] = NONEMPTY_ARRAY;
                newline();
            }
            case NONEMPTY_ARRAY -> {
                put(',');
                newline();
            }
            case DANGLING_NAME -> {
                put(options.isPretty() ? ": " : ":");
                stack[size - 1] = NONEMPTY_OBJECT;
            }
            default -> throw new JsonException("expected a name in a JSON object, not a value");
        }
    }

    private JsonOutputStream open(final int scope, final char bracket) throws JsonException {
        try {
            beforeValue();
            push(scope);
            put(bracket);
            return this;
        } catch (IOException e) {
            throw fail(e);
        }
    }

    private JsonOutputStream close(final int empty, final int nonEmpty, final char bracket) throws JsonException {
        final int scope = stack[size - 1];
        if (scope != empty && scope != nonEmpty) {
            throw new JsonException(scope == DANGLING_NAME ? "missing value after a name" : "cannot close " + bracket + " here");
        }
        try {
            size--;
            if (scope == nonEmpty) {
                newline();
            }
            put(bracket);
            return this;
        } catch (IOException e) {
            throw fail(e);
        }
    }

    @Override
    public JsonOutputStream beginObject() throws JsonException {
        return open(EMPTY_OBJECT, '{');
    }

    @Override
    public JsonOutputStream endObject() throws JsonException {
        return close(EMPTY_OBJECT, NONEMPTY_OBJECT, '}');
    }

    @Override
    public JsonOutputStream beginArray() throws JsonException {
        return open(EMPTY_ARRAY, '[');
    }

    @Override
    public JsonOutputStream endArray() throws JsonException {
        return close(EMPTY_ARRAY, NONEMPTY_ARRAY, ']');
    }

    @Override
    public JsonOutputStream key(final String key) throws JsonException {
        if (key == null) {
            throw new JsonException("a name in a JSON object cannot be null");
        }
        final int scope = stack[size - 1];
        if (scope != EMPTY_OBJECT && scope != NONEMPTY_OBJECT) {
            throw new JsonException("a name is only allowed in a JSON object, before a value");
        }
        try {
            if (scope == NONEMPTY_OBJECT) {
                put(',');
            }
            newline();
            stack[size - 1] = DANGLING_NAME;
            string(key);
            return this;
        } catch (IOException e) {
            throw fail(e);
        }
    }

    /** Writes a string in quotes, escaped like Gson does it. */
    private void string(final String value) throws IOException {
        put('"');
        int start = 0;
        final int length = value.length();
        for (int i = 0; i < length; i++) {
            final char c = value.charAt(i);
            final String escape;
            if (c < 0x80) {
                escape = ASCII_ESCAPES[c];
            } else if (c == ' ') {
                escape = "\\u2028"; // JavaScript does not allow these two unescaped in string literals
            } else if (c == ' ') {
                escape = "\\u2029";
            } else {
                escape = null;
            }
            if (escape != null) {
                put(value, start, i - start);
                put(escape);
                start = i + 1;
            }
        }
        put(value, start, length - start);
        put('"');
    }

    /** Writes a value that is already valid JSON text. */
    private JsonOutputStream literal(final String text) throws JsonException {
        try {
            beforeValue();
            put(text);
            return this;
        } catch (IOException e) {
            throw fail(e);
        }
    }

    @Override
    public JsonOutputStream nullValue() throws JsonException {
        return literal("null");
    }

    @Override
    public JsonOutputStream value(final String value) throws JsonException {
        if (value == null) {
            return nullValue();
        }
        try {
            beforeValue();
            string(value);
            return this;
        } catch (IOException e) {
            throw fail(e);
        }
    }

    @Override
    public JsonOutputStream value(final long value) throws JsonException {
        return literal(Long.toString(value));
    }

    @Override
    public JsonOutputStream value(final double value) throws JsonException {
        if (!Double.isFinite(value)) {
            return nonFinite(value);
        }
        return literal(Double.toString(value));
    }

    @Override
    public JsonOutputStream value(final Number value) throws JsonException {
        if (value == null) {
            return nullValue();
        }
        if ((value instanceof Double d && !Double.isFinite(d)) || (value instanceof Float f && !Float.isFinite(f))) {
            return nonFinite(value);
        }
        // like Gson, write what toString() says, e.g. "1.5" for a Float instead of its value as a double
        final String text = value.toString();
        if (!TRUSTED_NUMBERS.contains(value.getClass()) && !VALID_JSON_NUMBER.matcher(text).matches()) {
            throw new JsonException("not a valid JSON number: " + text + " (" + value.getClass().getName() + ")");
        }
        return literal(text);
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

    @Override
    public JsonOutputStream value(final boolean value) throws JsonException {
        return literal(value ? "true" : "false");
    }

    @Override
    public JsonOutputStream value(final LocalDateTime value) throws JsonException {
        return value(DateTimeUtils.toJsonZulu(value));
    }

    @Override
    public JsonOutputStream value(final Date value) throws JsonException {
        return value(DateTimeUtils.toJsonZulu(DateTimeUtils.convert(value)));
    }

    @Override
    public void flush() throws JsonException {
        try {
            drain();
            out.flush();
        } catch (IOException e) {
            throw fail(e);
        }
    }

    /**
     * Writes what is buffered, and closes the target.
     * @throws JsonException if that fails, or if an array or object has not been closed
     */
    @Override
    public void close() throws JsonException {
        if (closed) {
            return;
        }
        closed = true;
        try (out) {
            drain();
        } catch (IOException e) {
            throw fail(e);
        }
        if (size > 1) {
            throw new JsonException("incomplete JSON document");
        }
    }

}
