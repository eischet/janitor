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

    public static class ModuleFromScope implements JanitorModule {
        private final Scope moduleScope;

        public ModuleFromScope(Scope moduleScope) {
            this.moduleScope = moduleScope;
        }

        @Override
        public @Nullable JanitorObject janitorGetAttribute(final JanitorScriptProcess runningScript, final String name, final boolean required) throws JanitorNameException {
            final JanitorObject obj = moduleScope.lookup(runningScript, name, null);
            if (obj instanceof ModuleScopeAware scriptFunction) {
                scriptFunction.setModuleScope(moduleScope);
            }
            return obj;
        }

    }


}
