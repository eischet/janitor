package com.eischet.janitor.runtime.modules;

import com.eischet.janitor.api.errors.runtime.JanitorNameException;
import com.eischet.janitor.api.types.*;
import com.eischet.janitor.runtime.types.JNativeMethod;
import com.eischet.janitor.runtime.types.JanitorModuleRegistration;
import com.eischet.janitor.runtime.types.JanitorNativeModule;
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
