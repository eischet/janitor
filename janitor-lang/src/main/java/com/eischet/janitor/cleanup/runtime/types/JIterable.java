package com.eischet.janitor.cleanup.runtime.types;

import com.eischet.janitor.cleanup.api.api.types.JanitorObject;

import java.util.Iterator;

public interface JIterable {
    Iterator<? extends JanitorObject> getIterator();
}
