package com.eischet.janitor.api.modules;

import java.util.function.Supplier;

/**
 * A JanitorModuleRegistration gives a name to a module, so an interpreter can "import foo", where "foo" is the qualifiedName here.
 */
public class JanitorModuleRegistration {

    private final String qualifiedName;
    private final Supplier<JanitorModule> moduleSupplier;

    /**
     * Constructs a new JanitorModuleRegistration.
     * @param qualifiedName the qualified name of the module
     * @param moduleSupplier a supplier that creates a new instance of the module
     */
    public JanitorModuleRegistration(final String qualifiedName, final Supplier<JanitorModule> moduleSupplier) {
        this.qualifiedName = qualifiedName;
        this.moduleSupplier = moduleSupplier;
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
    public Supplier<JanitorModule> getModuleSupplier() {
        return moduleSupplier;
    }

}
