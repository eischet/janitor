// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.orm.rest;

import com.eischet.janitor.api.types.dispatch.DispatchTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Something with properties that the API exposes: an {@link ApiEntity}, or an {@link ApiLink} (the records of a join table). */
public interface ApiFields {

    /** @return the dispatch table that the properties come from */
    @NotNull DispatchTable<?> table();

    /** @return the properties that the API exposes */
    @NotNull List<ApiField> fields();

    /**
     * @param name the name of a property
     * @return the property, or null if the API does not expose such a property
     */
    @Nullable ApiField field(@NotNull String name);
}
