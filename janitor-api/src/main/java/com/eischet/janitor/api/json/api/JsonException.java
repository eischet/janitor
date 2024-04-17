package com.eischet.janitor.api.json.api;

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
