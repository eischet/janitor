package com.eischet.janitor.toolbox.listeners;

import java.util.function.Consumer;
import java.util.stream.Stream;

public interface ListenerSet<T> {
    ListenerRegistration add(T listener);

    Stream<T> stream();

    int size();

    void clear();

    default void fire(Consumer<T> listenerConsumer) {
        stream().forEach(listenerConsumer::accept);
    }

}
