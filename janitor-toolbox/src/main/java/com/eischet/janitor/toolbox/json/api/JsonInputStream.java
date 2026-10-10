package com.eischet.janitor.toolbox.json.api;

/** A pull-style reader for JSON documents. */
public interface JsonInputStream {
    /**
     * Consumes the start of an array.
     * @throws JsonException if the next token is not the start of an array
     */
    void beginArray() throws JsonException;

    /**
     * Consumes the end of an array.
     * @throws JsonException if the next token is not the end of an array
     */
    void endArray() throws JsonException;

    /**
     * Consumes the start of an object.
     * @throws JsonException if the next token is not the start of an object
     */
    void beginObject() throws JsonException;

    /**
     * Consumes the end of an object.
     * @throws JsonException if the next token is not the end of an object
     */
    void endObject() throws JsonException;

    /**
     * @return true if the current array or object has more elements
     * @throws JsonException on JSON errors
     */
    boolean hasNext() throws JsonException;

    /**
     * @return the type of the next token, without consuming it
     * @throws JsonException on JSON errors
     */
    JsonTokenType peek() throws JsonException;

    /**
     * Consumes the name of the next property of an object.
     * @return the name
     * @throws JsonException on JSON errors
     */
    String nextKey() throws JsonException;

    /**
     * Consumes the next token as a string.
     * @return the string
     * @throws JsonException if the next token is not a string
     */
    String nextString() throws JsonException;

    /**
     * Consumes the next token as a boolean.
     * @return the value
     * @throws JsonException if the next token is not a boolean
     */
    boolean nextBoolean() throws JsonException;

    /**
     * Consumes the next token, which must be null.
     * @throws JsonException if the next token is not null
     */
    void nextNull() throws JsonException;

    /**
     * Consumes the next token, which must be null.
     * @return null
     * @throws JsonException if the next token is not null
     */
    default Object nextNullObject() throws JsonException {
        nextNull();
        return null;
    }

    /**
     * Consumes the next token as a double.
     * @return the value
     * @throws JsonException if the next token is not a number
     */
    double nextDouble() throws JsonException;

    /**
     * Consumes the next token as a long.
     * @return the value
     * @throws JsonException if the next token is not a number
     */
    long nextLong() throws JsonException;

    /**
     * Consumes the next token as an int.
     * @return the value
     * @throws JsonException if the next token is not a number
     */
    int nextInt() throws JsonException;

    /**
     * Closes the reader.
     * @throws JsonException on errors
     */
    void close() throws JsonException;

    /**
     * Skips the next value, including all of its children.
     * @throws JsonException on JSON errors
     */
    void skipValue() throws JsonException;

    /**
     * @return the path to the current position in the document, for error messages
     */
    String getPath();
}
