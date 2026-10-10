// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.dbxs;

import com.eischet.dbxs.exceptions.DatabaseError;

/**
 * A consumer that may throw a {@link DatabaseError}.
 * @param <T> the type of the value to consume
 */
@FunctionalInterface
public interface DatabaseConsumer<T> {
    /**
     * Consumes a value.
     * @param t the value
     * @throws DatabaseError on database errors
     */
    void accept(T t) throws DatabaseError;
}
