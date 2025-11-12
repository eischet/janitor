package com.eischet.janitor.toolbox.functional;

import java.util.function.Function;

/**
 * Like Function, but can throw an exception.
 * @param <T> any single parameter type
 * @param <U> any return type
 */
@FunctionalInterface
public interface ThrowingFunction<T, U> {
    U apply(T t) throws Exception;

    /**
     * Turn a throwing function into a non-throwing one by wrapping any checked exception in a RuntimeException.
     * @param f any throwsing function
     * @return a wrapper function that only rethrows (or passes on) RuntimeExceptions.
     * @param <T> any single parameter type
     * @param <U> any return type
     */
    static <T, U> Function<T, U> rethrowing(ThrowingFunction<T, U> f) {
        return t -> {
            try {
                return f.apply(t);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

}