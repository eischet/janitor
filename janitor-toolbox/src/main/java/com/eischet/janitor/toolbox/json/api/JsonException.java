package com.eischet.janitor.toolbox.json.api;

/** Thrown when JSON cannot be read or written. */
public class JsonException extends RuntimeException {
    public JsonException() {
    }

    public JsonException(final String message) {
        super(message);
    }

    public JsonException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
