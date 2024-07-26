package com.eischet.janitor.runtime;


import com.eischet.janitor.api.*;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorNameException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.i18n.JanitorFormatting;
import com.eischet.janitor.api.modules.JanitorModule;
import com.eischet.janitor.api.modules.JanitorModuleRegistration;
import com.eischet.janitor.api.types.JanitorObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class BaseRuntime implements JanitorRuntime {

    private final JanitorEnvironment enviroment;
    private Consumer<String> traceListener;


    @FunctionalInterface
    public interface ModuleResolver {
        @Nullable JanitorModule resolveModuleByStringName(JanitorScriptProcess process, String name) throws JanitorRuntimeException;
    }


    private static final List<JanitorModuleRegistration> DEFAULT_MODULES = new ArrayList<>(); // TODO: move to env!!!

    private final List<ModuleResolver> resolvers = new ArrayList<>();

    private final List<JanitorModuleRegistration> moduleRegistrations = new ArrayList<>();
    private final JanitorFormatting formatting;

    public BaseRuntime(final JanitorEnvironment environment) {
        DEFAULT_MODULES.forEach(this::registerModule);
        this.enviroment = environment;
        this.formatting = environment.getFormatting();
    }

    @Override
    public JanitorObject lookupClassAttribute(final @NotNull JanitorScriptProcess runningScript, final @NotNull JanitorObject instance, final @NotNull String attributeName) {
        return enviroment.lookupClassAttribute(runningScript, instance, attributeName);
    }

    /**
     * Adds a module resolver.
     * The resolvers will be called in reverse order of addition, so later resolvers can override earlier ones.

     * @param resolver a module resolver for string based module names
     */
    public void addModuleResolver(final ModuleResolver resolver) {
        resolvers.add(0, resolver);
    }

    @Override
    public @NotNull JanitorModule getModuleByStringName(final JanitorScriptProcess process, final String name) throws JanitorRuntimeException {
        for (final ModuleResolver resolver : resolvers) {
            final JanitorModule module = resolver.resolveModuleByStringName(process, name);
            if (module != null) {
                return module;
            }
        }
        throw new JanitorNameException(process, "Module not found: '" + name + "'");
    }

    public static void addDefaultModule(JanitorModuleRegistration registration) {
        DEFAULT_MODULES.add(registration);
    }



    @Override
    public void registerModule(final JanitorModuleRegistration moduleRegistration) {
        moduleRegistrations.add(moduleRegistration);
    }

    public void setTraceListener(final Consumer<String> listener) {
        this.traceListener = listener;
    }

    @Override
    public @NotNull JanitorModule getModuleByQualifier(final JanitorScriptProcess process, final String name) throws JanitorRuntimeException {
        for (final JanitorModuleRegistration moduleRegistration : moduleRegistrations) {
            if (Objects.equals(name, moduleRegistration.getQualifiedName())) {
                return moduleRegistration.getModuleSupplier().get();
            }
        }
        throw new JanitorNameException(process, "Module not found: " + name);
    }


    @Override
    public void trace(final Supplier<String> traceMessageSupplier) {
        if (traceListener != null) {
            final String message = traceMessageSupplier.get();
            traceListener.accept(message);
        }
    }

    @Override
    @NotNull
    public JanitorFormatting getFormatting() {
        return formatting;
    }

    @Override
    public JanitorEnvironment getEnvironment() {
        return enviroment;
    }

    @Override
    public RunnableScript compile(String moduleName, String source) throws JanitorCompilerException {
        try {
            return new JanitorScript(this, moduleName, source == null ? "" : source);
        } catch (RuntimeException e) {
            throw new JanitorCompilerException("Compiler Error", e);
        }
    }

    @Override
    public RunnableScript checkCompile(String moduleName, String source) throws JanitorCompilerException {
        try {
            return new JanitorScript(this, moduleName, source == null ? "" : source, true);
        } catch (RuntimeException e) {
            throw new JanitorCompilerException("Compiler Error", e);
        }
    }


    @Override
    public void protect(final String title, final JanitorScriptProcess.ProtectedCall call) {
        try {
            call.call();
        } catch (JanitorRuntimeException e) {
            exception("Protected call " + title + " failed: " + e.getMessage(), e);
        }
    }

    protected void exception(final String s, final JanitorRuntimeException e) {
        warn(s); // TODO: print stack trace
    }

}
