package com.eischet.janitor.runtime.modules;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.calls.JNativeMethod;
import com.eischet.janitor.api.errors.runtime.JanitorNameException;
import com.eischet.janitor.api.modules.JanitorModuleRegistration;
import com.eischet.janitor.api.modules.JanitorNativeModule;
import com.eischet.janitor.api.types.JList;
import com.eischet.janitor.api.types.JSet;
import com.eischet.janitor.api.types.JanitorObject;
import org.jetbrains.annotations.Nullable;

public class CollectionsModule extends JanitorNativeModule {

    public static final JanitorModuleRegistration REGISTRATION = new JanitorModuleRegistration("collections", CollectionsModule::new);

    @Override
    public @Nullable JanitorObject janitorGetAttribute(final JanitorScriptProcess runningScript, final String name, final boolean required) throws JanitorNameException {
        if ("set".equals(name)) {
            return JNativeMethod.of(arguments -> new JSet(arguments.getList().stream()));
        }
        if ("list".equals(name)) {
            return JNativeMethod.of(arguments -> new JList(arguments.getList().stream()));
        }
        if ("map".equals(name)) {
            return JNativeMethod.of(arguments -> { arguments.require(0); return runningScript.getEnvironment().getBuiltins().map(); });
        }
        return null;
    }

}
