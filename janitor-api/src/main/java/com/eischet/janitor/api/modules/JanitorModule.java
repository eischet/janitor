// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.modules;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.metadata.HasMetaData;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.dispatch.HasDispatcher;
import org.jetbrains.annotations.Nullable;

/**
 * A JanitorModule is a module that can be imported into a Janitor script.
 * "import foo;", etc.
 */
public interface JanitorModule extends JanitorObject {

    /**
     * The documentation of the module, e.g. from the {@code /*!} comments of a module made from a script. An import stores
     * it as the documentation of the name it binds, so that {@code help(module)} returns it. By default, it is the HELP text
     * of the module, or of its dispatch table, e.g. {@code DISPATCH.setMetaData(Janitor.MetaData.HELP, "Access to the
     * operating system.")}.
     *
     * @return the documentation, or null if there is none
     */
    default @Nullable String getDocumentation() {
        if (this instanceof HasMetaData hasMetaData) {
            final String help = hasMetaData.getMetaData(Janitor.MetaData.HELP);
            if (help != null) {
                return help;
            }
        }
        if (this instanceof HasDispatcher<?> hasDispatcher) {
            return hasDispatcher.getDispatcher().getMetaData(Janitor.MetaData.HELP);
        }
        return null;
    }

}
