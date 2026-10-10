// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.entity;

import com.eischet.dbxs.DatabaseConnection;
import com.eischet.dbxs.exceptions.DatabaseError;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/**
 * Loads the join records for a {@link JoinedList}.
 * @param <T> the type of the join records
 * @param <V> the type of the DAO
 */
@FunctionalInterface
public interface JoinLoader<T, V> {
    /**
     * Loads the join records.
     * @param conn the database connection
     * @param dao the DAO of the join records
     * @return the join records
     * @throws DatabaseError on database errors
     */
    @NotNull @Unmodifiable
    List<T> load(final @NotNull DatabaseConnection conn, V dao) throws DatabaseError;
}
