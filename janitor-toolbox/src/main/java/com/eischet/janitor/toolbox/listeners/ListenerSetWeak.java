package com.eischet.janitor.toolbox.listeners;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

/**
 * A {@link ListenerSet} that keeps only weak references to its listeners, so it does not prevent them from being garbage collected.
 * Note that listeners must be referenced elsewhere, and that the registrations returned by {@code add} do nothing.
 * @param <T> the type of the listeners
 */
public class ListenerSetWeak<T> implements ListenerSet<T> {

    private final Set<WeakReference<T>> listeners = new HashSet<>();

    @Override
    public ListenerRegistration add(final T listener) {
        listeners.add(new WeakReference<>(listener));
        return () -> {};
    }

    @Override
    public Stream<T> stream() {
        listeners.removeIf(ref -> ref.refersTo(null));
        return listeners.stream().map(Reference::get).filter(Objects::nonNull);
    }

    @Override
    public int size() {
        return (int) stream().count();
    }

    @Override
    public void clear() {
        listeners.clear();
    }

}
