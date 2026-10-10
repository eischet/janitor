// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.dao;

import com.eischet.janitor.orm.entity.OrmEntity;
import com.eischet.janitor.orm.entity.OrmObject;

import java.util.List;

/** Receives notifications about lazy loading by DAOs, e.g. to log or count it. */
public interface DaoLogging {

    /**
     * Called when a foreign key was resolved by lazy loading.
     * @param entityClass the name of the entity class
     * @param identifier the ID or key of the entity
     * @param result the entity that was loaded
     */
    void lazyLoadedForeignKey(String entityClass, Object identifier, OrmObject result);

    /**
     * Called when an association was loaded lazily.
     * @param entityClass the name of the entity class
     * @param keyColumn the foreign key column
     * @param parentId the ID of the parent entity
     * @param results the entities that were loaded
     */
    void lazyLoadedAssociation(String entityClass, String keyColumn, long parentId, List<? extends OrmEntity> results);
}
