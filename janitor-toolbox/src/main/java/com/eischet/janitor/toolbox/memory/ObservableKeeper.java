package com.eischet.janitor.toolbox.memory;

import com.eischet.janitor.toolbox.listeners.ListenerRegistration;
import com.eischet.janitor.toolbox.listeners.ListenerSet;
import com.eischet.janitor.toolbox.listeners.ListenerSetStandard;

import java.util.function.Consumer;

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

    public ListenerRegistration addValueChangeListener(final Consumer<T> listener) {
        return selectionListeners.add(listener);
    }

    public ObservableKeeper<T> withValueChangeListener(final Consumer<T> listener) {
        addValueChangeListener(listener);
        return this;
    }

}
