package com.eischet.janitor.cleanup.runtime.types;

import com.eischet.janitor.cleanup.api.api.types.JanitorModule;

import java.util.function.Supplier;

public class JanitorModuleRegistration {

    private final String qualifiedName;
    private final Supplier<JanitorModule> moduleSupplier;

    public JanitorModuleRegistration(final String qualifiedName, final Supplier<JanitorModule> moduleSupplier) {
        this.qualifiedName = qualifiedName;
        this.moduleSupplier = moduleSupplier;
    }

    public String getQualifiedName() {
        return qualifiedName;
    }

    public Supplier<JanitorModule> getModuleSupplier() {
        return moduleSupplier;
    }

}
