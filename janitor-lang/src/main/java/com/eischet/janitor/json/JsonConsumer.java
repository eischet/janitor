package com.eischet.janitor.json;

public interface JsonConsumer {
    void beginArray() throws JsonException;

    void endArray() throws JsonException;

    void beginObject() throws JsonException;

    void endObject() throws JsonException;

    boolean hasNext() throws JsonException;

    JsonTokenType peek() throws JsonException;

    String nextName() throws JsonException;

    String nextString() throws JsonException;

    boolean nextBoolean() throws JsonException;

    void nextNull() throws JsonException;

    default Object nextNullObject() throws JsonException {
        nextNull();
        return null;
    }

    double nextDouble() throws JsonException;

    long nextLong() throws JsonException;

    int nextInt() throws JsonException;

    void close() throws JsonException;

    void skipValue() throws JsonException;

    String getPath();
}
