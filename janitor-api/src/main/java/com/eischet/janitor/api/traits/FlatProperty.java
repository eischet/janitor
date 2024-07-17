package com.eischet.janitor.api.traits;

/**
 * A functional interface for getting a property value of unknown type.
 * TODO: this should probably be replaced with Supplier&lt;Object&gt; or even be removed entirely.
 */
@FunctionalInterface
public interface FlatProperty {
    Object getValue();
}
