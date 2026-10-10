// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.ref;

import com.eischet.dbxs.DatabaseConnection;
import com.eischet.dbxs.exceptions.DatabaseError;
import com.eischet.janitor.orm.entity.OrmEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Helpers that work on any kind of {@link ForeignKey}, regardless of how it was loaded.
 * <p>
 * In JSON, a reference is always written as the short code of what it points to, never as a database id: ids differ
 * between installations, short codes are what ties exports and imports together.
 * </p>
 */
public final class ForeignKeys {

    private ForeignKeys() {
    }

    /**
     * The short code of the entity the reference points to, for writing it to JSON. A reference by short code or an
     * already resolved one answers directly; a reference by id has to be resolved first, which may mean a database
     * access (see the variant that takes a connection).
     *
     * @return the short code, or {@code null} for a null reference or an id that points to nothing (any more)
     * @throws IllegalStateException if the target exists but has no short code
     */
    public static @Nullable String keyOf(final @Nullable ForeignKey<?> reference) {
        try {
            return resolveKey(reference, null);
        } catch (DatabaseError e) {
            throw new IllegalStateException("cannot determine the short code of " + reference, e); // cannot happen without a connection
        }
    }

    /**
     * Like {@link #keyOf(ForeignKey)}, but resolves a reference by id on the given connection, e.g. the one an export is running on.
     */
    public static @Nullable String keyOf(final @Nullable ForeignKey<?> reference, final @NotNull DatabaseConnection connection) throws DatabaseError {
        return resolveKey(reference, connection);
    }

    private static @Nullable String resolveKey(final @Nullable ForeignKey<?> reference, final @Nullable DatabaseConnection connection) throws DatabaseError {
        if (reference == null || reference.isNull()) {
            return null;
        }
        if (reference instanceof ForeignKeyString<?> byKey) {
            return byKey.getKey();
        }
        if (reference instanceof ForeignKeySearchResult<?> found && found.getKey() != null && !found.getKey().isBlank()) {
            return found.getKey();
        }
        final OrmEntity target;
        if (reference instanceof ForeignKeyIdentity<?> identity) {
            target = identity.getIdentity(); // the entity itself: nothing to look up
        } else {
            target = connection == null ? reference.resolveOrNull() : reference.resolveOrNull(connection);
        }
        if (target == null) {
            return null;
        }
        final String key = target.getKey();
        if (key == null || key.isBlank()) {
            throw new IllegalStateException("the referenced " + reference.getReferencedEntityClassName() + " with id " + target.getId() + " has no short code");
        }
        return key;
    }

}
