package com.eischet.janitor.api.metadata;

/**
 * Fluent builder interface for attaching meta-data to a property or method.
 * @param <T> the type that the meta-data belongs to
 */
public interface MetaDataBuilder<T> {
    /**
     * Sets a meta-data entry.
     * @param key the key
     * @param value the value
     * @param <K> the type of the value
     * @return this builder
     */
    <K> MetaDataBuilder<T> setMetaData(MetaDataKey<K> key, K value);
}

