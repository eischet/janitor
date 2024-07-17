package com.eischet.janitor.toolbox.memory;

public class Fact<T> {

    private final T value;
    private final long timestamp;

    Fact(final T value, final long timestamp) {
        this.value = value;
        this.timestamp = timestamp;
    }

    public T getValue() {
        return value;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
