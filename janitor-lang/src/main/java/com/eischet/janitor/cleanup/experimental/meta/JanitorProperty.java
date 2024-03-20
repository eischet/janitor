package com.eischet.janitor.cleanup.experimental.meta;

import com.eischet.janitor.cleanup.api.api.types.JanitorObject;

public interface JanitorProperty<T> {
    JanitorObject getObjectProperty(T self);
}
