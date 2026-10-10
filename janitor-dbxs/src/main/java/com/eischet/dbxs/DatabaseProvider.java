// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.dbxs;

import com.eischet.dbxs.exceptions.DatabaseError;

/**
 * A supplier that may throw a {@link DatabaseError}.
 * @param <T> the type of the supplied value
 */
@FunctionalInterface
public interface DatabaseProvider<T> {
    /**
     * @return the value
     * @throws DatabaseError on database errors
     */
    T provide() throws DatabaseError;
}
