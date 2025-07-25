package com.eischet.janitor.api.modules;

import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

@FunctionalInterface
public interface DiscoverableModules {

    @Unmodifiable
    List<JanitorModuleRegistration> getModules();

}
