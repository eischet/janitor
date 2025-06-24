package com.eischet.janitor.api.metadata;

import java.util.HashMap;
import java.util.Map;

public class MetaDataMap {

    private final Map<MetaDataKey<?>, Object> storage = new HashMap<>();

    public <T> void put(final MetaDataKey<T> key, final T value) {
        storage.put(key, value);
    }

    public <T> T get(final MetaDataKey<T> key) {
        return key.getType().cast(storage.get(key));
    }

    public <T> boolean containsKey(final MetaDataKey<T> key) {
        return storage.containsKey(key);
    }
    
}
