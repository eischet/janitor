// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.metadata;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Functional helper interface for getting meta-data for an object from somewhere else.
 */
@FunctionalInterface
public interface MetaDataRetriever {
    /**
     * Gets a meta-data entry.
     * @param key the key
     * @param <K> the type of the value
     * @return the value, or null if there is none
     */
    <K> @Nullable K retrieveMetaData(final @NotNull MetaDataKey<K> key);
}
