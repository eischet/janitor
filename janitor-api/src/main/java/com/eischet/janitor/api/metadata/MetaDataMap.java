// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.metadata;

import java.util.HashMap;
import java.util.Map;

/** A simple type-safe map for meta-data, keyed by {@link MetaDataKey}. */
public class MetaDataMap {

    private final Map<MetaDataKey<?>, Object> storage = new HashMap<>();

    /**
     * Stores a value under the given key, replacing any previous value.
     * @param key the key
     * @param value the value to store
     * @param <T> the value type
     */
    public <T> void put(final MetaDataKey<T> key, final T value) {
        storage.put(key, value);
    }

    /**
     * Retrieves the value stored under the given key.
     * @param key the key
     * @param <T> the value type
     * @return the value, or null if there is none
     */
    public <T> T get(final MetaDataKey<T> key) {
        return key.getType().cast(storage.get(key));
    }

    /**
     * Checks whether a value is stored under the given key.
     * @param key the key
     * @param <T> the value type
     * @return true if a value is present
     */
    public <T> boolean containsKey(final MetaDataKey<T> key) {
        return storage.containsKey(key);
    }
    
}
