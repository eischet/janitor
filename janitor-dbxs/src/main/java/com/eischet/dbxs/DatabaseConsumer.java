/*
 * © Eischet Software e.K., Köln
 */

package com.eischet.dbxs;

import com.eischet.dbxs.exceptions.DatabaseError;

@FunctionalInterface
public interface DatabaseConsumer<T> {
    void accept(T t) throws DatabaseError;
}
