package com.eischet.janitor.api.modules;

import com.eischet.janitor.api.JanitorRuntime;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.RunnableScript;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.ResultAndScope;
import com.eischet.janitor.api.scopes.Scope;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * A script-based module that compiles its source immediately, in the constructor, rather than
 * lazily on first import (see {@link ScriptModuleLazy} for that variant). Every import (there's no
 * "first" import that's special here) runs that one compiled script fresh, giving each importer its
 * own private scope, never shared with any other import -- exactly like {@link ScriptModuleLazy},
 * just with the compile step moved earlier.
 * <p>
 * Compiling eagerly is the right choice for a module whose source ships as a fixed part of the
 * application itself (as opposed to being supplied or editable by an application user at runtime):
 * a syntax error in it is a bug in the application, and should fail as early and as loudly as
 * possible -- ideally at startup, while constructing the module registration, rather than being
 * discovered much later, the first time some script happens to import it.
 * <p>
 * Because compilation needs a {@link JanitorRuntime} (to compile against its environment/builtins),
 * and none is running yet at the point a module registration is normally constructed, the runtime to
 * compile against must be passed in explicitly here.
 */
public class ScriptModulePrecompiled extends JanitorModuleRegistration {

    /**
     * Constructs a new precompiled script module, compiling {@code source} immediately.
     *
     * @param qualifiedName the qualified name of the module
     * @param runtime       the runtime to compile the source against
     * @param source        the source code of the module
     * @param bindings      the bindings to apply to the module's scope every time it's run
     * @throws JanitorCompilerException if the source fails to compile
     */
    public ScriptModulePrecompiled(@NotNull final String qualifiedName, @NotNull final JanitorRuntime runtime, @NotNull @Language("Janitor") final String source, @Nullable final Consumer<Scope> bindings) throws JanitorCompilerException {
        super(qualifiedName, new PrecompiledSupplier(runtime.compile(qualifiedName, source), bindings));
    }

    /**
     * Holds the already-compiled script and does the per-import work of running it fresh.
     */
    private static final class PrecompiledSupplier implements JanitorModuleSupplier {
        private final RunnableScript compiled;
        private final @Nullable Consumer<Scope> bindings;

        private PrecompiledSupplier(final RunnableScript compiled, @Nullable final Consumer<Scope> bindings) {
            this.compiled = compiled;
            this.bindings = bindings;
        }

        @Override
        public JanitorModule getModule(final JanitorScriptProcess process) throws JanitorRuntimeException {
            final ResultAndScope result = compiled.runAndKeepGlobals(bindings == null ? g -> {
            } : bindings);
            return new ScriptModule.ModuleFromScope(result.getScope());
        }
    }

}
