// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.ref;

import com.eischet.dbxs.DatabaseConnection;
import com.eischet.janitor.orm.entity.OrmEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * A foreign key that is the referenced entity itself, so resolving it never needs database access.
 * @param <T> the type of the referenced entity
 */
public non-sealed interface ForeignKeyIdentity<T extends OrmEntity> extends ForeignKey<T> {

    /**
     * @return the ID of the entity
     */
    long getId();

    @Override
    default @NotNull Optional<T> resolve() {
        return Optional.of(getIdentity()); // which is the same as Optional.of(this), but works with javac.
    }

    /**
     * @param conn the database connection, which is not needed
     * @return the entity itself
     */
    default @NotNull Optional<T> resolve(@NotNull DatabaseConnection conn) {
        return Optional.of(getIdentity()); // DB access is never needed when the full object is used!
    }

    @NotNull T getIdentity();

    /**
     * Does nothing, because the entity is already there.
     * @param conn the database connection, which is not needed
     */
    default void preResolve(@NotNull DatabaseConnection conn) {
        // nothing to do at all, because this object is supposed to be the resolution
    }


    @Override
    default boolean isNull() {
        return false;
    }

    /**
     * @return the class of the entity that is embodied by this object
     */
    @Override
    default @Nullable Class<?> getReferencedEntityClass() {
        return getIdentity().getClass();
    }

    @Override
    default @NotNull String getReferencedEntityClassName() {
        return getIdentity().getClass().getSimpleName();
    }


}
