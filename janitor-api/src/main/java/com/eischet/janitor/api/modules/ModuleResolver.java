// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.modules;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Scope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Resolves modules by name when a script imports them. */
@FunctionalInterface
public interface ModuleResolver {
    /**
     * Find a module by name.
     * This code is to be implemented by embedders, there's no built-in implementation for it.
     *
     * @param process the running script process
     * @param name the name of the module
     * @return the module if found, or null if not found
     * @throws JanitorRuntimeException on errors
     */
    @Nullable
    JanitorModule resolveModuleByStringName(JanitorScriptProcess process, String name) throws JanitorRuntimeException;

    /**
     * For tools such as editors: the top-level scope of the module with this name, if the resolver has it at hand without running
     * anything, e.g. because the module ran before and the host keeps its scope. Tools only read the scope.
     *
     * @param name the name of the module, as in {@code import "name" as foo}
     * @return the scope, or null if the resolver does not know the module or has not built it
     */
    default @Nullable Scope describeModuleByStringName(final @NotNull String name) {
        return null;
    }
}
