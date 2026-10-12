// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.dao;

import com.eischet.dbxs.exceptions.DatabaseError;

/**
 * Thrown when a versioned entity (see {@link com.eischet.janitor.orm.JanitorOrm.MetaData#VERSION_FIELD}) cannot be updated or deleted because
 * its row has changed since the entity was loaded, or is gone.
 */
public class OptimisticLockError extends DatabaseError {

    private final String entityClassName;
    private final long id;
    private final long expectedVersion;

    /**
     * @param entityClassName the name of the entity class
     * @param id              the id of the entity
     * @param expectedVersion the version that the entity had when it was loaded
     */
    public OptimisticLockError(final String entityClassName, final long id, final long expectedVersion) {
        super(entityClassName + " " + id + " was changed or deleted by someone else: it is no longer at version " + expectedVersion);
        this.entityClassName = entityClassName;
        this.id = id;
        this.expectedVersion = expectedVersion;
    }

    /** @return the name of the entity class */
    public String getEntityClassName() {
        return entityClassName;
    }

    /** @return the id of the entity */
    public long getId() {
        return id;
    }

    /** @return the version that the entity had when it was loaded */
    public long getExpectedVersion() {
        return expectedVersion;
    }
}
