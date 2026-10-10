// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.toolbox.memory;

import com.eischet.janitor.toolbox.listeners.ListenerRegistration;
import com.eischet.janitor.toolbox.listeners.ListenerSet;
import com.eischet.janitor.toolbox.listeners.ListenerSetStandard;

import java.util.function.Consumer;

/**
 * A {@link Keeper} that notifies listeners when its value changes.
 * @param <T> the type of the value
 */
public class ObservableKeeper<T> extends Keeper<T> {

    private final ListenerSet<Consumer<T>> selectionListeners = new ListenerSetStandard<>();

    public ObservableKeeper() {
    }

    public ObservableKeeper(final T value) {
        super(value);
    }

    @Override
    public void setValue(final T value) {
        selectionListeners.stream().forEach(listener -> listener.accept(value));
        super.setValue(value);
    }

    /**
     * Adds a listener that is notified when the value changes.
     * @param listener the listener
     * @return a registration that can be used to remove the listener
     */
    public ListenerRegistration addValueChangeListener(final Consumer<T> listener) {
        return selectionListeners.add(listener);
    }

    /**
     * Adds a listener that is notified when the value changes.
     * @param listener the listener
     * @return this keeper
     */
    public ObservableKeeper<T> withValueChangeListener(final Consumer<T> listener) {
        addValueChangeListener(listener);
        return this;
    }

}
