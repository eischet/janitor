package com.eischet.janitor.api.scripting;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorNameException;
import com.eischet.janitor.api.types.JanitorObject;
import org.jetbrains.annotations.Nullable;


/**
 * A wrapper around a value that can be used in a Janitor script.
 * This is used to allow Java objects to be used in Janitor scripts.
 * You need to supply a dispatcher that will handle attribute lookups on the wrapped object.
 * @param <T> the type of the wrapped object
 */
public class JanitorWrapper<T> implements JanitorObject {

    private final Dispatcher<JanitorWrapper<T>> dispatcher;
    protected T wrapped;

    /**
     * Create a new JanitorWrapper.
     * @param dispatcher the dispatcher
     * @param wrapped the wrapped object
     */
    public JanitorWrapper(final Dispatcher<JanitorWrapper<T>> dispatcher, final T wrapped) {
        this.dispatcher = dispatcher;
        this.wrapped = wrapped;
    }

    public <X extends JanitorWrapper<T>> JanitorWrapper(final X dispatcher, final T wrapped, final Class<X> cls) {
        this.dispatcher = (Dispatcher<JanitorWrapper<T>>) dispatcher;
        this.wrapped = wrapped;
    }

    @Override
    public @Nullable JanitorObject janitorGetAttribute(final JanitorScriptProcess runningScript, final String name, final boolean required) throws JanitorNameException {
        final JanitorObject attribute = dispatcher.dispatch(this, runningScript, name);
        if (attribute != null) {
            return attribute;
        }
        return JanitorObject.super.janitorGetAttribute(runningScript, name, required);
    }

    @Override
    public T janitorGetHostValue() {
        return wrapped;
    }
}
