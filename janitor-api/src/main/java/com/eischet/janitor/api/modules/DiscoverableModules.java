package com.eischet.janitor.api.modules;

import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/** Implemented by objects that can list the modules they provide, e.g. for automatic registration with an environment. */
@FunctionalInterface
public interface DiscoverableModules {

    /**
     * @return the modules that this object provides
     */
    @Unmodifiable
    List<JanitorModuleRegistration> getModules();

}
