package com.eischet.janitor.cleanup.api.api.types;

import com.eischet.janitor.cleanup.api.api.errors.runtime.JanitorNameException;
import com.eischet.janitor.cleanup.runtime.types.JanitorClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface JanitorInstance<T extends JanitorObject> extends JanitorObject {

    @NotNull JanitorClass<T> getJanitorClass();

    private T self() {
        return (T) this;
    }

    @Override
    default @Nullable JanitorObject janitorGetAttribute(final JanitorScriptProcess runningScript, final String name, final boolean required) throws JanitorNameException {
        final JanitorObject classAttribute = getJanitorClass().getBoundMethod(name, self());
        if (classAttribute != null) {
            return classAttribute;
        }
        return JanitorObject.super.janitorGetAttribute(runningScript, name, required);
    }
}
