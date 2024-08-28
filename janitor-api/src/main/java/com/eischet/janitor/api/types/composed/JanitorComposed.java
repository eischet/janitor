package com.eischet.janitor.api.types.composed;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.dispatch.Dispatcher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Base class for "composing" Janitor objects in Java.
 * <p>
 * This class combines your own Java class (derived from this class) with a dispatch table that handles attribute lookups.
 * </p>
 * <p>
 * See {@link com.eischet.janitor.api.types.wrapped.JanitorWrapper} for an alternative approach that is easier
 * to apply when you have a single, pre-existing Java object that you want to wrap.
 * </p>
 *
 * @param <T> the type of the subclass you supply, e.g. Subclass extends JanitorComposed&lt;Subclass&gt;.
 */
public abstract class JanitorComposed<T extends JanitorComposed<T>> implements JanitorObject {
    protected final Dispatcher<T> dispatcher;

    /**
     * Create a new JanitorComposed.
     *
     * @param dispatcher dispatch table
     */
    protected JanitorComposed(final Dispatcher<T> dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public @Nullable JanitorObject janitorGetAttribute(final @NotNull JanitorScriptProcess process, final @NotNull String name, final boolean required) throws JanitorRuntimeException {
        // We cannot dispatch(this), but we can dispatch self(), which will always work - but javac is not
        // aware of this. Looks funny, but works.
        final JanitorObject attribute = dispatcher.dispatch(self(), process, name);
        if ("numberOfDigits".equals(name)) {
            process.trace(() -> "numberOfDigits lookup yielded: " +  attribute + ", required=" + required);
        }
        if (attribute != null) {
            return attribute;
        }
        @Nullable final JanitorObject superValue = JanitorObject.super.janitorGetAttribute(process, name, required);
        if ("numberOfDigits".equals(name)) {
            process.trace(() -> "numberOfDigits lookup yielded from super: " +  superValue + ", required=" + required);
            //throw new RuntimeException("should be required! required=" + required);
        }
        return superValue;
    }

    @SuppressWarnings("unchecked")
    protected T self() {
        return (T) this;
    }

}
