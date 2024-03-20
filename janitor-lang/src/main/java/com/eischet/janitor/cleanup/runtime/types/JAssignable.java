package com.eischet.janitor.cleanup.runtime.types;

import com.eischet.janitor.cleanup.api.api.types.JanitorObject;

public interface JAssignable {
    boolean assign(JanitorObject value);
    String describeAssignable();
}
