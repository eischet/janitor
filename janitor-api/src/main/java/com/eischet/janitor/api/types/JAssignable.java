package com.eischet.janitor.api.types;

import com.eischet.janitor.api.errors.glue.JanitorGlueException;

/**
 * An object that can be assigned to.
 * The interpreter uses this marker interface to perform assignments to object properties, e.g. "foo.jay = 17;".
 */
public interface JAssignable {
    /**
     * Assign a value to this object.
     * @param value the value to assign
     * @return true if the assignment was successful, false if the assignment was not possible
     * @throws JanitorGlueException if the assignment failed
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean assign(JanitorObject value) throws JanitorGlueException;

    /**
     * Describe the assignable properties of this object.
     * @return a string describing the assignable properties
     */
    String describeAssignable();
}
