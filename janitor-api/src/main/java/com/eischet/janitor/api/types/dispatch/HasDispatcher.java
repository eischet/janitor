package com.eischet.janitor.api.types.dispatch;

import com.eischet.janitor.api.types.JanitorObject;

public interface HasDispatcher<T extends JanitorObject> {
    Dispatcher<T> getDispatcher();
}
