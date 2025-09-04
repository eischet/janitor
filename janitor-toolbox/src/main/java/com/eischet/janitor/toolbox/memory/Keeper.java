package com.eischet.janitor.toolbox.memory;

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

    public T getValue() {
        return value;
    }

    public void setValue(final T value) {
        this.value = value;
    }

    public static <U> Keeper<U> empty() {
        return new Keeper<>();
    }

    public static <U> Keeper<U> of(U init) {
        return new Keeper<>(init);
    }

    public static <U> ObservableKeeper<U> observable(U init) {
        return new ObservableKeeper<>(init);
    }

}
