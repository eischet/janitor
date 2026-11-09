// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.scopes;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Something that holds names and their documentation, e.g. a scope or a module made from a script, so that
 * {@code help(module.name)} finds the doc comment of the name.
 */
public interface HasNameDocs {

    /**
     * @param name a name defined here
     * @return the documentation of the name, or null if it has none
     */
    @Nullable NameDoc getNameDoc(@NotNull String name);

}
