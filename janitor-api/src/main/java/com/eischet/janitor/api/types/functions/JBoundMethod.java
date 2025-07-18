package com.eischet.janitor.api.types.functions;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.metadata.HasMetaData;
import com.eischet.janitor.api.metadata.MetaDataKey;
import com.eischet.janitor.api.metadata.MetaDataRetriever;
import com.eischet.janitor.api.types.JanitorObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.eischet.janitor.api.util.ObjectUtilities.simpleClassNameOf;

/**
 * A method bound to an object.
 * @param <T> the type of object to which this method is bound.
 */
public class JBoundMethod<T> implements JCallable, JanitorObject, HasMetaData {
    private final T object;
    private final JUnboundMethod<T> worker;
    private final String name;
    private final MetaDataRetriever metaDataRetriever;
    private String help;

    /**
     * Create a new bound method.
     * @param name the name of the method.
     * @param object the object to which this method is bound.
     * @param worker the worker that will execute the method.
     * @param metaDataRetriever a helper that fetches meta-data about the method from somewhere else
     */
    public JBoundMethod(final String name, final T object, final JUnboundMethod<T> worker, final MetaDataRetriever metaDataRetriever) {
        this.object = object;
        this.worker = worker;
        this.name = name;
        this.metaDataRetriever = metaDataRetriever;
    }

    /**
     * Create a new bound method without meta-data.
     * @param name the name of the method.
     * @param object the object to which this method is bound.
     * @param worker the worker that will execute the method.
     */
    public JBoundMethod(final String name, final T object, final JUnboundMethod<T> worker) {
        this(name, object, worker, null);
    }

    @Override
    public <K> @Nullable K getMetaData(final @NotNull String attributeName, final @NotNull MetaDataKey<K> key) {
        return null; // methods do not have properties in Janitor, so always return null
    }

    /**
     * Retrieve "direct" meta-data "indirectly" (from an optional metaDataRetriever).
     * The NAME and HELP are served from within this instance when set and when not overridden by the retriever.
     *
     * @param key the key
     * @return the associated data
     * @param <K> the type the key defines for the data
     */
    @Override
    public <K> @Nullable K getMetaData(final @NotNull MetaDataKey<K> key) {
        if (metaDataRetriever != null) {
            final @Nullable K datum = metaDataRetriever.retrieveMetaData(key);
            if (datum != null) {
                return datum;
            }
        }
        if (key == Janitor.MetaData.NAME) {
            //noinspection unchecked
            return (K) name;
        }
        if (key == Janitor.MetaData.HELP) {
            //noinspection unchecked
            return (K) help;
        }
        return null;
    }

    @Override
    public String toString() {
        return simpleClassNameOf(object) + "::" +  name + "()";
    }

    @Override
    public JanitorObject call(final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
        return worker.call(object, process, arguments);
    }

    @Override
    public @NotNull String janitorClassName() {
        return "Function";
    }


    public String getHelp() {
        return help;
    }

    public void setHelp(final String help) {
        this.help = help;
    }

    public JBoundMethod<T> withHelp(final String help) {
        setHelp(help);
        return this;
    }

    /**
     * Create a new bound method.
     * @param name the name of the method.
     * @param object the object to which this method is bound.
     * @param worker the worker that will execute the method.
     * @return the new bound method.
     * @param <X> the type of object to which this method is bound.
     */
    public static <X> JBoundMethod<X> of(final String name, final X object, final JUnboundMethod<X> worker) {
        return new JBoundMethod<>(name, object, worker, null);
    }

}
