package com.eischet.janitor.api.modules;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorNameException;
import com.eischet.janitor.api.types.JanitorObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A JanitorNativeModule is a module that can be imported into a Janitor script.
 * This is simply a specialization of JanitorModule, for which you have to implement even less methods.
 */
public abstract class JanitorNativeModule implements JanitorModule {

    @Override
    public abstract @Nullable JanitorObject janitorGetAttribute(final @NotNull JanitorScriptProcess process, final @NotNull String name, final boolean required) throws JanitorNameException;

    @Override
    public @NotNull String janitorClassName() {
        return "Module";
    }

}
