package com.eischet.janitor.api.types.composed;

import com.eischet.janitor.api.JanitorEnvironment;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.dispatch.Dispatcher;
import com.eischet.janitor.toolbox.json.api.*;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

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
public abstract class JanitorComposed<T extends JanitorComposed<T>> implements JanitorObject, JsonWriter, JsonReader {
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

    public String toJson(final JanitorEnvironment env) throws JsonException {
        return dispatcher.writeToJson(env, self());
    }

    static <X extends JanitorComposed<X>> X fromJson(final JanitorEnvironment env, final Supplier<X> constructor, @Language("JSON") final String json) throws JsonException {
        final X instance = constructor.get();
        return instance.dispatcher.readFromJson(env, () -> instance, json);
    }

    @Override
    public void writeJson(final JsonOutputStream producer) throws JsonException {
        dispatcher.writeToJson(producer, self());
    }

    @Override
    public void readJson(final JsonInputStream stream) throws JsonException {
        dispatcher.readFromJson(this::self, stream); // we simply delegate this to our own dispatcher, which can do it for us
    }
}
