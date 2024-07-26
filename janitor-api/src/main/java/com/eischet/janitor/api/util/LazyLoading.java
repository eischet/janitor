package com.eischet.janitor.api.util;

/**
 * A lazy loading object.
 * @param <T> any type
 */
@FunctionalInterface
public interface LazyLoading<T> {
    /**
     * Get the object, e.g. by performing a calculation or by loading it from somewhere.
     * @return the object.
     */
    T get();
}
