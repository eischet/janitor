// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.entity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Set;

/** Implemented by entities that keep track of which of their fields were modified. */
public interface ChangeTracker {

    /**
     * Marks a field as modified.
     * Change tracking is cooperative, so the Entity implementation needs to call this method where appropriate.
     * @param fieldName the name of the field that was modified
     */
    void setModified(@NotNull String fieldName);
    @Unmodifiable @NotNull Set<String> getModifiedFields();

}
