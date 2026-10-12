// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.entity;

/**
 * Optional interface for entities that carry a version counter, which changes whenever the entity is written; it can serve as an ETag
 * or for optimistic locking. The column is named by {@link com.eischet.janitor.orm.JanitorOrm.MetaData#VERSION_FIELD}.
 */
public interface Versioned {

    /**
     * @return the current version
     */
    long getVersion();

    /**
     * Sets the version.
     * @param version the version
     */
    void setVersion(long version);

}
