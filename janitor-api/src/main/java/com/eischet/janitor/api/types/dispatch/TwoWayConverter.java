package com.eischet.janitor.api.types.dispatch;

/**
 * Converts between Janitor values and Java values, in both directions.
 * @param <T> the Java type
 */
public interface TwoWayConverter<T> extends ConverterFromJanitor<T>, ConverterToJanitor<T> {
}
