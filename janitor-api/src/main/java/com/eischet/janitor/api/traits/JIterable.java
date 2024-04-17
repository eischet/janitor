package com.eischet.janitor.api.traits;

import com.eischet.janitor.api.types.JanitorObject;

import java.util.Iterator;

public interface JIterable {
    Iterator<? extends JanitorObject> getIterator();
}
