// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.modules;

import com.eischet.janitor.api.types.JanitorObject;
import org.jetbrains.annotations.Nullable;

/**
 * A JanitorModule is a module that can be imported into a Janitor script.
 * "import foo;", etc.
 */
public interface JanitorModule extends JanitorObject {

    /**
     * The documentation of the module, e.g. from the {@code /*!} comments of a module made from a script. An import stores
     * it as the documentation of the name it binds, so that {@code help(module)} returns it.
     *
     * @return the documentation, or null if there is none
     */
    default @Nullable String getDocumentation() {
        return null;
    }

}
