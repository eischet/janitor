package com.eischet.janitor.api.types;

import com.eischet.janitor.api.errors.runtime.JanitorNameException;
import com.eischet.janitor.json.*;
import com.eischet.janitor.runtime.scope.Scope;
import com.eischet.janitor.runtime.types.JAssignable;
import com.eischet.janitor.runtime.types.JIterable;
import com.eischet.janitor.runtime.types.JNativeMethod;
import com.eischet.janitor.runtime.types.TemporaryAssignable;
import com.eischet.janitor.tools.JanitorConverter;
import com.eischet.janitor.json.JsonExportable;
import com.eischet.janitor.json.JsonExportableObject;
import net.eischet.json.*;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.api.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Consumer;

public class JMap implements JanitorObject, JIterable, JsonWriter, JsonExportableObject {

    private static final Logger log = LoggerFactory.getLogger(JMap.class);

    private final MutableMap<JanitorObject, JanitorObject> map = Maps.mutable.empty();
    private final ImmutableMap<String, JNativeMethod> methods;

    public JMap() {
        final MutableMap<String, JNativeMethod> methods = Maps.mutable.empty();
        methods.put("parseJson", JNativeMethod.of(arguments -> parseJson(arguments.require(1).getString(0).janitorGetHostValue())));

        methods.put("get", JNativeMethod.of(arguments -> get(arguments.require(1).get(0)) ));
        methods.put("__get__", JNativeMethod.of(arguments -> getIndexed(arguments.require(1).get(0)) ));
        methods.put("put", JNativeMethod.ofVoid(arguments -> put(arguments.require(2).get(0), arguments.get(1))));
        methods.put("size", JNativeMethod.of(arguments -> {
            arguments.require(0);
            return JInt.of(map.size());
        }));
        methods.put("isEmpty", JNativeMethod.of(arguments -> {
            arguments.require(0);
            return JBool.map(map.isEmpty());
        }));
        methods.put("keys", JNativeMethod.of(arguments -> {
            arguments.require(0);
            return new JList(map.keySet().stream());
        }));
        methods.put("values", JNativeMethod.of(arguments -> {
            arguments.require(0);
            return new JList(map.values().stream());
        }));
        methods.put("toJson", JNativeMethod.of(arguments -> {
            arguments.require(0);
            return JString.of(exportToJson());
        }));
        this.methods = methods.toImmutable();
    }

    @Override
    public @Nullable JanitorObject janitorGetAttribute(final JanitorScriptProcess runningScript, final String name, final boolean required) throws JanitorNameException {
        final JNativeMethod localMethod = methods.get(name);
        if (localMethod != null) {
            return localMethod;
        }
        if (log.isDebugEnabled()) {
            log.debug("trying to resolve key {} in map keys: {}", name, map.keySet());
        }
        final JString keyString = JString.of(name);
        if (map.containsKey(keyString)) {
            final JanitorObject found = map.get(keyString);
            log.debug("found: {}", found);
            return found;
        }
        return JanitorObject.super.janitorGetAttribute(runningScript, name, required);
    }

    public Set<JanitorObject> keySet() {
        return map.keySet();
    }

    public Collection<JanitorObject> values() {
        return map.values();
    }

    public int size() {
        return map.size();
    }

    @Override
    public Iterator<JanitorObject> getIterator() {
        return map.keysView().iterator();
    }

    @Override
    public MutableMap<JanitorObject, JanitorObject> janitorGetHostValue() {
        return map;
    }

    @Override
    public String janitorToString() {
        return map.toString();
    }

    @Override
    public boolean janitorIsTrue() {
        return !map.isEmpty();
    }

    public void put(final String key, final JanitorObject value) {
        map.put(JString.of(key), value);
    }

    public void put(final JanitorObject key, final JanitorObject value) {
        map.put(key, value);
    }

    public JanitorObject get(final JanitorObject key) {
        return JanitorConverter.orNull(map.get(key));
    }

    public JanitorObject getIndexed(final JanitorObject key) {
        return TemporaryAssignable.of(get(key), value -> put(key, value));
    }

    @Override
    public String toString() {
        return janitorToString();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public void putAll(final JMap map) {
        this.map.putAll(map.map);
    }

    public void applyTo(final JanitorScriptProcess rs, final JanitorObject target) throws JanitorNameException {
        final MutableSet<JanitorObject> notAssignable = Sets.mutable.empty();
        map.forEach((key, value) -> {
            @Nullable final JanitorObject prop = Scope.getOptionalMethod(target, rs, key.janitorToString());
            if (prop instanceof JAssignable assignableProperty) {
                if (!assignableProperty.assign(value)) {
                    notAssignable.add(key);
                }
            } else {
                log.warn("cannot assign to object {} property {} for key {}, value {}", target, prop, key, value);
                notAssignable.add(key);
            }
        });
        if (notAssignable.notEmpty()) {
            throw new JanitorNameException(rs, "assigned invalid object properties: " + notAssignable);
        }
    }

    public JMap parseJson(final String json) throws JsonException {
        if (json == null || json.isBlank()) {
            return this;
        }
        final JsonConsumer reader = GsonConsumer.lenient(json);
        return parseJson(reader);
    }

    public JMap parseJson(final JsonConsumer reader) throws JsonException {
        reader.beginObject();
        while (reader.hasNext()) {
            if (reader.peek() == JsonTokenType.END_OBJECT) {
                break;
            }
            put(JString.of(reader.nextName()), JCollection.parseJsonValue(reader));
        }
        reader.endObject();
        return this;
    }

    @Override
    public @NotNull String janitorClassName() {
        return "map";
    }

    public void extractString(final JanitorScriptProcess running, final String key, final Consumer<String> consumer) {
        final JanitorObject value = map.get(JString.of(key));
        if (value instanceof JString str) {
            consumer.accept(str.janitorGetHostValue());
        } else if (value != null) {
            running.warn(String.format("extractString for key=%s found %s [%s] where a string was expected", key, value, value.getClass()));
        }
    }

    public void extract(final String key, final Consumer<JanitorObject> consumer) {
        final JanitorObject value = map.get(JString.of(key));
        if (value != null && value != JNull.NULL) {
            consumer.accept(value);
        }
    }

    public @Nullable JanitorObject getNullable(final JString key) {
        if (map.containsKey(key)) {
            return map.get(key);
        } else {
            return null;
        }
    }

    @Override
    public boolean isDefaultOrEmpty() {
        return map.isEmpty();
    }


    @Override
    public void writeJson(final JsonProducer producer) throws JsonException {
        producer.beginObject();
        for (final Pair<JanitorObject, JanitorObject> pair : map.keyValuesView()) {
            if (pair.getTwo() instanceof JsonExportable ex) {
                if (ex.isDefaultOrEmpty()) {
                    continue;
                }
                producer.name(pair.getOne().janitorToString());
                ex.writeJson(producer);
            } else if (pair.getTwo() instanceof JsonWriter jw) {
                producer.name(pair.getOne().janitorToString());
                jw.writeJson(producer);
            } else {
                throw new JsonException("cannot write " + pair.getTwo() + " as json because it does not implement JsonExportable or JsonWriter");
            }
        }
        producer.endObject();
    }

}
