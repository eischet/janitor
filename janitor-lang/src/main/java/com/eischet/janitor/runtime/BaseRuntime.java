package com.eischet.janitor.runtime;


import com.eischet.janitor.api.JanitorRuntime;
import com.eischet.janitor.api.errors.runtime.JanitorNameException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorModule;
import com.eischet.janitor.api.types.JanitorScriptProcess;
import com.eischet.janitor.runtime.types.JanitorModuleRegistration;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class BaseRuntime implements JanitorRuntime {

    @FunctionalInterface
    public interface ModuleResolver {
        @Nullable JanitorModule resolveModuleByStringName(JanitorScriptProcess process, String name) throws JanitorRuntimeException;
    }


    private static final MutableList<JanitorModuleRegistration> DEFAULT_MODULES = Lists.mutable.empty();

    private final MutableList<ModuleResolver> resolvers = Lists.mutable.empty();

    private final MutableList<JanitorModuleRegistration> moduleRegistrations = Lists.mutable.empty();
    private Consumer<String> traceListener = null;
    private final JanitorFormatting formatting;

    public BaseRuntime(final JanitorFormatting formatting) {
        DEFAULT_MODULES.forEach(this::registerModule);
        this.formatting = formatting;
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
            traceListener.accept(traceMessageSupplier.get());
        }
    }

    @Override
    @NotNull
    public JanitorFormatting getFormatting() {
        return formatting;
    }

}
