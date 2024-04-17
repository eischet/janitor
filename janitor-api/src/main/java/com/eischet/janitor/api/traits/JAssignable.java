package com.eischet.janitor.api.traits;

import com.eischet.janitor.api.types.JanitorObject;

public interface JAssignable {
    boolean assign(JanitorObject value);
    String describeAssignable();
}
