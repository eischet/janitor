package com.eischet.janitor.toolbox.listeners;

import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * A set of listeners that can be notified.
 * @param <T> the type of the listeners
 */
public interface ListenerSet<T> {
    /**
     * Adds a listener.
     * @param listener the listener
     * @return a registration that can be used to remove the listener
     */
    ListenerRegistration add(T listener);

    /**
     * @return the listeners
     */
    Stream<T> stream();

    /**
     * @return the number of listeners
     */
    int size();

    /** Removes all listeners. */
    void clear();

    /**
     * Calls a consumer for each listener.
     * @param listenerConsumer the code to call for each listener
     */
    default void fire(Consumer<T> listenerConsumer) {
        stream().forEach(listenerConsumer::accept);
    }

}
