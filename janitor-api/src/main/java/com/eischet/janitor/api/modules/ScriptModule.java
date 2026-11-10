// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.modules;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.RunnableScript;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorNameException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.errors.runtime.JanitorScriptModuleException;
import com.eischet.janitor.api.scopes.HasNameDocs;
import com.eischet.janitor.api.scopes.NameDoc;
import com.eischet.janitor.api.scopes.ResultAndScope;
import com.eischet.janitor.api.scopes.Scope;
import com.eischet.janitor.api.types.JanitorObject;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * A module that is implemented in Janitor itself: its source code is compiled and run when the module is first requested,
 * and everything it defines at the top level becomes an attribute of the module.
 */
public class ScriptModule extends JanitorModuleRegistration {
    /**
     * Constructs a new JanitorModuleRegistration.
     *
     * @param qualifiedName  the qualified name of the module
     * @param source         the source code of the module
     * @param bindings       the bindings to use when creating the module
     */
    public ScriptModule(@NotNull String qualifiedName, @NotNull @Language("Janitor") String source, @Nullable Consumer<JanitorScriptProcess> bindings) {
        super(qualifiedName, process -> moduleFromScript(process, qualifiedName, source, bindings));
    }

    /**
     * Compiles and runs the given source code, and turns the resulting top-level scope into a module.
     * <p>
     * The module's top-level code does not inherit the meta-data of the importing process: hosts may keep a module once it
     * has run, and what it does must not depend on who imported it first. It sees the environment's values and what the
     * bindings callback sets. Functions of the module run later in the process that calls them, with that process's meta-data.
     * </p>
     * @param process the running script process
     * @param qualifiedName the qualified name of the module
     * @param source the source code of the module
     * @param bindings optional bindings to make available to the module while it runs
     * @return the module
     * @throws JanitorRuntimeException if the module cannot be compiled or run
     */
    public static JanitorModule moduleFromScript(@NotNull JanitorScriptProcess process,
                                                 @NotNull final String qualifiedName,
                                                 @NotNull @Language("Janitor") final String source,
                                                 @Nullable Consumer<JanitorScriptProcess> bindings) throws JanitorRuntimeException {
        try {
            @NotNull RunnableScript compiled = process.getRuntime().compile(qualifiedName, source);
            @NotNull ResultAndScope result = compiled.runAndKeepGlobals(process.getRuntime(), bindings == null ? g -> {} : bindings);
            return new ModuleFromScope(result.getScope(), compiled.getDocumentation());
        } catch (JanitorCompilerException e) {
            throw new JanitorScriptModuleException(process, e);
        }
    }

    /**
     * Whether a top-level name of a module made from a script is visible to those who import it. Names that start with an
     * underscore, e.g. {@code _helper}, are private: the module's own code uses them, importers cannot reach them, and editors
     * do not suggest them.
     *
     * @param name a top-level name of the module
     * @return true if importers can use it
     */
    public static boolean isExported(final @NotNull String name) {
        return !name.startsWith("_");
    }

    /** A module that exposes the top-level variables of a scope as its attributes, except private ones ({@link #isExported}). */
    public static class ModuleFromScope implements JanitorModule, HasNameDocs {
        private final Scope moduleScope;
        private final @Nullable String documentation;

        public ModuleFromScope(Scope moduleScope) {
            this(moduleScope, null);
        }

        /**
         * @param moduleScope the top-level scope of the script
         * @param documentation the documentation of the script from its {@code /*!} comments, or null
         */
        public ModuleFromScope(final Scope moduleScope, final @Nullable String documentation) {
            this.moduleScope = moduleScope;
            this.documentation = documentation;
        }

        @Override
        public @Nullable String getDocumentation() {
            return documentation;
        }

        @Override
        public @Nullable JanitorObject janitorGetAttribute(final JanitorScriptProcess runningScript, final String name, final boolean required) throws JanitorNameException {
            // Scope.get() answers only the names the module defines at its own top level, nothing more;
            // walking the parent chain, which for a module scope (created via VariableScope.createGlobalScope())
            // ends at the environment's builtin scope, would make every global builtin (print, assert, ...)
            // resolve as if it were a member of the module too.
            if (!isExported(name)) {
                return null;
            }
            final JanitorObject obj = moduleScope.get(runningScript, name);
            if (obj instanceof ModuleScopeAware scriptFunction) {
                scriptFunction.setModuleScope(moduleScope);
            }
            return obj;
        }

        /** The documentation of an exported top-level name of the module, so that help(module.name) finds it. */
        @Override
        public @Nullable NameDoc getNameDoc(final @NotNull String name) {
            return isExported(name) ? moduleScope.getNameDoc(name) : null;
        }

    }


}
