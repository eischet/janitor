package com.eischet.janitor.toolbox.memory;

import java.util.function.BiFunction;

public class Tracker<T> {

    private final BiFunction<T, T, T> chooser;
    private T value;

    protected Tracker(final T initValue, final BiFunction<T, T, T> chooser) {
        this.value = initValue;
        this.chooser = chooser;
    }

    public T getValue() {
        return value;
    }

    public void track(final T value) {
        this.value = chooser.apply(this.value, value);
    }


    public static Tracker<Long> highestLong(final long start) {
        return new Tracker<>(start, Math::max);
    }

}
