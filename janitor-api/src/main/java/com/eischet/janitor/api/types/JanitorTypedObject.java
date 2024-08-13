package com.eischet.janitor.api.types;

/**
 * Specialization of JanitorObject that specifies the Java-side type it contains.
 * This can make it easier to work with the object on the Java side.
 * @param <T> any Java-side type
 */
public interface JanitorTypedObject<T> extends JanitorObject {

    @Override
    T janitorGetHostValue();

}
