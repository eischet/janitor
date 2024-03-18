package com.eischet.janitor.experimental.meta;

import com.eischet.janitor.api.types.JanitorObject;

public interface JanitorProperty<T> {
    JanitorObject getObjectProperty(T self);
}
