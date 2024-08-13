package com.eischet.janitor.api.types;

import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;

/**
 * Like the functional interface Consumer, but with a throws clause, so it can be used in interpreted code.
 * @param <T> any type of consumable object.
 */
@FunctionalInterface
public interface RuntimeConsumer<T> {
    /**
     * Accepts the given object.
     * @param object the object to accept.
     * @throws JanitorRuntimeException on errors
     */
    void accept(T object) throws JanitorRuntimeException;
}
