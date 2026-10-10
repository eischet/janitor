package com.eischet.janitor.toolbox.memory;

import java.util.function.BiFunction;

/**
 * Tracks a value over time, choosing which one to keep each time a new value is seen, e.g. the highest value.
 * @param <T> the type of the value
 */
public class Tracker<T> {

    private final BiFunction<T, T, T> chooser;
    private T value;

    protected Tracker(final T initValue, final BiFunction<T, T, T> chooser) {
        this.value = initValue;
        this.chooser = chooser;
    }

    /**
     * @return the value that was chosen so far
     */
    public T getValue() {
        return value;
    }

    /**
     * Considers a new value.
     * @param value the new value
     */
    public void track(final T value) {
        this.value = chooser.apply(this.value, value);
    }


    /**
     * @param start the initial value
     * @return a tracker that keeps the highest value that it has seen
     */
    public static Tracker<Long> highestLong(final long start) {
        return new Tracker<>(start, Math::max);
    }

}
