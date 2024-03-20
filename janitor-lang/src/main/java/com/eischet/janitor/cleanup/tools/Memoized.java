package com.eischet.janitor.cleanup.tools;

public class Memoized<T> implements LazyLoading<T> {

    private final LazyLoading<T> delegate;
    private boolean loaded = false;
    private T value;

    public Memoized(final LazyLoading<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public T get() {
        if (!loaded) {
            value = delegate.get();
            loaded = true;
        }
        return value;
    }
}
