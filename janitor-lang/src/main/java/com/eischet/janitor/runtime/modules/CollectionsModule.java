package com.eischet.janitor.runtime.modules;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.lang.JNativeMethod;
import com.eischet.janitor.api.errors.runtime.JanitorNameException;
import com.eischet.janitor.api.modules.JanitorModuleRegistration;
import com.eischet.janitor.api.modules.JanitorNativeModule;
import com.eischet.janitor.api.types.JanitorObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CollectionsModule extends JanitorNativeModule {

    public static final JanitorModuleRegistration REGISTRATION = new JanitorModuleRegistration("collections", CollectionsModule::new);

    @Override
    public @Nullable JanitorObject janitorGetAttribute(final @NotNull JanitorScriptProcess process, final @NotNull String name, final boolean required) throws JanitorNameException {
        if ("set".equals(name) || "Set".equals(name)) {
            return JNativeMethod.of(arguments -> process.getEnvironment().getBuiltinTypes().set(arguments.getList().stream()));
        }
        if ("list".equals(name) || "List".equals(name)) {
            return JNativeMethod.of(arguments -> process.getEnvironment().getBuiltinTypes().list(arguments.getList()));
        }
        if ("map".equals(name) || "Map".equals(name)) {
            return JNativeMethod.of(arguments -> {
                arguments.require(0);
                return process.getEnvironment().getBuiltinTypes().map();
            });
        }
        return null;
    }

}
