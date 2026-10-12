// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.entity;

import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;

/**
 * Optional interface for entities that carry a modification timestamp.
 * <p>
 * It only takes effect if the entity's dispatch table also names the column, see
 * {@link com.eischet.janitor.orm.JanitorOrm.MetaData#MODIFIED_FIELD}; then the DAO sets the timestamp when it inserts or updates the entity.
 * </p>
 */
public interface HasModifiedDate {

    /**
     * @return when the entity was last written, or null if that is unknown
     */
    @Nullable LocalDateTime getModifiedDate();

    /**
     * Sets the modification timestamp. Called by the DAO, usually not by application code.
     * @param updateDate the timestamp
     */
    void setModifiedDate(@Nullable LocalDateTime updateDate);

}
