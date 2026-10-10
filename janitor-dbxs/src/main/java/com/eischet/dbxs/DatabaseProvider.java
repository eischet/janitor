/*
 * © Eischet Software e.K., Köln
 */

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
