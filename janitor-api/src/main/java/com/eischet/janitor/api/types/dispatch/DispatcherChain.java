package com.eischet.janitor.api.types.dispatch;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.metadata.MetaDataKey;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonInputStream;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class DispatcherChain<T extends JanitorObject> implements Dispatcher<T> {

    // TODO: unfinished experiment for chaining dispatchers, e.g. composite dispatcher instead of pseudo inheritance
    // Dispatcher<MyEntity> d = new DispatcherChain<>(List.of(child, parent, grandparent));


    private final List<Dispatcher<? super T>> chain;

    public DispatcherChain(List<Dispatcher<? super T>> chain) {
        this.chain = chain;
    }

    @Override
    public JanitorObject dispatch(T instance, JanitorScriptProcess process, String name) throws JanitorRuntimeException {
        for (Dispatcher<? super T> dispatcher : chain) {
            JanitorObject result = dispatcher.dispatch(instance, process, name);
            if (result != null) return result;
        }
        return null;
    }

    @Override
    public void writeToJson(final JsonOutputStream stream, final T instance) throws JsonException {
        for (Dispatcher<? super T> dispatcher : chain) {
            dispatcher.writeToJson(stream, instance);
        }
    }

    @Override
    public String writeToJson(final T instance) throws JsonException {
        return Janitor.current().writeJson(producer -> writeToJson(producer, instance));
    }

    @Override
    public T readFromJson(final Supplier<T> constructor, final JsonInputStream stream) throws JsonException {
        return null;
    }

    @Override
    public T readFromJson(final Supplier<T> constructor, final String json) throws JsonException {
        return null;
    }

    @Override
    public Stream<String> streamAttributeNames() {
        return Stream.empty();
    }

    @Override
    public @Nullable Supplier<T> getJavaDefaultConstructor() {
        return null;
    }

    @Override
    public <K> @Nullable K getMetaData(final @NotNull MetaDataKey<K> key) {
        return null;
    }

    @Override
    public <K> @Nullable K getMetaData(final @NotNull String attributeName, final @NotNull MetaDataKey<K> key) {
        return null;
    }


}
