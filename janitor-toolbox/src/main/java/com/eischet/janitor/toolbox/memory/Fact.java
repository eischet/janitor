package com.eischet.janitor.toolbox.memory;

/**
 * A value together with a timestamp, as stored by {@link Memory}.
 * @param <T> the type of the value
 */
public class Fact<T> {

    private final T value;
    private final long timestamp;

    Fact(final T value, final long timestamp) {
        this.value = value;
        this.timestamp = timestamp;
    }

    /**
     * @return the value
     */
    public T getValue() {
        return value;
    }

    /**
     * @return the timestamp, in milliseconds since the epoch
     */
    public long getTimestamp() {
        return timestamp;
    }
}
