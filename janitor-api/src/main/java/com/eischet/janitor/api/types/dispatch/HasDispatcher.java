package com.eischet.janitor.api.types.dispatch;

import com.eischet.janitor.api.types.JanitorObject;

/**
 * Implemented by objects that know their own {@link Dispatcher}.
 * @param <T> the type of the object
 */
public interface HasDispatcher<T extends JanitorObject> {
    /**
     * @return the dispatcher of this object
     */
    Dispatcher<T> getDispatcher();
}
