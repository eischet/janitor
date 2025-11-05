/*
 * © Eischet Software e.K., Köln
 */

package com.eischet.dbxs;

import com.eischet.dbxs.exceptions.DatabaseError;

@FunctionalInterface
public interface DatabaseFunction<T, R> {
    R apply(T t) throws DatabaseError;
}
