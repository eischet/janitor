package com.eischet.janitor.api.json;

/**
 * Exception thrown when there is a problem with JSON input/output.
 */
public class JsonException extends Exception {
    public JsonException() {
    }

    public JsonException(final String message) {
        super(message);
    }

    public JsonException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
