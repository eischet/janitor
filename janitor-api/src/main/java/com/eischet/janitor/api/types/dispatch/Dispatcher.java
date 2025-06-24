package com.eischet.janitor.api.types.dispatch;

import com.eischet.janitor.api.JanitorEnvironment;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.metadata.HasMetaData;
import com.eischet.janitor.api.metadata.MetaDataKey;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonInputStream;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * A dispatcher for JanitorObjects.
 *
 * @param <T> the type of JanitorObject
 */
public interface Dispatcher<T extends JanitorObject> extends HasMetaData {

    /**
     * Creates a combined dispatcher from a "parent" and a "child", or "superclass" and "subclass" if you prefer.
     *
     * @param parent the parent dispatcher
     * @param child  the child dispatcher
     * @param <P>    the type of the parent JanitorObject
     * @param <C>    the type of the child JanitorObject
     * @return a new dispatcher that first tries to dispatch to the child, and then to the parent
     */
    static <P extends JanitorObject, C extends JanitorObject> Dispatcher<P> inherit(Dispatcher<P> parent, Dispatcher<C> child) {
        // WE know that inherit works only when C extends P, but it's not easy to drive this point home to the Java compiler.
        // Therefore, this method looks more dangerous than it really is, with all those stupid casts.

        return new Dispatcher<P>() {
            @Override
            public <K> @Nullable K getMetaData(final @NotNull MetaDataKey<K> key) {
                return child.getMetaData(key);
            }

            @Override
            public <K> @Nullable K getMetaData(final @NotNull String attributeName, final @NotNull MetaDataKey<K> key) {
                return child.getMetaData(attributeName, key);
            }

            @SuppressWarnings("unchecked")
            @Override
            public JanitorObject dispatch(final P instance, final JanitorScriptProcess process, final String name) throws JanitorRuntimeException {
                final JanitorObject result = child.dispatch((C) instance, process, name);
                if (result != null) {
                    return result;
                } else {
                    return parent.dispatch(instance, process, name);
                }
            }

            @SuppressWarnings("unchecked")
            @Override
            public void writeToJson(final JsonOutputStream stream, final P instance) throws JsonException {
                child.writeToJson(stream, (C) instance);
            }

            @SuppressWarnings("unchecked")
            @Override
            public String writeToJson(final JanitorEnvironment env, final P instance) throws JsonException {
                return child.writeToJson(env, (C) instance);
            }

            @SuppressWarnings("unchecked")
            @Override
            public P readFromJson(final Supplier<P> constructor, final JsonInputStream stream) throws JsonException {
                return (P) child.readFromJson(() -> (C) constructor.get(), stream);
            }

            @SuppressWarnings("unchecked")
            @Override
            public P readFromJson(final JanitorEnvironment env, final Supplier<P> constructor, final String json) throws JsonException {
                return (P) child.readFromJson(env, () -> (C) constructor.get(), json);
            }
        };

        /* used to be a functional interface:
        return (instance, process, name) -> {
            @SuppressWarnings("unchecked") final JanitorObject result = child.dispatch((C) instance, process, name);
            if (result != null) {
                return result;
            } else {
                return parent.dispatch(instance, process, name);
            }
        };
         */
    }

    /**
     * Dispatches a method call to the appropriate method on the given JanitorObject.
     *
     * @param instance the JanitorObject to dispatch the method call to
     * @param process  the running script
     * @param name     the name of the method to call
     * @return the result of the method call
     */
    JanitorObject dispatch(final T instance, final JanitorScriptProcess process, final String name) throws JanitorRuntimeException;

    void writeToJson(JsonOutputStream stream, T instance) throws JsonException;

    @Language("JSON") String writeToJson(JanitorEnvironment env, T instance)  throws JsonException;

    T readFromJson(Supplier<T> constructor, JsonInputStream stream) throws JsonException;

    T readFromJson(JanitorEnvironment env, Supplier<T> constructor, @Language("JSON") String json) throws JsonException;

}
