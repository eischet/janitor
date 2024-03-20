package com.eischet.janitor.cleanup.runtime.modules;

import com.eischet.janitor.cleanup.api.api.errors.runtime.JanitorNameException;
import com.eischet.janitor.cleanup.api.api.types.*;
import com.eischet.janitor.cleanup.runtime.types.JNativeMethod;
import com.eischet.janitor.cleanup.runtime.types.JanitorModuleRegistration;
import com.eischet.janitor.cleanup.runtime.types.JanitorNativeModule;
import org.jetbrains.annotations.Nullable;

public class CollectionsModule extends JanitorNativeModule {

    public static final JanitorModuleRegistration REGISTRATION = new JanitorModuleRegistration("collections", CollectionsModule::new);

    @Override
    public @Nullable JanitorObject janitorGetAttribute(final JanitorScriptProcess runningScript, final String name, final boolean required) throws JanitorNameException {
        if ("set".equals(name)) {
            return JNativeMethod.of(arguments -> new JSet(arguments.getList()));
        }
        if ("list".equals(name)) {
            return JNativeMethod.of(arguments -> new JList(arguments.getList()));
        }
        if ("map".equals(name)) {
            return JNativeMethod.of(arguments -> { arguments.require(0); return new JMap(); });
        }
        return null;
    }

}
