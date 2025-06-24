package com.eischet.janitor.api.metadata;

public interface MetaDataBuilder<T> {
    <K> MetaDataBuilder<T> setMetaData(MetaDataKey<K> key, K value);
}

