// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.toolbox.memory;

import com.eischet.janitor.toolbox.listeners.ListenerSet;
import com.eischet.janitor.toolbox.listeners.ListenerSetStandard;

/** A boolean flag that notifies listeners when it changes. */
public class Flag {

    private final ListenerSet<FlagListener> listeners = new ListenerSetStandard<>();
    private transient boolean flag;

    /**
     * @return the current value of the flag
     */
    public boolean isFlag() {
        return flag;
    }

    /**
     * @return the current value of the flag, which is true if it is checked
     */
    public boolean isChecked() {
        return isFlag();
    }

    /**
     * Sets the flag, and notifies the listeners.
     * @param flag the new value
     * @return this flag
     */
    public Flag setFlag(final boolean flag) {
        this.flag = flag;
        listeners.stream().forEach(listener -> listener.onFlagChanged(flag));
        return this;
    }

    /**
     * Sets the flag to true.
     * @return this flag
     */
    public Flag check() {
        return setFlag(true);
    }

    /**
     * Sets the flag to false.
     * @return this flag
     */
    public Flag uncheck() {
        return setFlag(false);
    }

    /**
     * Inverts the flag.
     * @return the new value of the flag
     */
    public boolean invert() {
        setFlag(!isFlag());
        return isFlag();
    }

    /**
     * Adds a listener that is notified when the flag is set.
     * @param listener the listener
     * @return this flag
     */
    public Flag addListener(final FlagListener listener) {
        listeners.add(listener);
        return this;
    }

    /** A listener for changes of a {@link Flag}. */
    @FunctionalInterface
    public interface FlagListener {
        /**
         * Called when the flag is set.
         * @param flag the new value of the flag
         */
        void onFlagChanged(boolean flag);
    }

    /**
     * Returns the listener set for this flag.
     * @return the listener set
     */
    public ListenerSet<FlagListener> getListeners() {
        return listeners;
    }
}
