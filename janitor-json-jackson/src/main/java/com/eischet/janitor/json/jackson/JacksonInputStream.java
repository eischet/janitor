// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.json.jackson;

import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonInputStream;
import com.eischet.janitor.toolbox.json.api.JsonTokenType;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * JsonInputStream implementation using Jackson's {@link JsonParser}.
 * <p>
 * Jackson's parser always moves to the next token, while {@link JsonInputStream} can look at a token before consuming it.
 * The parser's current token is therefore treated as "peeked" until one of the next... methods consumes it.
 * Numbers are converted the way Gson does it, e.g. nextLong() accepts "1.0" and nextString() accepts a number.
 * </p>
 */
public class JacksonInputStream implements JsonInputStream {

    private static final int BYTE_ORDER_MARK = 0xFEFF;

    private final JsonParser parser;
    private final String source;
    private final boolean lenient;
    /** True if the parser's current token has been consumed, so that the next one must be read. */
    private boolean consumed = true;
    /** The number of arrays and objects that have been opened, but not closed. */
    private int depth = 0;
    private boolean rootValueSeen = false;

    /**
     * Constructor.
     * @param parser a Jackson parser
     * @param source the JSON text, for error messages, or null if unknown
     * @param lenient true if the parser is configured for {@link com.eischet.janitor.toolbox.json.api.JsonReadMode#LENIENT}
     */
    JacksonInputStream(final JsonParser parser, final String source, final boolean lenient) {
        this.parser = parser;
        this.source = source;
        this.lenient = lenient;
    }

    /**
     * Skips a leading byte order mark.
     * @param reader a reader
     * @return a reader that starts after the byte order mark, if there was one
     */
    static Reader skipBom(final Reader reader) {
        final PushbackReader pushback = new PushbackReader(reader, 1);
        try {
            final int first = pushback.read();
            if (first != -1 && first != BYTE_ORDER_MARK) {
                pushback.unread(first);
            }
        } catch (IOException e) {
            throw new JsonException("error reading JSON", e);
        }
        return pushback;
    }

    private JsonException fail(final Exception e) {
        return fail(e.getMessage(), e);
    }

    private JsonException fail(final String message, final Exception cause) {
        final String text = "error parsing JSON at " + getPath() + ": " + message + (source == null ? "" : "; JSON: " + source);
        return cause == null ? new JsonException(text) : new JsonException(text, cause);
    }

    /**
     * Reads the next token, unless the current one has not been consumed yet.
     * @return the current token, or null at the end of the document
     */
    private JsonToken token() throws JsonException {
        if (!consumed) {
            return parser.currentToken();
        }
        final JsonToken token;
        try {
            token = parser.nextToken();
        } catch (IOException | RuntimeException e) {
            throw fail(e);
        }
        if (token == null) {
            if (!rootValueSeen) {
                throw fail("empty document", null);
            }
        } else if (depth == 0 && token != JsonToken.FIELD_NAME) {
            // Jackson reads a sequence of documents; JSON, even LENIENT, has exactly one top-level value
            if (rootValueSeen) {
                throw fail("more than one top-level value", null);
            }
            rootValueSeen = true;
        }
        if (token == JsonToken.START_ARRAY || token == JsonToken.START_OBJECT) {
            depth++;
        } else if (token == JsonToken.END_ARRAY || token == JsonToken.END_OBJECT) {
            depth--;
        }
        consumed = false;
        return token;
    }

    /**
     * Consumes the current token, which must be of the expected type.
     * @param expected the expected token
     * @param description what is expected, for the error message
     */
    private void consume(final JsonToken expected, final String description) throws JsonException {
        final JsonToken token = token();
        if (token != expected) {
            throw unexpected(token, description);
        }
        consumed = true;
    }

    private JsonException unexpected(final JsonToken token, final String description) {
        return fail("expected " + description + " but was " + convert(token), null);
    }

    @Override
    public void beginArray() throws JsonException {
        consume(JsonToken.START_ARRAY, "BEGIN_ARRAY");
    }

    @Override
    public void endArray() throws JsonException {
        consume(JsonToken.END_ARRAY, "END_ARRAY");
    }

    @Override
    public void beginObject() throws JsonException {
        consume(JsonToken.START_OBJECT, "BEGIN_OBJECT");
    }

    @Override
    public void endObject() throws JsonException {
        consume(JsonToken.END_OBJECT, "END_OBJECT");
    }

    @Override
    public boolean hasNext() throws JsonException {
        final JsonToken token = token();
        return token != null && token != JsonToken.END_ARRAY && token != JsonToken.END_OBJECT;
    }

    @Override
    public JsonTokenType peek() throws JsonException {
        return convert(token());
    }

    private static JsonTokenType convert(final JsonToken token) {
        if (token == null) {
            return JsonTokenType.END_DOCUMENT;
        }
        return switch (token) {
            case START_ARRAY -> JsonTokenType.BEGIN_ARRAY;
            case END_ARRAY -> JsonTokenType.END_ARRAY;
            case START_OBJECT -> JsonTokenType.BEGIN_OBJECT;
            case END_OBJECT -> JsonTokenType.END_OBJECT;
            case FIELD_NAME -> JsonTokenType.NAME;
            case VALUE_STRING -> JsonTokenType.STRING;
            case VALUE_NUMBER_INT, VALUE_NUMBER_FLOAT -> JsonTokenType.NUMBER;
            case VALUE_TRUE, VALUE_FALSE -> JsonTokenType.BOOLEAN;
            case VALUE_NULL -> JsonTokenType.NULL;
            // not produced by the JSON parser
            case NOT_AVAILABLE, VALUE_EMBEDDED_OBJECT -> throw new JsonException("unexpected Jackson token " + token);
        };
    }

    @Override
    public String nextKey() throws JsonException {
        final JsonToken token = token();
        if (token != JsonToken.FIELD_NAME) {
            throw unexpected(token, "a name");
        }
        try {
            final String name = parser.currentName();
            consumed = true;
            return name;
        } catch (IOException | RuntimeException e) {
            throw fail(e);
        }
    }

    @Override
    public String nextString() throws JsonException {
        final JsonToken token = token();
        if (token != JsonToken.VALUE_STRING && token != JsonToken.VALUE_NUMBER_INT && token != JsonToken.VALUE_NUMBER_FLOAT) {
            throw unexpected(token, "a string");
        }
        try {
            final String text = parser.getText();
            consumed = true;
            return text;
        } catch (IOException | RuntimeException e) {
            throw fail(e);
        }
    }

    @Override
    public boolean nextBoolean() throws JsonException {
        final JsonToken token = token();
        if (token != JsonToken.VALUE_TRUE && token != JsonToken.VALUE_FALSE) {
            throw unexpected(token, "a boolean");
        }
        consumed = true;
        return token == JsonToken.VALUE_TRUE;
    }

    @Override
    public void nextNull() throws JsonException {
        consume(JsonToken.VALUE_NULL, "null");
    }

    @Override
    public double nextDouble() throws JsonException {
        final JsonToken token = token();
        try {
            final double value;
            if (token == JsonToken.VALUE_NUMBER_INT) {
                final double d = parser.getDoubleValue();
                value = d == 0 && parser.getText().startsWith("-") ? -0.0 : d; // Jackson reads the integer -0 as 0; Gson keeps the sign
            } else if (token == JsonToken.VALUE_NUMBER_FLOAT) {
                value = parser.getDoubleValue();
            } else if (token == JsonToken.VALUE_STRING) {
                value = Double.parseDouble(parser.getText());
            } else {
                throw unexpected(token, "a double");
            }
            if (!lenient && !Double.isFinite(value)) {
                throw fail("JSON forbids NaN and infinities: " + value, null);
            }
            consumed = true;
            return value;
        } catch (IOException | NumberFormatException e) {
            throw fail(e);
        }
    }

    @Override
    public long nextLong() throws JsonException {
        final JsonToken token = token();
        try {
            final long value;
            if (token == JsonToken.VALUE_NUMBER_INT) {
                value = parser.getLongValue();
            } else if (token == JsonToken.VALUE_NUMBER_FLOAT || token == JsonToken.VALUE_STRING) {
                value = exactLong(parser.getText());
            } else {
                throw unexpected(token, "a long");
            }
            consumed = true;
            return value;
        } catch (IOException | NumberFormatException e) {
            throw fail(e);
        }
    }

    @Override
    public int nextInt() throws JsonException {
        final JsonToken token = token();
        try {
            final long value;
            if (token == JsonToken.VALUE_NUMBER_INT) {
                value = parser.getIntValue();
            } else if (token == JsonToken.VALUE_NUMBER_FLOAT || token == JsonToken.VALUE_STRING) {
                value = exactLong(parser.getText());
            } else {
                throw unexpected(token, "an int");
            }
            if (value != (int) value) {
                throw new NumberFormatException("not an int: " + value);
            }
            consumed = true;
            return (int) value;
        } catch (IOException | NumberFormatException e) {
            throw fail(e);
        }
    }

    /**
     * Converts text to a long, like Gson: integers directly, other numbers only if they have no fraction, e.g. "1.0" or "1e3".
     * @param text the text of a number or string
     * @return the value
     * @throws NumberFormatException if the text is not a number, or not a whole one
     */
    private static long exactLong(final String text) {
        try {
            return Long.parseLong(text);
        } catch (NumberFormatException ignored) {
            // try as a double below
        }
        final double d = Double.parseDouble(text);
        final long l = (long) d;
        if (l != d) {
            throw new NumberFormatException("not a whole number: " + text);
        }
        return l;
    }

    @Override
    public void close() throws JsonException {
        try {
            parser.close();
        } catch (IOException e) {
            throw fail(e);
        }
    }

    @Override
    public void skipValue() throws JsonException {
        final JsonToken token = token();
        if (token == null || token == JsonToken.END_ARRAY || token == JsonToken.END_OBJECT) {
            throw unexpected(token, "a value");
        }
        try {
            if (token == JsonToken.START_ARRAY || token == JsonToken.START_OBJECT) {
                parser.skipChildren();
                depth--;
            }
        } catch (IOException | RuntimeException e) {
            throw fail(e);
        }
        consumed = true;
    }

    /**
     * Returns the path to the current position, in the style of Gson, e.g. {@code $.items[2].name}.
     * @return the path
     */
    @Override
    public String getPath() {
        final Deque<String> parts = new ArrayDeque<>();
        for (JsonStreamContext context = parser.getParsingContext(); context != null && !context.inRoot(); context = context.getParent()) {
            if (context.inArray()) {
                parts.push("[" + Math.max(context.getCurrentIndex(), 0) + "]");
            } else if (context.getCurrentName() != null) {
                parts.push("." + context.getCurrentName());
            }
        }
        return "$" + String.join("", parts);
    }

}
