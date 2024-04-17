package com.eischet.janitor.json.impl;

import com.eischet.janitor.api.json.api.JsonException;
import com.eischet.janitor.api.json.api.JsonInputStream;
import com.eischet.janitor.api.json.api.JsonTokenType;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class GsonInputStream implements JsonInputStream {

    private final JsonReader reader;
    private final String source;

    private GsonInputStream(final JsonReader reader, final String source) {
        this.reader = reader;
        this.source = source;
    }

    public static GsonInputStream lenient(final String source) {
        final StringReader sr = new StringReader(source);
        final JsonReader reader = new JsonReader(sr);
        reader.setLenient(true);
        return new GsonInputStream(reader, source);
    }

    public static JsonInputStream lenient(final Reader sr) {
        final JsonReader reader = new JsonReader(sr);
        reader.setLenient(true);
        return new GsonInputStream(reader, null);
    }

    private JsonException fail(final Exception e) {
        if (source == null) {
            return new JsonException("error parsing JSON", e);
        } else {
            return new JsonException("error parsing JSON: " + source, e);
        }
    }

    @Override
    public void beginArray() throws JsonException {
        try {
            reader.beginArray();
        } catch (RuntimeException | IOException e) {
            throw fail(e);
        }
    }

    @Override
    public void endArray() throws JsonException {
        try {
            reader.endArray();
        } catch (RuntimeException | IOException e) {
            throw fail(e);
        }
    }

    @Override
    public void beginObject() throws JsonException {
        try {
            reader.beginObject();
        } catch (RuntimeException | IOException e) {
            throw fail(e);
        }
    }

    @Override
    public void endObject() throws JsonException {
        try {
            reader.endObject();
        } catch (RuntimeException | IOException e) {
            throw fail(e);
        }
    }

    @Override
    public boolean hasNext() throws JsonException {
        try {
            return reader.hasNext();
        } catch (RuntimeException | IOException e) {
            throw fail(e);
        }
    }

    @Override
    public JsonTokenType peek() throws JsonException {
        try {
            final JsonToken gsonToken = reader.peek();
            return convert(gsonToken);
        } catch (RuntimeException | IOException e) {
            throw fail(e);
        }
    }

    private JsonTokenType convert(final JsonToken gsonToken) {
        return switch (gsonToken) {
            case BEGIN_ARRAY -> JsonTokenType.BEGIN_ARRAY;
            case END_ARRAY -> JsonTokenType.END_ARRAY;
            case BEGIN_OBJECT -> JsonTokenType.BEGIN_OBJECT;
            case END_OBJECT -> JsonTokenType.END_OBJECT;
            case NAME -> JsonTokenType.NAME;
            case STRING -> JsonTokenType.STRING;
            case NUMBER -> JsonTokenType.NUMBER;
            case BOOLEAN -> JsonTokenType.BOOLEAN;
            case NULL -> JsonTokenType.NULL;
            case END_DOCUMENT -> JsonTokenType.END_DOCUMENT;
        };
    }

    @Override
    public String nextName() throws JsonException {
        try {
            return reader.nextName();
        } catch (RuntimeException | IOException e) {
            throw fail(e);
        }
    }

    @Override
    public String nextString() throws JsonException {
        try {
            return reader.nextString();
        } catch (RuntimeException | IOException e) {
            throw fail(e);
        }
    }

    @Override
    public boolean nextBoolean() throws JsonException {
        try {
            return reader.nextBoolean();
        } catch (RuntimeException | IOException e) {
            throw fail(e);
        }
    }

    @Override
    public void nextNull() throws JsonException {
        try {
            reader.nextNull();
        } catch (RuntimeException | IOException e) {
            throw fail(e);
        }
    }

    @Override
    public double nextDouble() throws JsonException {
        try {
            return reader.nextDouble();
        } catch (RuntimeException | IOException e) {
            throw fail(e);
        }
    }

    @Override
    public long nextLong() throws JsonException {
        try {
            return reader.nextLong();
        } catch (RuntimeException | IOException e) {
            throw fail(e);
        }
    }

    @Override
    public int nextInt() throws JsonException {
        try {
            return reader.nextInt();
        } catch (RuntimeException | IOException e) {
            throw fail(e);
        }
    }

    @Override
    public void close() throws JsonException {
        try {
            reader.close();
        } catch (RuntimeException | IOException e) {
            throw fail(e);
        }
    }

    @Override
    public void skipValue() throws JsonException {
        try {
            reader.skipValue();
        } catch (RuntimeException | IOException e) {
            throw fail(e);
        }
    }

    @Override
    public String getPath() {
        return reader.getPath();
    }

}
