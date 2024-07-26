package com.eischet.janitor.api.types.cloak;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorNameException;
import com.eischet.janitor.api.types.dispatch.Dispatcher;
import com.eischet.janitor.api.types.JanitorObject;
import org.jetbrains.annotations.Nullable;

public class JanitorCloak<T extends JanitorCloak<T>> implements JanitorObject {
    protected final Dispatcher<T> dispatcher;

    protected JanitorCloak(final Dispatcher<T> dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public @Nullable JanitorObject janitorGetAttribute(final JanitorScriptProcess runningScript, final String name, final boolean required) throws JanitorNameException {
        // We cannot dispatch(this), but we can dispatch self(), which will always work - but javac is not
        // aware of this. Looks funny, but works.
        final JanitorObject attribute = dispatcher.dispatch(self(), runningScript, name);
        if (attribute != null) {
            return attribute;
        }
        return JanitorObject.super.janitorGetAttribute(runningScript, name, required);
    }

    @SuppressWarnings("unchecked")
    protected T self() {
        return (T) this;
    }

}
