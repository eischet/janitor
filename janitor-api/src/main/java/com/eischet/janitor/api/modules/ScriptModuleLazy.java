package com.eischet.janitor.api.modules;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.RunnableScript;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.errors.runtime.JanitorScriptModuleException;
import com.eischet.janitor.api.scopes.ResultAndScope;
import com.eischet.janitor.api.scopes.Scope;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * A script-based module that compiles its source exactly once -- lazily, on the first import -- and
 * then, on every import (including the first), runs that one compiled script fresh, giving each
 * importer its own private scope, never shared with any other import.
 * <p>
 * This sits between {@link ScriptModule} (which compiles *and* runs fresh on every import: fully
 * isolated, always reflects the current source, but pays the compile cost every time) and a
 * hypothetical "shared" module (which would compile and run only once, sharing that one scope across
 * every import): here, only the expensive compile step is cached, while each import still gets a
 * fresh run, so there's no shared mutable state to worry about between unrelated importers.
 * <p>
 * Because each import's scope is never shared, its lifetime has exactly one, well-defined owner: the
 * one importing script that just created it. That's the crucial difference from a cached/shared
 * module scope (where "when should this be cleaned up" has no single good answer, since many
 * unrelated scripts might be using it at once) -- a scope obtained here can correctly be left to that
 * one script's own lifetime.
 * <p>
 * Compiling lazily (on first import, rather than eagerly in the constructor -- see
 * {@link ScriptModulePrecompiled} for that) is the right choice when the source itself might not be
 * trustworthy/finished yet at construction time -- e.g. it was supplied by an application user and
 * could still be corrected at runtime -- so a compile error should surface only once the module is
 * actually imported and used, not fail the whole app's startup.
 */
public class ScriptModuleLazy extends JanitorModuleRegistration {

    /**
     * Constructs a new lazily-compiled script module.
     *
     * @param qualifiedName the qualified name of the module
     * @param source        the source code of the module, compiled exactly once, on first use
     * @param bindings      the bindings to apply to the module's scope every time it's (re-)run
     */
    public ScriptModuleLazy(@NotNull final String qualifiedName, @NotNull @Language("Janitor") final String source, @Nullable final Consumer<Scope> bindings) {
        super(qualifiedName, new LazyCompilingSupplier(qualifiedName, source, bindings));
    }

    /**
     * Holds the lazily-compiled script (across every import of this one registration) and does the
     * actual per-import work of running it fresh. A dedicated class, rather than a lambda, because
     * the compiled script needs somewhere to be cached between calls.
     */
    private static final class LazyCompilingSupplier implements JanitorModuleSupplier {
        private final String qualifiedName;
        private final @Language("Janitor") String source;
        private final @Nullable Consumer<Scope> bindings;

        // Compilation can race if two scripts import this module for the first time concurrently;
        // double-checked locking avoids compiling twice in that case without paying for
        // synchronization on every subsequent, already-compiled import.
        private volatile RunnableScript compiled;

        private LazyCompilingSupplier(final String qualifiedName, @Language("Janitor") final String source, @Nullable final Consumer<Scope> bindings) {
            this.qualifiedName = qualifiedName;
            this.source = source;
            this.bindings = bindings;
        }

        private RunnableScript getCompiled(final JanitorScriptProcess process) throws JanitorRuntimeException {
            RunnableScript result = compiled;
            if (result == null) {
                synchronized (this) {
                    result = compiled;
                    if (result == null) {
                        try {
                            result = process.getRuntime().compile(qualifiedName, source);
                        } catch (JanitorCompilerException e) {
                            throw new JanitorScriptModuleException(process, e);
                        }
                        compiled = result;
                    }
                }
            }
            return result;
        }

        @Override
        public JanitorModule getModule(final JanitorScriptProcess process) throws JanitorRuntimeException {
            final RunnableScript script = getCompiled(process);
            final ResultAndScope result = script.runAndKeepGlobals(bindings == null ? g -> {
            } : bindings);
            return new ScriptModule.ModuleFromScope(result.getScope());
        }
    }

}
