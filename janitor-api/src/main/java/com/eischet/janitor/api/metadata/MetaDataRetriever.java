package com.eischet.janitor.api.metadata;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Functional helper interface for getting meta-data for an object from somewhere else.
 */
@FunctionalInterface
public interface MetaDataRetriever {
    <K> @Nullable K retrieveMetaData(final @NotNull MetaDataKey<K> key);
}
