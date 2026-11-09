// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.modules;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.dispatch.Dispatcher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * A JanitorModuleRegistration gives a name to a module, so an interpreter can "import foo", where "foo" is the qualifiedName here.
 */
public class JanitorModuleRegistration {

    private final String qualifiedName;
    private final JanitorModuleSupplier moduleSupplier;
    private final @Nullable Supplier<? extends Dispatcher<?>> dispatcher;

    /**
     * Constructs a new JanitorModuleRegistration.
     * @param qualifiedName the qualified name of the module
     * @param moduleSupplier a supplier that creates a new instance of the module
     */
    public JanitorModuleRegistration(final String qualifiedName, final JanitorModuleSupplier moduleSupplier) {
        this(qualifiedName, moduleSupplier, null);
    }

    /**
     * Constructs a new JanitorModuleRegistration.
     * Use this variant when no process is needed to create the module.
     * @param qualifiedName the qualified name of the module
     * @param moduleSupplier a supplier that creates a new instance of the module
     */
    public JanitorModuleRegistration(final String qualifiedName, final Supplier<JanitorModule> moduleSupplier) {
        this(qualifiedName, process -> moduleSupplier.get());
    }

    private JanitorModuleRegistration(final String qualifiedName, final JanitorModuleSupplier moduleSupplier,
                                      final @Nullable Supplier<? extends Dispatcher<?>> dispatcher) {
        this.qualifiedName = qualifiedName;
        this.moduleSupplier = moduleSupplier;
        this.dispatcher = dispatcher;
    }

    /**
     * Names the dispatcher of the module's objects, so that tools such as editors can list its members without creating
     * the module. It is asked for when needed, so a registration may name a dispatch table declared after it in the same
     * class, e.g. {@code new JanitorModuleRegistration("foo", FooModule::new).withDispatcher(() -> FooModule.DISPATCH)}.
     *
     * @param dispatcher supplies the dispatcher of the module
     * @return a registration like this one, with the dispatcher
     */
    public @NotNull JanitorModuleRegistration withDispatcher(final @NotNull Supplier<? extends Dispatcher<?>> dispatcher) {
        return new JanitorModuleRegistration(qualifiedName, moduleSupplier, dispatcher);
    }

    /**
     * Gets the qualified name of the module.
     * @return the qualified name of the module
     */
    public String getQualifiedName() {
        return qualifiedName;
    }

    /**
     * Gets the supplier that creates a new instance of the module.
     * @return the supplier that creates a new instance of the module
     */
    public JanitorModuleSupplier getModuleSupplier() {
        return moduleSupplier;
    }

    /**
     * Gets the dispatcher of the module's objects, see {@link #withDispatcher}.
     * @return the dispatcher, or null if the registration does not name one
     */
    public @Nullable Dispatcher<?> getDispatcher() {
        return dispatcher == null ? null : dispatcher.get();
    }

}
