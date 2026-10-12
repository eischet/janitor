// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.rest;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

/**
 * An immutable set of {@link RestOperation}s: the value of {@link RestMetaData#OPERATIONS}. (Meta-data keys cannot be declared for a generic
 * set type, so this is a small class of its own.)
 */
public final class RestOperations {

    /** The operations that every entity offers unless it says otherwise: listing, searching and getting. */
    public static final RestOperations READ_ONLY = of(RestOperation.LIST, RestOperation.SEARCH, RestOperation.GET);

    private final @NotNull Set<RestOperation> operations;

    private RestOperations(final @NotNull Set<RestOperation> operations) {
        this.operations = Set.copyOf(operations);
    }

    /**
     * @param operations the operations
     * @return a set with exactly these operations
     */
    public static @NotNull RestOperations of(final @NotNull RestOperation... operations) {
        return new RestOperations(operations.length == 0 ? EnumSet.noneOf(RestOperation.class) : EnumSet.copyOf(Arrays.asList(operations)));
    }

    /**
     * @param more further operations
     * @return a set with these operations and the given ones
     */
    public @NotNull RestOperations with(final @NotNull RestOperation... more) {
        final Set<RestOperation> result = EnumSet.noneOf(RestOperation.class);
        result.addAll(operations);
        result.addAll(Arrays.asList(more));
        return new RestOperations(result);
    }

    /** @return the operations, as an unmodifiable set */
    public @NotNull Set<RestOperation> set() {
        return operations;
    }

    /**
     * @param operation an operation
     * @return true if it is in this set
     */
    public boolean contains(final @NotNull RestOperation operation) {
        return operations.contains(operation);
    }

    @Override
    public String toString() {
        return operations.toString();
    }
}
