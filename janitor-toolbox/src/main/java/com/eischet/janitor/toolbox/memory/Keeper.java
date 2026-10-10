package com.eischet.janitor.toolbox.memory;

/**
 * A mutable holder for a single value.
 * @param <T> the type of the value
 */
public class Keeper<T> {

    private T value;

    public Keeper() {
        this(null);
    }

    public Keeper(final T value) {
        this.value = value;
    }

    boolean isEmpty() {
        return value == null;
    }

    /**
     * @return the value
     */
    public T getValue() {
        return value;
    }

    /**
     * Sets the value.
     * @param value the new value
     */
    public void setValue(final T value) {
        this.value = value;
    }

    /**
     * @param <U> the type of the value
     * @return a holder without a value
     */
    public static <U> Keeper<U> empty() {
        return new Keeper<>();
    }

    /**
     * @param init the initial value
     * @param <U> the type of the value
     * @return a holder with the given value
     */
    public static <U> Keeper<U> of(U init) {
        return new Keeper<>(init);
    }

    /**
     * @param init the initial value
     * @param <U> the type of the value
     * @return a holder with the given value, which notifies listeners when the value changes
     */
    public static <U> ObservableKeeper<U> observable(U init) {
        return new ObservableKeeper<>(init);
    }

}
