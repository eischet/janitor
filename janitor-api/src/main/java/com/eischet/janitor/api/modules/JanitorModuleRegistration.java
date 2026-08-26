package com.eischet.janitor.api.modules;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;

import java.util.function.Supplier;

/**
 * A JanitorModuleRegistration gives a name to a module, so an interpreter can "import foo", where "foo" is the qualifiedName here.
 */
public class JanitorModuleRegistration {

    private final String qualifiedName;
    private final JanitorModuleSupplier moduleSupplier;

    /**
     * Constructs a new JanitorModuleRegistration.
     * @param qualifiedName the qualified name of the module
     * @param moduleSupplier a supplier that creates a new instance of the module
     */
    public JanitorModuleRegistration(final String qualifiedName, final JanitorModuleSupplier moduleSupplier) {
        this.qualifiedName = qualifiedName;
        this.moduleSupplier = moduleSupplier;
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

}
