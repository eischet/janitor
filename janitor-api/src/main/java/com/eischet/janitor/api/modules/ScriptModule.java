package com.eischet.janitor.api.modules;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.RunnableScript;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorNameException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.errors.runtime.JanitorScriptModuleException;
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
    public ScriptModule(@NotNull String qualifiedName, @NotNull @Language("Janitor") String source, @Nullable Consumer<Scope> bindings) {
        super(qualifiedName, process -> moduleFromScript(process, qualifiedName, source, bindings));
    }

    /**
     * Compiles and runs the given source code, and turns the resulting top-level scope into a module.
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
                                                 @Nullable Consumer<Scope> bindings) throws JanitorRuntimeException {
        try {
            @NotNull RunnableScript compiled = process.getRuntime().compile(qualifiedName, source);
            @NotNull ResultAndScope result = compiled.runAndKeepGlobals(bindings == null ? g -> {} : bindings);
            return new ModuleFromScope(result.getScope());
        } catch (JanitorCompilerException e) {
            throw new JanitorScriptModuleException(process, e);
        }
    }

    /** A module that exposes the top-level variables of a scope as its attributes. */
    public static class ModuleFromScope implements JanitorModule {
        private final Scope moduleScope;

        public ModuleFromScope(Scope moduleScope) {
            this.moduleScope = moduleScope;
        }

        @Override
        public @Nullable JanitorObject janitorGetAttribute(final JanitorScriptProcess runningScript, final String name, final boolean required) throws JanitorNameException {
            // lookupLocally(), not lookup(): a module's attributes are exactly what it defines at its
            // own top level, nothing more. lookup() would also walk this scope's parent chain, which
            // for a module scope (created via Scope.createGlobalScope()) ends at the environment's
            // builtin scope -- so every global builtin (print, assert, ...) would incorrectly resolve
            // as if it were a member of the module too.
            final JanitorObject obj = moduleScope.lookupLocally(runningScript, name);
            if (obj instanceof ModuleScopeAware scriptFunction) {
                scriptFunction.setModuleScope(moduleScope);
            }
            return obj;
        }

    }


}
