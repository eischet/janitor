package com.eischet.janitor.runtime.types;

import com.eischet.janitor.api.errors.runtime.JanitorNameException;
import com.eischet.janitor.api.types.JanitorModule;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.JanitorScriptProcess;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class JanitorNativeModule implements JanitorModule {

    @Override
    public abstract @Nullable JanitorObject janitorGetAttribute(final JanitorScriptProcess runningScript, final String name, final boolean required) throws JanitorNameException;

    @Override
    public @NotNull String janitorClassName() {
        return "module";
    }

}
