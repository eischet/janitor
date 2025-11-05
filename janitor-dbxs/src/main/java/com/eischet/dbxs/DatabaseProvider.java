/*
 * © Eischet Software e.K., Köln
 */

package com.eischet.dbxs;

import com.eischet.dbxs.exceptions.DatabaseError;

@FunctionalInterface
public interface DatabaseProvider<T> {
    T provide() throws DatabaseError;
}
