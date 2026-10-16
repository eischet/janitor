// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.toolbox.json.impl;

import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonInputStream;
import com.eischet.janitor.toolbox.json.api.JsonReadMode;
import com.eischet.janitor.toolbox.json.api.JsonTokenType;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;

/**
 * JsonInputStream implementation without any library: a hand-written pull parser.
 * <p>
 * It follows {@link JsonReadMode} exactly. Numbers are converted the way Gson, which Janitor used before, does it, e.g.
 * nextLong() accepts "1.0" and nextString() accepts a number. Arrays and objects may be nested {@value #MAX_DEPTH} levels
 * deep, numbers may have {@value #MAX_NUMBER_LENGTH} characters and strings {@value #MAX_STRING_LENGTH}.
 * </p>
 */
public class NativeInputStream implements JsonInputStream {

    /** The maximum nesting depth of arrays and objects. */
    public static final int MAX_DEPTH = 1000;
    /** The maximum length of a number, in characters. */
    public static final int MAX_NUMBER_LENGTH = 1000;
    /** The maximum length of a string or name, in characters. */
    public static final int MAX_STRING_LENGTH = 20_000_000;

    private static final int BYTE_ORDER_MARK = 0xFEFF;
    private static final int EOF = -1;

    // what the parser expects next, per nesting level
    private static final int EMPTY_DOCUMENT = 0;
    private static final int NONEMPTY_DOCUMENT = 1;
    private static final int EMPTY_ARRAY = 2;
    private static final int NONEMPTY_ARRAY = 3;
    private static final int EMPTY_OBJECT = 4;
    private static final int NONEMPTY_OBJECT = 5;
    /** In an object, after a name, before its value. */
    private static final int DANGLING_NAME = 6;

    private final Reader in;
    private final boolean lenient;
    private final char[] buffer;
    private int pos = 0;
    private int limit;
    /** The number of characters that were in the buffer before the current content, for error messages. */
    private long offset = 0;
    private int line = 1;
    private long lineStart = 0;
    private boolean closed = false;

    private int[] stack = new int[32];
    private String[] pathNames = new String[32];
    private int[] pathIndices = new int[32];
    private int size = 1;

    /** The token that has been read, but not consumed yet, or null if the next token has not been read yet. */
    private JsonTokenType peeked = null;
    /** The text of the peeked name, string or number. */
    private String peekedText;
    /** The value of the peeked boolean. */
    private boolean peekedBoolean;
    private final StringBuilder sb = new StringBuilder();

    /**
     * Creates a stream that reads a string.
     * @param json the JSON text
     * @param mode how strictly to parse
     */
    public NativeInputStream(final String json, final JsonReadMode mode) {
        this(null, json.toCharArray(), mode);
        limit = buffer.length;
    }

    /**
     * Creates a stream that reads from a reader; closing the stream closes the reader.
     * @param source the source
     * @param mode how strictly to parse
     */
    public NativeInputStream(final Reader source, final JsonReadMode mode) {
        this(source, new char[8192], mode);
        limit = 0;
    }

    private NativeInputStream(final Reader in, final char[] buffer, final JsonReadMode mode) {
        this.in = in;
        this.buffer = buffer;
        this.lenient = mode == JsonReadMode.LENIENT;
        stack[0] = EMPTY_DOCUMENT;
    }

    // ---- errors

    private JsonException fail(final String message) {
        return fail(message, null);
    }

    private JsonException fail(final String message, final Exception cause) {
        final long position = offset + pos;
        return new JsonException("error parsing JSON at " + getPath() + " (line " + line + ", column " + (position - lineStart + 1) + "): " + message, cause);
    }

    private JsonException unexpected(final JsonTokenType token, final String expected) {
        return fail("expected " + expected + " but was " + token);
    }

    private static String describe(final int c) {
        return c == EOF ? "the end of the document" : "'" + (char) c + "'";
    }

    // ---- characters

    /** Makes sure that the buffer has at least one more character, unless the input has ended. */
    private boolean fill() {
        if (pos < limit) {
            return true;
        }
        if (in == null) {
            return false;
        }
        try {
            offset += limit;
            pos = 0;
            limit = 0;
            int n;
            do {
                n = in.read(buffer, 0, buffer.length);
            } while (n == 0);
            if (n > 0) {
                limit = n;
                return true;
            }
            return false;
        } catch (IOException e) {
            throw fail("cannot read input", e);
        }
    }

    /** @return the next character without consuming it, or {@link #EOF} */
    private int peekChar() {
        return fill() ? buffer[pos] : EOF;
    }

    /** @return the next character, or {@link #EOF} */
    private int readChar() {
        return fill() ? buffer[pos++] : EOF;
    }

    /** Skips whitespace and, in LENIENT mode, comments. @return the next character without consuming it, or {@link #EOF} */
    private int nextNonWhitespace() {
        while (true) {
            final int c = peekChar();
            switch (c) {
                case '\n' -> {
                    pos++;
                    line++;
                    lineStart = offset + pos;
                }
                case ' ', '\t', '\r' -> pos++;
                case '/' -> {
                    if (!lenient) {
                        return c;
                    }
                    pos++;
                    skipComment();
                }
                default -> {
                    return c;
                }
            }
        }
    }

    private void skipComment() {
        final int c = readChar();
        if (c == '/') {
            while (true) {
                final int d = peekChar();
                if (d == EOF || d == '\n') {
                    return; // the newline is counted by the caller
                }
                pos++;
            }
        } else if (c == '*') {
            boolean star = false;
            while (true) {
                final int d = readChar();
                if (d == EOF) {
                    throw fail("unterminated comment");
                } else if (d == '/' && star) {
                    return;
                } else if (d == '\n') {
                    line++;
                    lineStart = offset + pos;
                }
                star = d == '*';
            }
        } else {
            throw fail("expected a comment after '/' but found " + describe(c));
        }
    }

    // ---- tokens

    private JsonTokenType token() throws JsonException {
        if (peeked == null) {
            if (closed) {
                throw fail("the stream is closed");
            }
            peeked = readToken();
        }
        return peeked;
    }

    private JsonTokenType readToken() {
        final int scope = stack[size - 1];
        switch (scope) {
            case EMPTY_DOCUMENT -> {
                stack[size - 1] = NONEMPTY_DOCUMENT;
                if (lenient && peekChar() == BYTE_ORDER_MARK) {
                    pos++;
                }
                if (nextNonWhitespace() == EOF) {
                    throw fail("empty document");
                }
                return readValue();
            }
            case NONEMPTY_DOCUMENT -> {
                final int c = nextNonWhitespace();
                if (c != EOF) {
                    throw fail("more than one top-level value, or unexpected " + describe(c));
                }
                return JsonTokenType.END_DOCUMENT;
            }
            case EMPTY_ARRAY, NONEMPTY_ARRAY -> {
                stack[size - 1] = NONEMPTY_ARRAY;
                int c = nextNonWhitespace();
                if (c == ']') {
                    pos++;
                    return JsonTokenType.END_ARRAY;
                }
                if (scope == NONEMPTY_ARRAY) {
                    if (c != ',') {
                        throw fail("expected ',' or ']' but found " + describe(c));
                    }
                    pos++;
                    c = nextNonWhitespace();
                    if (lenient && c == ']') {
                        pos++;
                        return JsonTokenType.END_ARRAY;
                    }
                }
                return readValue();
            }
            case EMPTY_OBJECT, NONEMPTY_OBJECT -> {
                int c = nextNonWhitespace();
                if (c == '}') {
                    pos++;
                    return JsonTokenType.END_OBJECT;
                }
                if (scope == NONEMPTY_OBJECT) {
                    if (c != ',') {
                        throw fail("expected ',' or '}' but found " + describe(c));
                    }
                    pos++;
                    c = nextNonWhitespace();
                    if (lenient && c == '}') {
                        pos++;
                        return JsonTokenType.END_OBJECT;
                    }
                }
                peekedText = readName(c);
                stack[size - 1] = DANGLING_NAME;
                return JsonTokenType.NAME;
            }
            case DANGLING_NAME -> {
                final int c = nextNonWhitespace();
                if (c != ':') {
                    throw fail("expected ':' but found " + describe(c));
                }
                pos++;
                stack[size - 1] = NONEMPTY_OBJECT;
                nextNonWhitespace();
                return readValue();
            }
            default -> throw new IllegalStateException("unknown scope " + scope);
        }
    }

    private String readName(final int c) {
        if (c == '"') {
            pos++;
            return readString('"');
        }
        if (lenient && c == '\'') {
            pos++;
            return readString('\'');
        }
        if (lenient && c != EOF && Character.isJavaIdentifierStart(c)) {
            sb.setLength(0);
            while (true) {
                final int d = peekChar();
                if (d == EOF || !Character.isJavaIdentifierPart(d)) {
                    return sb.toString();
                }
                sb.append((char) d);
                pos++;
            }
        }
        throw fail("expected a name but found " + describe(c));
    }

    /** Reads a value, whose first character has been peeked already. */
    private JsonTokenType readValue() {
        final int c = peekChar();
        switch (c) {
            case '[' -> {
                pos++;
                return JsonTokenType.BEGIN_ARRAY;
            }
            case '{' -> {
                pos++;
                return JsonTokenType.BEGIN_OBJECT;
            }
            case '"' -> {
                pos++;
                peekedText = readString('"');
                return JsonTokenType.STRING;
            }
            case 't' -> {
                keyword("true");
                peekedBoolean = true;
                return JsonTokenType.BOOLEAN;
            }
            case 'f' -> {
                keyword("false");
                peekedBoolean = false;
                return JsonTokenType.BOOLEAN;
            }
            case 'n' -> {
                keyword("null");
                return JsonTokenType.NULL;
            }
            default -> {
                if (c == '-' || (c >= '0' && c <= '9')) {
                    peekedText = readNumber();
                    return JsonTokenType.NUMBER;
                }
                if (lenient && c == '\'') {
                    pos++;
                    peekedText = readString('\'');
                    return JsonTokenType.STRING;
                }
                if (lenient && (c == 'N' || c == 'I')) {
                    peekedText = c == 'N' ? "NaN" : "Infinity";
                    keyword(peekedText);
                    return JsonTokenType.NUMBER;
                }
                throw fail("expected a value but found " + describe(c));
            }
        }
    }

    private void keyword(final String keyword) {
        for (int i = 0; i < keyword.length(); i++) {
            final int c = readChar();
            if (c != keyword.charAt(i)) {
                throw fail("expected '" + keyword + "' but found " + describe(c));
            }
        }
    }

    private void appendDigits() {
        int c = peekChar();
        if (c < '0' || c > '9') {
            throw fail("expected a digit but found " + describe(c));
        }
        do {
            sb.append((char) c);
            pos++;
            if (sb.length() > MAX_NUMBER_LENGTH) {
                throw fail("number longer than " + MAX_NUMBER_LENGTH + " characters");
            }
            c = peekChar();
        } while (c >= '0' && c <= '9');
    }

    /** Reads a number as specified by RFC 8259, and -Infinity in LENIENT mode. */
    private String readNumber() {
        sb.setLength(0);
        if (peekChar() == '-') {
            sb.append('-');
            pos++;
            if (lenient && peekChar() == 'I') {
                keyword("Infinity");
                return "-Infinity";
            }
        }
        if (peekChar() == '0') {
            sb.append('0');
            pos++;
            final int c = peekChar();
            if (c >= '0' && c <= '9') {
                throw fail("numbers must not have leading zeros");
            }
        } else {
            appendDigits();
        }
        if (peekChar() == '.') {
            sb.append('.');
            pos++;
            appendDigits();
        }
        final int e = peekChar();
        if (e == 'e' || e == 'E') {
            sb.append((char) e);
            pos++;
            final int sign = peekChar();
            if (sign == '+' || sign == '-') {
                sb.append((char) sign);
                pos++;
            }
            appendDigits();
        }
        return sb.toString();
    }

    /** Reads a string whose opening quote has been consumed. */
    private String readString(final char quote) {
        sb.setLength(0);
        while (true) {
            if (!fill()) {
                throw fail("unterminated string");
            }
            // copy a run of plain characters at once
            final int start = pos;
            char c = 0;
            while (pos < limit) {
                c = buffer[pos];
                if (c == quote || c == '\\' || c < 0x20) {
                    break;
                }
                pos++;
            }
            if (pos < limit && c == quote && sb.isEmpty() && pos - start <= MAX_STRING_LENGTH) {
                // the usual case: the whole string is in the buffer and has no escapes, so take it from there directly
                return new String(buffer, start, pos++ - start);
            }
            sb.append(buffer, start, pos - start);
            if (sb.length() > MAX_STRING_LENGTH) {
                throw fail("string longer than " + MAX_STRING_LENGTH + " characters");
            }
            if (pos == limit) {
                continue;
            }
            pos++;
            if (c == quote) {
                return sb.toString();
            }
            if (c != '\\') {
                pos--;
                throw fail("unescaped control character U+" + String.format("%04X", (int) c) + " in a string");
            }
            sb.append(readEscape());
        }
    }

    private char readEscape() {
        final int c = readChar();
        return switch (c) {
            case '"', '\\', '/' -> (char) c;
            case 'b' -> '\b';
            case 'f' -> '\f';
            case 'n' -> '\n';
            case 'r' -> '\r';
            case 't' -> '\t';
            case 'u' -> {
                int value = 0;
                for (int i = 0; i < 4; i++) {
                    final int digit = Character.digit(readChar(), 16);
                    if (digit < 0) {
                        throw fail("invalid unicode escape");
                    }
                    value = value * 16 + digit;
                }
                yield (char) value;
            }
            case '\'' -> {
                if (!lenient) {
                    throw fail("invalid escape \\' in STRICT mode");
                }
                yield '\'';
            }
            default -> throw fail("invalid escape \\" + (c == EOF ? "" : (char) c));
        };
    }

    // ---- consuming tokens

    private void push(final int scope) {
        if (size > MAX_DEPTH) {
            throw fail("arrays and objects nested more than " + MAX_DEPTH + " levels deep");
        }
        if (size == stack.length) {
            stack = Arrays.copyOf(stack, size * 2);
            pathNames = Arrays.copyOf(pathNames, size * 2);
            pathIndices = Arrays.copyOf(pathIndices, size * 2);
        }
        stack[size] = scope;
        pathNames[size] = null;
        pathIndices[size] = 0;
        size++;
    }

    /** Marks the peeked token as consumed; a consumed value counts as one more element of its array. */
    private void consumed() {
        peeked = null;
        peekedText = null;
        pathIndices[size - 1]++;
    }

    private void consume(final JsonTokenType expected, final String description) throws JsonException {
        final JsonTokenType token = token();
        if (token != expected) {
            throw unexpected(token, description);
        }
        switch (token) {
            case BEGIN_ARRAY -> {
                peeked = null;
                push(EMPTY_ARRAY);
            }
            case BEGIN_OBJECT -> {
                peeked = null;
                push(EMPTY_OBJECT);
            }
            case END_ARRAY, END_OBJECT -> {
                size--;
                consumed();
            }
            default -> consumed();
        }
    }

    @Override
    public void beginArray() throws JsonException {
        consume(JsonTokenType.BEGIN_ARRAY, "BEGIN_ARRAY");
    }

    @Override
    public void endArray() throws JsonException {
        consume(JsonTokenType.END_ARRAY, "END_ARRAY");
    }

    @Override
    public void beginObject() throws JsonException {
        consume(JsonTokenType.BEGIN_OBJECT, "BEGIN_OBJECT");
    }

    @Override
    public void endObject() throws JsonException {
        consume(JsonTokenType.END_OBJECT, "END_OBJECT");
    }

    @Override
    public boolean hasNext() throws JsonException {
        final JsonTokenType token = token();
        return token != JsonTokenType.END_ARRAY && token != JsonTokenType.END_OBJECT && token != JsonTokenType.END_DOCUMENT;
    }

    @Override
    public JsonTokenType peek() throws JsonException {
        return token();
    }

    @Override
    public String nextKey() throws JsonException {
        final JsonTokenType token = token();
        if (token != JsonTokenType.NAME) {
            throw unexpected(token, "a name");
        }
        final String name = peekedText;
        pathNames[size - 1] = name;
        peeked = null;
        peekedText = null;
        return name;
    }

    @Override
    public String nextString() throws JsonException {
        final JsonTokenType token = token();
        if (token != JsonTokenType.STRING && token != JsonTokenType.NUMBER) {
            throw unexpected(token, "a string");
        }
        final String text = peekedText;
        consumed();
        return text;
    }

    @Override
    public boolean nextBoolean() throws JsonException {
        final JsonTokenType token = token();
        if (token != JsonTokenType.BOOLEAN) {
            throw unexpected(token, "a boolean");
        }
        final boolean value = peekedBoolean;
        consumed();
        return value;
    }

    @Override
    public void nextNull() throws JsonException {
        consume(JsonTokenType.NULL, "null");
    }

    @Override
    public double nextDouble() throws JsonException {
        final JsonTokenType token = token();
        if (token != JsonTokenType.NUMBER && token != JsonTokenType.STRING) {
            throw unexpected(token, "a double");
        }
        final double value;
        try {
            value = Double.parseDouble(peekedText);
        } catch (NumberFormatException e) {
            throw fail("not a number: " + peekedText, e);
        }
        if (!lenient && !Double.isFinite(value)) {
            throw fail("JSON forbids NaN and infinities: " + value);
        }
        consumed();
        return value;
    }

    @Override
    public long nextLong() throws JsonException {
        final JsonTokenType token = token();
        if (token != JsonTokenType.NUMBER && token != JsonTokenType.STRING) {
            throw unexpected(token, "a long");
        }
        final long value = exactLong(peekedText);
        consumed();
        return value;
    }

    @Override
    public int nextInt() throws JsonException {
        final JsonTokenType token = token();
        if (token != JsonTokenType.NUMBER && token != JsonTokenType.STRING) {
            throw unexpected(token, "an int");
        }
        final long value = exactLong(peekedText);
        if (value != (int) value) {
            throw fail("not an int: " + peekedText);
        }
        consumed();
        return (int) value;
    }

    /**
     * Converts text to a long, like Gson: integers directly, other numbers only if they have no fraction, e.g. "1.0" or "1e3".
     * @param text the text of a number or string
     * @return the value
     * @throws JsonException if the text is not a number, or not a whole one
     */
    private long exactLong(final String text) throws JsonException {
        try {
            return Long.parseLong(text);
        } catch (NumberFormatException ignored) {
            // try as a double below
        }
        final double d;
        try {
            d = Double.parseDouble(text);
        } catch (NumberFormatException e) {
            throw fail("not a number: " + text, e);
        }
        final long l = (long) d;
        if (l != d) {
            throw fail("not a whole number in the range of long: " + text);
        }
        return l;
    }

    @Override
    public void skipValue() throws JsonException {
        final JsonTokenType token = token();
        switch (token) {
            case NAME -> nextKey(); // like Gson, skips only the name
            case BEGIN_ARRAY, BEGIN_OBJECT -> {
                final int depth = size;
                consume(token, token.name());
                while (size > depth) {
                    final JsonTokenType next = token();
                    switch (next) {
                        case BEGIN_ARRAY, BEGIN_OBJECT, END_ARRAY, END_OBJECT -> consume(next, next.name());
                        case NAME -> nextKey();
                        default -> consumed();
                    }
                }
            }
            case END_ARRAY, END_OBJECT, END_DOCUMENT -> throw unexpected(token, "a value");
            default -> consumed();
        }
    }

    @Override
    public void close() throws JsonException {
        closed = true;
        peeked = null;
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                throw new JsonException("error closing JSON reader", e);
            }
        }
    }

    /**
     * Returns the path to the current position, in the style of Gson, e.g. {@code $.items[2].name}.
     * @return the path
     */
    @Override
    public String getPath() {
        final StringBuilder path = new StringBuilder("$");
        for (int i = 1; i < size; i++) {
            if (stack[i] == EMPTY_ARRAY || stack[i] == NONEMPTY_ARRAY) {
                path.append('[').append(pathIndices[i]).append(']');
            } else if (pathNames[i] != null) {
                path.append('.').append(pathNames[i]);
            }
        }
        return path.toString();
    }

}
