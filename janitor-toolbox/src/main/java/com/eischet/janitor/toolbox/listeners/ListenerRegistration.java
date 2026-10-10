package com.eischet.janitor.toolbox.listeners;

/** The registration of a listener, which can be used to remove the listener again. */
@FunctionalInterface
public interface ListenerRegistration {
    /** Removes the listener. */
    void remove();
}
