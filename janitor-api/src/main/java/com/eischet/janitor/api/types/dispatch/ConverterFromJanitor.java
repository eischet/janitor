package com.eischet.janitor.api.types.dispatch;

import com.eischet.janitor.api.errors.glue.JanitorGlueException;
import com.eischet.janitor.api.types.JanitorObject;

/**
 * Converts Janitor values to Java values.
 * @param <T> the Java type
 */
@FunctionalInterface
public interface ConverterFromJanitor<T> {

    /**
     * Converts a Janitor value to a Java value.
     * @param janitorObject the value
     * @return the Java value
     * @throws JanitorGlueException if the value cannot be converted
     */
    T convertFromJanitor(JanitorObject janitorObject) throws JanitorGlueException;


}
