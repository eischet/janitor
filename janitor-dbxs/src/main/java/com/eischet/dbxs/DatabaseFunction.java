/*
 * © Eischet Software e.K., Köln
 */

package com.eischet.dbxs;

import com.eischet.dbxs.exceptions.DatabaseError;

/**
 * A function that may throw a {@link DatabaseError}.
 * @param <T> the type of the argument
 * @param <R> the type of the result
 */
@FunctionalInterface
public interface DatabaseFunction<T, R> {
    /**
     * Applies the function.
     * @param t the argument
     * @return the result
     * @throws DatabaseError on database errors
     */
    R apply(T t) throws DatabaseError;
}
