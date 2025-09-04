package com.eischet.janitor.api.types.wrapped;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorTypedObject;
import com.eischet.janitor.api.types.dispatch.Dispatcher;
import com.eischet.janitor.api.types.JanitorObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;


/**
 * A wrapper around a value that can be used in a Janitor script.
 * This is used to allow Java objects to be used in Janitor scripts.
 * You need to supply a dispatcher that will handle attribute lookups on the wrapped object.
 * @param <T> the type of the wrapped object
 */
public class JanitorWrapper<T> implements JanitorTypedObject<T> {

    protected final Dispatcher<JanitorWrapper<T>> dispatcher;
    protected @NotNull T wrapped;

    /**
     * Create a new JanitorWrapper.
     * @param dispatcher the dispatcher
     * @param wrapped the wrapped object
     */
    public JanitorWrapper(final @NotNull Dispatcher<JanitorWrapper<T>> dispatcher, final @NotNull T wrapped) {
        this.dispatcher = dispatcher;
        this.wrapped = wrapped;
    }

    @SuppressWarnings("unchecked")
    public <X extends JanitorWrapper<T>> JanitorWrapper(final @NotNull X dispatcher, final @NotNull T wrapped, final @NotNull Class<X> cls) {
        this.dispatcher = (Dispatcher<JanitorWrapper<T>>) dispatcher;
        this.wrapped = wrapped;
    }

    @Override
    public @Nullable JanitorObject janitorGetAttribute(final @NotNull JanitorScriptProcess process, final @NotNull String name, final boolean required) throws JanitorRuntimeException {
        final JanitorObject attribute = dispatcher.dispatch(this, process, name);
        if (attribute != null) {
            return attribute;
        }
        return JanitorTypedObject.super.janitorGetAttribute(process, name, required);
    }

    @Override
    public T janitorGetHostValue() {
        return wrapped;
    }

    @Override
    public String toString() {
        return wrapped.toString();
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        final JanitorWrapper<?> that = (JanitorWrapper<?>) object;
        return Objects.equals(wrapped, that.wrapped);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(wrapped);
    }

    public Dispatcher<JanitorWrapper<T>> getDispatcher() {
        return dispatcher;
    }
}
