package com.eischet.janitor.api.types;

/**
 * Interface for objects that can be converted to a JanitorObject.
 * This is a simple approach to making your existing Java classes compatible with Janitor by simply adding this
 * interface to provide some kind of wrapper object. probably something like JanitorWrapper&lt;T&gt;.
 */
public interface JanitorAware {

    /**
     * Convert this object to a JanitorObject.
     * @return the JanitorObject
     */
    JanitorObject asJanitorObject();

}
