package com.eischet.janitor.api.types;

import java.util.Iterator;

/**
 * Marker interface plus getIterator() method interface for classes that are "iterable".
 * if you want to make your own host object iterable, implement this interface.
 */
public interface JIterable {
    Iterator<? extends JanitorObject> getIterator();
}
