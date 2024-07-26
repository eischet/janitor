package com.eischet.janitor.api.types;

/**
 * Interface for objects that can be converted to a JanitorObject.
 * This is a simple approach to making your existing Java classes compatible with Janitor.
 * TODO: not currently used within the janitor project, but in existing client code. Figure out if needed at all in here.
 */
public interface JanitorAware {

    /**
     * Convert this object to a JanitorObject.
     * @return the JanitorObject
     */
    JanitorObject asJanitorObject();

}
