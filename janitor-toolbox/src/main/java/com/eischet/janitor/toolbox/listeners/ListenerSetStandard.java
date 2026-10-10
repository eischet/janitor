// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.toolbox.listeners;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

/**
 * A {@link ListenerSet} that keeps strong references to its listeners.
 * @param <T> the type of the listeners
 */
public class ListenerSetStandard<T> implements ListenerSet<T> {

    private final Set<T> listeners = new HashSet<>();

    @Override
    public ListenerRegistration add(final T listener) {
        listeners.add(listener);
        return () -> listeners.remove(listener);
    }

    @Override
    public Stream<T> stream() {
        return listeners.stream();
    }

    @Override
    public int size() {
        return listeners.size();
    }

    @Override
    public void clear() {
        listeners.clear();
    }


}
